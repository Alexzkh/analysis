package com.zqykj.tldw.aggregate.searching.esclientrhl.auto.intfproxy;

import com.zqykj.tldw.aggregate.searching.esclientrhl.annotation.EnableESTools;
import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.GetBasePackage;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generate {@link ESCRepository } proxy bean.
 **/
public class RepositoryFactorySupport<T extends ESCRepository<S, ID>, S, ID> implements ApplicationContextAware, ResourceLoaderAware, InitializingBean, FactoryBean<T>, BeanClassLoaderAware,
        BeanFactoryAware, ApplicationEventPublisherAware {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository interface.
     **/
    private final Class<? extends T> repositoryInterface;

    /**
     * @see org.springframework.core.io.ResourceLoader
     **/
    private ResourceLoader resourceLoader;

    /**
     * Repository entity.
     **/
    private T repository;

    /**
     * A class loader is an object that is responsible for loading classes.
     * @see java.lang.ClassLoader
     **/
    private ClassLoader classLoader;

    /**
     * The root interface for accessing a Spring bean container.
     * @see org.springframework.beans.factory.BeanFactory
     **/
    private BeanFactory beanFactory;


    /**
     * Interface that encapsulates event publication functionality.
     *<p>Serves as a super-interface for {@link ApplicationContext}.
     **/
    private ApplicationEventPublisher publisher;


    /**
     *Central interface to provide configuration for an application.
     *This is read-only while the application is running, but may be reloaded if the implementation supports this.
     *@see  org.springframework.context.ApplicationContext
     **/
    private ApplicationContext applicationContext;

    public RepositoryFactorySupport(Class<? extends T> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            this.repository = this.getRepository(repositoryInterface);
        } catch (Exception e) {
            logger.error("ESCRepository proxy create fail !", e);
        }
    }


    /**
     * Get Repositor by AOP.
     * @param repositoryInterface:
     * @return: T
     **/
    public <T> T getRepository(Class<T> repositoryInterface) throws Exception {
        SimpleESCRepository target = new SimpleESCRepository(applicationContext);
        getMetadata(target);
        ProxyFactory result = new ProxyFactory();
        result.setTarget(target);
        result.addAdvice(new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                Object result = invocation.proceed();
                return result;
            }
        });
        result.setInterfaces(this.repositoryInterface, ESCRepository.class);
        T repository = (T) result.getProxy(classLoader);
        return repository;
    }

    /**
     *
     * Get the entity class type and primary key type according to the interface
     *
     * @param target
     */
    private void getMetadata(SimpleESCRepository target) throws Exception {
        Type[] types = repositoryInterface.getGenericInterfaces();
        ParameterizedType parameterized = (ParameterizedType) types[0];
        // entity type name .
        String domainClassName = parameterized.getActualTypeArguments()[0].getTypeName();
        // entity keyword name .
        String idClassName = parameterized.getActualTypeArguments()[1].getTypeName();
        if (com.zqykj.tldw.aggregate.searching.esclientrhl.auto.util.EnableESTools.isPrintregmsg()) {
            logger.info("domainClassName：" + domainClassName + " idClassName：" + idClassName);
        }
        // Match entity class type by entity class type name
        List<String> entityList = getEntityList();
        for (int i = 0; i < entityList.size(); i++) {
            if (entityList.get(i).lastIndexOf("." + domainClassName) != -1 || entityList.get(i).equals(domainClassName)) {
                if (target.getDomainClass() == null) {
                    target.setDomainClass(Class.forName(entityList.get(i)));
                    break;
                } else {
                    target.setDomainClass(null);
                    throw new Exception("Entity Overmatched !");
                }
            }
        }
        // By entity class primary key type name primary key type.
        Map<String, Class> idTypeMap = getIdTypeMap();
        if (idTypeMap.containsKey(idClassName)) {
            target.setIdClass(idTypeMap.get(idClassName));
        } else {
            throw new Exception("Not Supported ID Type !");
        }
    }


    /**
     * Get id type map.
     * @return: java.util.Map<java.lang.String,java.lang.Class>
     **/
    private Map<String, Class> getIdTypeMap() {
        Map<String, Class> idTypeMap = new HashMap<>();
        idTypeMap.put("String", String.class);
        idTypeMap.put("Integer", Integer.class);
        idTypeMap.put("Long", Long.class);
        idTypeMap.put("java.lang.String", String.class);
        idTypeMap.put("java.lang.Integer", Integer.class);
        idTypeMap.put("java.lang.Long", Long.class);
        return idTypeMap;
    }


    /**
     * Gets all fully qualified class names on the entity class path.
     * @return
     */
    private List<String> getEntityList() {
        List<String> entityList = new ArrayList<>();
        GetBasePackage.getEntityPathsMap().get(EnableESTools.class).forEach(s -> {
            ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
            MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
            Resource[] resources = new Resource[0];
            try {
                resources = resolver.getResources("classpath*:" + s.replaceAll("\\.", "/") + "/**/*.class");
                for (Resource r : resources) {
                    MetadataReader reader = metaReader.getMetadataReader(r);
                    entityList.add(reader.getClassMetadata().getClassName());
                }
            } catch (IOException e) {
                logger.error("getEntityList error", e);
            }
        });
        return entityList;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * {@link org.springframework.beans.factory.FactoryBean} is implemented to host the generated proxy bean to spring
     *
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        return this.repository;
    }

    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
