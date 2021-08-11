/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zqykj.infrastructure;

import com.zqykj.infrastructure.serialize.SerializeFactory;
import com.zqykj.infrastructure.serialize.Serializer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@SpringBootTest
public class SerializeFactoryTest {

    @Test
    public void contextLoads() {
    }

    @Test
    public void testListSerialize() throws Exception {
        byte[] data = new byte[0];
        //Serializer serializer = SerializeFactory.getDefault();
        long currentTimeMills = System.currentTimeMillis();

        Serializer serializer = SerializeFactory.getSerializer("JSON");

        List<Integer> logsList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            logsList.add(i);
        }

        data = serializer.serialize(logsList);
        Assert.assertNotEquals(0, data.length);

        ArrayList<Integer> list = serializer.deserialize(data, ArrayList.class);
        System.out.println(list);
    }

    @Test
    public void testMapSerialize() {
        byte[] data = new byte[0];
        Serializer serializer = SerializeFactory.getDefault();
        Map<Integer, Integer> logsMap = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            logsMap.put(i, i);
        }
        data = serializer.serialize(logsMap);
        Assert.assertNotEquals(0, data.length);
        Map<Integer, Integer> result = serializer.deserialize(data, HashMap.class);
        System.out.println(result);
    }

    @Test
    public void testSetSerialize() {
        byte[] data = new byte[0];
        Serializer serializer = SerializeFactory.getDefault();
        Set<Integer> logsMap = new CopyOnWriteArraySet<>();
        for (int i = 0; i < 4; i++) {
            logsMap.add(i);
        }

        data = serializer.serialize(logsMap);
        Assert.assertNotEquals(0, data.length);
        Set<Integer> result = serializer.deserialize(data, HashSet.class);
        System.out.println(result);
    }

}
