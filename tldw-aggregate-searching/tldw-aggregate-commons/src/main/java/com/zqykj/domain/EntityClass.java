/**
 * @作者 Mcj
 */
package com.zqykj.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <h1> 用来包装标注有 {@link com.zqykj.annotations.Document} 的实体索引类 </h1>
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EntityClass {

    private Class<?> domain;
}
