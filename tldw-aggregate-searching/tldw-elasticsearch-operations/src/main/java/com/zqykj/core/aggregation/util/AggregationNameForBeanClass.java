/**
 * @作者 Mcj
 */
package com.zqykj.core.aggregation.util;

import com.zqykj.core.aggregation.util.bucket.AggregationNameForBeanClassOfBucket;
import com.zqykj.core.aggregation.util.metrics.AggregationNameForBeanClassOfMetrics;
import com.zqykj.core.aggregation.util.pipeline.AggregationNameForBeanClassOfPipeline;
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
 * 描述es 聚合名称 与 聚合类的关系 抽象类
 * </h1>
 */
@Slf4j
public abstract class AggregationNameForBeanClass {


    /**
     * aggregation name -> aggregation operation class
     */
    protected Map<String, Class<?>> aggregateNameForClass;

    /**
     * <h2> 通过 聚合类型, 获取一个map (key: 聚合名称 , value: 对应该聚合操作类) </h2>
     */
    protected void getAggregationNameForClassOfType(String aggregationType, Class<?> scanSuperClass, String... basePackages) {

        log.info("Scanning for aggregation, type = {}, scan class = {}, basePackages = {}",
                aggregationType, scanSuperClass.getName(), Arrays.toString(basePackages));
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
        Map<String, ? extends Class<?>> bucket = new AggregationNameForBeanClassOfBucket().getAggregateNameForClass();
        Map<String, ? extends Class<?>> metrics = new AggregationNameForBeanClassOfMetrics().getAggregateNameForClass();
        Map<String, ? extends Class<?>> pipeline = new AggregationNameForBeanClassOfPipeline().getAggregateNameForClass();
    }
}
