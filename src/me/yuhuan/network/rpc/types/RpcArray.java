/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc.types;

import me.yuhuan.network.rpc.RpcData;

import java.io.*;
import java.util.Arrays;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class RpcArray {
    double[] _data;

    public RpcArray(double[] data) {
        _data = data;
    }

    public RpcArray(RpcData[] data) throws IOException {
        /**
         * Figure out the size of the array.
         */

        ByteArrayInputStream byteArrayInputStreamForFirstRpcData = new ByteArrayInputStream(data[0].content.clone());
        DataInputStream inputStreamForFirstData = new DataInputStream(byteArrayInputStreamForFirstRpcData);
        int arraySize = inputStreamForFirstData.readInt();

        _data = new double[arraySize];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        int numTotalParts = data[0].numTotalParts;
        for (int i = 0; i < numTotalParts; i++) {
            RpcData curRpcData = data[i];

            byte[] withoutSizeHeader = Arrays.copyOfRange(curRpcData.content, 4, curRpcData.content.length);

            outputStream.write(withoutSizeHeader);
        }

        byte[] allBytes = byteArrayOutputStream.toByteArray();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allBytes);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
        for (int i = 0; i < arraySize; i++) {
            _data[i] = inputStream.readDouble();
        }
    }

    /**
     * Segments the array into parts of fixed size.
     * @param transactionId The transaction ID.
     * @param numNumbersPerPart The number of numbers (double's) each part should contain.
     * @return The segments of the array, each packed into an {@link me.yuhuan.network.rpc.RpcData RpcData} object.
     * @throws IOException
     */
    public RpcData[] prepareForSend(int transactionId, int numNumbersPerPart) throws IOException {
        double[] flatMatrix = _data;
        int totalNumNumbers = _data.length;

        // If the size of the array is less or equal to the limit, don't split
        if (flatMatrix.length <= numNumbersPerPart) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(stream);
            output.writeInt(totalNumNumbers);

            for (int i = 0; i < flatMatrix.length; i++) {
                output.writeDouble(flatMatrix[i]);
            }
            byte[] bytesForTheWholeArray = stream.toByteArray();
            return new RpcData[] { new RpcData(transactionId, 1, 0, bytesForTheWholeArray.length, bytesForTheWholeArray) };
        }

        int numParts = (totalNumNumbers / numNumbersPerPart) + 1;
        RpcData data[] = new RpcData[numParts];
        for (int i = 0; i < numParts - 1; i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

            // add size header
            outputStream.writeInt(_data.length);

            int curPartStart = i * numNumbersPerPart;
            int curPartEnd = i * numNumbersPerPart + numNumbersPerPart;
            double[] curPart = Arrays.copyOfRange(flatMatrix, curPartStart, curPartEnd);
            for (int j = 0; j < curPart.length; j++) {
                outputStream.writeDouble(curPart[j]);
            }
            byte[] bytesOfCurPart = byteArrayOutputStream.toByteArray();
            data[i] = new RpcData(transactionId, numParts, i, bytesOfCurPart.length, bytesOfCurPart);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        int lastPartStart = (numParts - 1) * numNumbersPerPart;
        int lastPartEnd = totalNumNumbers;
        double[] lastPart = Arrays.copyOfRange(flatMatrix, lastPartStart, lastPartEnd);

        // add size header
        outputStream.writeInt(_data.length);

        for (int i = 0; i < lastPart.length; i++) {
            outputStream.writeDouble(lastPart[i]);
        }
        byte[] bytesOfLastPart = byteArrayOutputStream.toByteArray();
        data[numParts - 1] = new RpcData(transactionId, numParts, numParts - 1, bytesOfLastPart.length, bytesOfLastPart);

        return data;
    }

    public double[] toArray() {
        return _data;
    }

}
