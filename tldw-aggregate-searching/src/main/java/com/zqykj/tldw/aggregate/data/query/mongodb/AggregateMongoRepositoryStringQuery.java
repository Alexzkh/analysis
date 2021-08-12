/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.mongodb;

import com.mongodb.client.MongoClient;
import com.zqykj.tldw.aggregate.data.query.AbstractAggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.query.AggregateRepositoryQuery;
import com.zqykj.tldw.aggregate.data.repository.RepositoryInformation;
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
    private final RepositoryInformation repositoryInformation;
    private final String query;

    public AggregateMongoRepositoryStringQuery(MongoClient mongoClient,
                                               @Nullable RepositoryInformation repositoryInformation,
                                               Method method,
                                               String query) {
        super(method);
        Assert.notNull(mongoClient, "Mongodb client cannot be empty!");
        this.repositoryInformation = repositoryInformation;
        this.mongoClient = mongoClient;
        this.query = query;
    }


    @Override
    public Object execute(Object[] parameters) {
        //TODO
        return null;
    }
}
