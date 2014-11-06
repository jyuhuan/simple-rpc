/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rudp;

import me.yuhuan.network.exceptions.ReliableUdpTransmissionFailedException;
import me.yuhuan.network.rpc.RpcData;
import me.yuhuan.utility.Console;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Created by Yuhuan Jiang on 10/28/14.
 */
public class ReliableUdpTransporter {
    private static Integer _lastSeqNumber;

    private static final int MAX_WAIT_TIME = 20000; //20000
    private static final int BURST_SIZE = 10;
    private static final int MAX_DATA_SIZE = 1000;
    private static final int MAX_TRY_TIME = 3; //10

    private static final boolean SHOULD_LOG = false;


    private static int getNextSeqNumber() {
        if (_lastSeqNumber == null) {
            _lastSeqNumber = 0;
            return 0;
        }
        else {
            _lastSeqNumber++;
            return _lastSeqNumber;
        }
    }

    /**
     * Converts an array of {@link me.yuhuan.network.rpc.RpcData RpcData} objects to an array of
     * {@link me.yuhuan.network.rudp.ReliableUdpData ReliableUdpData} objects, by adding unique sequence numbers.
     *
     * @param rpcDataArray The array of {@link me.yuhuan.network.rpc.RpcData RpcData} objects.
     * @return The array of array of {@link me.yuhuan.network.rudp.ReliableUdpData ReliableUdpData} objects
     * @throws java.io.IOException
     */
    public static ReliableUdpData[] rpcDataArrayToUdpDataArray(RpcData[] rpcDataArray) throws IOException {
        ReliableUdpData[] udpDataArray = new ReliableUdpData[rpcDataArray.length];
        for (int i = 0; i < rpcDataArray.length; i++) {
            RpcData rpcData = rpcDataArray[i];
            udpDataArray[i] = new ReliableUdpData(getNextSeqNumber(), rpcData.toBytes());
        }
        return udpDataArray;
    }

    /**
     * Converts an array of {@link me.yuhuan.network.rudp.ReliableUdpData ReliableUdpData} objects to an array of
     * {@link me.yuhuan.network.rpc.RpcData RpcData} objects, by sorting by the sequence numbers, and removing the
     * sequence numbers.
     * <br/> <br/>
     * The result array is usually used later as the input of the constructor of an RPC object
     * (e.g., {@link me.yuhuan.network.rpc.types.RpcDouble RpcDouble},
     * {@link me.yuhuan.network.rpc.types.RpcArray RpcArray},
     * {@link me.yuhuan.network.rpc.types.RpcMatrix RpcMatrix} ).
     *
     * @param sortedUdpDataArray The array of array of {@link me.yuhuan.network.rudp.ReliableUdpData ReliableUdpData} objects.
     * @return The array of {@link me.yuhuan.network.rpc.RpcData RpcData} objects.
     * @throws java.io.IOException
     */
    public static RpcData[] udpDataArrayToRpcDataArray(ReliableUdpData[] sortedUdpDataArray) throws IOException {
        RpcData[] rpcDataArray = new RpcData[sortedUdpDataArray.length];
        for (int i = 0; i< sortedUdpDataArray.length; i++) {
            rpcDataArray[i] = new RpcData(sortedUdpDataArray[i].data);
        }
        return rpcDataArray;
    }

