package com.zqykj;

import org.elasticsearch.action.search.SearchResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
