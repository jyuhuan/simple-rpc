/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

import me.yuhuan.network.core.ProcedureInfo;
import me.yuhuan.network.core.ServerInfo;
import me.yuhuan.network.rpc.PortMap;
import me.yuhuan.utility.Console;
import me.yuhuan.network.core.Messenger;
import me.yuhuan.utility.Tags;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */
public class JavaTest {

    //public static String PORT_MAPPER_IP = "150.212.71.79";
    public static String PORT_MAPPER_IP = "150.212.70.40";
    public static int PORT_MAPPER_PORT = 12345;

    public static void main(String[] args) throws IOException {
        //testPortMapperLookup();
        //testPortMapperRegistration();

        PortMap map = new PortMap();
        map.register(new ProcedureInfo(1, 2, 3), new ServerInfo("1.1.1.1", 45));
        map.register(new ProcedureInfo(2, 2, 3), new ServerInfo("1.1.1.1", 45));
        map.register(new ProcedureInfo(1, 2, 3), new ServerInfo("1.1.1.1", 45));
        map.register(new ProcedureInfo(1, 2, 3), new ServerInfo("1.1.1.1", 45));
        map.register(new ProcedureInfo(1, 3, 3), new ServerInfo("1.1.1.1", 45));

    }

    public static void testPortMapperLookup() throws IOException {
        Socket socketToPortMapper = new Socket(PORT_MAPPER_IP, PORT_MAPPER_PORT);

        DataOutputStream dataOutputStream = new DataOutputStream(socketToPortMapper.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToPortMapper.getInputStream());

        Messenger.sendTag(dataOutputStream, Tags.REQUEST_LOOK_FOR_PROCEDURE);
        Messenger.sendProcedureInfo(dataOutputStream, new ProcedureInfo(1234, 7, 1));

        ServerInfo found = Messenger.receiveServerInfo(dataInputStream);

        Console.writeLine(found.IPAddressString());
        Console.writeLine(found.portNumber + "");
    }

    public static void testPortMapperRegistration() throws IOException {
        Socket socketToPortMapper = new Socket(PORT_MAPPER_IP, PORT_MAPPER_PORT);
        DataOutputStream dataOutputStream = new DataOutputStream(socketToPortMapper.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socketToPortMapper.getInputStream());

        Messenger.sendTag(dataOutputStream, Tags.REQUEST_REGISTER_PROCEDURE);


        ArrayList<ProcedureInfo> toBeRegistered = new ArrayList<ProcedureInfo>();
        toBeRegistered.add(new ProcedureInfo(1234, 7, 1));
        toBeRegistered.add(new ProcedureInfo(1234, 8, 1));
        toBeRegistered.add(new ProcedureInfo(1234, 9, 1));
        toBeRegistered.add(new ProcedureInfo(1234, 10, 1));

        Messenger.sendProcedureInfos(dataOutputStream, toBeRegistered);
        Messenger.sendServerInfo(dataOutputStream, new ServerInfo(Inet4Address.getLocalHost().getHostAddress(), 8888));

        Console.writeLine("Response = " + Messenger.receiveTag(dataInputStream));
    }

}
