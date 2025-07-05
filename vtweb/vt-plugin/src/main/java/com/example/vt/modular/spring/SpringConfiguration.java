package com.example.vt.modular.spring;

import com.example.vt.common.service.Service1;
import com.example.vt.plugin.service.MyService;
import com.example.vt.plugin.service.ServiceProperties;
import io.github.nhtuan10.modular.api.annotation.ModularConfiguration;
import io.github.nhtuan10.modular.api.annotation.ModularSpringService;
import io.github.nhtuan10.modular.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ModularConfiguration
public class SpringConfiguration {

    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    @ModularSpringService
    public Service1 anotherService1() {
        ServiceProperties s = new ServiceProperties();
        s.setMessage("Another bean");
        return new MyService(s);
    }
}
