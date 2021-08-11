/**
 * @作者 Mcj
 */
package com.zqykj.domain.page;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1> 用于查询的排序选项,必须指定需要排序的字段 </h1>
 */
public class Sort implements Serializable {

    private static final long serialVersionUID = 2694925531027706418L;

    private static final Sort UNSORTED = Sort.by(new Order[0]);

    public static final Direction DEFAULT_DIRECTION = Direction.DESC;

    private List<Order> orders;

    public Sort(List<Order> orders) {
        this.orders = orders;
    }

    private Sort(Direction direction, List<String> properties) {

        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }

        this.orders = properties.stream()
                .map(it -> new Order(direction, it))
                .collect(Collectors.toList());
    }

    public static Sort by(String... properties) {

        Assert.notNull(properties, "Properties must not be null!");

        return properties.length == 0
                ? Sort.unsorted()
                : new Sort(DEFAULT_DIRECTION, Arrays.asList(properties));
    }

    public static Sort by(List<Order> orders) {

        Assert.notNull(orders, "Orders must not be null!");

        return orders.isEmpty() ? Sort.unsorted() : new Sort(orders);
    }

    public static Sort by(Order... orders) {

        Assert.notNull(orders, "Orders must not be null!");

        return new Sort(Arrays.asList(orders));
    }

    public static Sort by(Direction direction, String... properties) {

        Assert.notNull(direction, "Direction must not be null!");
        Assert.notNull(properties, "Properties must not be null!");
        Assert.isTrue(properties.length > 0, "At least one property must be given!");

        return Sort.by(Arrays.stream(properties)
                .map(it -> new Order(direction, it))
                .collect(Collectors.toList()));
    }

    public static Sort unsorted() {
        return UNSORTED;
    }

    public Sort descending() {
        return withDirection(Direction.DESC);
    }

    public Sort ascending() {
        return withDirection(Direction.ASC);
    }

    public Sort and(Sort sort) {

        Assert.notNull(sort, "Sort must not be null!");

        ArrayList<Order> these = new ArrayList<>(this.orders);

        these.addAll(sort.orders);

        return Sort.by(these);
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Sort)) {
            return false;
        }

        Sort that = (Sort) obj;

        return this.orders.equals(that.orders);
    }

    @Override
    public int hashCode() {

        int result = 17;
        result = 31 * result + orders.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return orders.isEmpty() ? "UNSORTED" : StringUtils.collectionToCommaDelimitedString(orders);
    }

    /**
     * <h2> 重新设置Orders的direction </h2>
     */
    private Sort withDirection(Direction direction) {

        return Sort.by(orders.stream().map(it -> new Order(direction, it.getProperty())).collect(Collectors.toList()));
    }

    public Iterator<Order> iterator() {
        return this.orders.iterator();
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    /**
     * <h2> 排序方向的枚举 </h2>
     */
    public static enum Direction {

        ASC, DESC;

        public boolean isAscending() {
            return this.equals(ASC);
        }

        public boolean isDescending() {
            return this.equals(DESC);
        }
    }

    /**
     * <h2> property 与 Direction 匹配 </h2>
     */
    public static class Order implements Serializable {

        private static final long serialVersionUID = -1280024931187915932L;

        // 需要排序的方向
        private final Direction direction;
        // 需要排序的字段
        private final String property;

        public Order(@Nullable Direction direction, String property) {
            if (!StringUtils.hasText(property)) {
                throw new IllegalArgumentException("Property must not null or empty!");
            }
            this.direction = direction == null ? DEFAULT_DIRECTION : direction;
            this.property = property;
        }

        /**
         * <h2> 返回属性顺序的排序 </h2>
         */
        public Direction getDirection() {
            return direction;
        }

        /**
         * <h2> 返回需要排序的字段 </h2>
         */
        public String getProperty() {
            return property;
        }

        /**
         * <h2> 返回此属性的排序是否为升序 </h2>
         */
        public boolean isAscending() {
            return this.direction.isAscending();
        }

        /**
         * <h2> 返回此属性的排序是否为降序 </h2>
         */
        public boolean isDescending() {
            return this.direction.isDescending();
        }

        public Order with(Direction direction) {
            return new Order(direction, this.property);
        }

        public Order withProperty(String property) {
            return new Order(this.direction, property);
        }

        public Sort withProperties(String... properties) {
            return Sort.by(this.direction, properties);
        }

        @Override
        public int hashCode() {

            int result = 17;
            result = 31 * result + direction.hashCode();
            result = 31 * result + property.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {

            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Order)) {
                return false;
            }
            Order that = (Order) obj;
            return this.direction.equals(that.direction) && this.property.equals(that.property);
        }

        @Override
        public String toString() {

            return String.format("%s: %s", property, direction);
        }
    }
}
