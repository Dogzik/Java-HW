package info.kgeorgiy.java.advanced.implementor;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InterfaceJarImplementorTest extends InterfaceImplementorTest {
    @Test
    @Override
    public void test01_constructor() {
        assertConstructor(Impler.class, JarImpler.class);
    }

    @Override
    protected void implement(final Path root, final Impler implementor, final Class<?> clazz) throws ImplerException {
        super.implement(root, implementor, clazz);
        implementJar(root, implementor, clazz);
    }

    static void implementJar(final Path root, final Impler implementor, final Class<?> clazz) throws ImplerException {
        final Path jarFile = root.resolve(clazz.getName() + ".jar");
        ((JarImpler) implementor).implementJar(clazz, jarFile);
        try (final URLClassLoader classLoader = getClassLoader(jarFile)) {
            check(classLoader, clazz);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
