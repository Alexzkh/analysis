package com.zqykj.controller;

import com.gitee.starblues.realize.PluginUtils;
import com.zqykj.app.service.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/17
 */
@RestController
public class TransactionController {


    private final TransactionService transactionService;

    @Autowired
    public TransactionController(PluginUtils pluginUtils) {
        this.transactionService = pluginUtils.getMainBean(TransactionService.class);
    }

    @GetMapping(value = "/transaction")
    public void test() throws Exception {
        transactionService.test();
    }
}
