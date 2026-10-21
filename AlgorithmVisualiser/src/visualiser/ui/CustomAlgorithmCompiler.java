package visualiser.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import visualiser.algorithms.SortingAlgorithm;

final class CustomAlgorithmCompiler {
    private static final String PREFIX = """
            package visualiser.algorithms;
            import visualiser.model.ArrayModel;
            public final class UserSort_%s extends SortingAlgorithm {
                public UserSort_%s() {
                    super("%s", "User-defined", "User-defined", "User-defined",
                          "User-defined", "User-provided sorting method.");
                }
                @Override public void sort(ArrayModel m) {
            """;
    private static final String SUFFIX = """
                }
            }
            """;

    private CustomAlgorithmCompiler() { }

    static SortingAlgorithm compile(String body) throws IOException, CompilationException {
        return compile("Custom Java Sort", body);
    }

    static SortingAlgorithm compile(String name, String body) throws IOException, CompilationException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new CompilationException("A JDK is required to compile custom code. "
                    + "Install JDK 17 or newer and run the application with it.");
        }

        String className = UUID.randomUUID().toString().replace("-", "");
        String generatedClass = "UserSort_" + className;
        String safeName = (name == null || name.isBlank())
                ? "Custom Java Sort"
                : name.replace("\\", "\\\\").replace("\"", "\\\"").trim();
        String prefix = PREFIX.formatted(className, className, safeName);
        String source = prefix + body + System.lineSeparator() + SUFFIX;
        int firstBodyLine = (int) prefix.lines().count() + 1;
        Path outputDirectory = Files.createTempDirectory("algorithm-visualiser-");
        Path sourceFile = outputDirectory.resolve(generatedClass + ".java");

        try {
            Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            SortingAlgorithm result;
            try (StandardJavaFileManager fileManager =
                         compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> units =
                        fileManager.getJavaFileObjects(sourceFile.toFile());
                String classPath = System.getProperty("java.class.path");
                boolean compiled = Boolean.TRUE.equals(compiler.getTask(null, fileManager, diagnostics,
                        java.util.List.of("-proc:none", "-classpath", classPath,
                                "-d", outputDirectory.toString()), null, units).call());
                if (!compiled) {
                    throw new CompilationException(formatDiagnostics(diagnostics, firstBodyLine));
                }
            }

            URL location = outputDirectory.toUri().toURL();
            try (URLClassLoader loader = new URLClassLoader(
                    new URL[]{location}, SortingAlgorithm.class.getClassLoader())) {
                Class<?> generated = Class.forName("visualiser.algorithms." + generatedClass, true, loader);
                result = (SortingAlgorithm) generated.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                     | IllegalAccessException | InvocationTargetException | ClassCastException e) {
                throw new CompilationException("Could not load the compiled sorting method: " + e.getMessage());
            }
            return result;
        } finally {
            deleteTree(outputDirectory);
        }
    }

    private static String formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics,
                                           int firstBodyLine) {
        StringBuilder message = new StringBuilder("Please fix the Java errors below:\n");
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                long line = Math.max(1, diagnostic.getLineNumber() - firstBodyLine + 1);
                message.append("Line ").append(line).append(": ")
                        .append(diagnostic.getMessage(java.util.Locale.ROOT)).append('\n');
            }
        }
        return message.toString().stripTrailing();
    }

    private static void deleteTree(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult postVisitDirectory(Path dir, IOException error)
                    throws IOException {
                if (error != null) throw error;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    static final class CompilationException extends Exception {
        CompilationException(String message) { super(message); }
    }
}
