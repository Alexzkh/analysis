package com.zqykj.config;


import com.zqykj.app.service.IAggregateOperate;
import com.zqykj.app.service.impl.EsAggregateOperateImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName AggregateOperationConfiguration
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/16 14:47
 */
@Configuration
public class AggregateOperationConfiguration {

    @Bean
    @ConditionalOnMissingBean(IAggregateOperate.class)
    @ConditionalOnProperty(
            name = {"aggregate.operate"},
            havingValue = "es",
            matchIfMissing = true
    )
    IAggregateOperate esIGenerateAggregateProxy() {
        return new EsAggregateOperateImpl();
    }

//    @Bean
//    @ConditionalOnMissingBean(IAggregateOperate.class)
//    @ConditionalOnProperty(
//            name = {"aggregate.operate"},
//            havingValue = "mongo",
//            matchIfMissing = true
//    )
//    IAggregateOperate bjCertificateParamServiceProxy() {
//        return new MongoAggregateOperateImpl();
//    }

}
