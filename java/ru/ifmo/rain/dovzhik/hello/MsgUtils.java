package ru.ifmo.rain.dovzhik.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MsgUtils {
    public static String getMsgText(final DatagramPacket msg) {
        return new String(msg.getData(), msg.getOffset(), msg.getLength(), StandardCharsets.UTF_8);
    }

    public static void setMsgText(final DatagramPacket msg, final String text) throws IOException {
        final byte[] response = text.getBytes(StandardCharsets.UTF_8);
        if (response.length > msg.getData().length - msg.getOffset()) {
            throw new IOException("Message too big to be sent");
        }
        System.arraycopy(response, 0, msg.getData(), msg.getOffset(), response.length);
        msg.setLength(response.length);
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
