package com.example.vt.web.service;

import com.example.vt.common.service.Service1;
import com.example.vt.util.Utils;
import lombok.extern.slf4j.Slf4j;

//@ModularService
@Slf4j
public class SampleServiceImpl implements Service1 {
    @Override
    public String message() {
        log.info("SampleServiceImpl: Invoke message");
        log.info("Utils.method1: " + Utils.method1());
        return "Hello from SampleServiceImpl";
    }
}
