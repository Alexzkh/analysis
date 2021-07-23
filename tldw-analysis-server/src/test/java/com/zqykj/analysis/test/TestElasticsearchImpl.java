package com.zqykj.analysis.test;


import com.zqykj.domain.bank.StandardBankTransactionFlow;
import com.zqykj.tldw.aggregate.searching.esclientrhl.repository.*;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TestElasticsearchImpl
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/6 19:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestElasticsearchImpl {

    @Autowired
    private ElasticsearchTemplate<StandardBankTransactionFlow,String> elasticsearchTemplate;


    @Test
    public void contextLoad(){

    }

    @Test
    public void testRequest() throws Exception {
        Request request = new Request("GET","/standard_bank_transaction_flow");
        request.setEntity(new NStringEntity(
                "{\"query\":{\"match_all\":{\"boost\":1.0}}}",
                ContentType.APPLICATION_JSON));
        Response response = elasticsearchTemplate.request(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
    }

    @Test
    public void testSave() throws Exception {
        StandardBankTransactionFlow standardBankTransactionFlow = new StandardBankTransactionFlow();
        standardBankTransactionFlow.setCase_id("7f071cdf-9197-479f-95a9-9ae46045cca9");
        standardBankTransactionFlow.setAccount_card("801382188800035754");
        standardBankTransactionFlow.setAccount_query("com00000002763");
        standardBankTransactionFlow.setBank("中国南京交通银行");
        elasticsearchTemplate.save(standardBankTransactionFlow);
    }

    @Test
    public void testSaveByRouting() throws Exception {
        StandardBankTransactionFlow standardBankTransactionFlow = new StandardBankTransactionFlow();
        standardBankTransactionFlow.setCase_id("7f071cdf-9197-479f-95a9-9ae46045cca5");
        standardBankTransactionFlow.setAccount_card("80138218880003575411");
        standardBankTransactionFlow.setAccount_query("com0000000276311");
        standardBankTransactionFlow.setBank("中国南京交通银行111");
        elasticsearchTemplate.save(standardBankTransactionFlow,"7f071cdf-9197-479f-95a9-9ae46045cca5");
    }

    @Test
    public void testSaveByIndexList() throws Exception {

        List<StandardBankTransactionFlow> standardBankTransactionFlowList = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            StandardBankTransactionFlow standardBankTransactionFlow = new StandardBankTransactionFlow();
            standardBankTransactionFlow.setCase_id("7f071cdf-9197-479f-95a9-9ae46045cca5");
            standardBankTransactionFlow.setAccount_card("80138218880003575411"+i);
            standardBankTransactionFlow.setAccount_query("com0000000276311"+i);
            standardBankTransactionFlow.setBank("中国南京交通银行111"+i);
            standardBankTransactionFlowList.add(standardBankTransactionFlow);
        }

        elasticsearchTemplate.save(standardBankTransactionFlowList);
    }

    /**
     * 更新操作必须要有ES主键id。
     * @return: void
     **/
    @Test
    public void testUpdate() throws Exception{
        StandardBankTransactionFlow standardBankTransactionFlow = new StandardBankTransactionFlow();
        standardBankTransactionFlow.setCase_id("7f071cdf-9197-479f-95a9-9ae46045cca88");
        standardBankTransactionFlow.setAccount_card("8013821888000357541188");
        standardBankTransactionFlow.setAccount_query("com0000000276311");
        standardBankTransactionFlow.setBank("中国南京交通银行888");
        elasticsearchTemplate.update(standardBankTransactionFlow);
    }

    /**
     * 删除操作
     * @return: void
     **/
    @Test
    public void testDelete () throws Exception {

        StandardBankTransactionFlow standardBankTransactionFlow = new StandardBankTransactionFlow();
        // 领域内相同主键id删除的一致性不能保证

        
    }

    @Test
    public void searchByPageTest() throws Exception {
        int currentPage = 1;
        int pageSize =20 ;
        PageSortHighLight pageSortHighLight = new PageSortHighLight(currentPage,pageSize);
        String sorter ="trade_amount";
        Sort.Order order = new Sort.Order(SortOrder.ASC,sorter);
        pageSortHighLight.setSort(new Sort(order));
        //定制高亮，如果定制了高亮，返回结果会自动替换字段值为高亮内容
        pageSortHighLight.setHighLight(new HighLight().field("case_id"));
        //可以单独定义高亮的格式
        //new HighLight().setPreTag("<em>");
        //new HighLight().setPostTag("</em>");
        PageList<StandardBankTransactionFlow> pageList = new PageList<>();
        pageList = elasticsearchTemplate.search(QueryBuilders.matchQuery("case_id","7f071cdf-9197-479f-95a9-9ae46045cca9"), pageSortHighLight, StandardBankTransactionFlow.class);
        pageList.getList().forEach(main2 -> System.out.println(main2));
    }

    @Test
    public void testHighLightResult() throws Exception {

    }



}
