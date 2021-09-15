/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.util.Streamable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * <h1> Repository configuration Source </h1>
 */
public interface RepositoryConfigurationSource {

    /**
     * <h2> 返回需要扫描 Repository interfaces 所在的包 </h2>
     */
    Streamable<String> getBasePackages();

    /**
     * <h2> 配置如何解析查询方法  </h2>
     */
    Optional<Object> getQueryLookupStrategyKey();

    /**
     * <h2> 获取命名查询位置 </h2>
     */
    Optional<String> getNamedQueryLocation();

    /**
     * <h2> Repository 基础类 </h2>
     */
    Optional<String> getRepositoryBaseClassName();

    /**
     * <h2> 返回存储库工厂 bean 类或 {@link Optional#empty()} 的名称 </h2>
     */
    Optional<String> getRepositoryFactoryBeanClassName();

    /**
     * <h2> 扫描存储库 Repository实例的候选类 </h2>
     */
    Streamable<BeanDefinition> getCandidates(ResourceLoader loader);

    /**
     * <h2> 定义要使用的存储库模式 </h2>
     */
    BootstrapMode getBootstrapMode();

    /**
     * <h2> 返回具有给定名称的属性值 </h2>
     */
    Optional<String> getAttribute(String name);

    /**
     * <h2> 返回具有给定名称和类型的属性值 </h2>
     */
    <T> Optional<T> getAttribute(String name, Class<T> type);

    /**
     * <h2> 返回给定名称的属性的属性值 </h2>
     */
    default <T> T getRequiredAttribute(String name, Class<T> type) {

        Assert.hasText(name, "Attribute name must not be null or empty!");

        return getAttribute(name, type)
                .orElseThrow(() -> new IllegalArgumentException(String.format("No attribute named %s found!", name)));
    }

    /**
     * <h2> 是否显示使用过滤器 </h2>
     */
    boolean usesExplicitFilters();

    /**
     * <h2> 过滤 一些 Repository interfaces </h2>
     */
    Streamable<TypeFilter> getExcludeFilters();

    /**
     * <h2> 返回beanDefinition 名称 </h2>
     */
    String generateBeanName(BeanDefinition beanDefinition);

    /**
     * <h2> 返回配置源自的实际源对象 </h2>
     */
    @Nullable
    Object getSource();
}
