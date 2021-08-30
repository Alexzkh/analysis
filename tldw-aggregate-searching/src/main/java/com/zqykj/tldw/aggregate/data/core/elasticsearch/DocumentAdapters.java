/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.core.elasticsearch;

import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.ElasticsearchDocument;
import com.zqykj.tldw.aggregate.data.query.elasticsearch.core.SearchDocumentResponse;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h2> Elasticsearch 文档数据简单适配处理 </h2>
 */
public class DocumentAdapters {


    @Nullable
    public static ElasticsearchDocument from(GetResponse source) {

        Assert.notNull(source, "GetResponse must not be null");

        // 判断查询的时候是否存在
        if (!source.isExists()) {
            return null;
        }

        // 文档资源数据是否为空
        if (source.isSourceEmpty()) {
            return SearchDocumentResponse.fromDocumentFields(source, source.getIndex(), source.getId(), source.getVersion(),
                    source.getSeqNo(), source.getPrimaryTerm());
        }

        ElasticsearchDocument document = ElasticsearchDocument.from(source.getSourceAsMap());
        document.setIndex(source.getIndex());
        document.setId(source.getId());
        document.setVersion(source.getVersion());
        document.setSeqNo(source.getSeqNo());
        document.setPrimaryTerm(source.getPrimaryTerm());

        return document;
    }

}
