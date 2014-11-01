/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Yuhuan Jiang on 10/18/14.
 */
public class ServerInfo {
    public int[] ipAddress;
    public int portNumber;

    public String IPAddressString() {
        return ipAddress[0] + "." + ipAddress[1] + "." + ipAddress[2] + "." + ipAddress[3];
    }

    public InetAddress inetAddress() throws UnknownHostException {
        return InetAddress.getByName(this.IPAddressString());
    }

    public ServerInfo(String ipAddressString, int portNumber) {
        String[] components = ipAddressString.split("\\.");
        ipAddress = new int[4];
        for (int i = 0; i < 4; i++) {
            ipAddress[i] = Integer.parseInt(components[i]);
        }
        this.portNumber = portNumber;
    }

    public ServerInfo(int[] IPAddress, int portNumber) {
        this.ipAddress = IPAddress;
        this.portNumber = portNumber;
    }
}
