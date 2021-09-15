package com.zqykj.core.index;

import com.zqykj.annotations.Document;
import com.zqykj.core.ElasticsearchRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @Description: Schedule Rollover When domain index have rollover relevant configuration.
 * @Author zhangkehou
 * @Date 2021/8/18
 */
@Configuration
@Slf4j
public class ScheduleRollover implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() != null) {
            return;
        }
        Map<String, Object> beansWithAnnotationMap = this.applicationContext.getBeansWithAnnotation(Document.class);
        List<Map.Entry<String, Object>> autoRolloverBeanList = beansWithAnnotationMap.entrySet().stream().filter(e -> {
            boolean rollover = e.getValue().getClass().getAnnotation(Document.class).rollover();
            boolean autoRollover = e.getValue().getClass().getAnnotation(Document.class).autoRollover();
            if (rollover && autoRollover) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(autoRolloverBeanList.size());
        autoRolloverBeanList.forEach(s -> {
            Document annotation = s.getValue().getClass().getAnnotation(Document.class);
            executor.scheduleAtFixedRate(() -> {
//                try {
//                    SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();
//                    context.setInitialEntitySet(new EntityScanner(applicationContext).scan(Document.class));
//                    context.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT);
//                    log.info("IndexName {} execute rollover", annotation.indexName());
//                    context.getPersistentEntities().forEach(c -> {
//                        c.getIndexName().equals(s.getValue().getClass().getAnnotation(Document.class).indexName());
//                        if (c instanceof SimpleElasticSearchPersistentEntity<?>) {
//                            elasticsearchRestTemplate.indexOps(c.getType()).rollover(c);
//                        }
//                    });
//                } catch (Exception e) {
//                    log.error("ScheduleRollover scheduleAtFixedRate error", e);
//                }
            }, annotation.autoRolloverInitialDelay(), annotation.autoRolloverPeriod(), annotation.autoRolloverTimeUnit());
        });
        log.info("Scan the bean with @Document annotation which have rollover* configurations," +
                "the number of beans which need auto rollover is：{}", autoRolloverBeanList.size());
    }
}