    public static void send(DatagramSocket socket, RpcData[] rpcDataArray, InetAddress receiverIp, int receiverPort) throws IOException, ReliableUdpTransmissionFailedException {

        /**
         * Create handshake data
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeInt(rpcDataArray.length);
        byte[] handShakeBytes = byteArrayOutputStream.toByteArray();
        ReliableUdpData handShakeData = new ReliableUdpData(getNextSeqNumber(), handShakeBytes);

        /**
         * Send handshake data
         */
        int handShakeTryCount = 0;
        boolean didSucceedInSendingHandShake = false;
        while (!didSucceedInSendingHandShake) {
            didSucceedInSendingHandShake = trySending(socket, handShakeData, receiverIp, receiverPort);
            handShakeTryCount++;
            if (handShakeTryCount == MAX_TRY_TIME) {
                throw new ReliableUdpTransmissionFailedException("Server does not respond. Tried to shake hand " + MAX_TRY_TIME + "times. ");
            }
        }

        /**
         * Send main data array.
         */
        ReliableUdpData[] arrayOfDataToSend = rpcDataArrayToUdpDataArray(rpcDataArray);
        boolean finish = false;
        int curBeginIdx = 0;

        // The following two are for counting repeats
        int numTimesSendingSamePacket = 0;
        int lastSeqNumber = arrayOfDataToSend[0].seqNumber + 1;

        while (!finish) {
            ReliableUdpData[] burst;
            int totalSize = arrayOfDataToSend.length;
            if (totalSize < BURST_SIZE) burst = arrayOfDataToSend;
            else {
                if (curBeginIdx < totalSize - BURST_SIZE) {
                    burst = Arrays.copyOfRange(arrayOfDataToSend, curBeginIdx, curBeginIdx + BURST_SIZE);
                }
                else {
                    burst = Arrays.copyOfRange(arrayOfDataToSend, curBeginIdx, curBeginIdx + totalSize - curBeginIdx);
                }
            }
            int ack = trySendingBurst(socket, burst, receiverIp, receiverPort);
            curBeginIdx = ack - arrayOfDataToSend[0].seqNumber;
            if (ack == arrayOfDataToSend[arrayOfDataToSend.length - 1].seqNumber + 1) finish = true;

            if (ack == lastSeqNumber) {
                numTimesSendingSamePacket++;
                Console.writeLine("Repeat detected.");
            }

            lastSeqNumber = ack;

            if (numTimesSendingSamePacket >= MAX_TRY_TIME) {
                Console.writeLine("Max try time reached.");
                throw new ReliableUdpTransmissionFailedException("Give up. ");
            }
        }
    }

