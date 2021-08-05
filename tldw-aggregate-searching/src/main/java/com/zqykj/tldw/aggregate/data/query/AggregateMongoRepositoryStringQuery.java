/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.mongodb.client.MongoClient;
import com.zqykj.tldw.aggregate.data.support.AggregateRepositoryInformation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

@Setter
@Getter
public class AggregateMongoRepositoryStringQuery extends AbstractAggregateRepositoryQuery
        implements AggregateRepositoryQuery {

    private final MongoClient mongoClient;
    private final AggregateRepositoryInformation repositoryInformation;

    public AggregateMongoRepositoryStringQuery(MongoClient mongoClient, @Nullable AggregateRepositoryInformation repositoryInformation) {
        this.repositoryInformation = repositoryInformation;
        Assert.notNull(mongoClient, "Mongodb client cannot be empty!");
        this.mongoClient = mongoClient;
    }


    @Override
    public Object execute(Object[] parameters) {
        //TODO
        return null;
    }

    @Override
    public Method getQueryMethod() {
        return super.getMethod();
    }
}
