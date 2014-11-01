/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc.types;

import me.yuhuan.network.rpc.RpcData;

import java.io.*;
import java.util.Arrays;

/**
 * Created by Yuhuan Jiang on 10/25/14.
 */
public class RpcMatrix {
    double[][] _data;

    public RpcMatrix(double[][] data) {
        _data = data;
    }

    public RpcMatrix(RpcData[] data) throws IOException {

        /**
         * Figure out the size of the matrix
         */
        ByteArrayInputStream byteArrayInputStreamForFirstRpcData = new ByteArrayInputStream(data[0].content.clone());
        DataInputStream inputStreamForFirstData = new DataInputStream(byteArrayInputStreamForFirstRpcData);
        int rowCount = inputStreamForFirstData.readInt();
        int colCount = inputStreamForFirstData.readInt();

        _data = new double[rowCount][colCount];

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        int numTotalParts = data[0].numTotalParts;
        for (int i = 0; i < numTotalParts; i++) {
            RpcData curRpcData = data[i];
            byte[] withHeader = curRpcData.content;
            byte[] withoutSizeHeader = Arrays.copyOfRange(withHeader, 8, withHeader.length);
            outputStream.write(withoutSizeHeader);
        }

        byte[] allBytes = byteArrayOutputStream.toByteArray();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(allBytes);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
        for (int i = 0; i < rowCount * colCount; i++) {
            int r = i / colCount;
            int c = i % colCount;
            _data[r][c] = inputStream.readDouble();
        }

    }

    /**
     * Segments the matrix into fixed sizes.
     * @param transactionId asdf
     * @param numNumbersPerPart dsf
     * @return dfg
     * @throws java.io.IOException
     */
    public RpcData[] prepareForSend(int transactionId, int numNumbersPerPart) throws IOException {
        int rowCount = _data.length;
        int colCount = _data[0].length;
        int totalNumNumbers = rowCount * colCount;
        double[] flatMatrix = new double[totalNumNumbers];
        for (int i = 0; i < totalNumNumbers; i++) {
            int r = i / colCount;
            int c = i % colCount;
            flatMatrix[i] = _data[r][c];
        }

        // If the size of the flat matrix is less or equal to the limit, don't split
        if (flatMatrix.length <= numNumbersPerPart) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream output = new DataOutputStream(stream);
            output.writeInt(rowCount);
            output.writeInt(colCount);
            for (int i = 0; i < flatMatrix.length; i++) {
                output.writeDouble(flatMatrix[i]);
            }
            byte[] bytesForTheWholeMatrix = stream.toByteArray();
            return new RpcData[] { new RpcData(transactionId, 1, 0, bytesForTheWholeMatrix.length, bytesForTheWholeMatrix) };
        }

        int numParts = (totalNumNumbers / numNumbersPerPart) + 1;
        RpcData data[] = new RpcData[numParts];
        for (int i = 0; i < numParts - 1; i++) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

            // Record the size of the matrix. The receiver needs this information.
            outputStream.writeInt(rowCount);
            outputStream.writeInt(colCount);

            // Obtain the subset of the matrix
            int curPartStart = i * numNumbersPerPart;
            int curPartEnd = i * numNumbersPerPart + numNumbersPerPart;
            double[] curPart = Arrays.copyOfRange(flatMatrix, curPartStart, curPartEnd);

            // Record the subset
            for (int j = 0; j < curPart.length; j++) {
                outputStream.writeDouble(curPart[j]);
            }

            // Obtain the byte array of the current part
            byte[] bytesOfCurPart = byteArrayOutputStream.toByteArray();

            // add headers
            data[i] = new RpcData(transactionId, numParts, i, bytesOfCurPart.length, bytesOfCurPart);
        }

        /**
         * The last part might not be as long as the part size.
         * Therefore, it is dealt with separately.
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        // Record the size of the matrix. The receiver needs this information.
        outputStream.writeInt(rowCount);
        outputStream.writeInt(colCount);

        // Obtain the subset of the matrix
        int lastPartStart = (numParts - 1) * numNumbersPerPart;
        int lastPartEnd = totalNumNumbers;
        double[] lastPart = Arrays.copyOfRange(flatMatrix, lastPartStart, lastPartEnd);

        // Record the subset
        for (int i = 0; i < lastPart.length; i++) {
            outputStream.writeDouble(lastPart[i]);
        }

        // Obtain the byte array of the current part
        byte[] bytesOfLastPart = byteArrayOutputStream.toByteArray();

        // add headers
        data[numParts - 1] = new RpcData(transactionId, numParts, numParts - 1, bytesOfLastPart.length, bytesOfLastPart);

        return data;
    }

    public double[][] toArray() {
        return _data;
    }

}
