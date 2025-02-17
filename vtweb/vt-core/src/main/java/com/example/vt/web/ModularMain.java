package com.example.vt.web;

import com.example.vt.common.service.Service1;
import com.example.vt.web.classloader.MavenArtifactsResolver;
import com.example.vt.web.classloader.ModularClassLoader;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

@Slf4j
public class ModularMain {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of("com.example:vt-plugin:0.0.1-SNAPSHOT"), URL.class);
        ModularClassLoader classLoader = new ModularClassLoader(depUrls);
        Class m = Class.forName("com.example.vt.plugin.service.MyService",true, classLoader);
        Service1 s = (Service1) m.getConstructor().newInstance();
        System.out.println(s.message());
//        ModuleLoader m = new ModuleLoader();
//        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT", "com.example");
//        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT");

//        Class c = new ModularClassLoader().loadClass("com.example.vtplugin.service.MyService");

//      List<Service1> service1List = m.getModularServiceHolder(Service1.class).stream().map(o -> (Service1) o.getProxyObject()).toList();
//      service1List.forEach(Service1::message);
//        Service1 service1 = (Service1) m.getModularService(Service1.class);
//        service1.message();
//        SpringApplication.run(VtwebApplication.class, args);
    }

}
