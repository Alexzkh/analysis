/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * <h1> RepositoryBeanNameGenerator  </h1>
 */
public class RepositoryBeanNameGenerator {

    private final ClassLoader beanClassLoader;
    private final BeanNameGenerator generator;
    private final BeanDefinitionRegistry registry;

    /**
     * Creates a new {@link RepositoryBeanNameGenerator} for the given {@link ClassLoader}, {@link BeanNameGenerator}, and
     * {@link BeanDefinitionRegistry}.
     *
     * @param beanClassLoader must not be {@literal null}.
     * @param generator       must not be {@literal null}.
     * @param registry        must not be {@literal null}.
     */
    public RepositoryBeanNameGenerator(ClassLoader beanClassLoader, BeanNameGenerator generator,
                                       BeanDefinitionRegistry registry) {

        Assert.notNull(beanClassLoader, "Bean ClassLoader must not be null!");
        Assert.notNull(generator, "BeanNameGenerator must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");

        this.beanClassLoader = beanClassLoader;
        this.generator = generator;
        this.registry = registry;
    }

    /**
     * Generate a bean name for the given bean definition.
     *
     * @param definition the bean definition to generate a name for
     * @return the generated bean name
     * @since 2.0
     */
    public String generateBeanName(BeanDefinition definition) {

        AnnotatedBeanDefinition beanDefinition = definition instanceof AnnotatedBeanDefinition //
                ? (AnnotatedBeanDefinition) definition //
                : new AnnotatedGenericBeanDefinition(getRepositoryInterfaceFrom(definition));

        return generator.generateBeanName(beanDefinition, registry);
    }

    /**
     * Returns the type configured for the {@code repositoryInterface} property of the given bean definition. Uses a
     * potential {@link Class} being configured as is or tries to load a class with the given value's {@link #toString()}
     * representation.
     *
     * @param beanDefinition beanDefinition
     */
    private Class<?> getRepositoryInterfaceFrom(BeanDefinition beanDefinition) {

        ConstructorArgumentValues.ValueHolder argumentValue = beanDefinition.getConstructorArgumentValues().getArgumentValue(0, Class.class);

        if (argumentValue == null) {
            throw new IllegalStateException(
                    String.format("Failed to obtain first constructor parameter value of BeanDefinition %s!", beanDefinition));
        }

        Object value = argumentValue.getValue();

        if (value == null) {

            throw new IllegalStateException(
                    String.format("Value of first constructor parameter value of BeanDefinition %s is null!", beanDefinition));

        } else if (value instanceof Class<?>) {

            return (Class<?>) value;

        } else {

            try {
                return ClassUtils.forName(value.toString(), beanClassLoader);
            } catch (Exception o_O) {
                throw new RuntimeException(o_O);
            }
        }
    }
}
