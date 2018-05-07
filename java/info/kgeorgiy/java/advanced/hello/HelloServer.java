package info.kgeorgiy.java.advanced.hello;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface HelloServer extends AutoCloseable {
    void start(int port, int threads);

    @Override
    void close();
}
