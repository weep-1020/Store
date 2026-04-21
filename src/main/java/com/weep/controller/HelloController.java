package com.weep.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @RequestMapping("/hello")
    public String hello(){
        return "Hello Security!";
    }
    @RequestMapping("/query")
    public String query(){
        return "query Security!";
    }
}
