/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.core.ElasticsearchRestTemplate;
import com.zqykj.core.SearchHitSupport;
import com.zqykj.core.SearchHits;
import com.zqykj.domain.PageRequest;
import com.zqykj.util.StreamUtils;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h1> elasticsearch query(针对@Query 中的 value script 查询) </h1>
 */
public class ElasticsearchStringQuery extends AbstractElasticsearchRepositoryQuery {

    // 该正则表达式 用来匹配@Query 注解内 value 值
    protected static final Pattern PARAMETER_PLACEHOLDER = Pattern.compile("\\?(\\d+)");
    // @Query 注解中的value值
    private String query;
    // 通用的一些转换服务
    private final GenericConversionService conversionService = new GenericConversionService();

    public ElasticsearchStringQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchRestTemplate elasticsearchOperations,
                                    String query) {
        super(queryMethod, elasticsearchOperations);
        Assert.notNull(query, "Query cannot be empty");
        this.query = query;
    }

    @Override
    public Object execute(Object[] parameterValues) {

        // 当前Repository interface 泛型参数中的 索引类T
        Class<?> clazz = queryMethod.getDomainClass();
        // 构造参数访问器
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameterValues);

        StringQuery stringQuery = createQuery(accessor);

        Assert.notNull(stringQuery, "unsupported query");

        // 查看当前查询方法是否 标注了 @Highlight 高亮注解
        if (queryMethod.hasAnnotatedHighlight()) {
            stringQuery.setHighlightQuery(queryMethod.getAnnotatedHighlightQuery());
        }

        // 获取domainType 索引类的 indexName
        String index = elasticsearchOperations.getIndexCoordinatesFor(clazz);

        Object result;
        // 判断是Page 分页查询、stream流查询、collection查询、普通查询方式
        if (queryMethod.isPageQuery()) {

            // 如果method 有分页参数需要设置的话
            stringQuery.setPageable(accessor.getPageable());
            // 使用client 进行查询
            SearchHits<?> searchHits = elasticsearchOperations.search(stringQuery, clazz, index);
            // 转换成Page 类型包装
            result = SearchHitSupport.page(searchHits, stringQuery.getPageable());
        } else if (queryMethod.isStreamQuery()) {

            if (accessor.getPageable().isUnpaged()) {
                stringQuery.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
            } else {
                stringQuery.setPageable(accessor.getPageable());
            }
            // 转成Stream 类型(内部使用滚动查询方式)
            result = StreamUtils.createStreamFromIterator(elasticsearchOperations.searchForStream(stringQuery, clazz, index));

        } else if (queryMethod.isCollectionQuery()) {

            if (accessor.getPageable().isPaged()) {
                stringQuery.setPageable(accessor.getPageable());
            }
            result = elasticsearchOperations.search(stringQuery, clazz, index);
        } else {

            result = elasticsearchOperations.searchOne(stringQuery, clazz, index);
        }

        // 最后解析包装类型
        return queryMethod.isNotSearchHitMethod() ? SearchHitSupport.unwrapSearchHits(result) : result;
    }

    /**
     * <h2> 生成特定的@Query 查询实现 </h2>
     */
    protected StringQuery createQuery(ParametersParameterAccessor parameterAccessor) {
        String queryString = replacePlaceholders(this.query, parameterAccessor);
        return new StringQuery(queryString);
    }

    /**
     * <h2> 替换 @Query("{\"bool\" : {\"must\" : {\"exists\" : {\"field\" : \"?0\"}}}}")
     * eg. 将@Query中的value 的 ?0 替换成方法指定位置参数的参数值  </h2>
     */
    private String replacePlaceholders(String input, ParametersParameterAccessor accessor) {

        Matcher matcher = PARAMETER_PLACEHOLDER.matcher(input);
        String result = input;
        while (matcher.find()) {

            String placeholder = Pattern.quote(matcher.group()) + "(?!\\d+)";
            int index = NumberUtils.parseNumber(matcher.group(1), Integer.class);
            result = result.replaceAll(placeholder, getParameterWithIndex(accessor, index));
        }
        return result;
    }

    /**
     * <h2> 获取方法 指定参数位置index 的参数值 </h2>
     */
    private String getParameterWithIndex(ParametersParameterAccessor accessor, int index) {
        Object parameter = accessor.getBindableValue(index);
        if (parameter == null) {
            return "null";
        }
        if (conversionService.canConvert(parameter.getClass(), String.class)) {
            return conversionService.convert(parameter, String.class);
        }
        return parameter.toString();
    }
}
