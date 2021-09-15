/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.config.ConfigurationUtils;
import com.zqykj.util.Streamable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <h1> AnnotationRepositoryConfigurationSource </h1>
 *
 * <p>
 * Annotation based {@link RepositoryConfigurationSource}.
 * </p>
 */
public class AnnotationRepositoryConfigurationSource extends RepositoryConfigurationSourceSupport {

    private static final String REPOSITORY_IMPLEMENTATION_POSTFIX = "repositoryImplementationPostfix";
    private static final String BASE_PACKAGES = "basePackages";
    private static final String BASE_PACKAGE_CLASSES = "basePackageClasses";
    private static final String NAMED_QUERIES_LOCATION = "namedQueriesLocation";
    private static final String QUERY_LOOKUP_STRATEGY = "queryLookupStrategy";
    private static final String REPOSITORY_FACTORY_BEAN_CLASS = "repositoryFactoryBeanClass";
    private static final String REPOSITORY_BASE_CLASS = "repositoryBaseClass";
    private static final String CONSIDER_NESTED_REPOSITORIES = "considerNestedRepositories";
    private static final String BOOTSTRAP_MODE = "bootstrapMode";


    private final AnnotationMetadata configMetadata;
    private final AnnotationMetadata enableAnnotationMetadata;
    private final AnnotationAttributes attributes;
    private final ResourceLoader resourceLoader;
    private final boolean hasExplicitFilters;


    /**
     * Creates a new {@link AnnotationRepositoryConfigurationSource} from the given {@link AnnotationMetadata} and
     * annotation.
     *
     * @param metadata       must not be {@literal null}.
     * @param annotation     must not be {@literal null}.
     * @param resourceLoader must not be {@literal null}.
     * @param environment    must not be {@literal null}.
     * @param registry       must not be {@literal null}.
     * @param generator      can be {@literal null}.
     */
    public AnnotationRepositoryConfigurationSource(AnnotationMetadata metadata, Class<? extends Annotation> annotation,
                                                   ResourceLoader resourceLoader, Environment environment, BeanDefinitionRegistry registry,
                                                   @Nullable BeanNameGenerator generator) {

        super(environment, ConfigurationUtils.getRequiredClassLoader(resourceLoader), registry,
                defaultBeanNameGenerator(generator));

        Assert.notNull(metadata, "Metadata must not be null!");
        Assert.notNull(annotation, "Annotation must not be null!");
        Assert.notNull(resourceLoader, "ResourceLoader must not be null!");

        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotation.getName());

        if (annotationAttributes == null) {
            throw new IllegalStateException(String.format("Unable to obtain annotation attributes for %s!", annotation));
        }

