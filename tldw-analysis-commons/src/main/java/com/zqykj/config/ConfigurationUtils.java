/**
 * @author Mcj
 */
package com.zqykj.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * <h1> 配置工具类 </h1>
 */
public class ConfigurationUtils {

    /**
     * <h2> 返回一个基于xml 的 ResourceLoader</h2>
     *
     * @param context must not be {@literal null}.
     */
    public static ResourceLoader getRequiredResourceLoader(XmlReaderContext context) {

        Assert.notNull(context, "XmlReaderContext must not be null!");

        ResourceLoader resourceLoader = context.getResourceLoader();

        if (resourceLoader == null) {
            throw new IllegalArgumentException("Could not obtain ResourceLoader from XmlReaderContext!");
        }

        return resourceLoader;
    }

    /**
     * <h2> 返回一个基于xml的ClassLoader  </h2>
     *
     * @param context must not be {@literal null}.
     */
    public static ClassLoader getRequiredClassLoader(XmlReaderContext context) {
        return getRequiredClassLoader(getRequiredResourceLoader(context));
    }

    /**
     * <h2> 返回一个基于resourceLoader的ClassLoader </h2>
     *
     * @param resourceLoader must not be {@literal null}.
     */
    public static ClassLoader getRequiredClassLoader(ResourceLoader resourceLoader) {

        Assert.notNull(resourceLoader, "ResourceLoader must not be null!");

        ClassLoader classLoader = resourceLoader.getClassLoader();

        if (classLoader == null) {
            throw new IllegalArgumentException("Could not obtain ClassLoader from ResourceLoader!");
        }

        return classLoader;
    }

    /**
     * <h2> 基于beanDefinition 返回bean class name </h2>
     *
     * @param beanDefinition must not be {@literal null}.
     */
    public static String getRequiredBeanClassName(BeanDefinition beanDefinition) {

        Assert.notNull(beanDefinition, "BeanDefinition must not be null!");

        String result = beanDefinition.getBeanClassName();

        if (result == null) {
            throw new IllegalArgumentException(
                    String.format("Could not obtain required bean class name from BeanDefinition!", beanDefinition));
        }

        return result;
    }
}
