package info.kgeorgiy.ja.minko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
/**
 * Implementor class
 *
 * Realizing {@code JarImpler} interface
 * @author Minko Elvira
 * */
public class Implementor implements JarImpler {
    /**
     * System-dependent line separator string
     * */
    private static final String END_LINE = System.lineSeparator();
    /**
     * The start substring of package string
     * */
    private static final String PACKAGE = "package";
    /**
     * The start substring of public class string
     * */
    private static final String PUBLIC_CLASS = "public class";
    /**
     * Implemented suffix
     * */
    private static final String IMPL = "Impl";
    /**
     * Implemented suffix and .java string
     * */
    private static final String IMPL_JAVA = "Impl.java";
    /**
     * Implements string
     * */
    private static final String IMPLEMENTS = "implements";

    /**
     * File visitor, which delete all files and directories in catalog
     *  <p>Modified Georgiy Korneev's code</p>
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Return string representation of default realization class implement {@code token}
     * <p> Generated class, realizing all methods by {@link Class#getMethods()} of {@code token}
     * @param token interface type token.
     * @return value in {@link String}
     *
     */
    private String getClassCode(Class<?> token) {

        String simpleName = token.getSimpleName();
        String canonicalName = token.getCanonicalName();
        String packageName = token.getPackageName();
        String packageNameString = buildPackageString(packageName);
        String definitionClass = String.join(" ", PUBLIC_CLASS, simpleName +
                IMPL, IMPLEMENTS, canonicalName, "{", END_LINE);
        Method[] methods = token.getMethods();
        StringBuilder methodsString = new StringBuilder();
        for (Method method : methods) {
            methodsString.append(createMethodString(method)).append(END_LINE);
        }
        return String.format("%s %s %s }", packageNameString, definitionClass, methodsString);
    }

    /**
     * Return string representation package
     * <p> Return empty string if package name was equals {@code ""}
     * @param packageName name of package.
     * @return value in {@link String}
     */
    private String buildPackageString(String packageName) {
        if (!packageName.equals("")) {
            return String.join(" ", PACKAGE, packageName, ";", END_LINE);
        }
        return "";
    }
    /**
     * Return string representation of default realization {@code method}
     * <p> Generated method always {@code public} and returns result of {@link #getReturnDefaultValue(Class)}
     * @param method realisable method
     * @return code of method in {@link String}
     *
     */
    private String createMethodString(Method method) {
        Class<?> returnType = method.getReturnType();
        return String.format("public %s %s(%s) { return %s;}",
                returnType.getCanonicalName(), method.getName(),
                getParametersToString(method), getReturnDefaultValue(returnType));
    }

    /**
     * Return string representation of default value of {@code token} type
     * <p>
     * {@code void} produce {@code ""}
     * <p>
     * constructable types produce {@code "null"}
     * <p>
     * {@code boolean} produce {@code "false"}
     * <p>
     * other primitive types produce zero
     * <p>
     * @param token interface type token.
     * @return value in {@link String}
     *
     */
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
    /**
     * Generate parameter's string to java method
     *
     * @param method containing parameters.
     * @return {@link String} with types and parameters names in correct order
     */
    private String getParametersToString(Method method) {
        Parameter[] parameters = method.getParameters();
        return Arrays.stream(parameters).
                map(parameter -> parameter.getType().getCanonicalName()
                        + " " + parameter.getName()).collect(Collectors.joining(", "));
    }

