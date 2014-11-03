/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.rpclibraries.math;

import me.yuhuan.network.core.TcpMessenger;
import me.yuhuan.network.core.ProcedureInfo;
import me.yuhuan.network.core.ServerInfo;
import me.yuhuan.network.core.UdpMessenger;
import me.yuhuan.network.exceptions.ReliableUdpTransmissionFailedException;
import me.yuhuan.network.rpc.PortMap;
import me.yuhuan.network.rpc.RpcData;
import me.yuhuan.network.rpc.types.*;
import me.yuhuan.utility.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */
public class ClientStub {

    private static final int PART_SIZE = 100;
    private static PortMap _localCache;

    public static double[][] multiply(double[][] a, double[][] b) throws IOException {
        /**
         * Go to the port mapper, and obtain the server information.
         */
        ProcedureInfo procedureToExecute = Converters.methodNameToProcedureInfo("multiply");
        ServerInfo targetServer = lookForServer(procedureToExecute);
        InetAddress serverIp = targetServer.inetAddress();
        int port = targetServer.portNumber;

        /**
         * Check if the server supports the procedure.
         */
        if (!procedureAvailabilityCheck(procedureToExecute, serverIp, port)) {
            // TODO: throw new ProcedureNotSupportedException();
        }

        /**
         * Tell the server which procedure to execute.
         */
        Socket socketToServer = new Socket(serverIp, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToServer.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToServer.getInputStream());

        TcpMessenger.sendTag(dataOutputStream, Tags.REQUEST_PROCEDURE_EXECUTION);
        TcpMessenger.sendProcedureInfo(dataOutputStream, procedureToExecute);


        /**
         * Tell the server the transaction ID
         */
        int transactionId = UidGenerator.next();
        TcpMessenger.sendTransactionId(dataOutputStream, transactionId);

        /**
         * Tell the server where to reply the result
         */
        DatagramSocket socket = new DatagramSocket();
        TcpMessenger.sendPortNumber(dataOutputStream, socket.getLocalPort());


        /**
         * Get the UDP port number of the server
         */
        int newServerPort = TcpMessenger.receivePortNumber(dataInputStream);


        /**
         * ALERT!!! FROM THIS POINT ON, THE TCP CONNECTION IS GONE!!! GONE FOR GOOD!!!!!
         * DO NOT ATTEMPT TO CALL Messenger.receiveXXX(dataInputStream) or Messenger.sendXXX(dataOutputStream)
         */

        /**
         * Send the two matrices to the server.
         */
        RpcMatrix rpcMatrixA = new RpcMatrix(a);
        RpcMatrix rpcMatrixB = new RpcMatrix(b);

        UdpMessenger.sendRpcDataArray(socket, rpcMatrixA.prepareForSend(transactionId, PART_SIZE), targetServer.inetAddress(), newServerPort);
        UdpMessenger.sendRpcDataArray(socket, rpcMatrixB.prepareForSend(transactionId, PART_SIZE), targetServer.inetAddress(), newServerPort);

        RpcData[] response = UdpMessenger.receiveRpcDataArray(socket);

        RpcInt tag = new RpcInt(response[0]);
        if (tag.toInt() == Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS) {
            RpcData[] resultData = UdpMessenger.receiveRpcDataArray(socket);
            RpcMatrix result = new RpcMatrix(resultData);
            return result.toArray();
        }
        else {
            RpcData[] errorData = UdpMessenger.receiveRpcDataArray(socket);
            RpcError error = new RpcError(errorData[0]);
            throw error.toException();
        }
    }

