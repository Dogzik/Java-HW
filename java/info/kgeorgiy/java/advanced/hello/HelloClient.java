package info.kgeorgiy.java.advanced.hello;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface HelloClient {
    void run(String host, int port, String prefix, int threads, int requests);
}
