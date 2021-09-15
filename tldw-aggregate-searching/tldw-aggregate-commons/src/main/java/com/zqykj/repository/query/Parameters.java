/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.domain.Pageable;
import com.zqykj.domain.Routing;
import com.zqykj.domain.Sort;
import com.zqykj.util.Lazy;
import com.zqykj.util.Streamable;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <h1> 抽象查询参数 </h1>
 */
public abstract class Parameters<S extends Parameters<S, T>, T extends Parameter> implements Streamable<T> {

    public static final List<Class<?>> TYPES = Arrays.asList(Pageable.class, Sort.class);

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private final int pageableIndex;
    private final int sortIndex;
    private final List<T> parameters;
    private int dynamicProjectionIndex;
    private final Lazy<S> bindable;
    private final int routingIndex;

    /**
     * Creates a new instance of {@link Parameters}.
     *
     * @param method must not be {@literal null}.
     */
    public Parameters(Method method) {

        Assert.notNull(method, "Method must not be null!");

        int parameterCount = method.getParameterCount();

        this.parameters = new ArrayList<>(parameterCount);
        this.dynamicProjectionIndex = -1;

        int pageableIndex = -1;
        int sortIndex = -1;
        int routingIndex = -1;

        for (int i = 0; i < parameterCount; i++) {

            MethodParameter methodParameter = new MethodParameter(method, i);
            methodParameter.initParameterNameDiscovery(PARAMETER_NAME_DISCOVERER);

            T parameter = createParameter(methodParameter);

            if (parameter.isDynamicProjectionParameter()) {
                this.dynamicProjectionIndex = parameter.getIndex();
            }

            if (Pageable.class.isAssignableFrom(parameter.getType())) {
                pageableIndex = i;
            }

            if (Sort.class.isAssignableFrom(parameter.getType())) {
                sortIndex = i;
            }

            if (Routing.class.isAssignableFrom(parameter.getType())) {
                routingIndex = i;
            }

            parameters.add(parameter);
        }

        this.pageableIndex = pageableIndex;
        this.sortIndex = sortIndex;
        this.routingIndex = routingIndex;

        this.bindable = Lazy.of(this::getBindable);
    }

    protected Parameters(List<T> originals) {

        this.parameters = new ArrayList<>(originals.size());

        int pageableIndexTemp = -1;
        int sortIndexTemp = -1;
        int dynamicProjectionTemp = -1;
        int routingIndexTemp = -1;

        for (int i = 0; i < originals.size(); i++) {

            T original = originals.get(i);
            this.parameters.add(original);

            pageableIndexTemp = original.isPageable() ? i : -1;
            sortIndexTemp = original.isSort() ? i : -1;
            dynamicProjectionTemp = original.isDynamicProjectionParameter() ? i : -1;
            routingIndexTemp = original.isRouting() ? i : -1;
        }

        this.pageableIndex = pageableIndexTemp;
        this.sortIndex = sortIndexTemp;
        this.dynamicProjectionIndex = dynamicProjectionTemp;
        this.routingIndex = routingIndexTemp;
        // parameters  value 绑定对应
        this.bindable = Lazy.of(() -> (S) this);
    }

    private S getBindable() {

        List<T> bindables = new ArrayList<>();

        for (T candidate : this) {

            if (candidate.isBindable()) {
                bindables.add(candidate);
            }
        }

        return createFrom(bindables);
    }

    protected abstract T createParameter(MethodParameter parameter);

    protected abstract S createFrom(List<T> parameters);

    public S getBindableParameters() {
        return this.bindable.get();
    }

    public boolean hasPageableParameter() {
        return pageableIndex != -1;
    }

    /**
     * <h2> 返回参数的个数 </h2>
     */
    public int getNumberOfParameters() {
        return parameters.size();
    }

    public int getPageableIndex() {
        return pageableIndex;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public boolean hasSortParameter() {
        return sortIndex != -1;
    }

    public int getDynamicProjectionIndex() {
        return dynamicProjectionIndex;
    }

    public boolean hasDynamicProjection() {
        return dynamicProjectionIndex != -1;
    }

    public T getBindableParameter(int bindableIndex) {
        return getBindableParameters().getParameter(bindableIndex);
    }

    public T getParameter(int index) {

        try {
            return parameters.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException(
                    "Invalid parameter index! You seem to have declared too little query method parameters!", e);
        }
    }

    public Iterator<T> iterator() {
        return parameters.iterator();
    }
}