    public static int trySendingBurst(DatagramSocket socket, ReliableUdpData[] burst, InetAddress receiverIp, int receiverPort) throws IOException, ReliableUdpTransmissionFailedException {
        if (SHOULD_LOG) System.out.println("Sender says: Try sending a burst of data beginning with seqNumber = " + burst[0].seqNumber);
        for (ReliableUdpData udpData : burst) {
            byte[] bytesToSend = udpData.toBytes();
            DatagramPacket packetToSend = new DatagramPacket(bytesToSend, bytesToSend.length, receiverIp, receiverPort);
            socket.send(packetToSend);
        }
        socket.setSoTimeout(MAX_WAIT_TIME);

        try {
            byte[] bytesForReceiving = new byte[4];
            DatagramPacket packetForReceiving = new DatagramPacket(bytesForReceiving, bytesForReceiving.length);
            socket.receive(packetForReceiving);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesForReceiving);
            DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
            int ack = inputStream.readInt();
            if (SHOULD_LOG) System.out.println("Sender says: Received ACK " + ack);
            return ack;
        }
        catch (SocketTimeoutException e) {
            return burst[0].seqNumber;
        }
    }

    public static boolean trySending(DatagramSocket socket, ReliableUdpData udpData, InetAddress receiverIp, int receiverPort) throws IOException {
        byte[] bytesToSend = udpData.toBytes();
        DatagramPacket packetToSend = new DatagramPacket(bytesToSend, bytesToSend.length, receiverIp, receiverPort);
        socket.send(packetToSend);
        if (SHOULD_LOG) System.out.println("Sender says: Try sending " + udpData.seqNumber);
        socket.setSoTimeout(MAX_WAIT_TIME);
        try {
            byte[] bytesForReceiving = new byte[4];
            DatagramPacket packetForReceiving = new DatagramPacket(bytesForReceiving, bytesForReceiving.length);
            socket.receive(packetForReceiving);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesForReceiving);
            DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
            int ack = inputStream.readInt();
            //System.out.println("Sender says: Received ACK " + ack);
            if (ack != udpData.seqNumber + 1) return false;
            else return true;
        }
        catch (SocketTimeoutException e) {
            return false;
        }
    }

    public static RpcData[] receive(DatagramSocket socket) throws IOException, ReliableUdpTransmissionFailedException {
        PriorityQueue<ReliableUdpData> received = new PriorityQueue<ReliableUdpData>();
        int totalPacketCount = -1;

        // Receive handshake packet
        boolean didReceiveFirst = false;
        while (!didReceiveFirst) {
            byte[] bytesForFirstPacket = new byte[MAX_DATA_SIZE]; // SeqNumber + Content
            DatagramPacket firstPacket = new DatagramPacket(bytesForFirstPacket, bytesForFirstPacket.length);
            socket.setSoTimeout(MAX_WAIT_TIME);
            try {
                if (SHOULD_LOG) System.out.println("Sender says: try receiving handshake data.");
                socket.receive(firstPacket);
                ReliableUdpData handShakeData = new ReliableUdpData(bytesForFirstPacket);
                received.add(handShakeData);

                ByteArrayInputStream byteArrayInputStreamForFirst = new ByteArrayInputStream(handShakeData.data);
                DataInputStream inputStreamForFirst = new DataInputStream(byteArrayInputStreamForFirst);
                totalPacketCount = inputStreamForFirst.readInt();
                didReceiveFirst = true;

                InetAddress senderIp = firstPacket.getAddress();
                int senderPort = firstPacket.getPort();
                int expectation = received.peek().seqNumber + 1;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
                outputStream.writeInt(expectation);
                byte[] bytesToSender = byteArrayOutputStream.toByteArray();
                DatagramPacket packetToClient = new DatagramPacket(bytesToSender, bytesToSender.length, senderIp, senderPort);
                socket.send(packetToClient);


            } catch (SocketTimeoutException e) {
                throw new ReliableUdpTransmissionFailedException("Hand shake message not received. ");
            }
        }

        if (SHOULD_LOG) System.out.println("Receiver says: received handshake data. Total packet number = " + totalPacketCount);


        // Receive the rest.

        int receivedPacketCount = 0;
        while (receivedPacketCount < totalPacketCount) {
            InetAddress senderIp = InetAddress.getByName("127.0.0.1"); // dummy init value
            int senderPort = -1; // dummy init value

            int numPacketsToReceive = BURST_SIZE;
            if (totalPacketCount - receivedPacketCount < BURST_SIZE) numPacketsToReceive = totalPacketCount - receivedPacketCount;
            for (int i = 0; i < numPacketsToReceive; i++) {
                // create packet for receiving
                byte[] bytesForReceiving = new byte[MAX_DATA_SIZE];
                DatagramPacket packetForReceiving = new DatagramPacket(bytesForReceiving, bytesForReceiving.length);

                // receive message
                boolean timedOut = false;
                try {
                    socket.receive(packetForReceiving);
                    senderIp = packetForReceiving.getAddress();
                    senderPort = packetForReceiving.getPort();
                }
                catch (SocketTimeoutException e) {
                    timedOut = true;
                }

                // if received (i.e., no timeout), add the data to the received data heap.
                if (!timedOut) {
                    ReliableUdpData receivedData = new ReliableUdpData(bytesForReceiving);
                    int lastSeqNumber = received.peek().seqNumber;
                    if (receivedData.seqNumber == lastSeqNumber + 1) {
                        received.add(receivedData);
                        receivedPacketCount++;
                    }
                }
            }

            //System.out.println("Receiver says: received " + received.peek().seqNumber);

            // always send ack(received.peek().seqNumber + 1) to sender.
            // this works, whether the last receive method timed out or not.
            int expectation = received.peek().seqNumber + 1;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            outputStream.writeInt(expectation);
            byte[] bytesToSender = byteArrayOutputStream.toByteArray();
            DatagramPacket packetToClient = new DatagramPacket(bytesToSender, bytesToSender.length, senderIp, senderPort);
            socket.send(packetToClient);
        }

        RpcData[] result = new RpcData[totalPacketCount];
        for (int i = 0; i < totalPacketCount; i++) {
            ReliableUdpData udpData = received.poll();
            result[i] = new RpcData(udpData.data);
        }

        return result;

    }


    /*public static RpcData[] receive(DatagramSocket socket) throws IOException, ReliableUdpTransmissionFailedException {
        PriorityQueue<ReliableUdpData> received = new PriorityQueue<ReliableUdpData>();
        int totalPacketCount = -1;


         // Receive the first packet, which contains the following information:
         // 1. Number of packets to come
         // 2. Whether to do burst acknowledgement


        boolean didReceiveFirst = false;
        while (!didReceiveFirst) {
            byte[] bytesForFirstPacket = new byte[MAX_DATA_SIZE]; // SeqNumber + Content
            DatagramPacket firstPacket = new DatagramPacket(bytesForFirstPacket, bytesForFirstPacket.length);
            socket.setSoTimeout(MAX_WAIT_TIME);
            try {
                System.out.println("Sender says: try receiving handshake data.");
                socket.receive(firstPacket);
                ReliableUdpData handShakeData = new ReliableUdpData(bytesForFirstPacket);
                received.add(handShakeData);

                ByteArrayInputStream byteArrayInputStreamForFirst = new ByteArrayInputStream(handShakeData.data);
                DataInputStream inputStreamForFirst = new DataInputStream(byteArrayInputStreamForFirst);
                totalPacketCount = inputStreamForFirst.readInt();
                didReceiveFirst = true;

                InetAddress senderIp = firstPacket.getAddress();
                int senderPort = firstPacket.getPort();
                int expectation = received.peek().seqNumber + 1;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
                outputStream.writeInt(expectation);
                byte[] bytesToSender = byteArrayOutputStream.toByteArray();
                DatagramPacket packetToClient = new DatagramPacket(bytesToSender, bytesToSender.length, senderIp, senderPort);
                socket.send(packetToClient);


            } catch (SocketTimeoutException e) {
                throw new ReliableUdpTransmissionFailedException("Hand shake message not received. ");
            }
        }

        System.out.println("Receiver says: received handshake data. Total packet number = " + totalPacketCount);


        // Receive the rest.
        int receivedPacketCount = 0;
        while (receivedPacketCount < totalPacketCount) {
            // create packet for receiving
            byte[] bytesForReceiving = new byte[MAX_DATA_SIZE];
            DatagramPacket packetForReceiving = new DatagramPacket(bytesForReceiving, bytesForReceiving.length);

            // receive message
            boolean timedOut = false;
            try {
                socket.receive(packetForReceiving);
            }
            catch (SocketTimeoutException e) {
                timedOut = true;
            }

            // if received (i.e., no timeout), add the data to the received data heap.
            if (!timedOut) {
                ReliableUdpData receivedData = new ReliableUdpData(bytesForReceiving);
                received.add(receivedData);
                receivedPacketCount++;
                System.out.println("Receiver says: received " + receivedData.seqNumber);
            }


            // always send ack(received.peek().seqNumber + 1) to sender.
            // this works, whether the last receive method timed out or not.
            InetAddress senderIp = packetForReceiving.getAddress();
            int senderPort = packetForReceiving.getPort();
            int expectation = received.peek().seqNumber + 1;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            outputStream.writeInt(expectation);
            byte[] bytesToSender = byteArrayOutputStream.toByteArray();
            DatagramPacket packetToClient = new DatagramPacket(bytesToSender, bytesToSender.length, senderIp, senderPort);
            socket.send(packetToClient);
        }

        RpcData[] result = new RpcData[totalPacketCount];
        for (int i = 0; i < totalPacketCount; i++) {
            ReliableUdpData udpData = received.poll();
            result[i] = new RpcData(udpData.data);
        }

        return result;

    }*/
}
