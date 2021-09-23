/**
 * @作者 Mcj
 */
package com.zqykj.annotations;


import com.zqykj.boot.RepositoryScanPackages;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * <h1> 扫描Repository , 如 EntranceRepository </h1>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RepositoryScanPackages.Registrar.class)
public @interface RepositoryScan {

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
