package com.zqykj.analysis.controller;

import com.zqykj.app.service.IStandardBankTransactionFlowService;
import com.zqykj.app.service.impl.StandardBankTransactionFlowServiceImpl;
import com.zqykj.domain.bank.StandardBankTransactionFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName StandardBankTransactionFlowController
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/14 14:04
 */
@RestController
public class StandardBankTransactionFlowController {

//    @Autowired
//    private ElasticsearchTemplate<IStandardBankTransactionFlowService,String> elasticsearchTemplate;

//    @Autowired
//    private TangDynastyService tangDynastyService;

//    @Autowired
//    private IAggregateOperate<IStandardBankTransactionFlowService> iAggregateOperate;
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
//        double sum = elasticsearchTemplate.aggs("trade_balance", AggsType.sum,null, IStandardBankTransactionFlowService.class);
//        double count = elasticsearchTemplate.aggs("trade_balance", AggsType.count,null,IStandardBankTransactionFlowService.class);
//        double avg = elasticsearchTemplate.aggs("trade_balance", AggsType.avg,null,IStandardBankTransactionFlowService.class);
//        double min = elasticsearchTemplate.aggs("trade_balance", AggsType.min,null,IStandardBankTransactionFlowService.class);
//        double max = elasticsearchTemplate.aggs("trade_balance", AggsType.max,null,IStandardBankTransactionFlowService.class);
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
//        PageList<IStandardBankTransactionFlowService> pageList = new PageList<>();
//        pageList = elasticsearchTemplate.search(QueryBuilders.matchQuery("case_id","7f071cdf-9197-479f-95a9-9ae46045cca9"), pageSortHighLight, IStandardBankTransactionFlowService.class);
//        pageList.getList().forEach(main2 -> System.out.println(main2));
//        return "success";
//    }

    private final IStandardBankTransactionFlowService iStandardBankTransactionFlowService;

    @Autowired
    public StandardBankTransactionFlowController(StandardBankTransactionFlowServiceImpl standardBankTransactionFlowService){
        this.iStandardBankTransactionFlowService =standardBankTransactionFlowService;
    }

    @RequestMapping(value = "/print")
    public String print() throws Exception {
//

        List<StandardBankTransactionFlow> result = iStandardBankTransactionFlowService.standardBankTransactionFlowList();
        return "success";
    }







}
