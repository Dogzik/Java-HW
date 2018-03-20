package ru.ifmo.rain.dovzhik.implementor;

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

/**
 * Implementation class for {@link JarImpler} interface
 */
public class Implementor implements JarImpler {
    /**
     * Intended for generated classes.
     */
    private final static String TAB = "    ";
    /**
     * Space for generated classes.
     */
    private final static String SPACE = " ";
    /**
     * Comma for generated classes
     */
    private final static String COMMA = ",";
    /**
     * Line separator for generated classes
     */
    private final static String EOLN = System.lineSeparator();
    /**
     * Filename extension for source java files
     */
    private final static String JAVA = ".java";
    /**
     * Filename extension for compiled java files
     */
    private final static String CLASS = ".class";
    /**
     * Static instance of {@link Cleaner} used inside {@link #clean(Path)}
     */
    private final static Cleaner DELETER = new Cleaner();

    /**
     * Static class used for correct representing {@link Method}
     */
    private static class MethodWrapper {
        /**
         * Wrapped instance of {@link Method}
         */
        private final Method inner;
        /**
         * Base used for calculating hashcode
         */
        private final static int BASE = 37;
        /**
         * Module used for calculating hashcode
         */
        private final static int MOD = (int) (1e9 + 7);

        /**
         * Constructs a wrapper for specified instance of {@link Method}.
         *
         * @param other instance if {@link Method} to be wrapped
         */
        MethodWrapper(Method other) {
            inner = other;
        }

        /**
         * Compares the specified object with this wrapper for equality. Wrappers are equal, if their wrapped
         * methods have equal name, return type and parameters' types.
         *
         * @param obj the object to be compared for equality with this wrapper
         * @return true if specified object is equal to this wrapper
         */
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

        /**
         * Calculates hashcode for this wrapper via polynomial hashing
         * using hashes of name, return type and parameters' types of its {@link #inner}
         *
         * @return hashcode for this wrapper
         */
        @Override
        public int hashCode() {
            return ((Arrays.hashCode(inner.getParameterTypes())
                    + BASE * inner.getReturnType().hashCode()) % MOD
                    + inner.getName().hashCode() * BASE * BASE) % MOD;
        }

        /**
         * Getter for {@link #inner}.
         * @return wrapped instance of {@link Method}
         */
        Method getInner() {
            return inner;
        }
    }

    /**
     * Static class used for recursive deleting of folders
     */
    private static class Cleaner extends SimpleFileVisitor<Path> {
        /**
         * Deletes file represented by <tt>file</tt>
         *
         * @param file current file in fileTree
         * @param attrs attributes of file
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if error occurred during deleting of file
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Deletes directory represented by <tt>dir</tt>
         *
         * @param dir current visited directory in fileTree
         * @param exc <tt>null</tt> if the iteration of the directory completes without an error;
         *           otherwise the I/O exception that caused the iteration of the directory to complete prematurely
         * @return {@link FileVisitResult#CONTINUE}
         * @throws IOException if error occurred during deleting of directory
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Checks if any of given arguments is <tt>null</tt>
     * @param args list of arguments
     * @throws ImplerException if any arguments is <tt>null</tt>
     */
    private static void checkForNull(Object... args) throws ImplerException {
        for (Object arg : args) {
            if (arg == null) {
                throw new ImplerException("Not-null arguments expected");
            }
        }
    }

