package com.example.vt.plugin.service;

//import com.example.vt.common.annotation.ModularMethod;

import com.example.vt.common.service.Service1;
import com.example.vt.common.service.SomeData;
import com.example.vt.modular.annotation.ModularService;
import com.example.vt.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ServiceProperties.class)
@Slf4j
@ToString
@EqualsAndHashCode
@ModularService
public class MyService implements Service1 {

	private final ServiceProperties serviceProperties;
//
//	public MyService() {
//		this.serviceProperties = new ServiceProperties();
//		serviceProperties.setMessage("Hardcoded message");
//	}

	public MyService(ServiceProperties serviceProperties) {
		this.serviceProperties = serviceProperties;
	}

	//	@ModularMethod
	public String message(SomeData data) {
		log.info("My Service: Invoke message with data {}", data);
		log.info("Invoking " + Utils.method1("Helena"));
		return this.serviceProperties.getMessage() + " " + data;
	}
}
