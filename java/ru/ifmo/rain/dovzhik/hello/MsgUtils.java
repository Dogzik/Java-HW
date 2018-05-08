package ru.ifmo.rain.dovzhik.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MsgUtils {

    public static String getMsgText(final DatagramPacket msg) {
        return new String(msg.getData(), msg.getOffset(), msg.getLength(), StandardCharsets.UTF_8);
    }

    public static DatagramPacket makeMsgToSend(final SocketAddress dst, final String text, final int maxSize) throws IOException {
        final byte[] respond = text.getBytes(StandardCharsets.UTF_8);
        if (respond.length > maxSize) {
            throw new IOException("Message too big to be sent");
        }
        return new DatagramPacket(respond, respond.length, dst);
    }

    public static DatagramPacket makeMsgToReceive(final int buffSize) {
        final byte[] buff = new byte[buffSize];
        return new DatagramPacket(buff, buff.length);
    }
}
