package ru.ifmo.rain.dovzhik.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class Implementor implements JarImpler {
    private final static String DEFAULT_OBJECT = " null";
    private final static String DEFAULT_PRIMITIVE = " 0";
    private final static String DEFAULT_VOID = "";
    private final static String DEFAULT_BOOLEAN = " false";
    private final static String TAB = "    ";
    private final static String SPACE = " ";
    private final static String COMMA = ",";
    private final static String EOLN = System.lineSeparator();
    private final static String JAVA = ".java";
    private final static String CLASS = ".class";

    private static class MethodWrapper {
        private final Method inner;
        private final static int BASE = 37;
        private final static int MOD = (int) (1e9 + 7);

        MethodWrapper(Method other) {
            inner = other;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof MethodWrapper) {
                MethodWrapper other = (MethodWrapper) obj;
                return Arrays.equals(inner.getParameterTypes(), other.inner.getParameterTypes())
                        && inner.getReturnType().equals(other.inner.getReturnType())
                        && inner.getName().equals(other.inner.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return ((Arrays.hashCode(inner.getParameterTypes())
                    + BASE * inner.getReturnType().hashCode()) % MOD
                    + inner.getName().hashCode() * BASE * BASE) % MOD;
        }

        Method getInner() {
            return inner;
        }
    }

    private static void checkForNull(Object... args) throws ImplerException {
        for (Object arg : args) {
            if (arg == null) {
                throw new ImplerException("Not-null arguments expected");
            }
        }
    }

    private static StringBuilder getTabs(int cnt) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            res.append(TAB);
        }
        return res;
    }

    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private static String getDefaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return DEFAULT_BOOLEAN;
        } else if (token.equals(void.class)) {
            return DEFAULT_VOID;
        } else if (token.isPrimitive()) {
            return DEFAULT_PRIMITIVE;
        }
        return DEFAULT_OBJECT;
    }

    private static StringBuilder getPackage(Class<?> token) {
        StringBuilder res = new StringBuilder();
        if (!token.getPackage().getName().equals("")) {
            res.append("package" + SPACE).append(token.getPackage().getName()).append(";").append(EOLN);
        }
        res.append(EOLN);
        return res;
    }

    private static void createDirectories(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkForNull(token, root);
        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Incorrect class token");
        }
        root = getFilePath(root, token, JAVA);
        createDirectories(root);
        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            try {
                writer.write(getClassHead(token));
                if (!token.isInterface()) {
                    implementConstructors(token, writer);
                }
                implementAbstractMethods(token, writer);
                writer.write("}" + EOLN);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to output file", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Unable to create output file", e);
        }
    }

    private Path getAncestor(Path file) {
        return file.getParent() != null ? file.getParent() : Paths.get(System.getProperty("user.dir"));
    }

    private static void clean(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Path getFilePath(Path path, Class<?> token, String end) {
        return path.resolve(token.getPackage().getName().replace('.', File.separatorChar))
                .resolve(getClassName(token) + end);
    }

    @Override
    public void implementJar(Class<?> token, Path outputFile) throws ImplerException {
        checkForNull(token, outputFile);
        createDirectories(outputFile);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(getAncestor(outputFile), "temp");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temp directory", e);
        }
        implement(token, tempDir);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = new String[3];
        args[0] = "-cp";
        args[1] = tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path");
        args[2] = getFilePath(tempDir, token, JAVA).toString();
        compiler.run(null, null, null, args);
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Lev Dovzhik");
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(outputFile), manifest)) {
            writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
            Files.copy(getFilePath(tempDir, token, CLASS), writer);
        } catch (IOException e) {
            throw new ImplerException("Unable to write JAR file", e);
        }
        try {
            clean(tempDir);
        } catch (IOException e) {
            throw new ImplerException("Unable to delete temp directory", e);
        }
    }

    private static String getClassHead(Class<?> token) {
        return getPackage(token) + "public class " + getClassName(token) + SPACE +
                (token.isInterface() ? "implements" : "extends") + SPACE +
                token.getSimpleName() + SPACE + "{" + EOLN;
    }

    private static String getParam(Parameter param, boolean typeNeeded) {
        return (typeNeeded ? param.getType().getCanonicalName() + SPACE : "") + param.getName();
    }

    private static String getParams(Executable exec, boolean typedNeeded) {
        return Arrays.stream(exec.getParameters())
                .map(param -> getParam(param, typedNeeded))
                .collect(Collectors.joining(COMMA + SPACE, "(", ")"));
    }

    private static StringBuilder getExceptions(Executable exec) {
        StringBuilder res = new StringBuilder();
        Class<?>[] exceptions = exec.getExceptionTypes();
        if (exceptions.length > 0) {
            res.append(SPACE + "throws" + SPACE);
        }
        res.append(Arrays.stream(exceptions)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(COMMA + SPACE))
        );
        return res;
    }

    private static String getReturnTypeAndName(Class<?> token, Executable exec) {
        if (exec instanceof Method) {
            Method tmp = (Method) exec;
            return tmp.getReturnType().getCanonicalName() + SPACE + tmp.getName();
        } else {
            return getClassName(token);
        }
    }

    private static String getBody(Executable exec) {
        if (exec instanceof Method) {
            return "return" + getDefaultValue(((Method) exec).getReturnType());
        } else {
            return "super" + getParams(exec, false);
        }
    }

    private static StringBuilder getExecutable(Class<?> token, Executable exec) {
        StringBuilder res = new StringBuilder(getTabs(1));
        final int mods = exec.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        res.append(Modifier.toString(mods))
                .append(mods > 0 ? SPACE : "")
                .append(getReturnTypeAndName(token, exec))
                .append(getParams(exec, true))
                .append(getExceptions(exec))
                .append(SPACE)
                .append("{")
                .append(EOLN)
                .append(getTabs(2))
                .append(getBody(exec))
                .append(";")
                .append(EOLN)
                .append(getTabs(1))
                .append("}")
                .append(EOLN);
        return res;
    }

    private static void getAbstractMethods(Method[] methods, Set<MethodWrapper> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrapper::new).collect(Collectors.toCollection(() -> storage));
    }

    private static void implementAbstractMethods(Class<?> token, Writer writer) throws IOException {
        HashSet<MethodWrapper> methods = new HashSet<>();
        getAbstractMethods(token.getMethods(), methods);
        while (token != null) {
            getAbstractMethods(token.getDeclaredMethods(), methods);
            token = token.getSuperclass();
        }
        for (MethodWrapper method : methods) {
            writer.write(getExecutable(null, method.getInner()).toString());
        }
    }

    private static void implementConstructors(Class<?> token, Writer writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("No non-private constructors in class");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(getExecutable(token, constructor).toString());
        }
    }

    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.out.println("Two or three arguments expected");
            return;
        }
        for (String arg : args) {
            if (arg == null) {
                System.out.println("All arguments must be non-null");
            }
        }
        Implementor implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (InvalidPathException e) {
            System.out.println("Incorrect path to root: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Incorrect class name: " + e.getMessage());
        } catch (ImplerException e) {
            System.out.println("An error occurred during implementation: " + e.getMessage());
        }
    }
}
