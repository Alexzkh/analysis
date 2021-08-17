/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data;

import com.zqykj.infrastructure.util.Lazy;
import com.zqykj.tldw.aggregate.BaseOperations;
import com.zqykj.tldw.aggregate.data.support.AggregateRepositoryFactorySupport;
import com.zqykj.tldw.aggregate.index.elasticsearch.associate.ElasticsearchIndexOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * <h1>实现FactoryBean,动态获取实例(代理类)</h1>
 *
 * @author Mcj
 */
@Slf4j
public class AggregateRepositoryFactoryBean<T extends BaseOperations<S, M>, S, M extends Serializable>
        extends AggregateRepositoryFactorySupport implements FactoryBean<T>, InitializingBean {

    private Class<? extends T> repositoryInterface;

    @Nullable
    private ElasticsearchIndexOperations elasticsearchIndexOperations;

    public void setElasticsearchIndexOperations(ElasticsearchIndexOperations elasticsearchIndexOperations) {
        this.elasticsearchIndexOperations = elasticsearchIndexOperations;
    }

    /**
     * AggregateRepositoryFactoryBean 真正返回的instance
     */
    private Supplier<T> repository;

    public AggregateRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }

    /**
     * <h1>
     * <p>
     * AggregateRepositoryFactoryBean 会被ImportBeanDefinition 处理注入到IOC
     * <p>
     * 当注入TestRepository的时候,会根据getObject 动态注入想要的实例
     * 这里我们通过JDK动态代理 注入代理类
     * </h1>
     */
    @Override
    public T getObject() {
        return repository.get();
    }

    /**
     * <h2> 当前被包装接口的类型 eg. TestRepository</h2>
     */
    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        // 只有调用Lazy.get()才会触发生成代理对象逻辑(延迟加载), Lazy 实现了 Supplier
        this.repository = Lazy.of(() -> this.getRepository(repositoryInterface, elasticsearchIndexOperations));
    }
}
