package ru.ifmo.rain.dovzhik.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService workers;
    private ExecutorService listener;
    private boolean closed;
    private int inBuffSize;
    private final int POOL_SIZE = 100_000;

    public HelloUDPServer() {
        socket = null;
        workers = null;
        closed = true;
        inBuffSize = 0;
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            inBuffSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            System.err.println("Unable to create socket bounded to port №" + port);
            return;
        }
        listener = Executors.newSingleThreadExecutor();
        workers = new ThreadPoolExecutor(threads, threads,
                1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(POOL_SIZE), new ThreadPoolExecutor.DiscardPolicy());
        closed = false;
        listener.submit(this::receiveAndRespond);
    }

    private void receiveAndRespond() {
        while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
            try {
                final DatagramPacket msg = MsgUtils.makeMsgToReceive(inBuffSize);
                socket.receive(msg);
                workers.submit(() -> sendResponse(msg));
            } catch (IOException e) {
                if (!closed) {
                    System.err.println("Error occurred during processing datagram: " + e.getMessage());
                }
            }
        }
    }

    private void sendResponse(final DatagramPacket msg) {
        final String msgText = MsgUtils.getMsgText(msg);
        try {
            final DatagramPacket respond = MsgUtils.makeMsgToSend(msg.getSocketAddress(), 0);
            MsgUtils.setMsgText(respond, "Hello, " + msgText);
            socket.send(respond);
        } catch (IOException e) {
            if (!closed) {
                System.err.println("Error occurred during processing datagram: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        socket.close();
        listener.shutdownNow();
        workers.shutdownNow();
        try {
            workers.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
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
