package com.example.vt.common.service;

import com.example.vt.common.annotation.ModularMethod;
import com.example.vt.common.annotation.ModularService;

@ModularService
public interface Service1 {
     @ModularMethod
     String message();
}