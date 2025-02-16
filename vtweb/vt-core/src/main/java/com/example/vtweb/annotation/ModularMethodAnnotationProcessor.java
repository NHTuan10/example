package com.example.vtweb.annotation;

import com.example.vtcommon.annotation.ModularMethod;
import com.example.vtcommon.annotation.ModularService;
import com.example.vtweb.classloader.CustomClassLoader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class ModularMethodAnnotationProcessor {

    CustomClassLoader classLoader;

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

    public void annotationProcess(String pkg){
        annotationScan(pkg, ModularService.class.getName());
    }

    public void annotationScan(String pkg, String annotation){
        try (ScanResult scanResult =
                     new ClassGraph().addClassLoader(this.classLoader.getUrlClassLoader())
                             .verbose()               // Log to stderr
                             .enableAllInfo()         // Scan classes, methods, fields, annotations
                             .acceptPackages(pkg)     // Scan com.xyz and subpackages (omit to scan all packages)
                             .scan()) {               // Start the scan
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(annotation)) {
//                AnnotationInfo annotationInfo = routeClassInfo.getAnnotationInfo(annotation);
                if (classInfo.isInterface()){
                    String interfaceName = classInfo.getName();
                    List<Class<?>> implClasses = scanResult.getClassesImplementing(interfaceName).loadClasses();
                    for (Class<?> implClass : implClasses) {
                        implementModularMethods(implClass);
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
            try {
                System.out.println(bd.getBeanClassName());

                implementModularMethods(Class.forName(bd.getBeanClassName()));
            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException |
                     NoSuchMethodException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

}