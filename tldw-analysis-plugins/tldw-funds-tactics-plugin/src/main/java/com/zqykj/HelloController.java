package com.zqykj;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 测试类
 * @Author zhangkehou
 * @Date 2021/9/6
 */
@RestController
@RequestMapping(path = "plugin1")
public class HelloController {

    @GetMapping()
    public String getConfig(){
        return "hello plugin1 example";
    }

}