    /**
     * Generate class file implementing interface {@code token}
     * <p>
     * If token wasn't valid, then throw {@code ImplerException}
     *
     * @param token interface type token.
     * @param root  path to place where file will create.
     * @throws ImplerException if token not supported or get {@link IOException} in writing.
     */
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
                throw new ImplerException("Can't create parent directory", e);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(getClassCode(token));
        } catch (IOException e) {
            throw new ImplerException("Problems with writer", e);
        }

    }

    /**
     * Static entry-point
     *
     * <p>There should be two or three arguments. All arguments have to be defined (not null).
     * When got two arguments run {@link JarImpler#implement(Class, Path)}
     * When got three arguments and first equals {@code -jar} run {@link JarImpler#implementJar(Class, Path)}
     *
     *
     * @param args array with given arguments.
     * @throws ImplerException when incorrect args or it throws {@link #implementJar(Class, Path)} or {@link #implement(Class, Path)}
     */
    public static void main(String[] args) throws ImplerException {
        if (args == null) {
            throw new ImplerException("Expected arguments");
        }
        Implementor implementor = new Implementor();

        try {

            Class<?> token;
            if (args.length == 3) {
                if (!"-jar".equals(args[0]) || args[1] == null || args[2] == null) {
                    throw new ImplerException("Expected not null args");
                }
                token = Class.forName(args[1]);
                Path root = Path.of(args[2]);
                implementor.implementJar(token, root);

            }

            if (args.length == 2) {
                if (args[0] == null || args[1] == null) {
                    throw new ImplerException("Expected not null args");
                }
                token = Class.forName(args[0]);
                Path root = Path.of(args[1]);
                implementor.implement(token, root);
            }

        } catch (ClassNotFoundException e) {
            throw new ImplerException("Can't find interface");
        } catch (InvalidPathException e) {
            throw new ImplerException("Incorrect path string");
        } catch (ImplerException e) {
            System.err.print("Problems with implement");
        }
    }
    /**
     * Create .jar file, which contains compiled implemented class
     *
     * Method takes type token and path to results .jar file. File will be compiled by system java compiler.
     * If something went wrong, {@code ImplerException} will throw.
     *
     * @param token interface type token.
     *
     * @param jarFile path to resulting .jar file
     *
     * @throws ImplerException
     *         if something IOException occurred(in creating/removing temporary directory or in creating/writing .jar file
     *         if compilation was failed
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Path temporaryDirectory;
        try {
            temporaryDirectory = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Unable to create temporary directory");
        }
        try {
            implement(token, temporaryDirectory);
            String filePath = getFile(temporaryDirectory, token).toString();
            compileFiles(token, temporaryDirectory, filePath);

            Manifest manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Elvira Minko");
            try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                String className = getImplName(token).replace('.', '/') + ".class";
                jarOutputStream.putNextEntry(new ZipEntry(className));
                Files.copy(Path.of(temporaryDirectory.toString(), className), jarOutputStream);
            } catch (IOException e) {
                throw new ImplerException("Error in writing in jar file");
            }
        } finally {
            try {
                Files.walkFileTree(temporaryDirectory, DELETE_VISITOR);
            } catch (IOException e) {
                System.err.print("Can't delete temporary directories");
            }

        }
    }
    /**
     * Get name of class implemented by token with suffix
     *
     * Modified Georgiy Korneev's code
     *
     * @param token interface type token.
     *
     * @return {@code String} which is result name
     */
    private static String getImplName(final Class<?> token) {
        return token.getPackageName() + "." + token.getSimpleName() + "Impl";
    }

    /**
     * Get absolute path to implemented file in {@code root} directory
     *
     * Modified Georgiy Korneev's code
     *
     * @param clazz interface type token.
     * @param root path to temporary directory
     * @return the resulting {@code Path}
     */
    public static Path getFile(final Path root, final Class<?> clazz) {
        return root.resolve(getImplName(clazz).replace(".", File.separator) + ".java").toAbsolutePath();
    }

    /**
     * Compile class for with default params.
     *
     * Modified Georgiy Korneev's code
     *
     * @param token interface type token.
     * @param root path to temporary directory
     * @param file name of compiled file
     * @throws ImplerException when compilation failed
     *
    */

    public static void compileFiles(Class<?> token, final Path root, final String file) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String classPath;
        try {
            classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
        final String classpath = root + File.pathSeparator + classPath;
        final String[] args = new String[]{file, "-cp", classpath};
        final int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Compilation failed. Exit code " + exitCode);
        }
    }
}

