package ru.ifmo.rain.dovzhik.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Implementor implements Impler {

    private final static String DEFAULT_OBJECT = " null";
    private final static String DEFAULT_PRIMITIVE = " 0";
    private final static String DEFAULT_VOID = "";
    private final static String DEFAULT_BOOLEAN = " false";
    private final static String TAB = "    ";
    private final static String SPACE = " ";
    private final static String COMMA = ",";
    private final static String EOLN = System.lineSeparator();

    private class MethodWrapper {
        final private Method inner;
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

    private StringBuilder getTabs(int cnt) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            res.append(TAB);
        }
        return res;
    }

    private String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private String getDefaultValue(Class<?> token) {
        if (token.equals(boolean.class)) {
            return DEFAULT_BOOLEAN;
        } else if (token.equals(void.class)) {
            return DEFAULT_VOID;
        } else if (token.isPrimitive()) {
            return DEFAULT_PRIMITIVE;
        }
        return DEFAULT_OBJECT;
    }

    private StringBuilder getPackage(Class<?> token) {
        StringBuilder res = new StringBuilder();
        if (!token.getPackageName().equals("")) {
            res.append("package" + SPACE).append(token.getPackageName()).append(";").append(EOLN);
        }
        res.append(EOLN);
        return res;
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (root == null || token == null) {
            throw new ImplerException("Not-null arguments expected");
        }
        if (token.isPrimitive() || token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Incorrect class token");
        }
        root = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(getClassName(token) + ".java");
        if (root.getParent() != null) {
            try {
                Files.createDirectories(root.getParent());
            } catch (IOException e) {
                throw new ImplerException("Unable to create directories for output file", e);
            }
        }

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

    private String getClassHead(Class<?> token) {
        return getPackage(token) + "public class " + getClassName(token) + SPACE +
                (token.isInterface() ? "implements" : "extends") + SPACE +
                token.getSimpleName() + SPACE + "{" + EOLN;
    }

    private String getParam(Parameter param, boolean typeNeeded) {
        return (typeNeeded ? param.getType().getCanonicalName() + SPACE : "") + param.getName();
    }

    private String getParams(Executable exec, boolean typedNeeded) {
        return Arrays.stream(exec.getParameters())
                .map(param -> getParam(param, typedNeeded))
                .collect(Collectors.joining(COMMA + SPACE, "(", ")"));
    }

    private StringBuilder getExceptions(Executable exec) {
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

    private String getReturnTypeAndName(Class<?> token, Executable exec) {
        if (exec instanceof Method) {
            Method tmp = (Method) exec;
            return tmp.getReturnType().getCanonicalName() + SPACE + tmp.getName();
        } else {
            return getClassName(token);
        }
    }

    private String getBody(Executable exec) {
        if (exec instanceof Method) {
            return "return" + getDefaultValue(((Method) exec).getReturnType());
        } else {
            return "super" + getParams(exec, false);
        }
    }

    private StringBuilder getExecutable(Class<?> token, Executable exec) {
        StringBuilder res = new StringBuilder(getTabs(1));
        final int mods = exec.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT;
        res.append(Modifier.toString(mods))
                .append(mods > 0 ? SPACE : "")
                .append(getReturnTypeAndName(token, exec))
                .append(getParams(exec, true))
                .append(getExceptions(exec)).append(SPACE)
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

    private void getAbstractMethods(Method[] methods, Set<MethodWrapper> storage) {
        Arrays.stream(methods)
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(MethodWrapper::new).collect(Collectors.toCollection(() -> storage));
    }

    private void implementAbstractMethods(Class<?> token, BufferedWriter writer) throws IOException {
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

    private void implementConstructors(Class<?> token, BufferedWriter writer) throws IOException, ImplerException {
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        constructors = Arrays.stream(constructors)
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);
        if (constructors.length == 0) {
            throw new ImplerException("No non-private constructors in class");
        }
        for (Constructor<?> constructor : constructors) {
            writer.write(getExecutable(token, constructor).toString());
        }
    }
}
