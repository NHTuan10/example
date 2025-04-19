package com.example.vt.plugin2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.vt")
public class Application {
//    static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            latch.countDown();
//        }));
//        latch.await();
    }

}