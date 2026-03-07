package com.example.vt.web;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.github.nhtuan10.modular.api.classloader.ModularClassLoader;
import org.springframework.context.ApplicationContext;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;

public class DynamicCompilerExample {

    public static String execGroovyCode(String sourceCode, ApplicationContext applicationContext) {
        Binding binding = new Binding();
        binding.setVariable("applicationContext", applicationContext);
        GroovyShell shellWithBinding = new GroovyShell(binding);
        Object message = shellWithBinding.evaluate(sourceCode);
        return Objects.toString(message);
    }

    public static String execJavaCode(final String className, final String sourceCode) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {
//        String sourceCode =
////            "package com.example.vt.web;\n" +
//                "import org.apache.commons.lang3.StringUtils;\n" +
//                        "public class " + CLASSNAME + "{\n" +
//                        "    public void exec() {\n" +
//                        "        System.out.println(\"Hello, dynamic compilation!\");\n" +
//                        "StringUtils.isBlank(\"abc\");\n" +
//                        "    }\n" +
//                        "}";

        // 2. Get the Java compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("JDK not found. Requires a JDK, not just a JRE.");
            return null;
        }
        CustomFileManager fileManager = new CustomFileManager(compiler.getStandardFileManager(null, null, null));

        // 3. Create a DiagnosticCollector to capture errors
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // 4. Wrap the source code string in a SimpleJavaFileObject
        JavaFileObject file = new JavaSourceFromString(className, sourceCode);
        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        final String classPath;
        if (currentClassLoader instanceof ModularClassLoader) {
            classPath = ((ModularClassLoader) currentClassLoader).getClassPath();
        } else if (currentClassLoader.getParent() instanceof ModularClassLoader) {
            classPath = ((ModularClassLoader) currentClassLoader.getParent()).getClassPath();
        } else {
            classPath = System.getProperty("java.class.path");
        }

        // 5. Create a compilation task
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, // standard output for errors (use null for default System.err)
                fileManager, // file manager (use null for standard)
                diagnostics,
                Arrays.asList("-g", "-classpath", classPath), // options (e.g., classpath settings)
                null, // classes
                compilationUnits);

        // 6. Perform the compilation
        boolean success = task.call();
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            sb.append(diagnostic.getKind() + " ");
            sb.append(diagnostic.getCode() + "\n");
            sb.append("At " + diagnostic.getPosition() + "; from ");
            sb.append(diagnostic.getStartPosition() + " to ");
            sb.append(diagnostic.getEndPosition() + "\n");
            sb.append(diagnostic.getSource() + "\n");
            sb.append(diagnostic.getMessage(null) + "\n");
        }
        // 7. Check the result and print diagnostics
        if (success) {
            System.out.println("Compilation successful!");
            // Further steps would involve a custom ClassLoader to load and use the class

            // 2. Use a URLClassLoader with the current classpath
//        URLClassLoader classLoader = new URLClassLoader(
//                new URL[]{},
//                Thread.currentThread().getContextClassLoader() // Share current dependencies
//        );
            byte[] bytecode = fileManager.outputObject.outputStream.toByteArray();
            ClassLoader classLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
                Class c = defineClass(className, bytecode, 0, bytecode.length);
            };

// 3. Load and execute
            Class<?> clazz = classLoader.loadClass(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            Method method = clazz.getMethod("exec");
            String resp = Objects.toString(method.invoke(instance));
            return "Compilation successful!\n" + sb + "\nResult: " + resp;
        } else {
            System.out.println("Compilation failed:");
            return "Compilation failed: " + sb;
        }

    }

    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        // 1. Define the source code as a string
        final String CLASSNAME = "GeneratedClass";
        String sourceCode =
//            "package com.example.vt.web;\n" +
                "import org.apache.commons.lang3.StringUtils;\n" +
                        "public class " + CLASSNAME + "{\n" +
                        "    public String exec() {\n" +
                        "        System.out.println(\"Hello, dynamic compilation!\");\n" +
                        "StringUtils.isBlank(\"abc\");\n" +
                        "    }\n" +
                        "}";
        execJavaCode(CLASSNAME, sourceCode);
    }

    // Helper class to represent a source code string as a JavaFileObject
    public static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;
        private ByteArrayOutputStream outputStream;

        JavaSourceFromString(URI uri, Kind kind) {
            super(uri, kind);
            this.code = null;
        }

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }


        @Override
        public OutputStream openOutputStream() throws IOException {
            return outputStream = new ByteArrayOutputStream();
        }
    }

    private static class CustomFileManager
            extends ForwardingJavaFileManager<StandardJavaFileManager> {
        JavaSourceFromString outputObject;

        CustomFileManager(StandardJavaFileManager target) {
            super(target);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            final JavaSourceFromString file;
            try {
                file = new JavaSourceFromString(new URI(null, null, className, null), JavaFileObject.Kind.CLASS);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            outputObject = file;
            return file;
        }
    }
}



