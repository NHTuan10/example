package com.example.vt.modular.spring;

import io.github.nhtuan10.modular.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringConfiguration {

//    @EventListener
//    public void handleContextRefreshEvent(ContextRefreshedEvent contextRefreshedEvent) {
//        log.info("ContextRefreshedEvent received.");
//        ModuleLoader.getContext().notifyModuleReady();
//    }


    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }
}
