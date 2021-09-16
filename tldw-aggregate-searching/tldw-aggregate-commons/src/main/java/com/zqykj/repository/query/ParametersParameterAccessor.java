/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.EntityClass;
import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import com.zqykj.domain.Sort;
import com.zqykj.repository.util.QueryExecutionConverters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Iterator;

/**
 * <h1> 具体的参数访问器实现,来查找特殊的参数 </h1>
 */
@Slf4j
public class ParametersParameterAccessor implements ParameterAccessor {

    private final Parameters<?, ?> parameters;
    private final Object[] values;


    /**
     * <h2> Creates a new {@link ParametersParameterAccessor}. </h2>
     *
     * @param parameters must not be {@literal null}.
     * @param values     must not be {@literal null}.
     */
    public ParametersParameterAccessor(Parameters<?, ?> parameters, Object[] values) {

        Assert.notNull(parameters, "Parameters must not be null!");
        Assert.notNull(values, "Values must not be null!");

        // 参数的个数 与 值的个数应该是对应的
        Assert.isTrue(parameters.getNumberOfParameters() == values.length, "Invalid number of parameters given!");

        this.parameters = parameters;

        // 如果需要 展开的话
        if (requiresUnwrapping(values)) {
            this.values = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                // 展开
                this.values[i] = QueryExecutionConverters.unwrap(values[i]);
            }
        } else {
            this.values = values;
        }
    }

    /**
     * <h2> 判断当前我们的QueryExecutionConverters(查询执行转换器) 是否支持 value 的 class类型 </h2>
     */
    private static boolean requiresUnwrapping(Object[] values) {

        for (Object value : values) {
            if (value != null && (QueryExecutionConverters.supports(value.getClass()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * <h2> Returns the {@link Parameters} instance backing the accessor. </h2>
     *
     * @return the parameters will never be {@literal null}.
     */
    public Parameters<?, ?> getParameters() {
        return parameters;
    }

    /**
     * <h2> Returns the potentially unwrapped values. </h2>
     */
    protected Object[] getValues() {
        return this.values;
    }

    @Override
    public Pageable getPageable() {

        if (!parameters.hasPageableParameter()) {
            return Pageable.unpaged();
        }

        Pageable pageable = (Pageable) values[parameters.getPageableIndex()];

        return pageable == null ? Pageable.unpaged() : pageable;
    }


    @Override
    public Sort getSort() {
        if (parameters.hasSortParameter()) {

            Sort sort = (Sort) values[parameters.getSortIndex()];
            return sort == null ? Sort.unsorted() : sort;
        }

        if (parameters.hasPageableParameter()) {
            return getPageable().getSort();
        }

        return Sort.unsorted();
    }

    @Override
    public Routing getRouting() {
        if (!parameters.hasRoutingParameter()) {

            return Routing.unRoute();
        }
        Routing sort = (Routing) values[parameters.getSortIndex()];
        return sort == null ? Routing.unRoute() : sort;
    }


    @Override
    public EntityClass getDomain() {
        if (!parameters.hasDomainParameter()) {

            log.error("Index EntityClass must not be null!");
            throw new IllegalArgumentException("index entityClass must not be null!");
        }

        return (EntityClass) values[parameters.getDomainIndex()];
    }


    @SuppressWarnings("unchecked")
    protected <T> T getValue(int index) {
        return (T) values[index];
    }

    /**
     * <h2> 获取指定参数位置 绑定的值 </h2>
     */
    public Object getBindableValue(int index) {
        return values[parameters.getBindableParameter(index).getIndex()];
    }

    /**
     * <h2> 是否还有绑定值 </h2>
     */
    public boolean hasBindableNullValue() {

        for (Parameter parameter : parameters.getBindableParameters()) {
            if (values[parameter.getIndex()] == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * <h2> 当前所有参数的一个迭代器 </h2>
     */
    @Override
    public Iterator<Object> iterator() {
        return new BindableParameterIterator(this);
    }

    private static class BindableParameterIterator implements Iterator<Object> {

        // 绑定参数的数量
        private final int bindableParameterCount;
        // 参数访问器抽象
        private final ParameterAccessor accessor;

        // 当前参数index 位置
        private int currentIndex = 0;

        /**
         * <h2> Creates a new {@link BindableParameterIterator}. </h2>
         *
         * @param accessor must not be {@literal null}.
         */
        public BindableParameterIterator(ParametersParameterAccessor accessor) {

            Assert.notNull(accessor, "ParametersParameterAccessor must not be null!");

            this.accessor = accessor;
            this.bindableParameterCount = accessor.getParameters().getBindableParameters().getNumberOfParameters();
        }

        /**
         * <h2> Returns the next bindable parameter. </h2>
         */
        public Object next() {
            return accessor.getBindableValue(currentIndex++);
        }

        public boolean hasNext() {
            return bindableParameterCount > currentIndex;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
