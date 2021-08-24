/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.searching.mongoclientrhl.impl;

import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
import com.zqykj.tldw.aggregate.index.mongodb.associate.MongodbIndexOperations;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoOperations;
import com.zqykj.tldw.aggregate.searching.mongoclientrhl.MongoRestTemplate;

import java.util.Optional;

/**
 * <h1>  MongoOperations 子接口 默认实现类 </h1>
 * TODO Mongodb 实现 等待后续Es 功能全部实现
 */
public class SimpleMongoOperations<T, ID> implements MongoOperations<T, ID> {


    private final MongodbIndexOperations indexOperations;
    private final RepositoryInformation information;
    private final Class<T> entityClass;
    private final MongoRestTemplate mongoRestTemplate;


    @SuppressWarnings("unchecked")
    public SimpleMongoOperations(RepositoryInformation information,
                                 MongoRestTemplate mongoRestTemplate) {
        this.mongoRestTemplate = mongoRestTemplate;
        this.information = information;
        this.entityClass = (Class<T>) information.getDomainType();
        // 获取一个索引操作类
        this.indexOperations = mongoRestTemplate.indexOps(entityClass);
        // 自动构建索引
        if (mongoRestTemplate.getMappingContext().isAutoIndexCreation()) {
            indexOperations.createIndex();
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        return null;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty();
    }

    @Override
    public boolean deleteById(ID id) {
        return false;
    }

    @Override
    public boolean updateByID(ID id, String name) {
        return false;
    }
}
