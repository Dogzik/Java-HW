package info.kgeorgiy.java.advanced.hello;

import info.kgeorgiy.java.advanced.base.BaseTester;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) {
        Util.i18n = args.length > 0 && args[0].contains("i18n");
        new Tester()
                .add("server", HelloServerTest.class)
                .add("client", HelloClientTest.class)
                .add("server-i18n", HelloServerTest.class)
                .add("client-i18n", HelloClientTest.class)
                .run(args);
    }
}
