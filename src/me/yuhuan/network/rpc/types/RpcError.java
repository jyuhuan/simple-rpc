/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rpc.types;

import me.yuhuan.network.exceptions.ProcedureExecutionException;
import me.yuhuan.network.rpc.RpcData;

import java.io.*;

/**
 * Created by Yuhuan Jiang on 11/1/14.
 */
public class RpcError {

    String _message;

    public RpcError(String message) {
        _message = message;
    }

    public RpcError(RpcData data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.content);
        DataInputStream outputStream = new DataInputStream(byteArrayInputStream);
        _message = outputStream.readUTF();
    }

    public RpcData prepareForSend(int transactionId) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        outputStream.writeUTF(_message);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return new RpcData(transactionId, 1, 0, bytes.length, bytes);
    }

    public ProcedureExecutionException toException() {
        return new ProcedureExecutionException(_message);
    }

}
