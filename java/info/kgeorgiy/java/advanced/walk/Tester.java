package info.kgeorgiy.java.advanced.walk;

import info.kgeorgiy.java.advanced.base.BaseTester;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) {
        new Tester()
                .add("Walk", WalkTest.class)
                .add("RecursiveWalk", RecursiveWalkTest.class)
                .run(args);
    }
}
