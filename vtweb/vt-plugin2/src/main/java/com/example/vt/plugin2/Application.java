package com.example.vt.plugin2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication(scanBasePackages = "com.example.vt")
public class Application implements CommandLineRunner {
    static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            latch.countDown();
        }));
        latch.await();
    }

    @Override
    public void run(String... args) throws Exception {
//        Thread.currentThread().join();
    }
}