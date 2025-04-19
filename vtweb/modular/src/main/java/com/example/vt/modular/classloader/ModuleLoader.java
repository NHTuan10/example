package com.example.vt.modular.classloader;

import com.example.vt.modular.annotation.ModularAnnotationProcessor;
import com.example.vt.modular.exception.ModuleLoadException;
import com.example.vt.modular.model.ModularContext;
import com.example.vt.modular.model.ModularServiceHolder;
import com.example.vt.modular.proxy.ServiceInvocationInterceptor;
import com.example.vt.modular.spring.ApplicationContextProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ModuleLoader {
    public static final String APPLICATION_CONTEXT_PROVIDER = ApplicationContextProvider.class.getName();
    public static final String PROXY_TARGET_FIELD_NAME = "target";
    //    public static final String MODULAR_ANNOTATION_PKG = "com.example.vt.modular.annotation";
    //    CustomClassLoader classLoader;

    //    ModularAnnotationProcessor m;

    Map<Class<?>, Collection<ModularServiceHolder>> loadedModularServices = new ConcurrentHashMap<>(); //UNUSED
    Map<String, Collection<ModularServiceHolder>> loadedModularServices2 = new ConcurrentHashMap<>();
    Map<String, ModularClassLoader> modularClassLoaders = new ConcurrentHashMap<>();
    Map<Class<?>, List<Object>> loadedProxyObjects = new ConcurrentHashMap<>();
    Map<String, ModuleDetail> moduleDetailMap = new ConcurrentHashMap<>();

    public static enum LoadStatus {
        LOADING,
        LOADED,
        FAILED,
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleDetail {
        @Getter
        String moduleName;
        @Getter
        LoadStatus loadStatus;
        @Getter
        ModularClassLoader modularClassLoader;
        CountDownLatch readyLatch;
    }

    private volatile static ModuleLoader instance;
    private static final Object lock = new Object();

    public static ModuleLoader getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ModuleLoader();
                }
            }
        }
        return instance;
    }

    private ModuleLoader() {
    }

    public void loadModule(String name, String locationUri, boolean lazyInit) {
        loadModule(name, locationUri, "", lazyInit);
    }

    public void loadModule(String name, String locationUri, String packageToScan, boolean lazyInit) {
        // Load module
        URI uri = URI.create(locationUri);
        log.info("Loading module from " + uri);
        switch (ArtifactLocationType.valueOf(uri.getScheme().toUpperCase())) {
            case MVN:
                loadModuleFromMaven(name, uri, packageToScan, lazyInit);
                break;
            case FILE:
                loadModuleFromFile(name, uri, packageToScan);
                break;
            default:
                throw new IllegalArgumentException("Unsupported artifact location type: " + uri.getScheme());
        }
//        synchronized (classLoader) {


//        }
    }

    private void loadModuleFromMaven(String name, URI uri, String packageToScan, boolean lazyInit) {
        // Load module from Maven
        String mvnArtifact = uri.getHost() + uri.getPath().replace("/", ":");
//        log.info("Loading module from Maven: {}", mvnArtifact);
        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of(mvnArtifact), URL.class);
        ModularClassLoader classLoader = new ModularClassLoader(name, depUrls);
//        classLoader.setExcludedClassPackages(Set.of(MODULAR_ANNOTATION_PKG));
        ModuleDetail moduleDetail = moduleDetailMap.get(name);
        moduleDetail.modularClassLoader = classLoader;

        ModularAnnotationProcessor m = new ModularAnnotationProcessor(classLoader);
        try {
            m.annotationProcess(packageToScan, lazyInit);
            m.configurationAnnotationProcessor(packageToScan);
            addModularServices(m.getModularServices());
        }
