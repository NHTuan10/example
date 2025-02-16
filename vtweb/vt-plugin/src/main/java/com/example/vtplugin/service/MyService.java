package com.example.vtplugin.service;

import com.example.vtcommon.annotation.ModularMethod;
import com.example.vtcommon.service.Service1;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ServiceProperties.class)
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
		System.out.println("My Service: Invoke message");
		return this.serviceProperties.getMessage();
	}
}
