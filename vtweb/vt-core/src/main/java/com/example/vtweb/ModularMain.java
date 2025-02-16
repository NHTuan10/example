package com.example.vtweb;

import com.example.vtweb.annotation.ModularMethodAnnotationProcessor;
import com.example.vtweb.classloader.CustomClassLoader;
import com.example.vtweb.classloader.MavenArtifactsResolver;
import org.springframework.boot.SpringApplication;

import java.net.URL;
import java.util.List;

public class ModularMain {
    public static void main(String[] args) throws ClassNotFoundException {
        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of("com.example:vt-plugin:0.0.1-SNAPSHOT"), URL.class);
        CustomClassLoader classLoader
                = new CustomClassLoader(depUrls);
        Class c = classLoader.loadClass("com.example.vtplugin.service.MyService");
        new ModularMethodAnnotationProcessor(classLoader).annotationProcess("com.example");
        SpringApplication.run(VtwebApplication.class, args);
    }

}
