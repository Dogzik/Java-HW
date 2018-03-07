package info.kgeorgiy.java.advanced.student;

import info.kgeorgiy.java.advanced.base.BaseTester;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) {
        new Tester()
                .add("StudentQuery", StudentQueryFullTest.class)
                .add("StudentGroupQuery", StudentGroupQueryFullTest.class)
                .run(args);
    }
}
