package com.example.vt.plugin3.service;

import com.example.vt.common.service.Service2;
import io.github.nhtuan10.modular.api.annotation.ModularService;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@EqualsAndHashCode
@ModularService
public class Service2Impl implements Service2 {
    @Override
    public void test() {
        log.info("Service 2 Impl: Invoke test");
    }
}
