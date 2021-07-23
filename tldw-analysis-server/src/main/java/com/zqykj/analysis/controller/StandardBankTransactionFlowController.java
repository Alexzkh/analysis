package com.zqykj.analysis.controller;

import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.app.service.AggregateOperateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName StandardBankTransactionFlowController
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/14 14:04
 */
@RestController
public class StandardBankTransactionFlowController {

//    @Autowired
//    private ElasticsearchTemplate<StandardBankTransactionFlow,String> elasticsearchTemplate;

//    @Autowired
//    private TangDynastyService tangDynastyService;

//    @Autowired
//    private IAggregateOperate<StandardBankTransactionFlow> iAggregateOperate;
//
//    @Autowired
//    private AggregateOperateService aggregateOperateService;
//
//
//    @GetMapping(value = "/count")
//    public ServerResponse count() throws Exception {
//
////        log.info("the total count is "+aggregateOperateService.countAPI().getData());
//        ServerResponse serverResponse = aggregateOperateService.countAPI();
//        return   serverResponse;
//    }

//    @RequestMapping(value = "/print")
//    public String print() throws Exception {
////
//        String result = generateAggregateService.print();
//        log.info("************"+result);
//        return "success";
//    }
//
//
//
//    @RequestMapping(value = "/aggs")
//    public String aggs() throws Exception {
//
//        double sum = elasticsearchTemplate.aggs("trade_balance", AggsType.sum,null, StandardBankTransactionFlow.class);
//        double count = elasticsearchTemplate.aggs("trade_balance", AggsType.count,null,StandardBankTransactionFlow.class);
//        double avg = elasticsearchTemplate.aggs("trade_balance", AggsType.avg,null,StandardBankTransactionFlow.class);
//        double min = elasticsearchTemplate.aggs("trade_balance", AggsType.min,null,StandardBankTransactionFlow.class);
//        double max = elasticsearchTemplate.aggs("trade_balance", AggsType.max,null,StandardBankTransactionFlow.class);
//
//
//        System.out.println("sum===="+sum);
//        System.out.println("count===="+count);
//        System.out.println("avg===="+avg);
//        System.out.println("min===="+min);
//        System.out.println("max===="+max);
//
//
//        return "success";
//    }
//
//
//    @RequestMapping(value = "/search/page")
//    public String searchByPage() throws Exception {
//
//        int currentPage = 1;
//        int pageSize =20 ;
//        PageSortHighLight pageSortHighLight = new PageSortHighLight(currentPage,pageSize);
//        String sorter ="trade_amount";
//        Sort.Order order = new Sort.Order(SortOrder.ASC,sorter);
//        pageSortHighLight.setSort(new Sort(order));
//        //定制高亮，如果定制了高亮，返回结果会自动替换字段值为高亮内容
//        pageSortHighLight.setHighLight(new HighLight().field("case_id"));
//        //可以单独定义高亮的格式
//        PageList<StandardBankTransactionFlow> pageList = new PageList<>();
//        pageList = elasticsearchTemplate.search(QueryBuilders.matchQuery("case_id","7f071cdf-9197-479f-95a9-9ae46045cca9"), pageSortHighLight, StandardBankTransactionFlow.class);
//        pageList.getList().forEach(main2 -> System.out.println(main2));
//        return "success";
//    }







}
