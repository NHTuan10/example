package com.example.vt.modular.annotation;

import com.example.vt.modular.classloader.ModularClassLoader;
import com.example.vt.modular.exception.ProxyCreationException;
import com.example.vt.modular.model.ModularServiceHolder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@NoArgsConstructor
@Slf4j
public class ModularAnnotationProcessor {

    ModularClassLoader modularClassLoader;
    Map<Class<?>, Collection<ModularServiceHolder>> container;

    public ModularAnnotationProcessor(ModularClassLoader modularClassLoader) {
        this.modularClassLoader = modularClassLoader;
        this.container = new ConcurrentHashMap<Class<?>, Collection<ModularServiceHolder>>();
    }

    public Map<Class<?>, Collection<ModularServiceHolder>> getModularServices() {
        return container;
    }

    public Collection<ModularServiceHolder> getModularServices(Class<?> key) {
        return container.get(key);
    }

    private void implementModularMethod(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> clazz = object.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ModularMethod.class)) {
                method.setAccessible(true);
//                method.invoke(object);
            }
        }
    }

    private void implementModularMethods(Class<?> clazz) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, InstantiationException {

        for (Method method : clazz.getDeclaredMethods()) {
            Object object = clazz.getConstructor().newInstance();
            method.setAccessible(true);
            method.invoke(object);
        }
    }


    public void annotationProcess(String pkg) throws ProxyCreationException {
        annotationScan(pkg, ModularService.class.getName());
    }

    public void annotationScan(String pkg, String annotation) throws ProxyCreationException {
        // TODO: need to handle multiple interfaces too
        try (ScanResult scanResult =
                     new ClassGraph()
//                             .addClassLoader(this.classLoader)
                             .overrideClasspath(this.modularClassLoader.getClassPathUrls())
                             .overrideClassLoaders(this.modularClassLoader)
//                             .verbose()               // Log to stderr
                             .enableAllInfo()         // Scan classes, methods, fields, annotations
                             .acceptPackages(pkg)     // Scan package and subpackages (omit to scan all packages)
                             .scan()) {               // Start the scan
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotation)) {
//                AnnotationInfo annotationInfo = routeClassInfo.getAnnotationInfo(annotation);
                if (classInfo.isInterface()) {
                    Class<?> interfaceClass = classInfo.loadClass();
//                    Class<?> interfaceClass = this.modularClassLoader.loadClass(classInfo.getName());
//                    Class<?> interfaceClass = classLoader.loadClass(classInfo.getName());
                    List<? extends Class<?>> implClasses = scanResult.getClassesImplementing(interfaceClass.getName()).stream()
//                            .map(c -> {
//                                try {
//                                    return Class.forName(c.getName(), true, modularClassLoader);
//                                } catch (ClassNotFoundException e) {
//                                    throw new RuntimeException("Internal error when loading class " + c.getName() , e);
//                                }
//                            })
                            .map(ClassInfo::loadClass)
                            .toList();
//                    implClasses.map(ClassInfo::loadClass);
                    Set<ModularServiceHolder> serviceInfoSet = new HashSet<>();
                    for (Class<?> implClass : implClasses) {
                        try {
                            serviceInfoSet.add(new ModularServiceHolder(implClass, implClass.getName(), ProxyCreator.createNoArgsContructorsProxyClass(interfaceClass, implClass), interfaceClass));
                        } catch (Exception e) {
                            throw new ProxyCreationException("Failed to create proxy for class %s with annotation %s in package %s".formatted(implClass.getName(), annotation, pkg), e);
                        }

                    }

                    container.put(interfaceClass, Collections.unmodifiableSet(serviceInfoSet));
//                    for (Class<?> implClass : implClasses) {
////                        implementModularMethods(implClass);
//                        if (!container.containsKey(interfaceClass)) {
//                            container.put(interfaceClass, new ArrayList<>());
//                        }
//                        container.get(interfaceClass).add(createProxyClass(interfaceClass, implClass));
//                    }
                }

                ;
//                List<AnnotationParameterValue> routeParamVals = routeAnnotationInfo.getParameterValues();
                // @com.xyz.Route has one required parameter
//                String route = (String) routeParamVals.get(0).getValue();
//                System.out.println(routeClassInfo.getName() + " is annotated with route " + route);
            }
        } catch (ProxyCreationException e) {
            throw e;
        }
//        catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

    }

//    public void annotationProcessUsingSpring() {
//        ClassPathScanningCandidateComponentProvider scanner =
//                new ClassPathScanningCandidateComponentProvider(false);
//
//        scanner.addIncludeFilter(new AnnotationTypeFilter(ModularService.class));
//
//        for (BeanDefinition bd : scanner.findCandidateComponents("com.example")) {
////            try {
//            System.out.println(bd.getBeanClassName());
//
////                implementModularMethods(Class.forName(bd.getBeanClassName()));
////            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
////                     NoSuchMethodException | InstantiationException e) {
////                throw new RuntimeException(e);
////            }
//        }
//    }

}