package com.zqykj;

import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: 单元测试-查询
 * @Author zhangkehou
 * @Date 2021/10/13
 */
@SpringBootTest
@Slf4j
public class QueryTest {

    @Autowired
    EntranceRepository entranceRepository;

    @Test
    public void findByCondition() {

        PageRequest pageRequest = PageRequest.of(0, 25, Sort.Direction.DESC, "trading_time");
        Page page = entranceRepository.findByCondition(pageRequest, "100376eb69614df4a7cd63ca6884827b", BankTransactionFlow.class
                , "60138216660012614", "100376eb69614df4a7cd63ca6884827b", "");
        System.out.println("*******");

    }
}
