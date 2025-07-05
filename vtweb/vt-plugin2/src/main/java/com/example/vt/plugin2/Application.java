package com.example.vt.plugin2;

import com.example.vt.common.service.DaggerSampleService;
import io.github.nhtuan10.modular.api.Modular;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.vt")
public class Application {
//    static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
        DaggerSampleService d = Modular.getModularServices(DaggerSampleService.class).get(0);
        System.out.println(d);
        d.test();
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            latch.countDown();
//        }));
//        latch.await();
    }

}