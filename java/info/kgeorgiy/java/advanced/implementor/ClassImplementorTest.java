package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.implementor.examples.full.ClassWithPackagePrivateConstructor;
import info.kgeorgiy.java.advanced.implementor.standard.basic.*;
import info.kgeorgiy.java.advanced.implementor.standard.full.*;
import org.junit.Test;

import javax.annotation.processing.Completions;
import java.io.IOException;
import java.util.Formatter;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ClassImplementorTest extends InterfaceImplementorTest {
    @Test
    public void test07_defaultConstructorClasses() throws IOException {
        test(false, BMPImageWriteParam.class, RelationNotFoundException.class);
    }

    @Test
    public void test08_noDefaultConstructorClasses() throws IOException {
        test(false, IIOException.class, ImmutableDescriptor.class, LdapReferralException.class, ClassLogger.class);
    }

    @Test
    public void test09_ambiguousConstructorClasses() throws IOException {
        test(false, IIOImage.class);
    }

    @Test
    public void test10_utilityClasses() throws IOException {
        test(true, Completions.class);
    }

    @Test
    public void test11_finalClasses() throws IOException {
        test(true, Integer.class, String.class);
    }

    @Test
    public void test12_standardNonClasses() throws IOException {
        test(true, void.class, String[].class, int[].class, String.class, boolean.class);
    }

    @Test
    public void test13_constructorThrows() throws IOException {
        test(false, FileCacheImageInputStream.class);
    }

    @Test
    public void test14_nonPublicAbstractMethod() throws IOException {
        test(false, RMIServerImpl.class, RMIIIOPServerImpl.class);
    }

    @Test
    public void test15_enum() throws IOException {
        test(true, Enum.class, Formatter.BigDecimalLayoutForm.class);
    }

    @Test
    public void test16_packagePrivateConstructor() throws IOException {
        test(false, ClassWithPackagePrivateConstructor.class);
    }
}
