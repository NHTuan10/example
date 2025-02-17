package com.example.vt.web.classloader;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Stream;


public class ModularClassLoader extends ClassLoader {
    private final static Logger LOGGER =
            LoggerFactory.getLogger(ModularClassLoader.class.getName());

    @Getter
    private Set<String> excludedClassPackages;

    @Getter
    private List<URL> classPathUrls;


    URLClassLoader urlClassLoader;

    public void setExcludedClassPackages(Set<String> excludedClassPackages) {
        this.excludedClassPackages = Collections.unmodifiableSet(excludedClassPackages);
    }

    public ModularClassLoader(List<URL> classPathUrls) {
        this();
        this.classPathUrls = Stream.concat(classPathUrls.stream(), this.classPathUrls.stream()).toList();;
    }

    public ModularClassLoader() {
        super();
        this.excludedClassPackages = Collections.unmodifiableSet(getDefaultExcludedPackages());
        this.classPathUrls= Collections.unmodifiableList (getJavaClassPath()) ;
    }
    // add set of string to classPathUrls property
//    public CustomClassLoader addClassPathUrls(List<URL> classPathUrls){
//        this.classPathUrls.addAll(classPathUrls);
//        return this;
//    }

    public URLClassLoader getUrlClassLoader(){
        return new URLClassLoader(this.classPathUrls.toArray(URL[]::new));
    }
//    @Override
//    protected Class findClass(String name) throws ClassNotFoundException {
//        try {
//            loadClassFromFile(name);
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
////        return defineClass(name, b, 0, b.length);
//    }

    protected Set<String> getDefaultExcludedPackages() {
//        return ModuleLayer.boot().modules().stream()
//                .map(Module::getName)
//                .collect(Collectors.toSet());
        return  new HashSet<>();
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        LOGGER.debug("Loading class: ", name);
        synchronized (getClassLoadingLock(name)) {
            // check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    try {
                        if (shouldLoadByPlatformClassLoader(name)) {
                            c = ClassLoader.getPlatformClassLoader().loadClass(name);
                        }
                    } catch (ClassNotFoundException e) {
                        // add logs
                    }
                    if (c == null) {
                        c = loadClassFromUrls(name);
                    }
                } catch (ClassNotFoundException | SecurityException | MalformedURLException e) {
                    // add logs
                }

                if (c == null) {
                    // If still not found, then invoke super.loadClass
                    c = super.loadClass(name, false);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    private boolean shouldLoadByPlatformClassLoader(String name) {
        return name.startsWith("java") || name.startsWith("jdk") ||
                excludedClassPackages.stream().anyMatch(name::startsWith);
    }

    private List<URL> getJavaClassPath(){
        String classPath = System.getProperty("java.class.path");
        return Arrays.stream(classPath.split(File.pathSeparator))
                .map(path -> {
                    try {
                        return new File(path).toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }

    private Class<?> loadClassFromUrls(String name) throws MalformedURLException, ClassNotFoundException {

        try (URLClassLoader urlClassLoader = new URLClassLoader(this.classPathUrls.toArray(URL[]::new), getParent())){
            return urlClassLoader.loadClass(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
//                name.replace('.', File.separatorChar) + ".class");
//        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//        int nextValue = 0;
//        try {
//            while ((nextValue = inputStream.read()) != -1) {
//                byteStream.write(nextValue);
//            }
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return byteStream.toByteArray();
    }

//    public static void main(String[] args) throws ClassNotFoundException {
//        new CustomClassLoader().loadClass("com.example.vtweb.ModularMain");
//    }
}