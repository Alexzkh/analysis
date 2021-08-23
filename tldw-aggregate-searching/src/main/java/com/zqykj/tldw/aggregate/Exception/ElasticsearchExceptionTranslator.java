/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.Exception;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.common.ValidationException;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * <h1> Elasticsearch Exception 解释 </h1>
 */
public class ElasticsearchExceptionTranslator {

    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {

        if (isSeqNoConflict(ex)) {
            return new OptimisticLockingFailureException("Cannot index a document due to seq_no+primary_term conflict", ex);
        }

        if (ex instanceof ElasticsearchException) {

            ElasticsearchException elasticsearchException = (ElasticsearchException) ex;

            if (!indexAvailable(elasticsearchException)) {
                return new NoSuchIndexException(ObjectUtils.nullSafeToString(elasticsearchException.getMetadata("es.index")),
                        ex);
            }

            return new UncategorizedElasticsearchException(ex.getMessage(), ex);
        }

        if (ex instanceof ValidationException) {
            return new DataIntegrityViolationException(ex.getMessage(), ex);
        }

        Throwable cause = ex.getCause();
        if (cause instanceof IOException) {
            return new DataAccessResourceFailureException(ex.getMessage(), ex);
        }

        return null;
    }

    private boolean isSeqNoConflict(Exception exception) {

        if (exception instanceof ElasticsearchStatusException) {

            ElasticsearchStatusException statusException = (ElasticsearchStatusException) exception;

            return statusException.status() == RestStatus.CONFLICT && statusException.getMessage() != null
                    && statusException.getMessage().contains("type=version_conflict_engine_exception")
                    && statusException.getMessage().contains("version conflict, required seqNo");
        }

        if (exception instanceof VersionConflictEngineException) {

            VersionConflictEngineException versionConflictEngineException = (VersionConflictEngineException) exception;

            return versionConflictEngineException.getMessage() != null
                    && versionConflictEngineException.getMessage().contains("version conflict, required seqNo");
        }

        return false;
    }

    private boolean indexAvailable(ElasticsearchException ex) {

        List<String> metadata = ex.getMetadata("es.index_uuid");
        if (metadata == null) {

            if (ex.getCause() instanceof ElasticsearchException) {
                return indexAvailable((ElasticsearchException) ex.getCause());
            }

            if (ex instanceof ElasticsearchStatusException) {
                return StringUtils.hasText(ObjectUtils.nullSafeToString(ex.getIndex()));
            }
            return true;
        }
        return !CollectionUtils.contains(metadata.iterator(), "_na_");
    }
}
