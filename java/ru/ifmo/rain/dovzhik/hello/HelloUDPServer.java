package ru.ifmo.rain.dovzhik.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService workers;
    private boolean closed;
    private int IN_BUFF_SIZE;
    private int OUT_BUFF_SIZE;

    public HelloUDPServer() {
        socket = null;
        workers = null;
        closed = true;
        IN_BUFF_SIZE = OUT_BUFF_SIZE = 0;
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            IN_BUFF_SIZE = socket.getReceiveBufferSize();
            OUT_BUFF_SIZE = socket.getSendBufferSize();
        } catch (SocketException e) {
            System.err.println("Unable to create socket bounded to port №" + port);
            return;
        }
        workers = Executors.newFixedThreadPool(threads);
        closed = false;
        for (int i = 0; i < threads; ++i) {
            workers.submit(this::receiveAndRespond);
        }
    }

    private void receiveAndRespond() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final DatagramPacket msg = MsgUtils.makeMsgToReceive(IN_BUFF_SIZE);
                socket.receive(msg);
                final String msgText = MsgUtils.getMsgText(msg);
                final DatagramPacket respond = MsgUtils.makeMsgToSend(msg.getSocketAddress(), "Hello, " + msgText, OUT_BUFF_SIZE);
                socket.send(respond);
            } catch (IOException e) {
                if (!closed) {
                    System.err.println("Error occurred during processing datagram: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
        workers.shutdownNow();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("2 non-null arguments expected");
            return;
        }
        try {

            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Integer arguments expected");
        }
    }
}
