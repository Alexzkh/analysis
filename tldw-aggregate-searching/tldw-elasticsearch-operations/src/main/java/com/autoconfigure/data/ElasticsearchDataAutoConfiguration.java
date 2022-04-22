/**
 * @author Mcj
 */
package com.autoconfigure.data;

import com.autoconfigure.rest.ElasticsearchRestClientAutoConfiguration;
import com.zqykj.core.ElasticsearchRestTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ElasticsearchRestTemplate.class})
// 指定配置类的初始化顺序(  ElasticsearchDataAutoConfiguration
// 配置类初始化需要在 ElasticsearchRestClientAutoConfiguration之后加载）
@AutoConfigureAfter({ElasticsearchRestClientAutoConfiguration.class})
// 引入一组配置类到当前配置类(相当于把其他配置类的bean
// 都加入到当前ElasticsearchDataAutoConfiguration 配置类中)
@Import({ElasticsearchDataConfiguration.BaseConfiguration.class,
        ElasticsearchDataConfiguration.RestClientConfiguration.class})
public class ElasticsearchDataAutoConfiguration {

}
