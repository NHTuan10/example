package com.example.vt.plugin.service;

import com.example.vt.common.annotation.ModularMethod;
import com.example.vt.common.service.Service1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ServiceProperties.class)
@Slf4j
public class MyService implements Service1 {

	private final ServiceProperties serviceProperties;

	public MyService() {
		this.serviceProperties = new ServiceProperties();
		serviceProperties.setMessage("Hardcoded message");
	}

	public MyService(ServiceProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	@ModularMethod
	public String message() {
		log.info("My Service: Invoke message");
		return this.serviceProperties.getMessage();
	}
}
