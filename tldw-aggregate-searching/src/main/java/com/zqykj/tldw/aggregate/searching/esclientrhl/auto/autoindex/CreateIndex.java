package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.EnableESTools;
import com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndex;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * <p>
 * It used to scan the annotations {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData} and automatically create index mapping.
 * <p>
 * <p>
 * Call at startup, but {@link com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex.ESIndexProcessor} is required
 * if spring needs to know which beans are configured with {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData} annotations
 * <p>
 * The {@link org.springframework.core.annotation.Order} default is the lowest priority. The lower the value, the higher the priority.
 **/
//@Configuration
@Order(1)

public class CreateIndex implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ElasticsearchIndex elasticsearchIndex;

    private ApplicationContext applicationContext;

    /**
     * scan the annotations {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData} and automatically create index mapping.
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        Map<String, Object> beansWithAnnotationMap = this.applicationContext.getBeansWithAnnotation(ESMetaData.class);
        logger.info("Scan the @ESMetaData annotation beans number ：{}", beansWithAnnotationMap.size());
        beansWithAnnotationMap.forEach((beanName, bean) ->
                {
                    try {
                        MetaData metaData = elasticsearchIndex.getMetaData(bean.getClass());
                        // automatically create index mapping configuration.
                        if (metaData.isAutoCreateIndex()) {
                            // when the alias is configured, the automatic index creation function will be disabled.
                            if (metaData.isAlias()) {
                                elasticsearchIndex.createAlias(bean.getClass());
                            } else if (!elasticsearchIndex.exists(bean.getClass())) {
                                elasticsearchIndex.createIndex(bean.getClass());
                                if (EnableESTools.isPrintregmsg()) {
                                    logger.info("Create Index succeed and index name is ：" + metaData.getIndexname());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Create index error {}", e);
                    }
                }
        );
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
