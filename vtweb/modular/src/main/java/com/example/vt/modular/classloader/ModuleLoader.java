package com.example.vt.modular.classloader;

import com.example.vt.modular.annotation.ModularAnnotationProcessor;
import com.example.vt.modular.model.ModularContext;
import com.example.vt.modular.model.ModularServiceHolder;
import com.example.vt.modular.proxy.ServiceInvocationInterceptor;
import com.example.vt.modular.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Slf4j
public class ModuleLoader {
    public static final String APPLICATION_CONTEXT_PROVIDER = ApplicationContextProvider.class.getName();
//    public static final String MODULAR_ANNOTATION_PKG = "com.example.vt.modular.annotation";
    //    CustomClassLoader classLoader;

    //    ModularAnnotationProcessor m;

    Map<Class<?>, Collection<ModularServiceHolder>> loadedModularServices = new ConcurrentHashMap<>();
    Map<String, Collection<ModularServiceHolder>> loadedModularServices2 = new ConcurrentHashMap<>();
    Map<String, ModularClassLoader> modularClassLoaders = new ConcurrentHashMap<>();

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
        modularClassLoaders.put(name, classLoader);
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
        return loadedModularServices.get(modularClassLoaders.get(module).loadClass(key));
    }

    public Class<?> loadClass(String module, String name) throws ClassNotFoundException {
        return modularClassLoaders.get(module).loadClass(name);
    }

    public ClassLoader getClassLoader(String module) throws ClassNotFoundException {
        return modularClassLoaders.get(module);
    }

//    public Object getModularServiceByExactClass(Class<?> key){
//        return loadedModularServices.get(key).stream().findFirst().map(ModularServiceHolder::getProxyObject).orElse(null);
//    }

    public <I> List<I> getModularServicesFromSpring(Class<?> apiClass) {
        Collection<ModularServiceHolder> serviceHolders = loadedModularServices2.get(apiClass.getName());
        // TODO: cache service & interceptor
        return serviceHolders.stream()
//                .map(ModularServiceHolder::getServiceClass)
                .map(serviceHolder -> {
                    try {
                        Class serviceClass = serviceHolder.getServiceClass();
                        ClassLoader apiClassLoader = apiClass.getClassLoader();
                        Class serviceAppContextProvide = Class.forName(APPLICATION_CONTEXT_PROVIDER, true, serviceClass.getClassLoader());
                        Object service = serviceAppContextProvide.getDeclaredMethod("getBean", Class.class).invoke(null, serviceClass);
                        Object svcInvocationInterceor = apiClassLoader.loadClass(ServiceInvocationInterceptor.class.getName())
                                .getConstructor(Object.class).newInstance(service);
                        Class<I> c = (Class<I>) new ByteBuddy()
                                .<I>subclass(apiClass)
                                .method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(svcInvocationInterceor))
                                .method(named("toString")).intercept(MethodDelegation.to(svcInvocationInterceor))
//                        .method(named("toString")).intercept(FixedValue.value("Hello World!"))
                                .make()
                                .load(apiClassLoader)
                                .getLoaded();
                        I instance = c.getConstructor(new Class[]{}).newInstance();
                        instance.toString();
//                Class apiAppContextProvide = Class.forName(APPLICATION_CONTEXT_PROVIDER, true, apiClassLoader);

//                apiAppContextProvide.getDeclaredMethod("registerBean", String.class, Object.class).invoke(null, serviceHolder.getName(), instance);
                        return instance;
                    } catch (Exception e) {
                        return null;
                    }
                }).toList();
    }

    public <I> List<I> getModularServices(Class<?> apiClass) {

        Collection<ModularServiceHolder> serviceHolders = loadedModularServices2.get(apiClass.getName());
        // TODO: cache service & interceptor
        return serviceHolders.stream().map(serviceHolder -> {
            try {
                ClassLoader apiClassLoader = apiClass.getClassLoader();
//                Object service = serviceClass.getConstructor().newInstance();
                Object service = serviceHolder.getInstance();
                Object svcInvocationInterceor = apiClassLoader.loadClass(ServiceInvocationInterceptor.class.getName())
                        .getConstructor(Object.class).newInstance(service);
                Class<I> c = (Class<I>) new ByteBuddy()
                        .<I>subclass(apiClass)
                        .method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(svcInvocationInterceor))
                        .make()
                        .load(apiClassLoader)
                        .getLoaded();
                return c.getConstructor(new Class[]{}).newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toList();
//            assertThat((String) dynamicType.newInstance().apply("Byte Buddy"), is("Hello from Byte Buddy"));

    }


}
