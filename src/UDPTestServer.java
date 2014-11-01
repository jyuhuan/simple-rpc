/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

import me.yuhuan.network.core.UdpMessenger;
import me.yuhuan.network.exceptions.ReliableUdpTransmissionFailedException;
import me.yuhuan.network.rpc.RpcData;
import me.yuhuan.network.rpc.types.RpcMatrix;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Created by Yuhuan Jiang on 10/27/14.
 */
public class UDPTestServer {

    public static void main(String[] args) throws IOException, ReliableUdpTransmissionFailedException {
        DatagramSocket serverSocket = new DatagramSocket(24601);

        RpcData[] rpcDataArray = UdpMessenger.receiveRpcDataArray(serverSocket);
        RpcMatrix matrix = new RpcMatrix(rpcDataArray);

    }

}
