package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.implementor.standard.basic.IIOException;
import info.kgeorgiy.java.advanced.implementor.standard.basic.IIOImage;
import info.kgeorgiy.java.advanced.implementor.standard.basic.RMIServerImpl;
import info.kgeorgiy.java.advanced.implementor.standard.basic.RelationNotFoundException;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class BasicClassImplementorTest extends BasicInterfaceImplementorTest {
    @Test
    public void test07_defaultConstructorClasses() throws IOException {
        test(false, RelationNotFoundException.class);
    }

    @Test
    public void test08_noDefaultConstructorClasses() throws IOException {
        test(false, IIOException.class);
    }

    @Test
    public void test09_ambiguousConstructorClasses() throws IOException {
        test(false, IIOImage.class);
    }

    @Test
    public void test14_nonPublicAbstractMethod() throws IOException {
        test(false, RMIServerImpl.class);
    }

    @Test
    public void test15_enum() throws IOException {
        test(true, Enum.class);
    }
}
