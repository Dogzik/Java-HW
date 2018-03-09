package info.kgeorgiy.java.advanced.implementor.examples.basic;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface InterfaceWithStaticMethod {
    int hello();
    static void staticMethod() {
        System.out.println("staticMethod");
    }
}
