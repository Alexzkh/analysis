package com.zqykj.core.query;

import com.zqykj.common.enums.QueryType;
import com.zqykj.common.request.QueryParams;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.util.ReflectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @Description: Elasticsearch query and aggregation builder .
 * @Author zhangkehou
 * @Date 2021/10/11
 */
public class QueryAndAggregationBuilder {

    /**
     * @param queryParams:         查询入参
     * @param searchSourceBuilder: elasticsearch查询构建器
     * @param entity:              根据入参class构建的entity模板类,主要用于获取实体类的索引名称以及字段别名。
     * @return: void
     **/
    public static void queryAndAggregationBuilder(List<QueryParams> queryParams, SearchSourceBuilder searchSourceBuilder, ElasticsearchPersistentEntity<?> entity) {
        if (!CollectionUtils.isEmpty(queryParams)) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            queryParams.stream().forEach(queryParam -> {
                String queryField = entity.getRequiredPersistentProperty(queryParam.getField()).getFieldName();
                if (queryParam.getQueryType().equals(QueryType.term)) {
                    TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(queryField, queryParam.getValue());
                    boolQueryBuilder.must(termsQueryBuilder);
                }

                if (queryParam.getQueryType().equals(QueryType.terms)) {
                    TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(queryField, queryParam.getTermsValues());
                    boolQueryBuilder.must(termsQueryBuilder);
                }

                if (queryParam.getQueryType().equals(QueryType.range)) {
                    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(queryField);

                    queryParam.getOperatorParams().stream().forEach(operatorParam -> {

                        Optional<Method> method = null;
                        try {
                            method = ReflectionUtils.findMethod(Class.forName("org.elasticsearch.index.query.RangeQueryBuilder"),
                                    operatorParam.getOperator().toString(), Object.class, boolean.class);
                            Method method1 = method.get();
                            method1.invoke(rangeQueryBuilder, operatorParam.getOperatorValue(), operatorParam.isInclude());
                            boolQueryBuilder.must(rangeQueryBuilder);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                }

            });
            searchSourceBuilder.query(boolQueryBuilder);
        }
    }
}
