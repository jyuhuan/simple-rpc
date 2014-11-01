/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

/**
 * Created by Yuhuan Jiang on 10/16/14.
 */

import me.yuhuan.network.core.Messenger;
import me.yuhuan.network.core.ProcedureInfo;
import me.yuhuan.network.core.ServerInfo;
import me.yuhuan.network.core.UdpMessenger;
import me.yuhuan.network.rpc.RpcData;
import me.yuhuan.network.rpc.types.*;
import me.yuhuan.rpclibraries.math.Implementations;
import me.yuhuan.utility.*;
import me.yuhuan.utility.Console;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Server {
    static String portMapperIp;
    static int portMapperPort;
    public static int PART_SIZE = 512;


    public static String myIpAddress;
    public static int myPortNumber;

    public static HashSet<ProcedureInfo> supportedProcedures;

    public static void main(String[] args) throws IOException {

        // Creates a socket that the server listens to.
        // The port number is automatically assigned by the OS.
        ServerSocket serverSocket = new ServerSocket(0);

        // Figure out the IP address that this server is running at.
        myIpAddress = InetAddress.getLocalHost().getHostAddress();

        // Figure out the port number that the server socket is listening to.
        myPortNumber = serverSocket.getLocalPort();

        // Prompt to the command line the IP and port number of this server.
        Console.writeLine("I am running on " + myIpAddress + ", at port " + myPortNumber);

        // Read the well-known file to figure out where the Port Mapper is
        String[] lines = TextFile.read("port_mapper_info");
        portMapperIp = lines[0];
        portMapperPort = Integer.parseInt(lines[1]);

        // Register procedures
        ProcedureInfo procedureMultiply = Converters.methodNameToProcedureInfo("multiply");
        ProcedureInfo procedureSort = Converters.methodNameToProcedureInfo("sort");
        ProcedureInfo procedureMax = Converters.methodNameToProcedureInfo("max");
        ProcedureInfo procedureMin = Converters.methodNameToProcedureInfo("min");
        supportedProcedures = new HashSet<ProcedureInfo>();
        supportedProcedures.add(procedureMultiply);
        supportedProcedures.add(procedureSort);
        supportedProcedures.add(procedureMax);
        supportedProcedures.add(procedureMin);
        registerProcedures(supportedProcedures);

        // Listen to incoming requests
        try {
            while (true) {
                // Obtain the client's TCP socket.
                Socket clientSocket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

                // Determine whether the client wants to:
                //     (1) perform PAC, or
                //     (2) execute a procedure.
                int tag = Messenger.receiveTag(dataInputStream);
                if (tag == Tags.REQUEST_PROCEDURE_AVAILABILITY_CHECK) {
                    Console.writeLine("Client " + clientSocket + " wants to perform a procedure availability check. ");
                    (new ProcedureAvailabilityCheckWorker(clientSocket)).start();
                }
                else if (tag == Tags.REQUEST_PROCEDURE_EXECUTION) {
                    Console.writeLine("Client " + clientSocket + " wants to execute a procedure. ");

                    // Figure out which procedure the client wants to execute.
                    ProcedureInfo procedureToExecute = Messenger.receiveProcedureInfo(dataInputStream);

                    // Get the transaction ID.
                    int transactionId = Messenger.receiveTransactionId(dataInputStream);

                    // Get the UDP port number of the client, to which the result should be sent.
                    int clientUdpPort = Messenger.receivePortNumber(dataInputStream);

                    // Create a UDP socket for the current client to send data to.
                    DatagramSocket udpSocketOfServer = new DatagramSocket(0);

                    // Tell the client to which port should the data be sent.
                    Messenger.sendPortNumber(dataOutputStream, udpSocketOfServer.getLocalPort());

                    // Start the execution worker.
                    if (procedureToExecute.equals(procedureMultiply)) {
                        (new MultiplicationWorker(udpSocketOfServer, clientUdpPort, transactionId, clientSocket.getInetAddress())).start();
                    }
                    else if (procedureToExecute.equals(procedureSort)) {
                        (new SortWorker(udpSocketOfServer, clientUdpPort, transactionId, clientSocket.getInetAddress())).start();
                    }
                    else if (procedureToExecute.equals(procedureMax)) {
                        (new MaxWorker(udpSocketOfServer, clientUdpPort, transactionId, clientSocket.getInetAddress())).start();
                    }
                    else if (procedureToExecute.equals(procedureMin)) {
                        (new MinWorker(udpSocketOfServer, clientUdpPort, transactionId, clientSocket.getInetAddress())).start();
                    }

                    // Close the current TCP connection, because the parameters will be sent via UDP.
                    // This TCP connection is now useless.
                    clientSocket.close();
                }
            }
        }
        finally {
            serverSocket.close();
        }
    }

    /**
     * Contacts the port mapper and register given methods.
     * @param toBeRegistered Methods to register.
     * @throws IOException
     */
    private static void registerProcedures(HashSet<ProcedureInfo> toBeRegistered) throws IOException {
        // Connect to the port mapper using TCP.
        Socket socketToPortMapper = new Socket(portMapperIp, portMapperPort);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToPortMapper.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToPortMapper.getInputStream());

        // Sends a tag to indication the intent to register procedures.
        Messenger.sendTag(dataOutputStream, Tags.REQUEST_REGISTER_PROCEDURE);

        // Prepare procedures to register
        ArrayList<ProcedureInfo> procedures = new ArrayList<ProcedureInfo>();
        for (ProcedureInfo p : toBeRegistered) {
            procedures.add(p);
        }

        // Send the ProcedureInfo objects to the port mapper.
        Messenger.sendProcedureInfos(dataOutputStream, procedures);

        // Send the ServerInfo of this server to the port mapper.
        Messenger.sendServerInfo(dataOutputStream, new ServerInfo(myIpAddress, myPortNumber));

        // Prompt the response from the port mapper to the command line.
        Console.writeLine("Response = " + Messenger.receiveTag(dataInputStream));
    }

    /**
     * A worker that checks the availability of a procedure on this server.
     * This is to detect the situation where the port mapper, or the local cache on the client stub is out-dated.
     */
    private static class ProcedureAvailabilityCheckWorker extends Thread {
        /**
         * The TPC socket of the client. Used to talk back to the client.
         */
        Socket _clientSocket;

        /**
         * Creates a worker that checks the availability of a procedure on this server.
         * @param clientSocket The TPC socket on the client side.
         *                     This is needed because the worker sends the checking result back to the client.
         */
        public ProcedureAvailabilityCheckWorker(Socket clientSocket) {
            _clientSocket = clientSocket;
        }

        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(_clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(_clientSocket.getOutputStream());
                ProcedureInfo procedureToCheck = Messenger.receiveProcedureInfo(dataInputStream);

                // Checks whether the requested procedure is in the set of supported procedures,
                // and send the result back to the client.
                if (supportedProcedures.contains(procedureToCheck)) {
                    Messenger.sendTag(dataOutputStream, Tags.RESPOND_PROCEDURE_AVAILABILITY_CHECK_YES);
                }
                else Messenger.sendTag(dataOutputStream, Tags.RESPOND_PROCEDURE_AVAILABILITY_CHECK_NO);
            }
            catch (IOException e) {
                Console.writeLine("IO error");
            }
            finally {
                try {
                    _clientSocket.close();
                    Console.writeLine("Thread ends. Connection closes.");
                }
                catch (IOException e) {
                    Console.writeLine("Client socket failed to close. ");
                }
            }
        }

    }

    /**
     * A worker that receives parameters from the client via a UDP socket, does the multiplication,
     * and sends the result back to the client via a UDP socket.
     */
    private static class MultiplicationWorker extends Thread {

        InetAddress _clientIp;
        int _clientUdpPort;
        DatagramSocket _serverUdpSocket;
        int _transactionId;

        /**
         * Creates a multiplication worker.
         * @param serverUdpSocket The UDP socket of the server.
         * @param clientUdpPortNumber The port number of the UDP socket of the client.
         * @param transactionId The transaction ID.
         * @param clientIp The IP address of the client.
         */
        public MultiplicationWorker(DatagramSocket serverUdpSocket, int clientUdpPortNumber, int transactionId, InetAddress clientIp) {
            _serverUdpSocket = serverUdpSocket;
            _clientIp = clientIp;
            _clientUdpPort = clientUdpPortNumber;
            _transactionId = transactionId;
        }

        public void run() {
            try {
                // Receive two matrices from the client via UDP.
                RpcData[] dataArrayA = UdpMessenger.receiveRpcDataArray(_serverUdpSocket);
                RpcMatrix matrixA = new RpcMatrix(dataArrayA);

                RpcData[] dataArrayB = UdpMessenger.receiveRpcDataArray(_serverUdpSocket);
                RpcMatrix matrixB = new RpcMatrix(dataArrayB);

                try {
                    // Perform multiplication
                    RpcMatrix result = new RpcMatrix(Implementations.multiply(matrixA.toArray(), matrixB.toArray()));

                    // Create success message
                    RpcInt successMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { successMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, result.prepareForSend(_transactionId, PART_SIZE), _clientIp, _clientUdpPort);
                }
                catch (Exception e) {
                    RpcError error = new RpcError(e.getMessage());

                    // Create error message
                    RpcInt errorMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_FAILURE);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { errorMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { error.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                }

            }
            catch (Exception e) { }
        }
    }

    private static class SortWorker extends Thread {
        InetAddress _clientIp;
        int _clientUdpPort;
        DatagramSocket _serverUdpSocket;
        int _transactionId;

        public SortWorker(DatagramSocket serverUdpSocket, int clientUdpPortNumber, int transactionId, InetAddress clientIp) {
            _serverUdpSocket = serverUdpSocket;
            _clientIp = clientIp;
            _clientUdpPort = clientUdpPortNumber;
            _transactionId = transactionId;
        }

        public void run() {
            try {
                RpcData[] data = UdpMessenger.receiveRpcDataArray(_serverUdpSocket);
                RpcArray array = new RpcArray(data);

                try {
                    // Perform sorting
                    RpcArray result = new RpcArray(Implementations.sort(array.toArray()));

                    // Create success message
                    RpcInt successMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { successMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, result.prepareForSend(_transactionId, PART_SIZE), _clientIp, _clientUdpPort);
                }
                catch (Exception e) {
                    RpcError error = new RpcError(e.getMessage());

                    // Create error message
                    RpcInt errorMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_FAILURE);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { errorMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { error.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                }

            }
            catch (Exception e) { }
        }
    }

    private static class MaxWorker extends Thread {
        InetAddress _clientIp;
        int _clientUdpPort;
        DatagramSocket _serverUdpSocket;
        int _transactionId;

        public MaxWorker(DatagramSocket serverUdpSocket, int clientUdpPortNumber, int transactionId, InetAddress clientIp) {
            _serverUdpSocket = serverUdpSocket;
            _clientIp = clientIp;
            _clientUdpPort = clientUdpPortNumber;
            _transactionId = transactionId;
        }

        public void run() {
            try {
                RpcData[] data = UdpMessenger.receiveRpcDataArray(_serverUdpSocket);
                RpcArray array = new RpcArray(data);

                try {
                    // Perform max
                    RpcDouble result = new RpcDouble(Implementations.max(array.toArray()));

                    // Create success message
                    RpcInt successMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { successMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { result.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                }
                catch (Exception e) {
                    RpcError error = new RpcError(e.getMessage());

                    // Create error message
                    RpcInt errorMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_FAILURE);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { errorMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { error.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                }

            }
            catch (Exception e) { }
        }
    }

    private static class MinWorker extends Thread {
        InetAddress _clientIp;
        int _clientUdpPort;
        DatagramSocket _serverUdpSocket;
        int _transactionId;

        public MinWorker(DatagramSocket serverUdpSocket, int clientUdpPortNumber, int transactionId, InetAddress clientIp) {
            _serverUdpSocket = serverUdpSocket;
            _clientIp = clientIp;
            _clientUdpPort = clientUdpPortNumber;
            _transactionId = transactionId;
        }

        public void run() {
            try {
                RpcData[] data = UdpMessenger.receiveRpcDataArray(_serverUdpSocket);
                RpcArray array = new RpcArray(data);

                try {
                    // Perform max
                    RpcDouble result = new RpcDouble(Implementations.min(array.toArray()));

                    // Create success message
                    RpcInt successMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_SUCCESS);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { successMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[]{result.prepareForSend(_transactionId)}, _clientIp, _clientUdpPort);
                }
                catch (Exception e) {
                    RpcError error = new RpcError(e.getMessage());

                    // Create error message
                    RpcInt errorMsg = new RpcInt(Tags.RESPOND_PROCEDURE_EXECUTION_FAILURE);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { errorMsg.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                    UdpMessenger.sendRpcDataArray(_serverUdpSocket, new RpcData[] { error.prepareForSend(_transactionId) }, _clientIp, _clientUdpPort);
                }
            }
            catch (Exception e) { }
        }
    }




}
