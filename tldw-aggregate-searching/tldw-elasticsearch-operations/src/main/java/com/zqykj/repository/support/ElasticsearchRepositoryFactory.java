/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.repository.ElasticsearchRepository;
import com.zqykj.repository.core.NamedQueries;
import com.zqykj.repository.core.RepositoryInformation;
import com.zqykj.repository.core.RepositoryMetadata;
import com.zqykj.repository.core.support.RepositoryFactorySupport;
import com.zqykj.repository.query.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * <h1> Factory to create {@link ElasticsearchRepository} </h1>
 */
public class ElasticsearchRepositoryFactory extends RepositoryFactorySupport {

    private final ElasticsearchRestTemplate elasticsearchOperations;
    private final ElasticsearchEntityInformationCreator entityInformationCreator;

    public ElasticsearchRepositoryFactory(ElasticsearchRestTemplate elasticsearchOperations) {

        Assert.notNull(elasticsearchOperations, "ElasticsearchOperations must not be null!");

        this.elasticsearchOperations = elasticsearchOperations;
        this.entityInformationCreator = new ElasticsearchEntityInformationCreatorImpl(
                elasticsearchOperations.getElasticsearchConverter().getMappingContext());
    }

    @Override
    public <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return entityInformationCreator.getEntityInformation(domainClass);
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation metadata) {
        return getTargetRepositoryViaReflection(metadata, getEntityInformation(metadata.getDomainType()),
                elasticsearchOperations);
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {

        return SimpleElasticsearchRepository.class;
    }


    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key) {
        return Optional.of(new ElasticsearchQueryLookupStrategy());
    }


    /**
     * <h2> Elasticsearch 查询方法 查询查找策略 </h2>
     */
    private class ElasticsearchQueryLookupStrategy implements QueryLookupStrategy {

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
                                            NamedQueries namedQueries) {

            ElasticsearchQueryMethod queryMethod = new ElasticsearchQueryMethod(method, metadata,
                    elasticsearchOperations.getElasticsearchConverter().getMappingContext());
            String namedQueryName = queryMethod.getNamedQueryName();

            if (namedQueries.hasQuery(namedQueryName)) {
                String namedQuery = namedQueries.getQuery(namedQueryName);
                return new ElasticsearchStringQuery(queryMethod, elasticsearchOperations, namedQuery);
            } else if (queryMethod.hasAnnotatedQuery()) {
                // 处理方法 上 标注有 @Query的 查询策略
                return new ElasticsearchStringQuery(queryMethod, elasticsearchOperations, queryMethod.getAnnotatedQuery());
            } else {
                // 默认方法处理
                return new ElasticsearchPartQuery(queryMethod, elasticsearchOperations);
            }
            //  当然也可以像 spring data elasticsearch 那样处理动态投影方法查询 eg. 以 find|read|get|query|search|stream , delete|remove, exists ,count
            //  固定开头,后面跟需要查询的参数名称(索引类中的property)  eg. findByName
            //  具体实现上述的查询策略 见此类 {@link org.springframework.data.elasticsearch.repository.query.ElasticsearchPartQuery} */
        }
    }

    /**
     * <h2> 获取Repository Metadata </h2>
     */
    @Override
    protected RepositoryMetadata getRepositoryMetadata(Class<?> repositoryInterface) {
        return new ElasticsearchRepositoryMetadata(repositoryInterface);
    }
}
