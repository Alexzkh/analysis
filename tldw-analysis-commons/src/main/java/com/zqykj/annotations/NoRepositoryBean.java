/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

import java.lang.annotation.*;

/**
 * Annotation to exclude repository interfaces from being picked up and thus in consequence getting an instance being
 * created.
 * <p/>
 * This will typically be used when providing an extended base interface for all repositories in combination with a
 * custom repository base class to implement methods declared in that intermediate interface. In this case you typically
 * derive your concrete repository interfaces from the intermediate one but don't want to create a Spring bean for the
 * intermediate interface.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface NoRepositoryBean {

}
