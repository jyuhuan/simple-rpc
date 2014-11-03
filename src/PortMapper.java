/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */


import me.yuhuan.network.core.*;
import me.yuhuan.network.rpc.PortMap;
import me.yuhuan.utility.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Created by Yuhuan Jiang on 10/18/14.
 */

/**
 * A port mapper program that acts as a name server.
 * It handles:
 *     (1) Registration request from servers
 *     (2) Lookup request from clients
 */
public class PortMapper {

    // The port mapper table. See class PortMap for details.
    public static PortMap portMap;

    public static void main(String[] main) throws IOException {

        // When the PortMapper program starts, the portMap is create anew.
        portMap = new PortMap();

        // The socket on the Port Mapper side. This is used to listen to incoming requests.
        ServerSocket serverSocket = new ServerSocket(0);

        // Figure out the IP address of this port mapper
        String myIpAddress = InetAddress.getLocalHost().getHostAddress();

        // Figure out what port mapper the OS assigned to this port mapper.
        int myPortNumber = serverSocket.getLocalPort();

        // A commandline prompt of the information about this port mapper.
        Console.writeLine("I am running on " + myIpAddress + ", at port " + myPortNumber);

        // Write the IP and port number of this port mapper to a publicly known file location.
        TextFile.write("port_mapper_info", new String[] { myIpAddress, String.valueOf(myPortNumber) });

        // This try block listens to the socket, dispatches the worker threads
        // based on what the incoming request is.
        try {
            while (true) {
                // Get the client's TCP socket.
                Socket clientSocket = serverSocket.accept();

                // A stream for reading
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

                // Reads the first int. This int should be either:
                //     Tags.REQUEST_REGISTER_PROCEDURE, or
                //     Tags.REQUEST_LOOK_FOR_PROCEDURE
                int tag = TcpMessenger.receiveTag(dataInputStream);
                if (tag == Tags.REQUEST_REGISTER_PROCEDURE) {
                    Console.writeLine("Client " + clientSocket + " wants to register a procedure. ");
                    (new PortMapperRegistrationWorker(clientSocket)).start();
                }
                else if (tag == Tags.REQUEST_LOOK_FOR_PROCEDURE) {
                    Console.writeLine("Client " + clientSocket + " wants to look for a procedure. ");
                    (new PortMapperLookupWorker(clientSocket)).start();
                }
            }
        }
        finally {
            serverSocket.close();
        }
    }

    /**
     * A worker that registers procedure information in the port mapper table.
     */
    private static class PortMapperRegistrationWorker extends Thread {
        /**
         * The TPC socket of the client. Used to talk back to the client.
         */
        Socket _clientSocket;

        /**
         * Creates a registration worker.
         * @param clientSocket The TPC socket on the client side.
         *                     This is needed because the worker confirms the registration
         *                     by talking back to the client.
         */
        public PortMapperRegistrationWorker(Socket clientSocket) {
            _clientSocket = clientSocket;
        }

        public void run() {
            // The following try block reads the list of pairs of ProcedureInfo and ServerInfo objects,
            // and registers each pair into the table.
            try {
                DataInputStream dataInputStream = new DataInputStream(_clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(_clientSocket.getOutputStream());

                // Reads a list of ProcedureInfo objects.
                ArrayList<ProcedureInfo> proceduresToRegister = TcpMessenger.receiveProcedureInfos(dataInputStream);

                // Reads the ServerInfo.
                ServerInfo serverInfo = TcpMessenger.receiveServerInfo(dataInputStream);

                // The actually registration
                Console.writeLine("Start registering for " + serverInfo.IPAddressString() + " at port " + serverInfo.portNumber);
                for (ProcedureInfo p : proceduresToRegister) {
                    portMap.register(p, serverInfo);
                    Console.writeLine("\t" + p.programID + ", " + p.procedureID + ", " + p.versionID);
                }

                // Send a tag back to client to confirm the registration.
                TcpMessenger.sendTag(dataOutputStream, Tags.RESPOND_REGISTER_PROCEDURE);
            }
            catch (IOException e) {
                Console.writeLine("IO error");
            }
            finally {
                try {
                    _clientSocket.close();
                    Console.writeLine("Socket to client " + _clientSocket.getInetAddress().getHostAddress() + ":" + _clientSocket.getPort() + " is closed. ");
                }
                catch (IOException e) {
                    Console.writeLine("Socket to client " + _clientSocket.getInetAddress().getHostAddress() + ":" + _clientSocket.getPort() + " failed to close. ");
                }
            }
        }
    }

    /**
     * A worker that looks up the port mapper table using the procedure info as key.
     */
    private static class PortMapperLookupWorker extends Thread {
        /**
         * The TPC socket of the client. Used to talk back to the client.
         */
        Socket _clientSocket;

        /**
         * Creates a table lookup worker.
         * @param clientSocket The TPC socket on the client side.
         *                     This is needed because the worker sends the result back to the client.
         */
        public PortMapperLookupWorker(Socket clientSocket) {
            _clientSocket = clientSocket;
        }

        public void run() {
            // The following try block receives a ProcedureInfo object from the client,
            // and looks up for the server, and sends the ServerInfo found back to the client.
            try {
                DataInputStream dataInputStream = new DataInputStream(_clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(_clientSocket.getOutputStream());

                // Read a ProcedureInfo object from the client.
                ProcedureInfo toFind = TcpMessenger.receiveProcedureInfo(dataInputStream);

                // Query the port mapper table.
                ServerInfo result = portMap.getServerByProcedure(toFind);

                // Send the found ServerInfo back to client.
                TcpMessenger.sendServerInfo(dataOutputStream, result);
            }
            catch (IOException e) {
                Console.writeLine("IO error");
            }
            finally {
                try {
                    _clientSocket.close();
                    Console.writeLine("Socket to client " + _clientSocket.getInetAddress().getHostAddress() + ":" + _clientSocket.getPort() + " is closed. ");
                }
                catch (IOException e) {
                    Console.writeLine("Socket to client " + _clientSocket.getInetAddress().getHostAddress() + ":" + _clientSocket.getPort() + " failed to close. ");
                }
            }
        }
    }
}
