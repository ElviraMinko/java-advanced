package info.kgeorgiy.ja.minko.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Implementor implements Impler {
    private static final String END_LINE = System.lineSeparator();
    private static final String PACKAGE = "package";
    private static final String PUBLIC_CLASS = "public class";
    private static final String IMPL = "Impl";
    private static final String IMPL_JAVA = "Impl.java";
    private static final String IMPLEMENTS = "implements";

    private String getClassCode(Class<?> token) {
        String simpleName = token.getSimpleName();
        String canonicalName = token.getCanonicalName();
        String packageName = token.getPackageName();
        String packageNameString = String.join(" ", PACKAGE, packageName, ";", END_LINE);
        String definitionClass = String.join(" ", PUBLIC_CLASS, simpleName +
                IMPL, IMPLEMENTS, canonicalName, "{", END_LINE);
        Method[] methods = token.getMethods();
        StringBuilder methodsString = new StringBuilder();
        for (Method method : methods) {
            methodsString.append(createMethodString(method)).append(END_LINE);
        }
        return String.format("%s %s %s }", packageNameString, definitionClass, methodsString);
    }

    private String createMethodString(Method method) {
        Class<?> returnType = method.getReturnType();
        return String.format("public %s %s(%s) { return %s;}",
                returnType.getCanonicalName(), method.getName(),
                getParametersToString(method), getReturnDefaultValue(returnType));
    }

    private String getReturnDefaultValue(Class<?> token) {
        if (token == void.class) {
            return "";
        } else if (token.isPrimitive()) {
            if (token == boolean.class) {
                return "false";
            } else {
                return "0";
            }
        } else {
            return "null";
        }
    }

    private String getParametersToString(Method method) {
        Parameter[] parameters = method.getParameters();
        return Arrays.stream(parameters).
                map(parameter -> parameter.getType().getCanonicalName()
                        + " " + parameter.getName()).collect(Collectors.joining(", "));
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if (token == void.class || token.isPrimitive() || token.isArray() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't implements from incorrect type");
        }
        Path path = root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(token.getSimpleName() + IMPL_JAVA);
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can't create parent directory");
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(getClassCode(token));
        } catch (IOException e) {
            System.err.println("Can't write in file");
        }

    }


    public static void main(String[] args) throws ImplerException {
        if (args == null || args[0] == null || args[1] == null) {
            return;
        }
        Implementor implementor = new Implementor();
        try {
            Class<?> token = Class.forName(args[0]);
            Path root = Path.of(args[1]);
            implementor.implement(token, root);
        } catch (ClassNotFoundException e) {
            throw new ImplerException("Can't find interface");
        } catch (InvalidPathException e) {
            throw new ImplerException("Incorrect path string");
        }
    }
}

