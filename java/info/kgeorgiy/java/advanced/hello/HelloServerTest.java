package info.kgeorgiy.java.advanced.hello;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

import static info.kgeorgiy.java.advanced.hello.Util.response;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HelloServerTest extends BaseTest {
    private static int port = 8880;
    public static final String REQUEST = HelloServerTest.class.getName();
    int index = 0;

    @Test
    public void test01_singleRequest() throws IOException {
        test(1, 1, port -> socket -> checkResponse(port, socket));
    }

    @Test
    public void test02_multipleClients() throws IOException {
        test(1, 10, port -> socket -> checkResponse(port, socket));
    }

    @Test
    public void test03_multipleRequests() throws IOException {
        test(1, 1, port -> socket -> {
            for (int i = 0; i < 10; i++) {
                checkResponse(port, socket);
            }
        });
    }

    @Test
    public void test04_parallelRequests() throws IOException {
        test(1, 1, port -> socket -> checkParallel(port, socket, 100));
    }

    private void checkParallel(final int port, final DatagramSocket socket, final int requests) throws IOException {
        final Set<String> responses = new HashSet<>();
        for (int i = 0; i < requests; i++) {
            final String request = REQUEST + i;
            responses.add(response(request));
            send(port, socket, request);
        }
        for (int i = 0; i < requests; i++) {
            final String response = Util.receive(socket);
            Assert.assertTrue("Unexpected response " + response, responses.remove(response));
        }
    }

    @Test
    public void test05_parallelClients() throws IOException {
        test(1, 10, port -> socket -> parallel(1, () -> checkResponse(port, socket)));
    }

    @Test
    public void test06_dos() throws IOException {
        test(1, 10, port -> socket -> parallel(10, () -> {
            for (int i = 0; i < 10000; i++) {
                send(port, socket, REQUEST + i);
            }
        }));
    }

    @Test
    public void test07_noDoS() throws IOException {
        test(1, 10, port -> socket -> {
            for (int i = 0; i < 100; i++) {
                checkParallel(port, socket, 50);
            }
        });
    }

    private void send(final int port, final DatagramSocket socket, final String request) throws IOException {
        Util.send(socket, request, new InetSocketAddress("localhost", port));
    }

    public void test(final int workers, final int sockets, final IntFunction<ConsumerCommand<DatagramSocket, IOException>> command) throws IOException {
        try (final HelloServer server = createCUT()) {
            final int port = HelloServerTest.port++;
            server.start(port, workers);
            for (int i = 0; i < sockets; i++) {
                try (final DatagramSocket socket = new DatagramSocket()) {
                    command.apply(port).run(socket);
                }
            }
        }
    }

    private void checkResponse(final int port, final DatagramSocket socket) throws IOException {
        final String request = REQUEST + index++;
        final String response = Util.request(request, socket, new InetSocketAddress("localhost", port));
        Assert.assertEquals("Invalid response", response(request), response);
    }
}
