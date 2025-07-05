package com.example.vt.plugin2.service;

import com.example.vt.common.service.Service1;
import com.example.vt.common.service.SomeData;
import com.example.vt.util.Utils;
import io.github.nhtuan10.modular.api.annotation.ModularSpringService;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

//@ModularService
@Service
@Slf4j
@ToString
@EqualsAndHashCode
//@ModularService
@ModularSpringService
public class SampleServiceImpl implements Service1 {
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public String message(SomeData data) {
        log.info("SampleServiceImpl: Invoke message with data {}", data);
        log.info("Utils.method1: " + Utils.method1());
        return "Hello from SampleServiceImpl " + data;
    }
}
