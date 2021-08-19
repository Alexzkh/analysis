/**
 * @作者 Mcj
 */
package com.zqykj.domain.routing;

import lombok.Getter;
import lombok.Setter;

/**
 * <h1> 用来给  Elasticsearch CRUD 设置 routing  </h1>
 */
@Setter
@Getter
public class Route {

    /**
     * <h2> 路由key </h2>
     */
    String routing;

    public Route(String routing) {
        this.routing = routing;
    }

    public Route() {

    }

    public static Route unRoute() {
        return new Route();
    }

    public boolean isRoute() {
        return null != routing;
    }
}
