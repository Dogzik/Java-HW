package info.kgeorgiy.java.advanced.implementor.examples.basic;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface InterfaceWithDefaultMethod {
    int hello();
    default void defaultMethod() {
        System.out.println("defaultMethod");
    }
}
