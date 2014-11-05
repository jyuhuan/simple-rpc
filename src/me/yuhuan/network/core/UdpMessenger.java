/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.core;

import me.yuhuan.network.exceptions.ReliableUdpTransmissionFailedException;
import me.yuhuan.network.rpc.RpcData;
import me.yuhuan.network.rudp.ReliableUdpTransporter;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Yuhuan Jiang on 10/25/14.
 */

/**
 * Sends and receives RPC data through UDP sockets.
 */
public class UdpMessenger {

    /**
     * Sends an array of {@link me.yuhuan.network.rpc.RpcData RpcData} objects through UDP.
     * @param socket Sender's socket.
     * @param rpcDataArray The array of RpcData objects to send.
     * @param receiverIp Receiver's IP address.
     * @param receiverPort Receiver's port that the receiver's UDP socket is bound to.
     * @throws IOException
     */
    public static void sendRpcDataArray(DatagramSocket socket, RpcData[] rpcDataArray, InetAddress receiverIp, int receiverPort) throws IOException, ReliableUdpTransmissionFailedException {
        ReliableUdpTransporter.send(socket, rpcDataArray, receiverIp, receiverPort);
    }

    /**
     * Waits and receives an array of {@link me.yuhuan.network.rpc.RpcData} objects through UDP.
     * This is a blocking method.
     * @param receiverSocket The socket that the data are being sent to, and that the data are about to be extracteds.
     * @return The array of RpcData objects received.
     * @throws IOException
     */
    public static RpcData[] receiveRpcDataArray(DatagramSocket receiverSocket) throws IOException, ReliableUdpTransmissionFailedException {
        return ReliableUdpTransporter.receive(receiverSocket);
    }


}
