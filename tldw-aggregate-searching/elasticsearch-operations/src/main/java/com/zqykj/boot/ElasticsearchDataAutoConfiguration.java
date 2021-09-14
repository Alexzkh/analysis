/**
 * @author Mcj
 */
package com.zqykj.boot;

import com.zqykj.core.ElasticsearchRestTemplate;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ElasticsearchRestTemplate.class})
@AutoConfigureAfter({ElasticsearchRestClientAutoConfiguration.class})
@Import({ElasticsearchDataConfiguration.BaseConfiguration.class,
        ElasticsearchDataConfiguration.RestClientConfiguration.class})
public class ElasticsearchDataAutoConfiguration {

}
