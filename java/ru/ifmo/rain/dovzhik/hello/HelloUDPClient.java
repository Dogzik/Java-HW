package ru.ifmo.rain.dovzhik.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetAddress add;
        try {
            add = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.println("Unable to find host :" + host);
            return;
        }
        final SocketAddress dst = new InetSocketAddress(add, port);
        final ExecutorService workers = Executors.newFixedThreadPool(threads);
        IntStream.range(0, threads)
                .forEach(id -> workers.submit(() ->sendAndReceive(dst, prefix, requests, id)));
        workers.shutdown();
        try {
            workers.awaitTermination(threads * requests, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) {
        }
    }

    private static void sendAndReceive(final SocketAddress dst, final String prefix, final int cnt, final int id) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(400);
            final DatagramPacket respond = MsgUtils.makeMsgToReceive(socket.getReceiveBufferSize());
            final DatagramPacket request = MsgUtils.makeMsgToSend(dst, 0);
            for (int num = 0; num < cnt; ++num) {
                final String requestText = makeRequestText(prefix, id, num);
                while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                    try {
                        MsgUtils.setMsgText(request, requestText);
                        socket.send(request);
                        System.out.println("\nRequest sent:\n" + requestText);
                        socket.receive(respond);
                        final String respondText = MsgUtils.getMsgText(respond);
                        if (check(requestText, respondText)) {
                            System.out.println("\nRespond received:\n" + respondText);
                            break;
                        }
                    } catch (IOException e) {
                        System.err.println("Error occurred during processing request: " + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Unable to create socket to server: " + dst.toString());
        }
    }

    private static boolean check(final String request, final String response) {
        return  response.length() != request.length()
                && (response.endsWith(request) || response.contains(request + " "));
        //return response.matches(".*" + Pattern.quote(request) + "(|\\p{javaWhitespace}.*)");
    }

    private static String makeRequestText(final String prefix, final int thread, final int num) {
        return prefix + thread + "_" + num;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("5 arguments expected");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("Non-null arguments expected");
                return;
            }
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException e) {
            System.err.println("Correct integer arguments expected: " + e.getMessage());
        }
    }
}
