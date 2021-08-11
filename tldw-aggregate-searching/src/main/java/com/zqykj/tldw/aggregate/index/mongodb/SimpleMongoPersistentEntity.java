/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb;

import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.annotations.Document;
import com.zqykj.annotations.Sharded;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.associate.ShardKey;
import com.zqykj.tldw.aggregate.index.mongodb.associate.ShardingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Setter
@Getter
public class SimpleMongoPersistentEntity<T> extends BasicPersistentEntity<T, SimpleMongodbPersistentProperty> {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private final String collection;
    private final String language;
    private final ShardKey shardKey;

    public SimpleMongoPersistentEntity(TypeInformation<T> typeInformation) {

        super(typeInformation, null);

        Class<?> rawType = typeInformation.getType();
        String fallback = StringUtils.uncapitalize(rawType.getSimpleName());

        if (this.isAnnotationPresent(Document.class)) {
            Document document = this.getRequiredAnnotation(Document.class);
            this.collection = StringUtils.hasText(document.indexName()) ? document.indexName() : fallback;
            this.language = StringUtils.hasText(document.language()) ? document.language() : "";
        } else {

            this.collection = fallback;
            this.language = "";
        }
        this.shardKey = detectShardKey();
    }

    /**
     * <h2> 检测分片 </h2>
     */
    private ShardKey detectShardKey() {

        if (!isAnnotationPresent(Sharded.class)) {
            return ShardKey.none();
        }

        Sharded sharded = getRequiredAnnotation(Sharded.class);

        String[] keyProperties = sharded.shardKey();
        if (ObjectUtils.isEmpty(keyProperties)) {
            keyProperties = new String[]{"_id"};
        }

        ShardKey shardKey = ShardingStrategy.HASH.equals(sharded.shardingStrategy()) ? ShardKey.hash(keyProperties)
                : ShardKey.range(keyProperties);

        return sharded.immutableKey() ? ShardKey.immutable(shardKey) : shardKey;
    }

    public ShardKey getShardKey() {
        return shardKey;
    }

    public boolean isSharded() {
        return getShardKey().isSharded();
    }

    public String getCollection() {

        return this.collection;
    }

    public String getLanguage() {
        return this.language;
    }

}
