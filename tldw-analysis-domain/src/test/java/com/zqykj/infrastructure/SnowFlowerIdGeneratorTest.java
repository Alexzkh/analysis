package com.zqykj.infrastructure;

import com.zqykj.infrastructure.id.SnowFlowerIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;

/**
 * @ClassName SnowFlowerIdGeneratorTest
 * @Description TODO
 * @Author zhangkehou
 * @Date 2021/7/28 15:05
 */
@SpringBootTest
public class SnowFlowerIdGeneratorTest {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test_idGenerator() {
//        EnvUtil.setEnvironment(new StandardEnvironment());
        SnowFlowerIdGenerator generator1 = new SnowFlowerIdGenerator();
        SnowFlowerIdGenerator generator2 = new SnowFlowerIdGenerator();
        SnowFlowerIdGenerator generator3 = new SnowFlowerIdGenerator();
        SnowFlowerIdGenerator generator4 = new SnowFlowerIdGenerator();
        SnowFlowerIdGenerator generator5 = new SnowFlowerIdGenerator();
        SnowFlowerIdGenerator generator6 = new SnowFlowerIdGenerator();

        generator1.initialize(1);
        generator2.initialize(2);
        generator3.initialize(3);
        generator4.initialize(4);
        generator5.initialize(5);
        generator6.initialize(6);

        long id1 = generator1.nextId();
        long id2 = generator2.nextId();
        long id3 = generator3.nextId();
        long id4 = generator4.nextId();
        long id5 = generator5.nextId();
        long id6 = generator6.nextId();

        Assert.assertNotEquals(id1, id2);
        Assert.assertNotEquals(id1, id3);
        Assert.assertNotEquals(id2, id3);

    }
}
