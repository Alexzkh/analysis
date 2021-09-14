/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.core.MethodParameter;
import com.zqykj.repository.query.ElasticsearchParameters.ElasticsearchParameter;

import java.util.List;

/**
 * <h1> elasticsearch query parameters </h1>
 */
public class ElasticsearchParameters extends Parameters<ElasticsearchParameters, ElasticsearchParameter> {

    private ElasticsearchParameters(List<ElasticsearchParameter> parameters) {
        super(parameters);
    }

    @Override
    protected ElasticsearchParameter createParameter(MethodParameter parameter) {
        return new ElasticsearchParameter(parameter);
    }

    @Override
    protected ElasticsearchParameters createFrom(List<ElasticsearchParameter> parameters) {
        return new ElasticsearchParameters(parameters);
    }

    /**
     * <h2> ElasticsearchParameter </h2>
     */
    static class ElasticsearchParameter extends Parameter {

        /**
         * Creates a new {@link ElasticsearchParameter}.
         *
         * @param parameter must not be {@literal null}.
         */
        ElasticsearchParameter(MethodParameter parameter) {
            super(parameter);
        }

    }
}
