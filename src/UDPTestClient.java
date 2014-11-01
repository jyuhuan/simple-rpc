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
import java.net.InetAddress;

/**
 * Created by Yuhuan Jiang on 10/27/14.
 */
public class UDPTestClient {

    public static void main(String[] args) throws IOException, ReliableUdpTransmissionFailedException {

        double[][] a = new double[][] {
                new double[] { 2.0, 4.1, 6.3, 0.4, 1.3 },
                new double[] { 1.2, 3.4, 5.6, 7.8, 9.9 },
                new double[] { 2.1, 3.2, 4.3, 5.4, 6.5 }
        };

        RpcMatrix matrix = new RpcMatrix(a);
        RpcData[] toSend = matrix.prepareForSend(222, 4);


        DatagramSocket clientSocket = new DatagramSocket(12345);

        UdpMessenger.sendRpcDataArray(clientSocket, toSend, InetAddress.getByName("127.0.0.1"), 24601);

    }

}
