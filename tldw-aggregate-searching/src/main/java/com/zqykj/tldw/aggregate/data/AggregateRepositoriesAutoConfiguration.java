/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data;

import com.zqykj.tldw.aggregate.repository.TestRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <h1> 基于ImportBeanDefinitionRegistrar 和 FactoryBean 动态注册bean 并动态生成聚合查询接口的代理 </h1>
 *
 * @author Mcj
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(TestRepository.class)
@ConditionalOnMissingBean(AggregateRepositoryFactoryBean.class)
@Import(AggregateRepositoriesRegister.class)
public class AggregateRepositoriesAutoConfiguration {
    
}
