/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.repository.Repository;
import com.zqykj.repository.core.support.RepositoryFactoryBeanSupport;
import com.zqykj.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import com.zqykj.annotations.NoRepositoryBean;

/**
 * <h2>
 * 所有数据存储 Repository 最终 都会被包装成ElasticsearchRepositoryFactoryBean 交由spring 管理
 * 除了标注此注解的Repository {@link NoRepositoryBean} 不会注册到spring
 * <p>
 * 当需要注入 Repository 时 , 因为 RepositoryFactoryBeanSupport 实现了 FactoryBean.
 * 会自动调用 方法 getObject(), 注入真正的 bean,
 * 方法 getObjectType() 代表的是当前 接口 Repository 类型
 * </h2>
 */
public class ElasticsearchRepositoryFactoryBean<T extends Repository> extends RepositoryFactoryBeanSupport<T> {

    @Nullable
    private ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * <h2> Creates a new {@link ElasticsearchRepositoryFactoryBean} for the given repository interface. </h2>
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ElasticsearchRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * <h2> Configures the {@link ElasticsearchRestTemplate} to be used to create Elasticsearch repositories. </h2>
     *
     * @param elasticsearchTemplate the operations to set
     */
    public void setElasticsearchTemplate(ElasticsearchRestTemplate elasticsearchTemplate) {

        Assert.notNull(elasticsearchTemplate, "ElasticsearchOperations must not be null!");

        setMappingContext(elasticsearchTemplate.getElasticsearchConverter().getMappingContext());
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(elasticsearchTemplate, "ElasticsearchOperations must be configured!");
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {

        Assert.notNull(elasticsearchTemplate, "operations are not initialized");

        return new ElasticsearchRepositoryFactory(elasticsearchTemplate);
    }
}
