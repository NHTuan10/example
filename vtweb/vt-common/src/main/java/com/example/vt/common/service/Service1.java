package com.example.vt.common.service;

import com.example.vt.modular.annotation.ModularMethod;
import com.example.vt.modular.annotation.ModularService;

@ModularService
public interface Service1 {
     @ModularMethod
     String message(SomeData someData);
}