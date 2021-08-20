package com.zqykj.app.service.transaction.service;

import com.zqykj.app.service.operation.TransactionRecordOperations;
import com.zqykj.domain.transaction.TransactionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/8/16
 */
@Service
public class TransactionService {


    @Autowired
    private TransactionRecordOperations transactionRecordOperations;



    public void test() throws Exception {


//        TransactionRecord transactionRecord = new TransactionRecord();
//        transactionRecord.setAccount_card("77777");
//        transactionRecord.setBank("中国银行");
//        transactionRecord.setCase_id("2132");
//        transactionRecord.setTrade_amount(660.09d);
//        transactionRecord.setTrade_opposite_balance( 888.09d);
//        transactionRecord.setTrade_balance(33.33d);
//
//
////        transactionRecord.setTrade_time(LocalDate.now().);
//        transactionRecordOperations.save(transactionRecord);

        List<TransactionRecord> list = new ArrayList<>();
        for (int i = 0; i < 10 ; i++) {
            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setAccountCard("77777"+i);
            transactionRecord.setBank("中国银行"+i);
            transactionRecord.setCaseId("2132"+i);
            transactionRecord.setTradeAmount(660.09d);
            transactionRecord.setTradeOppositeBalance( 888.09d);
            transactionRecord.setTradeBalance(33.33d);
            list.add(transactionRecord);
        }
        transactionRecordOperations.save(list);
    }
}
