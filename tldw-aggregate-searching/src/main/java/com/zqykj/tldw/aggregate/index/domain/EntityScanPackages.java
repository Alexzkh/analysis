package com.zqykj.tldw.aggregate.index.domain;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * Class for storing {@link EntityScan @EntityScan} specified packages for reference later
 * (e.g. by JPA auto-configuration).
 *
 * @author Phillip Webb
 * @see EntityScan
 * @see EntityScanner
 * @since 1.4.0
 */
public class EntityScanPackages {

    private static final String BEAN = EntityScanPackages.class.getName();

    private static final EntityScanPackages NONE = new EntityScanPackages();

    private final List<String> packageNames;

    EntityScanPackages(String... packageNames) {
        List<String> packages = new ArrayList<>();
        for (String name : packageNames) {
            if (StringUtils.hasText(name)) {
                packages.add(name);
            }
        }
        this.packageNames = Collections.unmodifiableList(packages);
    }

    /**
     * Return the package names specified from all {@link EntityScan @EntityScan}
     * annotations.
     *
     * @return the entity scan package names
     */
    public List<String> getPackageNames() {
        return this.packageNames;
    }

    /**
     * Return the {@link EntityScanPackages} for the given bean factory.
     *
     * @param beanFactory the source bean factory
     * @return the {@link EntityScanPackages} for the bean factory (never {@code null})
     */
    public static EntityScanPackages get(BeanFactory beanFactory) {
        // Currently we only store a single base package, but we return a list to
        // allow this to change in the future if needed
        try {
            return beanFactory.getBean(BEAN, EntityScanPackages.class);
        } catch (NoSuchBeanDefinitionException ex) {
            return NONE;
        }
    }

    /**
     * Register the specified entity scan packages with the system.
     *
     * @param registry     the source registry
     * @param packageNames the package names to register
     */
    public static void register(BeanDefinitionRegistry registry, String... packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        register(registry, Arrays.asList(packageNames));
    }

    /**
     * Register the specified entity scan packages with the system.
     *
     * @param registry     the source registry
     * @param packageNames the package names to register
     */
    public static void register(BeanDefinitionRegistry registry, Collection<String> packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        if (registry.containsBeanDefinition(BEAN)) {
            EntityScanPackagesBeanDefinition beanDefinition = (EntityScanPackagesBeanDefinition) registry
                    .getBeanDefinition(BEAN);
            beanDefinition.addPackageNames(packageNames);
        } else {
            registry.registerBeanDefinition(BEAN, new EntityScanPackagesBeanDefinition(packageNames));
        }
    }

    /**
     * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
     * configuration.
     */
    static class Registrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            register(registry, getPackagesToScan(metadata));
        }

        private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
            AnnotationAttributes attributes = AnnotationAttributes
                    .fromMap(metadata.getAnnotationAttributes(EntityScan.class.getName()));
            assert attributes != null;
            String[] basePackages = attributes.getStringArray("basePackages");
            Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
            Set<String> packagesToScan = new LinkedHashSet<>(Arrays.asList(basePackages));
            for (Class<?> basePackageClass : basePackageClasses) {
                packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
            }
            if (packagesToScan.isEmpty()) {
                String packageName = ClassUtils.getPackageName(metadata.getClassName());
                Assert.state(StringUtils.hasLength(packageName), "@EntityScan cannot be used with the default package");
                return Collections.singleton(packageName);
            }
            return packagesToScan;
        }

    }

    static class EntityScanPackagesBeanDefinition extends GenericBeanDefinition {

        private final Set<String> packageNames = new LinkedHashSet<>();

        EntityScanPackagesBeanDefinition(Collection<String> packageNames) {
            setBeanClass(EntityScanPackages.class);
            setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            addPackageNames(packageNames);
        }

        @Override
        public Supplier<?> getInstanceSupplier() {
            return () -> new EntityScanPackages(StringUtils.toStringArray(this.packageNames));
        }

        private void addPackageNames(Collection<String> additionalPackageNames) {
            this.packageNames.addAll(additionalPackageNames);
        }

    }

}
