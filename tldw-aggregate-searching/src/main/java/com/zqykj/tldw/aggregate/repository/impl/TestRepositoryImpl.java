/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.repository.impl;

import com.zqykj.tldw.aggregate.repository.TestRepository;

public class TestRepositoryImpl implements TestRepository {

    @Override
    public String getName(String name) {
        return "小明";
    }
}
