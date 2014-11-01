/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc.types;

import me.yuhuan.network.rpc.RpcData;

import java.io.*;

/**
 * Created by Yuhuan Jiang on 11/1/14.
 */
public class RpcInt {

    int _data;

    public RpcInt(int data) {
        _data = data;
    }

    public RpcInt(RpcData data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.content);
        DataInputStream outputStream = new DataInputStream(byteArrayInputStream);
        _data = outputStream.readInt();
    }

    public RpcData prepareForSend(int transactionId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        outputStream.writeInt(_data);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new RpcData(transactionId, 1, 0, bytes.length, bytes);
    }

    public double toInt() {
        return _data;
    }


}
