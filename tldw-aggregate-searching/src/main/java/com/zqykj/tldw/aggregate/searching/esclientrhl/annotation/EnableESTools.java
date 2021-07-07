package com.zqykj.tldw.aggregate.searching.esclientrhl.annotation;

import com.zqykj.tldw.aggregate.searching.esclientrhl.auto.ESCRegistrar;
import com.zqykj.tldw.aggregate.searching.esclientrhl.config.ElasticSearchConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * An annotation are used to identify the following actions to be down on the Application:
 * <p>
 * RestHighLevelClient with auto configuration by {@link com.zqykj.tldw.aggregate.searching.esclientrhl.config.ElasticSearchConfiguration};
 * Realize the function of the interface of the cut (refer to JPA Implementation) by{@link com.zqykj.tldw.aggregate.searching.esclientrhl.auto.ESCRegistrar}
 * Configure path to identify elasticsearch entity automatically create indexes and mapping,
 * (if not configured, scan according to the path package of main application method)
 * <p>
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ElasticSearchConfiguration.class, ESCRegistrar.class})
public @interface EnableESTools {

    /**
     * Configure the repository package path. If not, scan the package according to the path of main method.
     */
    String[] basePackages() default {};

    /**
     * Configure the repository package path. If not, scan the package according to the path of main method.
     *
     */
    String[] value() default {};

    /**
     * Configure the entity package path. If not, scan the package according to the path of main method.
     */
    String[] entityPath() default {};

    /**
     * Whether print registration information.
     */
    boolean printregmsg() default false;
}
