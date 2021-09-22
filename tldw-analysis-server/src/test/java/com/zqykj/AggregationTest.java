package com.zqykj;

import com.zqykj.common.request.AggregateBuilder;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.enums.AggsType;
import com.zqykj.repository.EntranceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description: aggregations test .
 * @Author zhangkehou
 * @Date 2021/9/18
 */
@SpringBootTest
@Slf4j
public class AggregationTest {

    @Autowired
    EntranceRepository entranceRepository;

    /**
     * 选择个体聚合操作：聚合证件号码以及证件号码下挂的卡号的调单账号数量、交易总次数、入账笔数，入账金额、出账笔数、出账金额、交易总金额、最早交易时间、最晚交易时间
     * POST standard_bank_transaction_flow/_search?routing=7f071cdf-9197-479f-95a9-9ae46045cca9
     * {
     * "aggs": {
     *         "terms_custom_identity_id": {
     *             "terms": {
     *                 "field": "customer_identity_card",
     *                 "size": 25,
     *                 "collect_mode": "breadth_first"
     *              },
     *                "aggs": {
     *                    "terms_account_card": {
     *                        "terms": {
     *                            "field": "account_card",
     *                            "size": 1000,
     *                            "collect_mode": "breadth_first"
     *                         },
     *                           "aggs": {
     *                                "min_date": {
     *                                    "min": {
     *                                        "field": "trade_time"
     *                                       }
     *                                 },
     *                                "max_date": {
     *                                     "max": {
     *                                         "field": "trade_time"
     *                                       }
     *                                 },
     *                                 "sum_trade_amount": {
     *                                      "sum": {
     *                                          "field": "trade_amount"
     *                                              }
     *                                 },
     *                                 "terms_lend_mark": {
     *                                       "terms": {
     *                                           "field": "lend_mark"
     *                                           },
     *                                            "aggs": {
     *                                                "sum_trade_amount": {
     *                                                      "sum": {
     *                                                           "field": "trade_amount"
     *                                                       }
     *                                                  }
     *                                              }
     *                                         }
     *                                     }
     *                              },
     *                 "terms_lend_mark": {
     *                       "terms": {
     *                                "field": "lend_mark"
 *                             },
 *                                "aggs": {
 *                                    "sum_trade_amount": {
 *                                         "sum": {
 *                                             "field": "trade_amount"
 *                                          }
 *                                      }
 *                                  }
     *                 },
     *                  "min_date": {
     *                        "min": {
     *                           "field": "trade_time"
     *                          }
     *                    },
     *                  "max_date": {
     *                        "max": {
     *                           "field": "trade_time"
     *                         }
     *                  }
     *             }
     *        }
     *    },
     * "size": 0
     * }
     */
    @Test
    public void multilayerAggsTest() {
        AggregateBuilder aggregateBuilder = new AggregateBuilder();
        aggregateBuilder.setAggregateName("customer_identity_card");
        aggregateBuilder.setSize(5);
        aggregateBuilder.setAggregateType(AggsType.terms);
        aggregateBuilder.setRouting("7f071cdf-9197-479f-95a9-9ae46045cca9");

        List<AggregateBuilder> aggregateBuilders = new ArrayList<>();

        AggregateBuilder aggregateBuilder1 = new AggregateBuilder();
        aggregateBuilder1.setAggregateName("account_card");
        aggregateBuilder1.setSize(10);
        aggregateBuilder1.setAggregateType(AggsType.terms);


        List<AggregateBuilder> aggregateBuilders1 = new ArrayList<>();

        AggregateBuilder aggregateBuilder2 = new AggregateBuilder();
        aggregateBuilder2.setAggregateName("lend_mark");
        aggregateBuilder2.setAggregateType(AggsType.terms);
        List<AggregateBuilder> builders = new ArrayList<>();
        aggregateBuilder2.setSubAggregations(builders);

        AggregateBuilder aggregateBuilder3 = new AggregateBuilder();
        aggregateBuilder3.setAggregateName("trade_amount");
        aggregateBuilder3.setAggregateType(AggsType.sum);

        AggregateBuilder aggregateBuilder4 = new AggregateBuilder();
        aggregateBuilder4.setAggregateName("trade_amount");
        aggregateBuilder4.setAggregateType(AggsType.sum);

        AggregateBuilder aggregateBuilder5 = new AggregateBuilder();
        aggregateBuilder5.setAggregateName("trade_time");
        aggregateBuilder5.setAggregateType(AggsType.max);

        AggregateBuilder aggregateBuilder6 = new AggregateBuilder();
        aggregateBuilder6.setAggregateName("trade_time");
        aggregateBuilder6.setAggregateType(AggsType.min);

        builders.add(aggregateBuilder3);

        aggregateBuilders1.add(aggregateBuilder2);
        aggregateBuilders1.add(aggregateBuilder4);
        aggregateBuilders1.add(aggregateBuilder5);
        aggregateBuilders1.add(aggregateBuilder6);

        AggregateBuilder aggregate1 = new AggregateBuilder();
        aggregate1.setAggregateName("lend_mark");
        aggregate1.setAggregateType(AggsType.terms);
        List<AggregateBuilder> builders1 = new ArrayList<>();
        aggregate1.setSubAggregations(builders);

        AggregateBuilder aggregate2 = new AggregateBuilder();
        aggregate2.setAggregateName("trade_amount");
        aggregate2.setAggregateType(AggsType.sum);

        AggregateBuilder aggregate3 = new AggregateBuilder();
        aggregate3.setAggregateName("trade_amount");
        aggregate3.setAggregateType(AggsType.sum);

        AggregateBuilder aggregate4 = new AggregateBuilder();
        aggregate4.setAggregateName("trade_time");
        aggregate4.setAggregateType(AggsType.max);

        AggregateBuilder aggregate5 = new AggregateBuilder();
        aggregate5.setAggregateName("trade_time");
        aggregate5.setAggregateType(AggsType.min);

        builders1.add(aggregate3);


        aggregateBuilder1.setSubAggregations(aggregateBuilders1);
        aggregateBuilders.add(aggregateBuilder1);
        aggregateBuilders.add(aggregate1);
        aggregateBuilders.add(aggregate2);
        aggregateBuilders.add(aggregate4);
        aggregateBuilders.add(aggregate5);

        aggregateBuilder.setSubAggregations(aggregateBuilders);

        Map map = entranceRepository.multilayerAggs(aggregateBuilder, StandardBankTransactionFlow.class);
        System.out.println("********************");

    }


}
