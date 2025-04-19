package com.example.vt.modular.spring;

import com.example.vt.modular.classloader.ModuleLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class SpringConfiguration {

    @EventListener
    public void handleContextRefreshEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("ContextRefreshedEvent received.");
        ModuleLoader.getContext().notifyModuleReady();
    }
}
