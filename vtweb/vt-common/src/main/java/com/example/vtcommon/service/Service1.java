package com.example.vtcommon.service;

import com.example.vtcommon.annotation.ModularMethod;
import com.example.vtcommon.annotation.ModularService;

@ModularService
public interface Service1 {
     @ModularMethod
     String message();
}