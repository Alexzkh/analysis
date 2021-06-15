package com.zqykj.analysis.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liuyi
 * 测试用
 * @date 2021-06-15
 */
@RestController
public class HelloController {

    @RequestMapping(value = "/hello")
    public String hello(){
        return "HELLO";
    }
}
