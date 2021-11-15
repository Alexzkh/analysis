/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util;

import com.zqykj.core.aggregation.util.aggregate.bucket.ClassNameForBeanClassOfBucket;
import com.zqykj.core.aggregation.util.aggregate.metrics.ClassNameForBeanClassOfMetrics;
import com.zqykj.core.aggregation.util.aggregate.pipeline.ClassNameForBeanClassOfPipeline;
import com.zqykj.util.ReflectionUtils;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>
 * 描述 类作用名称 与 类的关系 抽象类
 * </h1>
 */
@Slf4j
@SuppressWarnings("all")
public abstract class ClassNameForBeanClass {


    /**
     * class effect name ->  operation class
     */
    protected Map<String, Class<?>> aggregateNameForClass;

    /**
     * <h2> 给定接口类 和 基础扫描包, 得到接口下所有实现类 </h2>
     */
    protected void getAggregationNameForClassOfType(String effectType, Class<?> scanSuperClass, String... basePackages) {

        log.info("Scanning for type = {}, scan class = {}, basePackages = {}",
                effectType, scanSuperClass.getName(), Arrays.toString(basePackages));
        // true：默认TypeFilter生效，这种模式会查询出许多不符合你要求的class名
        // false：关闭默认TypeFilter
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // 接口不会被扫描，其子类会被扫描出来
        provider.addIncludeFilter(new AssignableTypeFilter(scanSuperClass));
        // Spring会将 .换成/  ("."-based package path to a "/"-based)
        // Spring拼接的扫描地址：classpath*:xxx/xxx/xxx/**/*.class

        Set<String> packages = new HashSet<>(Arrays.asList(basePackages));


        Streamable<BeanDefinition> candidateComponents = findCandidateComponents(provider, packages);

        Set<? extends Class<?>> beanClasses = candidateComponents.stream().map(beanDefinition -> {
            try {
                return Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toSet());

        aggregateNameForClass = beanClasses.stream().collect(Collectors.toConcurrentMap(
                beanClass -> {
                    Field field = ReflectionUtils.findRequiredField(beanClass, "NAME");
                    if (field.getType().isAssignableFrom(String.class)) {
                        try {
                            Object value = field.get(beanClass);
                            if (value == null) {
                                return "UNKNOWN";
                            }
                            return value.toString();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            return "UNKNOWN";
                        }
                    }
                    return "UNKNOWN";
                },
                beanClass -> beanClass,
                (beanClass, beanClassOther) -> beanClass
        ));
    }

    /**
     * <h2> 扫描指定包下的候选类 </h2>
     */
    protected Streamable<BeanDefinition> findCandidateComponents(ClassPathScanningCandidateComponentProvider scanner, Set<String> packages) {

        return Streamable.of(() -> getBasePackages(packages).stream()//
                .flatMap(it -> scanner.findCandidateComponents(it).stream()));
    }

    private Streamable<String> getBasePackages(Set<String> packages) {
        return Streamable.of(packages);
    }

    public Map<String, Class<?>> getAggregateNameForClass() {
        return aggregateNameForClass;
    }

    // Test
    public static void main(String[] args) {
        Map<String, ? extends Class<?>> bucket = new ClassNameForBeanClassOfBucket().getAggregateNameForClass();
        Map<String, ? extends Class<?>> metrics = new ClassNameForBeanClassOfMetrics().getAggregateNameForClass();
        Map<String, ? extends Class<?>> pipeline = new ClassNameForBeanClassOfPipeline().getAggregateNameForClass();
    }
}
