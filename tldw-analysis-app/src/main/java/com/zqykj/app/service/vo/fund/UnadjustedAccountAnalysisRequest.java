/**
 * @作者 Mcj
 */
package com.zqykj.app.service.vo.fund;

import com.zqykj.app.service.annotation.FeatureRatio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * <h1> 未调单账户分析请求 </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class UnadjustedAccountAnalysisRequest extends FundTacticsPartGeneralRequest {

    /**
     * 需要展示的账户特征  来源、中转、沉淀、其他
     */
    private List<String> accountCharacteristics;

    /**
     * 特征比值
     */
    private FeatureRatioValue ratioValue;

    /**
     * <h2> 重新设置前端传递的特征比 </h2>
     */
    public void changeRatioValue() {
        this.ratioValue.setSourceRatio(this.ratioValue.getSourceRatio() / 100);
        this.ratioValue.setTransitRatio(this.ratioValue.getTransitRatio() / 100);
        this.ratioValue.setDepositRatio(this.ratioValue.getDepositRatio() / 100);
    }

    /**
     * 用户设置的来源、中转、沉淀特征比阈值
     */
    @Setter
    @Getter
    @NoArgsConstructor
    public static class FeatureRatioValue {
        /**
         * 来源特征比阈值
         */
        private double sourceRatio;
        /**
         * 中转特征比阈值
         */
        private double transitRatio;
        /**
         * 沉淀特征比阈值
         */
        private double depositRatio;
    }

    /**
     * <h2> 特征比枚举 </h2>
     */
    @Getter
    @AllArgsConstructor
    public enum FeatureRatioEnum {

        source("来源"), transit("中转"), deposit("沉淀"), other("其他");

        /**
         * 特征比值
         */
        private String value;
    }

    // 来源特征比筛选过滤
    private static final String SOURCE_SELECTOR = "params.sourceRatio";
    // 中转特征比筛选过滤
    private static final String TRANSIT_SELECTOR = "params.transitRatio";
    // 沉淀特征比筛选过滤
    private static final String DEPOSIT_SELECTOR = "params.depositRatio";

    /**
     * <h2> 根据用户选择的账户特征(需要筛选特定数据), 生成对应过滤脚本参数 </h2>
     */
    public String getPipelineSelectorScript() {
        StringBuilder sb = new StringBuilder();
        Method[] declaredMethods = ReflectionUtils.getDeclaredMethods(UnadjustedAccountAnalysisRequest.class);
        for (String e : this.accountCharacteristics) {
            Method findMethod = Arrays.stream(declaredMethods).filter(method -> {
                if (Modifier.isPrivate(method.getModifiers())) {
                    FeatureRatio featureRatio = method.getAnnotation(FeatureRatio.class);
                    if (null != featureRatio) {
                        return e.equals(featureRatio.value().getValue());
                    }
                }
                return false;
            }).findFirst().orElse(null);
            if (null == findMethod) {
                continue;
            }
            ReflectionUtils.makeAccessible(findMethod);
            String scriptParam = (String) ReflectionUtils.invokeMethod(findMethod, this, this.ratioValue);
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            sb.append(scriptParam);
        }
        return sb.toString();
    }

    /**
     * <h2> 来源特征比脚本参数 </h2>
     */
    @FeatureRatio(value = FeatureRatioEnum.source)
    private String sourceFeatureRatioGenerate(FeatureRatioValue ratioValue) {
        return SOURCE_SELECTOR + ">=" + ratioValue.getSourceRatio();
    }

    /**
     * <h2> 中转特征比脚本参数 </h2>
     */
    @FeatureRatio(value = FeatureRatioEnum.transit)
    private String transitFeatureRatioGenerate(FeatureRatioValue ratioValue) {

        return TRANSIT_SELECTOR + "<=" + ratioValue.getTransitRatio();
    }

    /**
     * <h2> 沉淀特征比脚本参数 </h2>
     */
    @FeatureRatio(value = FeatureRatioEnum.deposit)
    private String depositFeatureRatioGenerate(FeatureRatioValue ratioValue) {
        return DEPOSIT_SELECTOR + ">=" + ratioValue.getDepositRatio();
    }

    /**
     * <h2> 其他特征比脚本参数 </h2>
     */
    @FeatureRatio(value = FeatureRatioEnum.other)
    private String otherFeatureRatioGenerate(FeatureRatioValue ratioValue) {

        // 来源的是 >= 反向取 < , 中转的是<= 反向取> , 沉淀是 >= 反向取<
        return "(" + SOURCE_SELECTOR + "<" + ratioValue.getSourceRatio() +
                " && " +
                TRANSIT_SELECTOR + ">" + ratioValue.getTransitRatio() +
                " && " +
                DEPOSIT_SELECTOR + "<" + ratioValue.getDepositRatio() + ")";
    }
}