        this.attributes = new AnnotationAttributes(annotationAttributes);
        this.enableAnnotationMetadata = AnnotationMetadata.introspect(annotation);
        this.configMetadata = metadata;
        this.resourceLoader = resourceLoader;
        this.hasExplicitFilters = hasExplicitFilters(attributes);
    }

    /**
     * <h2> 返回是否存在包含或排除过滤器的显式配置 </h2>
     *
     * @param attributes must not be {@literal null}.
     */
    private static boolean hasExplicitFilters(AnnotationAttributes attributes) {

        return Stream.of("includeFilters", "excludeFilters") //
                .anyMatch(it -> attributes.getAnnotationArray(it).length > 0);
    }

    /**
     * <h2> 获取需要扫描的包(扫描Repository interface 所在的包) </h2>
     */
    @Override
    public Streamable<String> getBasePackages() {
        String[] value = attributes.getStringArray("value");
        String[] basePackages = attributes.getStringArray(BASE_PACKAGES);
        Class<?>[] basePackageClasses = attributes.getClassArray(BASE_PACKAGE_CLASSES);

        // Default configuration - return package of annotated class
        if (value.length == 0 && basePackages.length == 0 && basePackageClasses.length == 0) {

            String className = configMetadata.getClassName();
            return Streamable.of(ClassUtils.getPackageName(className));
        }

        Set<String> packages = new HashSet<>();
        packages.addAll(Arrays.asList(value));
        packages.addAll(Arrays.asList(basePackages));

        Arrays.stream(basePackageClasses)//
                .map(ClassUtils::getPackageName)//
                .forEach(it -> packages.add(it));

        return Streamable.of(packages);
    }

    /**
     * <h2> 查询方法 查询策略 </h2>
     */
    @Override
    public Optional<Object> getQueryLookupStrategyKey() {
        return Optional.ofNullable(attributes.get(QUERY_LOOKUP_STRATEGY));
    }


    @Override
    public Optional<String> getNamedQueryLocation() {
        return getNullDefaultedAttribute(NAMED_QUERIES_LOCATION);
    }

    @Override
    protected Iterable<TypeFilter> getIncludeFilters() {
        return parseFilters("includeFilters");
    }

    @Override
    public Streamable<TypeFilter> getExcludeFilters() {
        return parseFilters("excludeFilters");
    }

    @NonNull
    public Object getSource() {
        return configMetadata;
    }

    @Override
    public Optional<String> getRepositoryFactoryBeanClassName() {
        return Optional.of(attributes.getClass(REPOSITORY_FACTORY_BEAN_CLASS).getName());
    }

    /**
     * <h2> 获取Repository 基类 </h2>
     */
    @Override
    public Optional<String> getRepositoryBaseClassName() {

        if (!attributes.containsKey(REPOSITORY_BASE_CLASS)) {
            return Optional.empty();
        }

        Class<? extends Object> repositoryBaseClass = attributes.getClass(REPOSITORY_BASE_CLASS);
        return DefaultRepositoryBaseClass.class.equals(repositoryBaseClass) ? Optional.empty()
                : Optional.of(repositoryBaseClass.getName());
    }

    public AnnotationAttributes getAttributes() {
        return attributes;
    }

    public AnnotationMetadata getEnableAnnotationMetadata() {
        return enableAnnotationMetadata;
    }

    @Override
    public boolean shouldConsiderNestedRepositories() {
        return attributes.containsKey(CONSIDER_NESTED_REPOSITORIES) && attributes.getBoolean(CONSIDER_NESTED_REPOSITORIES);
    }

    @Override
    public BootstrapMode getBootstrapMode() {
        try {
            return attributes.getEnum(BOOTSTRAP_MODE);
        } catch (IllegalArgumentException o_O) {
            return BootstrapMode.DEFAULT;
        }
    }

    @Override
    public Optional<String> getAttribute(String name) {
        return getAttribute(name, String.class);
    }

    @Override
    public <T> Optional<T> getAttribute(String name, Class<T> type) {
        if (!attributes.containsKey(name)) {
            throw new IllegalArgumentException(String.format("No attribute named %s found!", name));
        }

        Object value = attributes.get(name);

        if (value == null) {
            return Optional.empty();
        }

        Assert.isInstanceOf(type, value,
                () -> String.format("Attribute value for %s is of type %s but was expected to be of type %s!", name,
                        value.getClass(), type));

        Object result = String.class.isInstance(value) //
                ? StringUtils.hasText((String) value) ? value : null //
                : value;

        return Optional.ofNullable(type.cast(result));
    }

    @Override
    public boolean usesExplicitFilters() {
        return hasExplicitFilters;
    }

    /**
     * <h2> 返回具有给定名称的属性 </h2>
     *
     * @param attributeName 属性名称
     */
    private Optional<String> getNullDefaultedAttribute(String attributeName) {

        String attribute = attributes.getString(attributeName);

        return StringUtils.hasText(attribute) ? Optional.of(attribute) : Optional.empty();
    }

    private Streamable<TypeFilter> parseFilters(String attributeName) {

        AnnotationAttributes[] filters = attributes.getAnnotationArray(attributeName);

        return Streamable.of(() -> Arrays.stream(filters).flatMap(it -> typeFiltersFor(it).stream()));
    }

    private List<TypeFilter> typeFiltersFor(AnnotationAttributes filterAttributes) {

        List<TypeFilter> typeFilters = new ArrayList<>();
        FilterType filterType = filterAttributes.getEnum("type");

        for (Class<?> filterClass : filterAttributes.getClassArray("value")) {
            switch (filterType) {
                case ANNOTATION:
                    Assert.isAssignable(Annotation.class, filterClass,
                            "An error occured when processing a @ComponentScan " + "ANNOTATION type filter: ");
                    @SuppressWarnings("unchecked")
                    Class<Annotation> annoClass = (Class<Annotation>) filterClass;
                    typeFilters.add(new AnnotationTypeFilter(annoClass));
                    break;
                case ASSIGNABLE_TYPE:
                    typeFilters.add(new AssignableTypeFilter(filterClass));
                    break;
                case CUSTOM:
                    Assert.isAssignable(TypeFilter.class, filterClass,
                            "An error occured when processing a @ComponentScan " + "CUSTOM type filter: ");
                    typeFilters.add(BeanUtils.instantiateClass(filterClass, TypeFilter.class));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown filter type " + filterType);
            }
        }

        for (String expression : getPatterns(filterAttributes)) {

            String rawName = filterType.toString();

            if ("REGEX".equals(rawName)) {
                typeFilters.add(new RegexPatternTypeFilter(Pattern.compile(expression)));
            } else if ("ASPECTJ".equals(rawName)) {
                typeFilters.add(new AspectJTypeFilter(expression, this.resourceLoader.getClassLoader()));
            } else {
                throw new IllegalArgumentException("Unknown filter type " + filterType);
            }
        }

        return typeFilters;
    }

    /**
     * Safely reads the {@code pattern} attribute from the given {@link AnnotationAttributes} and returns an empty list if
     * the attribute is not present.
     *
     * @param filterAttributes must not be {@literal null}.
     */
    private String[] getPatterns(AnnotationAttributes filterAttributes) {

        try {
            return filterAttributes.getStringArray("pattern");
        } catch (IllegalArgumentException o_O) {
            return new String[0];
        }
    }

    /**
     * <h2> BeanNameGenerator </h2>
     *
     * @param generator can be {@literal null}.
     */
    private static BeanNameGenerator defaultBeanNameGenerator(@Nullable BeanNameGenerator generator) {

        return generator == null || ConfigurationClassPostProcessor.IMPORT_BEAN_NAME_GENERATOR.equals(generator) //
                ? new AnnotationBeanNameGenerator() //
                : generator;
    }
}
