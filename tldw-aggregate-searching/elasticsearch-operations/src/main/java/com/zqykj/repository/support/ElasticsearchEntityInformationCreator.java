/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

/**
 * <h1> ElasticsearchEntityInformationCreator </h1>
 */
public interface ElasticsearchEntityInformationCreator {

    <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass);
}
