/**
 * @作者 Mcj
 */
package com.zqykj.domain.routing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * <h1> 用来给  Elasticsearch CRUD 设置 routing  </h1>
 */
@Setter
@Getter
@NoArgsConstructor
public class Routing {

    /**
     * <h2> 路由key </h2>
     */
    @Nullable
    private String routing;

    private String name;

    public Routing(@Nullable String routing) {
        this.routing = routing;
    }

    public static Routing unRoute() {
        return new Routing(null);
    }

    public boolean isRoute() {
        return null != routing;
    }
}
