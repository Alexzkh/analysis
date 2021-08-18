package com.zqykj.controller;

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

    @Autowired
    private TransactionService transactionService;


    @GetMapping(value = "/transaction")
    public void test() throws Exception {
        transactionService.test();
    }
}
