package com.example.vtweb.launcher;

import io.github.nhtuan10.modular.api.Modular;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Modular.startSpringModuleSyncWithMainClassLoop("vt-plugin", List.of("mvn://com.example/vt-plugin/0.0.1"), "com.example.vt.plugin.Application", List.of("com.example"));
        Modular.startSpringModuleAsyncWithMainClassLoop("vt-plugin2", List.of("mvn://com.example/vt-plugin-2/0.0.1"), "com.example.vt.plugin2.Application", List.of("*"));
        Modular.startModuleAsync("vt-plugin-3-nospring", List.of("mvn://com.example/vt-plugin-3-nospring/0.0.1"), List.of("*"));
        Modular.startSpringModuleSyncWithMainClass("vt-core", List.of("mvn://com.example/vt-core/0.0.1"), "com.example.vt.web.VtwebApplication", List.of("com.example"));
        System.out.println("Loaded all modules");
        System.out.println("Load time: " + (System.currentTimeMillis() - startTime));
    }

    private int aaa(String a) {
        try {
            return 0;
        } finally {
            System.out.println("Doinng something");
        }
    }
}