//        catch (ProxyCreationException e) {
//            log.error("Fail to create proxy", e);
//            throw new RuntimeException(e);
//        }
        catch (Exception e) {
//            log.error("Fail during annotation processing", e);
            throw new RuntimeException(e);
        }
    }

    private void addModularServices(Map<Class<?>, Collection<ModularServiceHolder>> container) {
        // TODO: revise the implementation for thread-safety
        synchronized (lock) {
            container.forEach((key, value) -> {
                if (!loadedModularServices.containsKey(key)) {
                    loadedModularServices.put(key, Collections.synchronizedSet(new HashSet<>()));
                }
                loadedModularServices.get(key).addAll(value);
                if (!loadedModularServices2.containsKey(key.getName())) {
                    loadedModularServices2.put(key.getName(), Collections.synchronizedSet(new HashSet<>()));
                }
                loadedModularServices2.get(key.getName()).addAll(value);
            });
        }
    }

    private void loadModuleFromFile(String name, URI uri, String packageToScan) {
        // Load module from file
//        log.debug("Loading module from file");
    }

    public static ModularContext getContext() {
        Object moduleLoader = null;
        try {
            moduleLoader = Class.forName(ModuleLoader.class.getName(), true, ClassLoader.getSystemClassLoader()).getDeclaredMethod("getInstance").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return new ModularContext(moduleLoader);
    }

    public Collection<ModularServiceHolder> getModularServiceHolder(Class<?> key) {
        return loadedModularServices.get(key);
    }

    public Collection<ModularServiceHolder> getModularServiceHolder(String module, String key) throws ClassNotFoundException {
        return loadedModularServices.get(moduleDetailMap.get(module).modularClassLoader.loadClass(key));
    }

    public Class<?> loadClass(String module, String name) throws ClassNotFoundException {
        return moduleDetailMap.get(module).modularClassLoader.loadClass(name);
    }

    public ClassLoader getClassLoader(String module) {
        return moduleDetailMap.get(module).modularClassLoader;
    }

//    public Object getModularServiceByExactClass(Class<?> key){
//        return loadedModularServices.get(key).stream().findFirst().map(ModularServiceHolder::getProxyObject).orElse(null);
//    }

    public <I> List<I> getModularServices(Class<?> apiClass, boolean fromSpringAppContext) {
        Collection<ModularServiceHolder> serviceHolders = loadedModularServices2.get(apiClass.getName());
        if (!loadedProxyObjects.containsKey(apiClass)) {
            List<I> proxyObjects = serviceHolders.stream()
//                .map(ModularServiceHolder::getServiceClass)
                    .map(serviceHolder -> {
                        try {
                            Object service;
                            if (fromSpringAppContext) {
                                Class serviceClass = serviceHolder.getServiceClass();
                                Class serviceAppContextProvide = Class.forName(APPLICATION_CONTEXT_PROVIDER, true, serviceClass.getClassLoader());
                                service = serviceAppContextProvide.getDeclaredMethod("getBean", Class.class).invoke(null, serviceClass);
                            } else {
                                service = serviceHolder.getInstance();
                            }
                            return this.<I>createProxyObject(apiClass, service);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException | ClassNotFoundException | NoSuchFieldException e) {
                            return null;
                        }
                    }).toList();
            loadedProxyObjects.put(apiClass, (List<Object>) proxyObjects);
            return proxyObjects;
        } else {
            return (List<I>) loadedProxyObjects.get(apiClass);
        }
    }

//    public <I> List<I> getModularServices(Class<?> apiClass) {
//
//        Collection<ModularServiceHolder> serviceHolders = loadedModularServices2.get(apiClass.getName());
//        if (!loadedProxyObjects.containsKey(apiClass)) {
//            List<I> proxyObjects = serviceHolders.stream().map(serviceHolder -> {
//                try {
//                    Object service = serviceHolder.getInstance();
//                    return this.<I>createProxyObject(apiClass, service);
//                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
//                         NoSuchMethodException | ClassNotFoundException | NoSuchFieldException e) {
//                    throw new RuntimeException(e);
//                }
//            }).toList();

    /// /            assertThat((String) dynamicType.newInstance().apply("Byte Buddy"), is("Hello from Byte Buddy"));
//            loadedProxyObjects.put(apiClass, (List<Object>) proxyObjects);
//            return proxyObjects;
//        } else {
//            return (List<I>) loadedProxyObjects.get(apiClass);
//        }
//    }
    private <I> I createProxyObject(Class<?> apiClass, Object service) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        ClassLoader apiClassLoader = apiClass.getClassLoader();
        Object svcInvocationInterceor = apiClassLoader.loadClass(ServiceInvocationInterceptor.class.getName())
                .getConstructor(Object.class).newInstance(service);
        Class<I> c = (Class<I>) new ByteBuddy()
                .<I>subclass(apiClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(svcInvocationInterceor))
                .defineField(PROXY_TARGET_FIELD_NAME, Object.class, Visibility.PRIVATE)
                .make()
                .load(apiClassLoader)
                .getLoaded();
        I proxy = c.getConstructor(new Class[]{}).newInstance();
        Field targetField = c.getDeclaredField(PROXY_TARGET_FIELD_NAME);
        targetField.setAccessible(true);
        targetField.set(proxy, service);
        return proxy;
    }

    private Thread startModule(String moduleName, String locationUri, boolean lazyInit, String mainClass, String packageToScan, boolean awaitMainClass) {
        if (!moduleDetailMap.containsKey(moduleName)) {
            ModuleDetail moduleDetail = new ModuleDetail(moduleName, LoadStatus.LOADING, null, new CountDownLatch(1));
            moduleDetailMap.put(moduleName, moduleDetail);

            Thread t = new Thread(() -> {
                loadModule(moduleName, locationUri, packageToScan, lazyInit);
                Thread.currentThread().setContextClassLoader(getClassLoader(moduleName));
                if (mainClass != null) {
                    try {
                        loadClass(moduleName, mainClass).getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[]{});
                        if (awaitMainClass) {
                            CountDownLatch countDownLatch = new CountDownLatch(1);
                            Runtime.getRuntime().addShutdownHook(new Thread(countDownLatch::countDown));
                            countDownLatch.await();
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                             ClassNotFoundException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            t.start();
            return t;
        } else {
            throw new ModuleLoadException("Module '" + moduleName + "' is already loaded");
        }
    }

    public void startModuleSync(String moduleName, String locationUri, String packageToScan) {
        Thread t = startModule(moduleName, locationUri, false, null, packageToScan, false);
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupt during start module [%s]".formatted(moduleName), e);
        }
    }

    public void startModuleAsync(String moduleName, String locationUri, String packageToScan) {
        startModule(moduleName, locationUri, false, null, packageToScan, false);
    }

    public void startModuleAsync(String moduleName, String locationUri) {
        startModuleAsync(moduleName, locationUri, "");
    }

    public void startModuleAsyncWithMainClass(String moduleName, String locationUri, String packageToScan, String mainClass) {
        startModule(moduleName, locationUri, false, mainClass, packageToScan, false);
    }

//    public void startSpringModuleAsync(String moduleName, String locationUri, String packageToScan) {
//        startModule(moduleName, locationUri, true, null, packageToScan);
//    }

    public void startSpringModuleSyncWithMainClassLoop(String moduleName, String locationUri, String mainClass, String packageToScan) {
        startModule(moduleName, locationUri, true, mainClass, packageToScan, true);
        ModuleDetail moduleDetail = moduleDetailMap.get(moduleName);
        try {
            moduleDetail.readyLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void startSpringModuleSyncWithMainClassLoop(String moduleName, String locationUri, String mainClass) {
        startSpringModuleSyncWithMainClassLoop(moduleName, locationUri, mainClass, "");
    }

    public void startSpringModuleAsyncWithMainClassLoop(String moduleName, String locationUri, String mainClass, String packageToScan) {
        startModule(moduleName, locationUri, true, mainClass, packageToScan, true);
    }

    public void startSpringModuleAsyncWithMainClassLoop(String moduleName, String locationUri, String mainClass) {
        startSpringModuleAsyncWithMainClassLoop(moduleName, locationUri, mainClass, "");
    }

    public void notifyModuleReady(String moduleName) {
        ModuleDetail moduleDetail = moduleDetailMap.get(moduleName);
        CountDownLatch readyLatch = moduleDetail.readyLatch;
        if (readyLatch != null && readyLatch.getCount() > 0) {
            readyLatch.countDown();
        }
        moduleDetail.loadStatus = LoadStatus.LOADED;
    }
}
