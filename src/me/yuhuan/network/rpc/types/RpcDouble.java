/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc.types;

import me.yuhuan.network.rpc.RpcData;

import java.io.*;

/**
 * Created by Yuhuan Jiang on 10/26/14.
 */
public class RpcDouble {

    double _data;

    public RpcDouble(double data) {
        _data = data;
    }

    public RpcDouble(RpcData data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.content);
        DataInputStream outputStream = new DataInputStream(byteArrayInputStream);
        _data = outputStream.readDouble();
    }

    public RpcData prepareForSend(int transactionId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        outputStream.writeDouble(_data);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new RpcData(transactionId, 1, 0, bytes.length, bytes);
    }

    public double toDouble() {
        return _data;
    }

}
