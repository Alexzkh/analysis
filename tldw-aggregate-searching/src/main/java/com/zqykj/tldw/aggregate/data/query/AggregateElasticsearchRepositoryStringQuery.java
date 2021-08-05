/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query;

import com.zqykj.tldw.aggregate.data.covert.DateTimeConverters;
import com.zqykj.tldw.aggregate.data.support.AggregateRepositoryInformation;
import lombok.Getter;
import lombok.Setter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h1> 处理@Query 注解的query <h1/>
 */
@Setter
@Getter
public class AggregateElasticsearchRepositoryStringQuery extends AbstractAggregateRepositoryQuery
        implements AggregateRepositoryQuery {

    private final RestHighLevelClient restHighLevelClient;
    private final GenericConversionService conversionService = new GenericConversionService();
    private final AggregateRepositoryInformation aggregateRepositoryInformation;

    {
        if (!conversionService.canConvert(java.util.Date.class, String.class)) {
            conversionService.addConverter(DateTimeConverters.JavaDateConverter.INSTANCE);
        }
        if (ClassUtils.isPresent("org.joda.time.DateTimeZone", AggregateElasticsearchRepositoryStringQuery.class.getClassLoader())) {
            if (!conversionService.canConvert(org.joda.time.ReadableInstant.class, String.class)) {
                conversionService.addConverter(DateTimeConverters.JodaDateTimeConverter.INSTANCE);
            }
            if (!conversionService.canConvert(org.joda.time.LocalDateTime.class, String.class)) {
                conversionService.addConverter(DateTimeConverters.JodaLocalDateTimeConverter.INSTANCE);
            }
        }
    }

    public AggregateElasticsearchRepositoryStringQuery(RestHighLevelClient restHighLevelClient, @Nullable AggregateRepositoryInformation aggregateRepositoryInformation) {
        this.aggregateRepositoryInformation = aggregateRepositoryInformation;
        Assert.notNull(restHighLevelClient, "Elasticsearch high rest client cannot be empty!");
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public Object execute(Object[] parameters) {

        // 当前domain
        Class<?> domainType = this.getAggregateRepositoryInformation().getDomainType();
        // 构建查询参数
        ElasticsearchStringQuery stringQuery = createQuery();

        return null;
    }

    @Override
    public Method getQueryMethod() {
        return super.getMethod();
    }

    /**
     * <h2> 构建Elasticsearch Query查询 </h2>
     */
    protected ElasticsearchStringQuery createQuery() {
        String queryString = replacePlaceHolders(getQuery());
        return new ElasticsearchStringQuery(queryString);
    }

    private String replacePlaceHolders(String input) {

        Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
        String result = input;
        while (matcher.find()) {

            String placeholder = Pattern.quote(matcher.group()) + "(?!\\d+)";
            return null;
        }
        return null;
    }
}
