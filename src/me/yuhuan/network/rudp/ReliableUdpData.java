/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
 * International License (http://creativecommons.org/licenses/by-nc-nd/4.0/).
 */

package me.yuhuan.network.rudp;

import java.io.*;

/**
 * Created by Yuhuan Jiang on 10/28/14.
 */
public class ReliableUdpData implements Comparable<ReliableUdpData> {
    public int seqNumber;
    public int lengthOfData;
    public byte[] data;

    public ReliableUdpData(int seqNumber, byte[] data) throws IOException {
        this.seqNumber = seqNumber;
        this.lengthOfData = data.length;
        this.data = data;
    }

    public ReliableUdpData(byte[] bytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
        this.seqNumber = inputStream.readInt();
        this.lengthOfData = inputStream.readInt();
        byte[] bytesToConstruct = new byte[lengthOfData];
        inputStream.read(bytesToConstruct);
        this.data = bytesToConstruct;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
        outputStream.writeInt(seqNumber);
        outputStream.writeInt(lengthOfData);
        outputStream.write(data);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int hashCode() {
        return seqNumber;
    }

    @Override
    public boolean equals(Object other) {
        return this.seqNumber == ((ReliableUdpData)other).seqNumber;
    }


    @Override
    public int compareTo(ReliableUdpData o) {
        return o.seqNumber - this.seqNumber;
    }
}
