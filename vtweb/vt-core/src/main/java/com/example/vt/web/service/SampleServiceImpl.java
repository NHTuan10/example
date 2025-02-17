package com.example.vt.web.service;

import com.example.vt.common.service.Service1;
import lombok.extern.slf4j.Slf4j;

//@ModularService
@Slf4j
public class SampleServiceImpl implements Service1 {
    @Override
    public String message() {
        log.info("SampleServiceImpl: Invoke message");
        return "Hello World";
    }
}
