/**
 * @作者 Mcj
 */
package com.zqykj;

import com.zqykj.app.service.dao.AliasPojoDao;
import com.zqykj.app.service.dao.TeacherInfoDao;
import com.zqykj.domain.aggregate.AliasPojo;
import com.zqykj.domain.aggregate.TeacherInfo;
import com.zqykj.domain.graph.EntityGraph;
import com.zqykj.domain.graph.LinkGraph;
import com.zqykj.domain.page.Page;
import com.zqykj.domain.page.PageRequest;
import com.zqykj.domain.page.Sort;
import com.zqykj.domain.routing.Routing;
import com.zqykj.infrastructure.id.SnowFlowerIdGenerator;
import com.zqykj.infrastructure.util.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AggregateDataTest {

    @Autowired
    private TeacherInfoDao teacherInfoDao;

    @Autowired
    private AliasPojoDao aliasPojoDao;

    @Test
    public void test() {
        Page<TeacherInfo> infoPage = teacherInfoDao.matchAll(PageRequest.of(0, 2,
                Sort.by(new Sort.Order(Sort.Direction.DESC, "age"))));
        System.out.println(infoPage.getAggregations());
        System.out.println(JacksonUtils.toJson(infoPage.getContent()));
        System.out.println(infoPage.getMaxScore());
        System.out.println(JacksonUtils.toJson(infoPage.getPageable()));
        System.out.println(infoPage.getTotalElements());
        System.out.println(infoPage.getTotalPages());
        System.out.println(JacksonUtils.toJson(infoPage.getSort()));
    }


    @Test
    public void testOptionalQuery() {
        Optional<List<TeacherInfo>> teacherInfos = teacherInfoDao.matchAllofOptional();
        System.out.println(JacksonUtils.toJson(teacherInfos));
    }

    @Test
    public void testListQuery() {
        Page<TeacherInfo> teacherInfos = teacherInfoDao.matchAllofList(PageRequest.of(0, 1, Sort.Direction.DESC, "age"));
        System.out.println(JacksonUtils.toJson(teacherInfos.getContent()));
    }

    @Test
    public void testGenerator() {
        SnowFlowerIdGenerator snowFlowerIdGenerator = new SnowFlowerIdGenerator();
        System.out.println(snowFlowerIdGenerator.currentId());
        System.out.println(snowFlowerIdGenerator.nextId());
    }

    @Test
    public void batchInsertAliasPojo() {

        AliasPojo aliasPojo = new AliasPojo();
        aliasPojo.setId("1");
        aliasPojo.setName("alias_name");
        aliasPojo.setDescribe(new TeacherInfo("1", "22", 1, "job", 1, new BigDecimal("2.66"), new Routing("44")));
        aliasPojo.setDate(new Date());
        aliasPojo.setRouting(new Routing("1"));


        AliasPojo aliasPojo2 = new AliasPojo();
        aliasPojo2.setId("2");
        aliasPojo2.setName("alias_name");
        aliasPojo2.setDescribe(new TeacherInfo("1", "22", 1, "job", 1, new BigDecimal("2.66"), new Routing("44")));
        aliasPojo2.setDate(new Date());
        aliasPojo2.setRouting(new Routing("2"));


        AliasPojo aliasPojo3 = new AliasPojo();
        aliasPojo3.setId("3");
        aliasPojo3.setName("alias_name");
        aliasPojo3.setDescribe(new TeacherInfo("1", "22", 1, "job", 1, new BigDecimal("2.66"), new Routing("44")));
        aliasPojo3.setDate(new Date());
        aliasPojo3.setRouting(new Routing("3"));


        AliasPojo aliasPojo4 = new AliasPojo();
        aliasPojo4.setId("4");
        aliasPojo4.setName("alias_name");
        aliasPojo4.setDescribe(new TeacherInfo("1", "22", 1, "job", 1, new BigDecimal("2.66"), new Routing("44")));
        aliasPojo4.setDate(new Date());
        aliasPojo4.setRouting(new Routing("4"));


        AliasPojo aliasPojo5 = new AliasPojo();
        aliasPojo5.setId("5");
        aliasPojo5.setName("alias_name");
        aliasPojo5.setDescribe(new TeacherInfo("1", "22", 1, "job", 1, new BigDecimal("2.66"), new Routing("44")));
        aliasPojo5.setDate(new Date());
        aliasPojo5.setRouting(new Routing("1"));

        List<AliasPojo> aliasPojoList = new ArrayList<>();
        aliasPojoList.add(aliasPojo);
        aliasPojoList.add(aliasPojo2);
        aliasPojoList.add(aliasPojo3);
        aliasPojoList.add(aliasPojo4);
        aliasPojoList.add(aliasPojo5);

        aliasPojoDao.saveAll(aliasPojoList);
    }

    @Test
    public void testAliasFind() throws Exception {

        Optional<AliasPojo> aliasPojo = aliasPojoDao.findById("1", "1");
        System.out.println(JacksonUtils.toJson(aliasPojo.orElse(new AliasPojo())));
        System.out.println(JacksonUtils.toJson(aliasPojo.orElse(new AliasPojo()).getRouting()));
    }

    @Test
    public void queryByFind() {

        Optional<AliasPojo> aliasPojo = aliasPojoDao.findIdByRouting("id", "1", new Routing("2"));
        System.out.println(JacksonUtils.toJson(aliasPojo.orElse(new AliasPojo())));
    }

    @Test
    public void testMultiField() {

        AliasPojo aliasPojo = new AliasPojo();

        aliasPojo.setId("1");
        aliasPojo.setRouting(new Routing("1"));
        aliasPojo.setDate(new Date());
        aliasPojo.setDescribe(new TeacherInfo());
        aliasPojo.setName("12312");

        EntityGraph entity = new EntityGraph();
        entity.setId(1223213232L);
        entity.setType("card");
        EntityGraph entity1 = new EntityGraph();
        entity1.setId(2343242342L);
        entity1.setType("card");
        List<EntityGraph> entityGraphs = new ArrayList<>();
        entityGraphs.add(entity);
        entityGraphs.add(entity1);
        aliasPojo.setEntityGraphs(entityGraphs);

        LinkGraph linkGraph = new LinkGraph();
        linkGraph.setId(1223213232L);
        linkGraph.setType("trade_card");
        LinkGraph linkGraph1 = new LinkGraph();
        linkGraph1.setId(2343242342L);
        linkGraph1.setType("trade_card");
        List<LinkGraph> linkGraphs = new ArrayList<>();
        linkGraphs.add(linkGraph);
        linkGraphs.add(linkGraph1);

        aliasPojo.setLinkGraphs(linkGraphs);

        aliasPojoDao.save(aliasPojo);
    }


    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Entity {
        private String type;
        private String id;
    }

    @Test
    public void findMultiField() throws Exception {

        Optional<AliasPojo> byId = aliasPojoDao.findById("1", "1");
        System.out.println(JacksonUtils.toJson(byId.orElse(new AliasPojo())));
    }
}
