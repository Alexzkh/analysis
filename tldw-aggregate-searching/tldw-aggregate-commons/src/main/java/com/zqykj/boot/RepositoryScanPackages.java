/**
 * @作者 Mcj
 */
package com.zqykj.boot;

import com.zqykj.annotations.RepositoryScan;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.annotation.DeterminableImports;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * <h1>  </h1>
 */
public class RepositoryScanPackages {


    private static final Log logger = LogFactory.getLog(RepositoryScanPackages.class);

    private static final String BEAN = RepositoryScanPackages.class.getName();

    public static boolean has(BeanFactory beanFactory) {
        return beanFactory.containsBean(BEAN) && !get(beanFactory).isEmpty();
    }

    public static List<String> get(BeanFactory beanFactory) {
        try {
            return beanFactory.getBean(BEAN, BasePackages.class).get();
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("Unable to retrieve @RepositoryScan base packages");
        }
    }

    public static void register(BeanDefinitionRegistry registry, String... packageNames) {
        if (registry.containsBeanDefinition(BEAN)) {
            BasePackagesBeanDefinition beanDefinition = (BasePackagesBeanDefinition) registry.getBeanDefinition(BEAN);
            beanDefinition.addBasePackages(packageNames);
        } else {
            registry.registerBeanDefinition(BEAN, new BasePackagesBeanDefinition(packageNames));
        }
    }

    /**
     * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
     * configuration.
     */
    public static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
        }

        @Override
        public Set<Object> determineImports(AnnotationMetadata metadata) {
            return Collections.singleton(new PackageImports(metadata));
        }

    }

    /**
     * <h2> 用于packages 导入的包装 , 主要检查 RepositoryScan 注解的attributes 是否定义了需要扫描的包以及类  </h2>
     */
    private static final class PackageImports {

        private final List<String> packageNames;

        PackageImports(AnnotationMetadata metadata) {
            AnnotationAttributes attributes = AnnotationAttributes
                    .fromMap(metadata.getAnnotationAttributes(RepositoryScan.class.getName(), false));
            assert attributes != null;
            List<String> packageNames = new ArrayList<>(Arrays.asList(attributes.getStringArray("basePackages")));
            for (Class<?> basePackageClass : attributes.getClassArray("basePackageClasses")) {
                packageNames.add(basePackageClass.getPackage().getName());
            }
            if (packageNames.isEmpty()) {
                packageNames.add(ClassUtils.getPackageName(metadata.getClassName()));
            }
            this.packageNames = Collections.unmodifiableList(packageNames);
        }

        List<String> getPackageNames() {
            return this.packageNames;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return this.packageNames.equals(((PackageImports) obj).packageNames);
        }

        @Override
        public int hashCode() {
            return this.packageNames.hashCode();
        }

        @Override
        public String toString() {
            return "Package Imports " + this.packageNames;
        }

    }

    /**
     * <h2> 基本包的持有者（名称可以为空以表示不扫描） </h2>
     */
    static final class BasePackages {

        private final List<String> packages;

        private boolean loggedBasePackageInfo;

        BasePackages(String... names) {
            List<String> packages = new ArrayList<>();
            for (String name : names) {
                if (StringUtils.hasText(name)) {
                    packages.add(name);
                }
            }
            this.packages = packages;
        }

        List<String> get() {
            if (!this.loggedBasePackageInfo) {
                if (this.packages.isEmpty()) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("@RepositoryScan was declared on a class "
                                + "in the default package. Automatic @Repository scanning is not enabled.");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        String packageNames = StringUtils.collectionToCommaDelimitedString(this.packages);
                        logger.debug("@RepositoryScan was declared on a class in the package '" + packageNames
                                + "'. Automatic @Repository scanning is enabled.");
                    }
                }
                this.loggedBasePackageInfo = true;
            }
            return this.packages;
        }

    }

    static final class BasePackagesBeanDefinition extends GenericBeanDefinition {

        private final Set<String> basePackages = new LinkedHashSet<>();

        BasePackagesBeanDefinition(String... basePackages) {
            setBeanClass(BasePackages.class);
            setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            addBasePackages(basePackages);
        }

        @Override
        public Supplier<?> getInstanceSupplier() {
            return () -> new BasePackages(StringUtils.toStringArray(this.basePackages));
        }

        private void addBasePackages(String[] additionalBasePackages) {
            this.basePackages.addAll(Arrays.asList(additionalBasePackages));
        }

    }
}
