package com.example.vt.web;

import com.example.vt.web.classloader.ModuleLoader;
import com.example.vt.web.model.ModularServiceHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Slf4j
public class ModularMain {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

//        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of("com.example:vt-plugin:0.0.1-SNAPSHOT"), URL.class);
//        ModularClassLoader classLoader = new ModularClassLoader(depUrls);
//        Class m = Class.forName("com.example.vt.plugin.service.MyService",true, classLoader);
//        Class.forName("com.example.vt.common.service.Service1",true,classLoader);
//        Object s =  m.getConstructor().newInstance();
//        System.out.println(m.getDeclaredMethod("message",new Class[]{}).invoke(s));
        ModuleLoader m = new ModuleLoader();
        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT", "com.example");
        m.loadModule("vt-plugin-2", "mvn://com.example/vt-plugin-2/0.0.1-SNAPSHOT", "com.example");
//        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT");

//        Class c = new ModularClassLoader().loadClass("com.example.vtplugin.service.MyService");

//        List<Service1> modularServices = m.getModularServiceHolder(Service1.class).stream().map(o -> (Service1) o.getProxyObject()).toList();
//        modularServices.forEach(Service1::message);
//        Service1 service1 = (Service1) m.getModularService(Service1.class);
//        service1.message();
//        SpringApplication.run(VtwebApplication.class, args);
        List<ModularServiceHolder> modularServices = m.getModularServiceHolder("vt-plugin", "com.example.vt.common.service.Service1").stream().toList();
        List<ModularServiceHolder> modularServices2 = m.getModularServiceHolder("vt-plugin-2", "com.example.vt.common.service.Service1").stream().toList();
        for (ModularServiceHolder modularService : modularServices) {
//            Service1 service1 = (Service1) modularService.getProxyObject();
//            System.out.println(service1.message());
            System.out.println(modularService.getInterfaceClass().getDeclaredMethod("message", new Class[]{}).invoke(modularService.getProxyObject()));
        }

        for (ModularServiceHolder modularService : modularServices2) {
//            Service1 service1 = (Service1) modularService.getProxyObject();
//            System.out.println(service1.message());
            System.out.println(modularService.getInterfaceClass().getDeclaredMethod("message", new Class[]{}).invoke(modularService.getProxyObject()));
        }
    }

}