    /**
     * Returns tabs, whose amount if specified by <tt>cnt</tt>
     * @param cnt number of tabs to return
     * @return {@link StringBuilder} consisting of needed amount of tabs
     */
    private static StringBuilder getTabs(int cnt) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            res.append(TAB);
        }
        return res;
    }

    /**
     * Adds "Impl" suffix to simple name of given class
     * @param token class to get name
     * @return {@link String} with specified class name
     */
    private static String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Gets default value of given class
     * @param token class to get default value
     * @return {@link String} representing value
     */
    private static String getDefaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return " false";
        } else if (token.equals(void.class)) {
            return "";
        } else if (token.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    /**
     * Gets package of given file. Package is empty, if class is situated in default package
     * @param token class to get package
     * @return {@link String} representing package
     */
    private static StringBuilder getPackage(Class<?> token) {
        StringBuilder res = new StringBuilder();
        if (!token.getPackage().getName().equals("")) {
            res.append("package" + SPACE).append(token.getPackage().getName()).append(";").append(EOLN);
        }
        res.append(EOLN);
        return res;
    }

    /**
     * Creates parent directory for file represented by <tt>file</tt>
     *
     * @param path file to create parent directory
     * @throws ImplerException if error occurred during creation
     */
    private static void createDirectories(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }
    }

    /**
     * @throws ImplerException if the given class cannot be generated for one of such reasons:
     *  <ul>
     *  <li> Some arguments are <tt>null</tt></li>
     *  <li> Given <tt>class</tt> is primitive or array. </li>
     *  <li> Given <tt>class</tt> is final class or {@link Enum}. </li>
     *  <li> The process is not allowed to create files or directories. </li>
     *  <li> <tt>class</tt> isn't an interface and contains only private constructors. </li>
     *  <li> The problems with I/O occurred during implementation. </li>
     *  </ul>
     */
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


    /**
     * Recursively deletes directory represented by <tt>path</tt>
     *
     * @param path directory to be recursively deleted
     * @throws IOException if error occurred during deleting
     */
    private static void clean(Path path) throws IOException {
        Files.walkFileTree(path, DELETER);
    }

    /**
     * Return path to file, containing implementation of given class, with specific file extension
     * located in directory represented by <tt>path</tt>
     *
     * @param path path to parent directory of class
     * @param token class to get name from
     * @param end file extension
     * @return {@link Path} representing path to certain file
     */
    private static Path getFilePath(Path path, Class<?> token, String end) {
        return path.resolve(token.getPackage().getName().replace('.', File.separatorChar))
                .resolve(getClassName(token) + end);
    }

    /**
     * Produces <tt>.jar</tt> file implementing class or interface specified by provided <tt>token</tt>.
     * <p>
     * Generated class full name should be same as full name of the type token with <tt>Impl</tt> suffix
     * added.
     * <p>
     * During implementation creates temporary folder to store temporary <tt>.java</tt> and <tt>.class</tt> files.
     * If program fails to delete temporary folder, it informs user about it.
     * @throws ImplerException if the given class cannot be generated for one of such reasons:
     *  <ul>
     *  <li> Some arguments are <tt>null</tt></li>
     *  <li> Error occurs during implementation via {@link #implement(Class, Path)} </li>
     *  <li> The process is not allowed to create files or directories. </li>
     *  <li> {@link JavaCompiler} failed to compile implemented class </li>
     *  <li> The problems with I/O occurred during implementation. </li>
     *  </ul>
     */
    @Override
    public void implementJar(Class<?> token, Path outputFile) throws ImplerException {
        checkForNull(token, outputFile);
        createDirectories(outputFile);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(outputFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temp directory", e);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] args = new String[3];
        args[0] = "-cp";
        args[1] = tempDir.toString() + File.pathSeparator + System.getProperty("java.class.path");
        args[2] = getFilePath(tempDir, token, JAVA).toString();
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Lev Dovzhik");
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(outputFile), manifest)) {
            implement(token, tempDir);
            if (compiler.run(null, null, null, args) != 0) {
                throw new ImplerException("Unable to compile generated files");
            }
            try {
                writer.putNextEntry(new ZipEntry(token.getName().replace('.', '/') + "Impl.class"));
                Files.copy(getFilePath(tempDir, token, CLASS), writer);
            } catch (IOException e) {
                throw new ImplerException("Unable to write to JAR file", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Unable to create JAR file", e);
        } finally {
            try {
                clean(tempDir);
            } catch (IOException e) {
                System.out.println("Unable to delete temp directory: " + e.getMessage());
            }
        }
    }


    /**
     * Returns beginning declaration of the class, containing its package, name, base class or
     * implemented interface
     * @param token base class or implemented interface
     * @return {@link String} representing beginning of class declaration
     */
    private static String getClassHead(Class<?> token) {
        return getPackage(token) + "public class " + getClassName(token) + SPACE +
                (token.isInterface() ? "implements" : "extends") + SPACE +
                token.getSimpleName() + SPACE + "{" + EOLN;
    }


    /**
     * Returns name of {@link Parameter}, optionally adding its type
     * @param param parameter to get name from
     * @param typeNeeded flag responsible for adding parameter type
     * @return {@link String} representing parameter's name
     */
    private static String getParam(Parameter param, boolean typeNeeded) {
        return (typeNeeded ? param.getType().getCanonicalName() + SPACE : "") + param.getName();
    }

    /**
     * Returns list of parameters of {@link Executable}, surrounded by round parenthesis,
     * optionally adding their types
     *
     * @param exec {@link Executable}
     * @param typedNeeded flag responsible for adding parameter type
     * @return {@link String} representing list of parameters
     */
    private static String getParams(Executable exec, boolean typedNeeded) {
        return Arrays.stream(exec.getParameters())
                .map(param -> getParam(param, typedNeeded))
                .collect(Collectors.joining(COMMA + SPACE, "(", ")"));
    }

    /**
     * Returns list of exceptions, that given {@link Executable} may throw
     *
     * @param exec {@link Executable} to get exceptions from
     * @return {@link StringBuilder} representing list of exceptions
     */
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

    /**
     * If given {@link Executable} is instance of {@link Constructor} returns name of generated class,
     * otherwise returns return type and name of such {@link Method}
     *
     * @param exec given {@link Constructor} or {@link Method}
     * @return {@link String} representing such return type and name
     */
    private static String getReturnTypeAndName(Executable exec) {
        if (exec instanceof Method) {
            Method tmp = (Method) exec;
            return tmp.getReturnType().getCanonicalName() + SPACE + tmp.getName();
        } else {
            return getClassName(((Constructor<?>) exec).getDeclaringClass());
        }
    }

    /**
     * Calls constructor of super class if given {@link Executable} if instance of {@link Constructor},
     * otherwise return default value of return type of such {@link Method}
     * @param exec given {@link Constructor} or {@link Method}
     * @return {@link String} representing body, defined above
     */
    private static String getBody(Executable exec) {
        if (exec instanceof Method) {
            return "return" + getDefaultValue(((Method) exec).getReturnType());
        } else {
            return "super" + getParams(exec, false);
        }
    }

    /**
     * Returns fully constructed {@link Executable}, that calls constructor of super class if
     * <tt>exec</tt> is instance of {@link Constructor}, otherwise returns default value of return type
     * of such {@link Method}
     * @param exec given {@link Constructor} or {@link Method}
     * @return {@link StringBuilder} representing code of such {@link Executable}
     */
    private static StringBuilder getExecutable(Executable exec) {
        StringBuilder res = new StringBuilder(getTabs(1));
        final int mods = exec.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        res.append(Modifier.toString(mods))
                .append(mods > 0 ? SPACE : "")
                .append(getReturnTypeAndName(exec))
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

    /**
     * Filters given array of {@link Method}, leaving only declared as abstract and puts them
     * in given {@link Set}, after wrapping them to {@link MethodWrapper}
     * @param methods given array of {@link Method}
     * @param storage {@link Set} where to store methods
     */
    private static void getAbstractMethods(Method[] methods, Set<MethodWrapper> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrapper::new)
                .collect(Collectors.toCollection(() -> storage));
    }

    /**
     * Writes implementation of abstract methods of given {@link Class} via specified
     * {@link Writer}
     *
     * @param token given class to implement abstract methods
     * @param writer given {@link Writer}
     * @throws IOException if error occurs during writing
     */
    private static void implementAbstractMethods(Class<?> token, Writer writer) throws IOException {
        HashSet<MethodWrapper> methods = new HashSet<>();
        getAbstractMethods(token.getMethods(), methods);
        while (token != null) {
            getAbstractMethods(token.getDeclaredMethods(), methods);
            token = token.getSuperclass();
        }
        for (MethodWrapper method : methods) {
            writer.write(getExecutable(method.getInner()).toString());
        }
    }

    /**
     * Writes implementation of constructors of given {@link Class} via specified
     * {@link Writer}
     *
     * @param token given class to implement consructors
     * @param writer given {@link Writer}
     * @throws ImplerException if class doesn't have any non-private constructors
     * @throws IOException if error occurs during writing
     */
    private static void implementConstructors(Class<?> token, Writer writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("No non-private constructors in class");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(getExecutable(constructor).toString());
        }
    }

    /**
     * This function is used to choose which way of implementation to execute.
     * Runs {@link Implementor} in two possible ways:
     *  <ul>
     *  <li> 2 arguments: <tt>className rootPath</tt> - runs {@link #implement(Class, Path)} with given arguments</li>
     *  <li> 3 arguments: <tt>-jar className jarPath</tt> - runs {@link #implementJar(Class, Path)} with two second arguments</li>
     *  </ul>
     *  If arguments are incorrect or an error occurs during implementation returns message with information about error
     *
     * @param args arguments for running an application
     */
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
        JarImpler implementor = new Implementor();
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
