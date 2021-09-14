/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;
import java.util.List;

public final class DefaultParameters extends Parameters<DefaultParameters, Parameter> {

    /**
     * Creates a new {@link DefaultParameters} instance from the given {@link Method}.
     *
     * @param method must not be {@literal null}.
     */
    public DefaultParameters(Method method) {
        super(method);
    }

    private DefaultParameters(List<Parameter> parameters) {
        super(parameters);
    }

    @Override
    protected Parameter createParameter(MethodParameter parameter) {
        return new Parameter(parameter);
    }

    @Override
    protected DefaultParameters createFrom(List<Parameter> parameters) {
        return new DefaultParameters(parameters);
    }


}
