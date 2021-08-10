package com.zqykj.tldw.aggregate.searching.esclientrhl.config;

import com.zqykj.tldw.aggregate.searching.BaseOperations;
import com.zqykj.tldw.aggregate.searching.ElasticsearchTemplateOperations;
import com.zqykj.tldw.aggregate.searching.esclientrhl.config.exception.DataOperationClientException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: Generate corresponding data source instance according to type.
 * @Author zhangkehou
 * @Date 2021/8/7
 */
public class GenerateClientManager {

    // k-->data source type  v--> the implementions of ElasticsearchTemplateOperations
    private final Map<BaseOperations.DatasoureType, Object> templateMap = new ConcurrentHashMap<>();
    private static GenerateClientManager instance = null;

    public GenerateClientManager(ElasticsearchOperationClientProperties config) {
        initialize();
        ElasticsearchTemplateOperations estemplate = DataOpertionClientFactory.open(config);
        templateMap.put(BaseOperations.DatasoureType.Elasticsearch, estemplate);
    }

    // initialize the manager class .
    private synchronized void initialize() {
        if (null != instance) {
            final String errMsg = "GenerateClientManager should be instantiated once";
            throw new DataOperationClientException(errMsg);
        }
        instance = this;
    }


    public static GenerateClientManager getInstance() {
        return instance;
    }

    public Object getTemplete(BaseOperations.DatasoureType datasoureType) {
        return templateMap.get(datasoureType);
    }


    public void removeTemplate(String templateName) {
        templateMap.remove(templateName);
    }

    public Object getTemplate(BaseOperations.DatasoureType datasoureType) {
        return templateMap.get(datasoureType);
    }
}
