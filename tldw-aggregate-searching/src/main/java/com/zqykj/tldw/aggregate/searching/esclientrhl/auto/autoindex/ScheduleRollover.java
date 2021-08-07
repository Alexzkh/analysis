package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.autoindex;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.index.ElasticsearchIndex;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 *Rollover at Regular time.
 * @see {https://www.elastic.co/guide/en/elasticsearch/reference/7.9/indices-rollover-index.html}
 **/
//@Configuration
@Order(2)
public class ScheduleRollover implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ElasticsearchIndex elasticsearchIndex;

    private ApplicationContext applicationContext;

    /**
     *<p>
     * Scan the class annotated by {@link com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.ESMetaData}
     * and automatically start the timing task to execute rollover according to the configuration.
     *<p>
     * In particular, distributed scheduling is not supported.
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        Map<String, Object> beansWithAnnotationMap = this.applicationContext.getBeansWithAnnotation(ESMetaData.class);
        List<Map.Entry<String, Object>> autoRolloverBeanList = beansWithAnnotationMap.entrySet().stream().filter(e -> {
            boolean rollover = e.getValue().getClass().getAnnotation(ESMetaData.class).rollover();
            boolean autoRollover = e.getValue().getClass().getAnnotation(ESMetaData.class).autoRollover();
            if (rollover && autoRollover) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(autoRolloverBeanList.size());
        autoRolloverBeanList.forEach(s -> {
            ESMetaData annotation = s.getValue().getClass().getAnnotation(ESMetaData.class);
            executor.scheduleAtFixedRate(() -> {
                try {
                    logger.info("index name {} execute rollover", annotation.indexName());
                    elasticsearchIndex.rollover(s.getValue().getClass(), false);
                } catch (Exception e) {
                    logger.error("ScheduleRollover scheduleAtFixedRate error :{}", e);
                }
            }, annotation.autoRolloverInitialDelay(), annotation.autoRolloverPeriod(), annotation.autoRolloverTimeUnit());
        });
        logger.info("Scan @ESMetaData bean，need automaticallily rollover bean number ：{}", autoRolloverBeanList.size());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
