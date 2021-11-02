/**
 * @作者 Mcj
 */
package com.zqykj.parameters.annotation;

import java.lang.annotation.*;

/**
 * <h1> 不参与聚合构建解析 </h1>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface NotResolve {

}
