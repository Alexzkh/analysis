/**
 * @作者 Mcj
 */
package com.zqykj;


import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.util.JacksonUtils;
import com.zqykj.util.WebApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Slf4j
public class OriginEsOperationTest {

    @Autowired
    private ApplicationContext applicationContext;


    private EntranceRepository entranceRepository = WebApplicationContext.getBean(EntranceRepository.class);


    @Test
    public void testTeacherById() throws Exception {

        EntranceRepository entranceRepository = applicationContext.getBean(EntranceRepository.class);
        Optional<TeacherInfo> teacherInfo = entranceRepository.findById("110", "22", TeacherInfo.class);
        log.info(JacksonUtils.toJson(teacherInfo.orElse(null)));
    }

    @Test
    public void testQueryAnn() {

        TeacherInfoDao teacherInfoDao = applicationContext.getBean(TeacherInfoDao.class);
        Page<TeacherInfo> teacherInfo = teacherInfoDao.matchAll(new PageRequest(0, 20, Sort.unsorted()),
                new EntityClass(TeacherInfo.class));
        log.info(JacksonUtils.toJson(teacherInfo.getContent()));
    }

    @Test
    public void testSave() {
        TeacherInfo teacherInfo = new TeacherInfo();
        teacherInfo.setAge(1);
        teacherInfo.setId("1");
        teacherInfo.setJob("test job");
        teacherInfo.setName("test name");
        teacherInfo.setSalary(new BigDecimal("1.00"));
        teacherInfo.setSex(1);
        entranceRepository.save(teacherInfo, "82c3e52e-019b-4d02-a4a3-e4fecc7f347b", TeacherInfo.class);
    }

    @Test
    public void testSaveAll() {
        List<TeacherInfo> teacherInfos = new ArrayList<>();
        for (int i = 2; i < 30; i++) {
            TeacherInfo teacherInfo = new TeacherInfo();
            teacherInfo.setAge(i);
            teacherInfo.setId("1" + i);
            teacherInfo.setJob("test job " + i);
            teacherInfo.setName("test name " + i);
            teacherInfo.setSalary(new BigDecimal("1.00"));
            teacherInfo.setSex(i);
            teacherInfos.add(teacherInfo);
        }
        entranceRepository.saveAll(teacherInfos, "61e9e22a-a6b1-4838-8cea-df8995bc2d8c", TeacherInfo.class);
    }