    public static double[] sort(double[] array) throws IOException {
        /**
         * Go to the port mapper, and obtain the server information.
         */
        ProcedureInfo procedureToExecute = Converters.methodNameToProcedureInfo("sort");
        ServerInfo targetServer = lookForServer(procedureToExecute);
        InetAddress serverIp = targetServer.inetAddress();
        int port = targetServer.portNumber;

        /**
         * Check if the server supports the procedure.
         */
        if (!procedureAvailabilityCheck(procedureToExecute, serverIp, port)) {
            // TODO: throw new ProcedureNotSupportedException();
        }

        /**
         * Tell the server which procedure to execute.
         */
        Socket socketToServer = new Socket(serverIp, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToServer.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToServer.getInputStream());

        TcpMessenger.sendTag(dataOutputStream, Tags.REQUEST_PROCEDURE_EXECUTION);
        TcpMessenger.sendProcedureInfo(dataOutputStream, procedureToExecute);


        /**
         * Tell the server the transaction ID
         */
        int transactionId = UidGenerator.next();
        TcpMessenger.sendTransactionId(dataOutputStream, transactionId);

        /**
         * Tell the server where to reply the result
         */
        DatagramSocket socket = new DatagramSocket();
        TcpMessenger.sendPortNumber(dataOutputStream, socket.getLocalPort());


        /**
         * Get the UDP port number of the server
         */
        int newServerPort = TcpMessenger.receivePortNumber(dataInputStream);

        /**
         * Send the two matrices to the server.
         */
        RpcArray rpcArray = new RpcArray(array);

        UdpMessenger.sendRpcDataArray(socket, rpcArray.prepareForSend(transactionId, PART_SIZE), targetServer.inetAddress(), newServerPort);

        RpcData[] response = UdpMessenger.receiveRpcDataArray(socket);

        RpcInt tag = new RpcInt(response[0]);
        if (tag.toInt() == Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS) {
            RpcData[] resultData = UdpMessenger.receiveRpcDataArray(socket);
            RpcArray result = new RpcArray(resultData);
            return result.toArray();
        }
        else {
            RpcData[] errorData = UdpMessenger.receiveRpcDataArray(socket);
            RpcError error = new RpcError(errorData[0]);
            throw error.toException();
        }

    }

    public static double max(double[] array) throws IOException {
        /**
         * Go to the port mapper, and obtain the server information.
         */
        ProcedureInfo procedureToExecute = Converters.methodNameToProcedureInfo("max");
        ServerInfo targetServer = lookForServer(procedureToExecute);
        InetAddress serverIp = targetServer.inetAddress();
        int port = targetServer.portNumber;

        /**
         * Check if the server supports the procedure.
         */
        if (!procedureAvailabilityCheck(procedureToExecute, serverIp, port)) {
            // TODO: throw new ProcedureNotSupportedException();
        }

        /**
         * Tell the server which procedure to execute.
         */
        Socket socketToServer = new Socket(serverIp, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToServer.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToServer.getInputStream());

        TcpMessenger.sendTag(dataOutputStream, Tags.REQUEST_PROCEDURE_EXECUTION);
        TcpMessenger.sendProcedureInfo(dataOutputStream, procedureToExecute);


        /**
         * Tell the server the transaction ID
         */
        int transactionId = UidGenerator.next();
        TcpMessenger.sendTransactionId(dataOutputStream, transactionId);

        /**
         * Tell the server where to reply the result
         */
        DatagramSocket socket = new DatagramSocket();
        TcpMessenger.sendPortNumber(dataOutputStream, socket.getLocalPort());


        /**
         * Get the UDP port number of the server
         */
        int newServerPort = TcpMessenger.receivePortNumber(dataInputStream);

        /**
         * Send the parameter to the server.
         */
        RpcArray rpcArray = new RpcArray(array);

        UdpMessenger.sendRpcDataArray(socket, rpcArray.prepareForSend(transactionId, PART_SIZE), targetServer.inetAddress(), newServerPort);

        RpcData[] response = UdpMessenger.receiveRpcDataArray(socket);

        RpcInt tag = new RpcInt(response[0]);
        if (tag.toInt() == Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS) {
            RpcData[] resultData = UdpMessenger.receiveRpcDataArray(socket);
            RpcDouble result = new RpcDouble(resultData[0]);
            return result.toDouble();
        }
        else {
            RpcData[] errorData = UdpMessenger.receiveRpcDataArray(socket);
            RpcError error = new RpcError(errorData[0]);
            throw error.toException();
        }
    }

