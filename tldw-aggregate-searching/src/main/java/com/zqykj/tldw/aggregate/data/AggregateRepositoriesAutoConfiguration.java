/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data;

import com.zqykj.tldw.aggregate.BaseOperations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <h1> 基于ImportBeanDefinitionRegistrar 和 FactoryBean 动态注册 {@link BaseOperations}, 动态代理各数据源顶级接口 </h1>
 * <p>
 * beanName: {@link BaseOperations} 子接口, BeanDefinition: {@link AggregateRepositoryFactoryBean}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(BaseOperations.class)
@ConditionalOnMissingBean(AggregateRepositoryFactoryBean.class)
@Import(AggregateRepositoriesRegister.class)
public class AggregateRepositoriesAutoConfiguration {

}
