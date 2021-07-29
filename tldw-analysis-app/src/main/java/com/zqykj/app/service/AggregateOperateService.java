package com.zqykj.app.service;

import com.zqykj.infrastructure.common.entity.StandardBankTransactionFlow;
import com.zqykj.infrastructure.core.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName AggregateOperateService
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/16 14:42
 */
@Service
public class AggregateOperateService {

    @Autowired
    private IAggregateOperate iAggregateOperate;


    /**
     * 获取当前案件下所有数据量
     * @return: com.zqykj.infrastructure.core.ServerResponse
     **/
    public ServerResponse countAPI() throws Exception {
        return ServerResponse.createBySuccess(iAggregateOperate.count(StandardBankTransactionFlow.class));
    }

}
