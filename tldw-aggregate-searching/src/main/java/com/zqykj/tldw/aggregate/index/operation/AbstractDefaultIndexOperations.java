/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.operation;

import lombok.extern.slf4j.Slf4j;


/**
 * <h2>  抽象数据源索引操作: 可以将一些通用的操作放在这里 </h2>
 */
@Slf4j
public abstract class AbstractDefaultIndexOperations implements IndexOperations {


    public abstract String getIndexCoordinatesFor(Class<?> clazz);
}
