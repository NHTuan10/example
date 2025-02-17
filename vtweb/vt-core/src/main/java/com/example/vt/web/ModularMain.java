package com.example.vt.web;

import com.example.vt.common.service.Service1;
import com.example.vt.web.classloader.ModuleLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

import java.util.List;

@Slf4j
public class ModularMain {
    public static void main(String[] args) throws ClassNotFoundException {
        ModuleLoader m = new ModuleLoader();
        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT", "com.example");
        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT");

//        Class c = new ModularClassLoader().loadClass("com.example.vtplugin.service.MyService");

      List<Service1> service1List = m.getModularServices(Service1.class).stream().map(o -> (Service1) o.getProxyObject()).toList();
      service1List.forEach(Service1::message);
        SpringApplication.run(VtwebApplication.class, args);
    }

}
