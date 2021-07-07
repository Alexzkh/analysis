package com.zqykj.tldw.aggregate.searching.esclientrhl.index;

import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MappingData;
import com.zqykj.tldw.aggregate.searching.esclientrhl.util.MetaData;

import java.util.Map;

/**
 * Index structure basic method interface
 **/
public interface ElasticsearchIndex<T> {
    /**
     * create index
     *
     * @param clazz
     * @throws Exception
     */
    public void createIndex(Class<T> clazz) throws Exception;


    /**
     *
     * Switch alias write index
     *
     * @param clazz
     * @throws Exception
     */
    public void switchAliasWriteIndex(Class<T> clazz, String writeIndex) throws Exception;


    /**
     *Create alias
     *
     * @param clazz
     * @throws Exception
     */
    public void createAlias(Class<T> clazz) throws Exception;

    /**
     *Create index
     *
     * @param settings     settings map information
     * @param settingsList settings map information list
     * @param mappingJson  mapping json
     * @param indexName    index name
     * @throws Exception
     */
    public void createIndex(Map<String, String> settings, Map<String, String[]> settingsList, String mappingJson, String indexName) throws Exception;

    /**
     * Delete index
     *
     * @param clazz
     * @throws Exception
     */
    public void dropIndex(Class<T> clazz) throws Exception;

    /**
     * Does the index exist
     *
     * @param clazz
     * @throws Exception
     */
    public boolean exists(Class<T> clazz) throws Exception;

    /**
     * Rolling index
     *
     * @param clazz
     * @param isAsyn Asynchronous or not
     * @throws Exception
     */
    public void rollover(Class<T> clazz, boolean isAsyn) throws Exception;

    /**
     * Get index name
     *
     * @param clazz
     * @return
     */
    public String getIndexName(Class<T> clazz);

    /**
     * Get metadata configuration
     *
     * @param clazz
     * @return
     */
    public MetaData getMetaData(Class<T> clazz);

    /**
     * Get mappingdata configuration
     *
     * @param clazz
     * @return
     */
    public MappingData[] getMappingData(Class<T> clazz);
}
