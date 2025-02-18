package com.example.vt.web.classloader;

import com.example.vt.web.annotation.ModularAnnotationProcessor;
import com.example.vt.web.exception.ProxyCreationException;
import com.example.vt.web.model.ModularServiceHolder;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ModuleLoader {
//    CustomClassLoader classLoader;

//    ModularAnnotationProcessor m;
    Map<Class<?>, Collection<ModularServiceHolder>> loadedModularServices = new ConcurrentHashMap<>();
    Map<String, ModularClassLoader> modularClassLoaders = new HashMap<>();
    public void loadModule(String name, String locationUri) {
        loadModule(name, locationUri, "");
    }

    public void loadModule(String name, String locationUri, String packageToScan) {
        // Load module
        URI uri = URI.create(locationUri);
        log.info("Loading module from " + uri);
        switch (ArtifactLocationType.valueOf(uri.getScheme().toUpperCase())) {
            case MVN:
                loadModuleFromMaven(name, uri, packageToScan);
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

    private void loadModuleFromMaven(String name, URI uri, String packageToScan) {
        // Load module from Maven
        String mvnArtifact = uri.getHost() + uri.getPath().replace("/", ":");
        log.info("Loading module from Maven: {}", mvnArtifact);
        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of(mvnArtifact), URL.class);
        ModularClassLoader classLoader = new ModularClassLoader(depUrls);
        modularClassLoaders.put(name, classLoader);
        ModularAnnotationProcessor m = new ModularAnnotationProcessor(classLoader);
        try {
            m.annotationProcess(packageToScan);
            addModularServices(m.getModularServices());
        } catch (ProxyCreationException e) {
            log.error("Fail to create proxy", e);
            throw new RuntimeException(e);
        }
        catch (Exception e){
            log.error("Fail during annotation processing", e);
        }
    }

    private void addModularServices(Map<Class<?>, Collection<ModularServiceHolder>> container){
        container.forEach((key, value) -> {
            if (!loadedModularServices.containsKey(key)) {
                loadedModularServices.put(key,  Collections.synchronizedSet(new HashSet<>()));
            }
            loadedModularServices.get(key).addAll(value);
        });
    }

    private void loadModuleFromFile(String name, URI uri, String packageToScan) {
        // Load module from file
        log.debug("Loading module from file");
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

    public Object getModularService(Class<?> key){
        return loadedModularServices.get(key).stream().findFirst().map(ModularServiceHolder::getProxyObject).orElse(null);
    }
}
