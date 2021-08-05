/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.tldw.aggregate.data.support.AggregateRepositoryInformation;
import com.zqykj.tldw.aggregate.util.ApplicationUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Setter
@Getter
public abstract class AbstractAggregateRepositoryQuery {

    public static final String ENABLE_TYPE = "enable.datasource.type";
    public static final String MONGODBTYPE = "mongodb";
    public static final String ELASTICSEARCHTYPE = "elasticsearch";
    // 该正则表达式 用来匹配@Query 注解内 value 值
    protected static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    // @Query 注解内 value 值
    private String query;
    private Method method;
    private AggregateRepositoryInformation repositoryInformation;

    /**
     * <h2> 检查当前数据的类型 </h2>
     */
    protected Class<?> checkCurAggregateDataSourceType() {
        Environment environment = ApplicationUtils.getBean(Environment.class);
        String property = environment.getProperty(ENABLE_TYPE);
        Assert.notNull(property, "aggregate datasource type not open yet!");
        switch (property) {
            case MONGODBTYPE:
                return AggregateMongoRepositoryStringQuery.class;
            default:
                return AggregateElasticsearchRepositoryStringQuery.class;
        }
    }

    @Nullable
    public abstract Object execute(Object[] parameters);
}
