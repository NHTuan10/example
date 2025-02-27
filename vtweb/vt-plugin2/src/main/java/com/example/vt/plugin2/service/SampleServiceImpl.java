package com.example.vt.plugin2.service;

import com.example.vt.common.service.Service1;
import com.example.vt.common.service.SomeData;
import com.example.vt.util.Utils;
import lombok.extern.slf4j.Slf4j;

//@ModularService
@Slf4j
public class SampleServiceImpl implements Service1 {
    @Override
    public String message(SomeData data) {
        log.info("SampleServiceImpl: Invoke message with data {}", data);
        log.info("Utils.method1: " + Utils.method1());
        return "Hello from SampleServiceImpl " + data;
    }
}
