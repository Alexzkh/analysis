/*
 * Copyright 2011-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zqykj.tldw.aggregate.annotations;

import java.lang.annotation.*;

/**
 * Mark a class to use compound indexes.
 * <p/>
 * <p>
 * <b>NOTE: This annotation is repeatable according to Java 8 conventions using {@link CompoundIndexes#value()} as
 * container.</b>
 *
 * <pre class="code">
 * &#64;Document
 * &#64;CompoundIndex(def = "{'firstname': 1, 'lastname': 1}")
 * &#64;CompoundIndex(def = "{'address.city': 1, 'address.street': 1}")
 * class Person {
 * 	String firstname;
 * 	String lastname;
 *
 * 	Address address;
 * }
 * </pre>
 *
 * @author Jon Brisbin
 * @author Oliver Gierke
 * @author Philipp Schneider
 * @author Johno Crawford
 * @author Christoph Strobl
 * @author Dave Perryman
 */
@Target({ElementType.TYPE})
@Documented
@Repeatable(CompoundIndexes.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface CompoundIndex {

    String def() default "";

    /** 是否创建唯一索引，默认false */
    boolean unique() default false;

    /**
     * 对文档中不存在的字段是否不起用索引，默认false
     */
    boolean sparse() default false;

    /**  索引名称 */
    String name() default "";

    boolean useGeneratedName() default false;

    /** 是否在后台创建索引，在后台创建索引不影响数据库当前的操作，默认为false */
    boolean background() default false;
}
