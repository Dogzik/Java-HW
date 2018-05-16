package ru.ifmo.rain.dovzhik.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MsgUtils {
    public static String getMsgText(final DatagramPacket msg) {
        return new String(msg.getData(), msg.getOffset(), msg.getLength(), StandardCharsets.UTF_8);
    }

    public static void setMsgText(final DatagramPacket msg, final String text) {
        msg.setData(text.getBytes(StandardCharsets.UTF_8));
    }

    public static DatagramPacket makeMsgToSend(final SocketAddress dst, final int buffSize) {
        final byte[] buff = new byte[buffSize];
        return new DatagramPacket(buff, buffSize, dst);
    }

    public static DatagramPacket makeMsgToReceive(final int buffSize) {
        final byte[] buff = new byte[buffSize];
        return new DatagramPacket(buff, buff.length);
    }
}
