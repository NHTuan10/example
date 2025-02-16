package com.example.vtweb.annotation;

import com.example.vtcommon.annotation.ModularMethod;
import com.example.vtcommon.annotation.ModularService;
import com.example.vtweb.classloader.CustomClassLoader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@NoArgsConstructor
public class ModularMethodAnnotationProcessor {

    CustomClassLoader classLoader;
    Map<Class<?>, List<Object>> container;

    public ModularMethodAnnotationProcessor(CustomClassLoader classLoader) {
        this.classLoader = classLoader;
        this.container = new ConcurrentHashMap<>();
    }

    public List<Object> getModularServices(Class<?> key) {
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

    public static class MyInvocationHandler implements InvocationHandler {
        private final Object target;

        public MyInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Before method call");
            Object result = method.invoke(target, args);
            System.out.println("After method call");
            return result;
        }
    }

    // TODO: need to handle multiple interfaces
    private Object createProxyClass(Class<?> interfaceClass, Class<?> impl) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object implObject = impl.getConstructor().newInstance();
         Object proxyObject =  Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new MyInvocationHandler(implObject)
        );
        return proxyObject;
    }
    public void annotationProcess(String pkg){
        annotationScan(pkg, ModularService.class.getName());
    }

    public void annotationScan(String pkg, String annotation){
        // TODO: need to handle multiple interfaces too
        try (ScanResult scanResult =
                     new ClassGraph().addClassLoader(this.classLoader.getUrlClassLoader())
//                             .verbose()               // Log to stderr
                             .enableAllInfo()         // Scan classes, methods, fields, annotations
                             .acceptPackages(pkg)     // Scan com.xyz and subpackages (omit to scan all packages)
                             .scan()) {               // Start the scan
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotation)) {
//                AnnotationInfo annotationInfo = routeClassInfo.getAnnotationInfo(annotation);
                if (classInfo.isInterface()){
                    Class<?> interfaceClass = classInfo.loadClass();
//                    Class<?> interfaceClass = classLoader.loadClass(classInfo.getName());
                    List<Class<?>> implClasses = scanResult.getClassesImplementing(interfaceClass.getName()).loadClasses();
                    for (Class<?> implClass : implClasses) {
//                        implementModularMethods(implClass);
                        if (!container.containsKey(interfaceClass)) {
                            container.put(interfaceClass, new ArrayList<>());
                        }
                        container.get(interfaceClass).add(createProxyClass(interfaceClass, implClass));
                    }
                }

                ;
//                List<AnnotationParameterValue> routeParamVals = routeAnnotationInfo.getParameterValues();
                // @com.xyz.Route has one required parameter
//                String route = (String) routeParamVals.get(0).getValue();
//                System.out.println(routeClassInfo.getName() + " is annotated with route " + route);
            }
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }

    }
    public void annotationProcessUsingSpring(){
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(ModularService.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.example")) {
//            try {
                System.out.println(bd.getBeanClassName());

//                implementModularMethods(Class.forName(bd.getBeanClassName()));
//            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
//                     NoSuchMethodException | InstantiationException e) {
//                throw new RuntimeException(e);
//            }
        }
    }

}