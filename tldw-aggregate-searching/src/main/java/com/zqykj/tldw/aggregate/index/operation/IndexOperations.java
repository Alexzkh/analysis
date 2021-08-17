/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.operation;


import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;

/**
 * <h1> Index Operations </h1>
 */
public interface IndexOperations {

    boolean createIndex(PersistentEntity<?, ?> persistentEntity);


}
