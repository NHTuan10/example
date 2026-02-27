package com.example.vt;

import com.example.vt.web.VtwebApplication;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratedClass {
    private static Logger log = LoggerFactory.getLogger(GeneratedClass.class);

    public void exec() {
        System.out.println("Hello, dynamic compilation!");
        StringUtils.isBlank("abc");
        log.info(VtwebApplication.ThreadController.applicationContext.toString());
        //return "hello there!" ;
    }
}