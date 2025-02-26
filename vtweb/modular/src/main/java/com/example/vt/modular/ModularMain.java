package com.example.vt.modular;

import com.example.vt.modular.classloader.ModuleLoader;
import com.example.vt.modular.model.ModularServiceHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Slf4j
public class ModularMain {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        testByteBuddy();
//        List<URL> depUrls = new MavenArtifactsResolver<URL>().resolveMavenDeps(List.of("com.example:vt-plugin:0.0.1-SNAPSHOT"), URL.class);
//        ModularClassLoader classLoader = new ModularClassLoader(depUrls);
//        Class m = Class.forName("com.example.vt.plugin.service.MyService",true, classLoader);
//        Class.forName("com.example.vt.common.service.Service1",true,classLoader);
//        Object s =  m.getConstructor().newInstance();
//        System.out.println(m.getDeclaredMethod("message",new Class[]{}).invoke(s));
        ModuleLoader m = ModuleLoader.getInstance();
        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT", "com.example");
//        m.loadModule("calc-core", "mvn://com.finalhints/calc-core/0.0.1", "com.finalhints");
//        m.loadModule("vt-plugin-2", "mvn://com.example/vt-plugin-2/0.0.1-SNAPSHOT", "com.example");
//        m.loadModule("vt-plugin", "mvn://com.example/vt-plugin/0.0.1-SNAPSHOT");

//        Class c = new ModularClassLoader().loadClass("com.example.vtplugin.service.MyService");

//        List<Service1> modularServices = m.getModularServiceHolder(Service1.class).stream().map(o -> (Service1) o.getProxyObject()).toList();
//        modularServices.forEach(Service1::message);
//        Service1 service1 = (Service1) m.getModularService(Service1.class);
//        service1.message();
//        SpringApplication.run(VtwebApplication.class, args);
        List<ModularServiceHolder> modularServices = m.getModularServiceHolder("vt-plugin", "com.example.vt.common.service.Service1").stream().toList();
//        List<ModularServiceHolder> modularServices2 = m.getModularServiceHolder("vt-plugin-2", "com.example.vt.common.service.Service1").stream().toList();
//        for (ModularServiceHolder modularService : modularServices) {
//            Service1 service1 = (Service1) modularService.getProxyObject();
//            System.out.println(service1.message());
//            System.out.println(modularService.getInterfaceClass().getDeclaredMethod("message", new Class[]{}).invoke(modularService.getProxyObject()));
//        }


//        for (ModularServiceHolder modularService : modularServices2) {
////            Service1 service1 = (Service1) modularService.getProxyObject();
////            System.out.println(service1.message());
//            System.out.println(modularService.getInterfaceClass().getDeclaredMethod("message", new Class[]{}).invoke(modularService.getProxyObject()));
//        }
//        SpringApplication.run(VtwebApplication.class, args);
        log.info("Test new method");
//        m.loadClass("calc-core", "com.finalhints.osgi.Activator").getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[]{});
        new Thread(() -> {
            try {
                m.loadModule("vt-core", "mvn://com.example/vt-core/0.0.1-SNAPSHOT", "com.example");
                Thread.currentThread().setContextClassLoader(m.getClassLoader("vt-core"));
                m.loadClass("vt-core", "com.example.vt.web.VtwebApplication").getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[]{});
            } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }).start();

        //        m.loadClass("vt-core", "org.springframework.boot.loader.launch.JarLauncher").getDeclaredMethod("main", String[].class).invoke(null, (Object) new String[]{});

//        List<ModularServiceHolder> modularServices = m.getModularServices(Service1.class);
    }

    public static void testByteBuddy() throws InstantiationException, IllegalAccessException {
//        Class<?> c = new ByteBuddy().subclass(Service1.class)
//                .method(ElementMatchers.named("message"))
//                .intercept(FixedValue.value("Hello"))
//                .make()
//                .load(ModularMain.class.getClassLoader())
//                .getLoaded();
//        log.info( ( (Service1) c.newInstance()).message());

    }
}
