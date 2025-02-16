package com.example.vtweb.service;

import com.example.vtcommon.service.Service1;
import lombok.extern.slf4j.Slf4j;

//@ModularService
@Slf4j
public class SampleServiceImpl implements Service1 {
    public String message() {
        log.info("SampleServiceImpl: Invoke message");
        return "Hello World";
    }
}
