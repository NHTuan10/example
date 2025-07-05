package com.example.vt.common.service;

import io.github.nhtuan10.modular.api.annotation.ModularMethod;
import io.github.nhtuan10.modular.api.annotation.ModularService;

@ModularService
public interface Service1 {
     @ModularMethod
     String message(SomeData someData);
}