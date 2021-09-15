/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
public @interface Field {

    @AliasFor("value")
    String name() default "";

    /**
     * <h2> 用于存储文档的字段名称, 如果未设置,则使用注释属性的名称</h2>
     */
    @AliasFor("name")
    String value() default "";

    /**
     * <h2> 用于存储字段的类型, 如果未设置,则使用注释属性的类型(通常mongodb 不需要指定)</h2>
     */
    FieldType type() default FieldType.Text;

    String pattern() default "";

    /**
     * <h2> 字段是否索引 </h2>
     */
    boolean index() default true;

    DateFormat format() default DateFormat.none;

    /**
     * <h2> 字段是否存储  </h2>
     */
    boolean store() default false;

    /**
     * <h2> 分词器细粒度设置</h2>
     * <p> 字段搜索时使用的分词器</p>
     */
    String searchAnalyzer() default "";

    /**
     * <h2> 分词器细粒度设置</h2>
     * <p>默认的分词器</p>
     */
    String analyzer() default "";

    String[] ignoreFields() default {};

    boolean fielddata() default false;

    String normalizer() default "";

    boolean includeInParent() default false;

    String[] copyTo() default {};

    /**
     * @since 4.0
     */
    int ignoreAbove() default -1;

    /**
     * @since 4.0
     */
    boolean coerce() default true;

    /**
     * @since 4.0
     */
    boolean docValues() default true;

    /**
     * @since 4.0
     */
    boolean ignoreMalformed() default false;

    /**
     * @since 4.0
     */
    IndexOptions indexOptions() default IndexOptions.none;

    /**
     * @since 4.0
     */
    boolean indexPhrases() default false;

    /**
     * implemented as array to enable the empty default value
     *
     * @since 4.0
     */
    IndexPrefixes[] indexPrefixes() default {};

    /**
     * @since 4.0
     */
    boolean norms() default true;

    /**
     * @since 4.0
     */
    String nullValue() default "";

    /**
     * @since 4.0
     */
    int positionIncrementGap() default -1;

    /**
     * @since 4.0
     */
    Similarity similarity() default Similarity.Default;

    /**
     * @since 4.0
     */
    TermVector termVector() default TermVector.none;

    /**
     * @since 4.0
     */
    double scalingFactor() default 1;

    /**
     * @since 4.0
     */
    int maxShingleSize() default -1;

    /**
     * if true, the field will be stored in Elasticsearch even if it has a null value
     *
     * @since 4.1
     */
    boolean storeNullValue() default false;

    /**
     * to be used in combination with {@link FieldType#Rank_Feature}
     *
     * @since 4.1
     */
    boolean positiveScoreImpact() default true;

    /**
     * to be used in combination with {@link FieldType#Object}
     *
     * @since 4.1
     */
    boolean enabled() default true;

    /**
     * @since 4.1
     */
    boolean eagerGlobalOrdinals() default false;

    /**
     * @since 4.1
     */
    NullValueType nullValueType() default NullValueType.String;

}
