/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

public class DeferredRepositoryInitializationListener implements ApplicationListener<ContextRefreshedEvent>, Ordered {

    private static final Log logger = LogFactory.getLog(DeferredRepositoryInitializationListener.class);
    private final ListableBeanFactory beanFactory;

    DeferredRepositoryInitializationListener(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        logger.info("Triggering deferred initialization of Spring Data repositories…");

        beanFactory.getBeansOfType(Repository.class);

        logger.info("Spring Data repositories initialized!");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
