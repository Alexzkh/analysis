/**
 * @作者 Mcj
 */
package com.zqykj.app.service.annotation;

import com.zqykj.app.service.vo.fund.UnadjustedAccountAnalysisRequest;

import java.lang.annotation.*;

/**
 * <h1> 特征比 </h1>
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface FeatureRatio {

    UnadjustedAccountAnalysisRequest.FeatureRatioEnum value() default UnadjustedAccountAnalysisRequest.FeatureRatioEnum.source;
}