    @Test
    public void testSaveTransactionFlow() {
        List<BankTransactionFlow> bankTransactionFlows = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            BankTransactionFlow bankTransactionFlow = new BankTransactionFlow();
            bankTransactionFlow.setId((long) i);
            bankTransactionFlow.setCaseId("61e9e22a-a6b1-4838-8cea-df8995bc2d8c" + i);
            bankTransactionFlow.setResourceId("a0e16cb6b48f4516aa200fca3218574c" + i);
            bankTransactionFlow.setResourceKeyId(i + "");
            bankTransactionFlow.setBank("中国银行");
            bankTransactionFlow.setCustomerName("客户" + i);
            bankTransactionFlow.setQueryAccount("320123" + i);
            bankTransactionFlow.setQueryCard("728834032432" + i);
            bankTransactionFlow.setTransactionOppositeName("对方客户" + i);
            bankTransactionFlow.setTransactionOppositeCertificateNumber("7772343" + i);
            bankTransactionFlow.setTransactionOppositeAccount("7772343" + i);
            bankTransactionFlow.setTransactionOppositeCard("4843242" + i);
            bankTransactionFlow.setTransactionType("1" + i);
            bankTransactionFlow.setLoanFlag("进");
            bankTransactionFlow.setCurrency("CNY");
            bankTransactionFlow.setTransactionMoney(2.33 + i);
            bankTransactionFlow.setTransactionBalance(1.11 + i);
            bankTransactionFlow.setTransactionOppositeAccountOpenBank("建设银行");
            bankTransactionFlow.setTransactionSummary("test");
            bankTransactionFlow.setTransactionChannel("test");
            bankTransactionFlow.setTransactionNetworkName("test");
            bankTransactionFlow.setTransactionNetworkCode("1101");
            bankTransactionFlow.setLogNumber("1");
            bankTransactionFlow.setCertificateType("身份证");
            bankTransactionFlow.setCertificateNumber("11111");
            bankTransactionFlow.setCashFlag("111");
            bankTransactionFlow.setTerminalNumber("1111");
            bankTransactionFlow.setTransactionSuccessFlag("1");
            bankTransactionFlow.setTransactionPlace("地点");
            bankTransactionFlow.setMerchantNumber("34343");
            bankTransactionFlow.setIpAddress("127.0.0.1");
            bankTransactionFlow.setMacAddress("223232::232::11");
            bankTransactionFlow.setTransactionTellerNumber("1232131");
            bankTransactionFlow.setNote("备注");
//            bankTransactionFlow.setTradingTime(new Date());
            bankTransactionFlow.setDataSchemaId("21321dataSchemaId");
            EntityGraph entityGraph = new EntityGraph(132132131L, "bank_card");
            EntityGraph entityGraph1 = new EntityGraph(2222132132131L, "bank_card");
            List<EntityGraph> entityGraphs = new ArrayList<>();
            entityGraphs.add(entityGraph);
            entityGraphs.add(entityGraph1);
            LinkGraph linkGraph = new LinkGraph(2132131221324324L, "trade_bank_card");
            LinkGraph linkGraph1 = new LinkGraph(33332132131224L, "trade_bank_card");
            List<LinkGraph> linkGraphs = new ArrayList<>();
            linkGraphs.add(linkGraph);
            linkGraphs.add(linkGraph1);
            bankTransactionFlow.setEntityGraphs(entityGraphs);
            bankTransactionFlow.setLinkGraphs(linkGraphs);
            bankTransactionFlows.add(bankTransactionFlow);
        }
        entranceRepository.save(bankTransactionFlows.get(0), "61e9e22a-a6b1-4838-8cea-df8995bc2d8c", BankTransactionFlow.class);
    }

    @Test
    public void testSaveTransactionFlowAll() {
        StopWatch started = new StopWatch();
        started.start();
        List<BankTransactionFlow> bankTransactionFlows = new ArrayList<>();
        for (int i = 10000; i < 18000; i++) {
            BankTransactionFlow bankTransactionFlow = new BankTransactionFlow();
            bankTransactionFlow.setId((long) i);
            bankTransactionFlow.setCaseId("61e9e22a-a6b1-4838-8cea-df8995bc2d8c" + i);
            bankTransactionFlow.setResourceId("a0e16cb6b48f4516aa200fca3218574c" + i);
            bankTransactionFlow.setResourceKeyId(i + "");
            bankTransactionFlow.setBank("中国银行");
            bankTransactionFlow.setCustomerName("客户" + i);
            bankTransactionFlow.setQueryAccount("320123" + i);
            bankTransactionFlow.setQueryCard("728834032432" + i);
            bankTransactionFlow.setTransactionOppositeName("对方客户" + i);
            bankTransactionFlow.setTransactionOppositeCertificateNumber("7772343" + i);
            bankTransactionFlow.setTransactionOppositeAccount("7772343" + i);
            bankTransactionFlow.setTransactionOppositeCard("4843242" + i);
            bankTransactionFlow.setTransactionType("1" + i);
            bankTransactionFlow.setLoanFlag("进");
            bankTransactionFlow.setCurrency("CNY");
            bankTransactionFlow.setTransactionMoney(2.33 + i);
            bankTransactionFlow.setTransactionBalance(1.11 + i);
            bankTransactionFlow.setTransactionOppositeAccountOpenBank("建设银行");
            bankTransactionFlow.setTransactionSummary("test");
            bankTransactionFlow.setTransactionChannel("test");
            bankTransactionFlow.setTransactionNetworkName("test");
            bankTransactionFlow.setTransactionNetworkCode("1101");
            bankTransactionFlow.setLogNumber("1");
            bankTransactionFlow.setCertificateType("身份证");
            bankTransactionFlow.setCertificateNumber("11111");
            bankTransactionFlow.setCashFlag("111");
            bankTransactionFlow.setTerminalNumber("1111");
            bankTransactionFlow.setTransactionSuccessFlag("1");
            bankTransactionFlow.setTransactionPlace("地点");
            bankTransactionFlow.setMerchantNumber("34343");
            bankTransactionFlow.setIpAddress("127.0.0.1");
            bankTransactionFlow.setMacAddress("223232::232::11");
            bankTransactionFlow.setTransactionTellerNumber("1232131");
            bankTransactionFlow.setNote("备注");
            bankTransactionFlow.setTradingTime(new Date());
            bankTransactionFlow.setDataSchemaId("21321dataSchemaId");
            EntityGraph entityGraph = new EntityGraph(132132131L, "bank_card");
            EntityGraph entityGraph1 = new EntityGraph(2222132132131L, "bank_card");
            List<EntityGraph> entityGraphs = new ArrayList<>();
            entityGraphs.add(entityGraph);
            entityGraphs.add(entityGraph1);
            LinkGraph linkGraph = new LinkGraph(2132131221324324L, "trade_bank_card");
            LinkGraph linkGraph1 = new LinkGraph(33332132131224L, "trade_bank_card");
            List<LinkGraph> linkGraphs = new ArrayList<>();
            linkGraphs.add(linkGraph);
            linkGraphs.add(linkGraph1);
            bankTransactionFlow.setEntityGraphs(entityGraphs);
            bankTransactionFlow.setLinkGraphs(linkGraphs);
            bankTransactionFlows.add(bankTransactionFlow);
        }
        entranceRepository.saveAll(bankTransactionFlows, "61e9e22a-a6b1-4838-8cea-df8995bc2d8c", BankTransactionFlow.class);
        started.stop();
        log.info("save 10000 entity cost time = {} ms ", started.getTotalTimeMillis());
    }

}
