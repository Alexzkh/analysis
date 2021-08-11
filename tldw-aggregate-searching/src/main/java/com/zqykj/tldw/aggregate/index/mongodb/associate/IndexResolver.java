/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongoPersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbPersistentProperty;
import org.springframework.util.Assert;

public interface IndexResolver {

    static IndexResolver create(
            AbstractMappingContext<? extends SimpleMongoPersistentEntity<?>, SimpleMongodbPersistentProperty> mappingContext) {

        Assert.notNull(mappingContext, "MongoMappingContext must not be null!");

        return new MongodbPersistentEntityIndexResolver(mappingContext);
    }

    Iterable<? extends IndexDefinition> resolveIndexFor(TypeInformation<?> typeInformation);
}
