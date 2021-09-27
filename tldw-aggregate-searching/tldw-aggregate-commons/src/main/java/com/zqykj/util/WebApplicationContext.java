package com.zqykj.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * bean加载器 实现org.springframework.context.ApplicationContextAware接口 让Spring在启动的时候为我们注入ApplicationContext对象.
 */
@Component
@Order(0)
public class WebApplicationContext implements ApplicationContextAware {

    private static ApplicationContext ctx = null;// 默认是空 在系统启动的时候初始化

    public static ApplicationContext getContext() {
        return ctx;
    }

    /**
     * 根据类型 获取Spring 管理的 bean 1.如果cls是接口则返回在应用环境中注册的第一个实现类，如果该接口没有实现则抛出异常 2.如果cls是类，则只找完全匹配的类，则不处理父子关系的类
     *
     * @param <T>
     * @param cls
     * @return
     * @throws Exception
     */
    public static <T> T getBean(Class<T> cls)  {
        if (ctx == null)
            return null;
        // String names[] = ctx.getBeanDefinitionNames();
        // for(String name:names)
        // System.out.println(name);
        // 为了防止存在cls有多种实现，采用了下方式获取对象
        Map<String, T> beans = ctx.getBeansOfType(cls);
        Set<String> key = beans.keySet();
        String beanName = null;
        Iterator<String> it = null;
        if (cls.isInterface()) { // 如果是接口则直接调用接口的第一个实现类
            it = key.iterator();
            if (it.hasNext()) {// 如果存在实现类则获取第一个实现类，如果不存在 则走下面抛出异常
                beanName = it.next();
                // System.out.println(beans.get(beanName).getClass());
                return beans.get(beanName);
            }
        } else { // 如果是类，则需要找到完全匹配的类
            for (it = key.iterator(); it.hasNext();) {
                beanName = it.next();
                // System.out.println(beans.get(beanName).getClass());
                if (beans.get(beanName).getClass().isAssignableFrom(cls)) {
                    return beans.get(beanName);
                }
            }
        }
       return null;
    }

    public static <T> T getBeanForETL(Class<T> cls)  {

            return getBean(cls);

    }

    /**
     * 根据beanId 获取Spring 管理的 bean 该返回值是未知类型，所以需要强转类型,建议使用下面的带返回值类型的方法getBean(String beanId,Class<T> cls)
     *
     * @param <T>
     * @param beanId
     * @return
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> T getBean(String beanId) throws BeansException {
        return (T)ctx.getBean(beanId);
    }

    /**
     * 根据beanId，bean类型获取对应的实体
     *
     * @param <T>
     * @param beanId
     * @param cls
     *            bean对应的cls
     * @return
     * @throws BeansException
     */
    public static <T> T getBean(String beanId, Class<T> cls) throws BeansException {
        return ctx.getBean(beanId, cls);
    }

    public void setApplicationContext(ApplicationContext contex) throws BeansException {
        ctx = contex;
    }
}