    public static double min(double[] array) throws IOException {
        /**
         * Go to the port mapper, and obtain the server information.
         */
        ProcedureInfo procedureToExecute = Converters.methodNameToProcedureInfo("min");
        ServerInfo targetServer = lookForServer(procedureToExecute);
        InetAddress serverIp = targetServer.inetAddress();
        int port = targetServer.portNumber;

        /**
         * Check if the server supports the procedure.
         */
        if (!procedureAvailabilityCheck(procedureToExecute, serverIp, port)) {
            // TODO: throw new ProcedureNotSupportedException();
        }

        /**
         * Tell the server which procedure to execute.
         */
        Socket socketToServer = new Socket(serverIp, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToServer.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToServer.getInputStream());

        TcpMessenger.sendTag(dataOutputStream, Tags.REQUEST_PROCEDURE_EXECUTION);
        TcpMessenger.sendProcedureInfo(dataOutputStream, procedureToExecute);


        /**
         * Tell the server the transaction ID
         */
        int transactionId = UidGenerator.next();
        TcpMessenger.sendTransactionId(dataOutputStream, transactionId);

        /**
         * Tell the server where to reply the result
         */
        DatagramSocket socket = new DatagramSocket();
        TcpMessenger.sendPortNumber(dataOutputStream, socket.getLocalPort());


        /**
         * Get the UDP port number of the server
         */
        int newServerPort = TcpMessenger.receivePortNumber(dataInputStream);

        /**
         * Send the parameter to the server.
         */
        RpcArray rpcArray = new RpcArray(array);

        UdpMessenger.sendRpcDataArray(socket, rpcArray.prepareForSend(transactionId, PART_SIZE), targetServer.inetAddress(), newServerPort);

        RpcData[] response = UdpMessenger.receiveRpcDataArray(socket);

        RpcInt tag = new RpcInt(response[0]);
        if (tag.toInt() == Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS) {
            RpcData[] resultData = UdpMessenger.receiveRpcDataArray(socket);
            RpcDouble result = new RpcDouble(resultData[0]);
            return result.toDouble();
        }
        else {
            RpcData[] errorData = UdpMessenger.receiveRpcDataArray(socket);
            RpcError error = new RpcError(errorData[0]);
            throw error.toException();
        }

    }

    private static boolean procedureAvailabilityCheck(ProcedureInfo procedureInfo, InetAddress serverIp, int serverTcpPort) throws IOException {
        // TODO: check availability

        Socket socketToServer = new Socket(serverIp, serverTcpPort);
        DataOutputStream outputStream = new DataOutputStream(socketToServer.getOutputStream());
        DataInputStream inputStream = new DataInputStream(socketToServer.getInputStream());

        TcpMessenger.sendTag(outputStream, Tags.REQUEST_PROCEDURE_AVAILABILITY_CHECK);
        TcpMessenger.sendProcedureInfo(outputStream, procedureInfo);

        int result = TcpMessenger.receiveTag(inputStream);

        if (result == Tags.RESPOND_PROCEDURE_AVAILABILITY_CHECK_YES) {
            return true;
        }
        else {
            if (_localCache.containsProcedure(procedureInfo)) {
                _localCache.removeProcedure(procedureInfo);
            }
            return false;
        }
    }

    private static ServerInfo lookForServer(ProcedureInfo procedureInfo) throws IOException {
        // Check local cache
        if (_localCache == null) {
            _localCache = new PortMap();
        }

        if (_localCache.containsProcedure(procedureInfo)) {
            return _localCache.getServerByProcedure(procedureInfo);
        }

        // Below deals with the case where the local cache does not have the entry.

        // Figure out where the Port Mapper is
        String[] lines = TextFile.read("port_mapper_info");
        String portMapperIp = lines[0];
        int portMapperPort = Integer.parseInt(lines[1]);

        // Look up the server info by procedure info
        Socket socketToPortMapper = new Socket(portMapperIp, portMapperPort);

        DataOutputStream dataOutputStream = new DataOutputStream(socketToPortMapper.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToPortMapper.getInputStream());

        TcpMessenger.sendTag(dataOutputStream, Tags.REQUEST_LOOK_FOR_PROCEDURE);
        TcpMessenger.sendProcedureInfo(dataOutputStream, procedureInfo);

        ServerInfo result = TcpMessenger.receiveServerInfo(dataInputStream);
        _localCache.register(procedureInfo, result);
        return result;
    }
}
