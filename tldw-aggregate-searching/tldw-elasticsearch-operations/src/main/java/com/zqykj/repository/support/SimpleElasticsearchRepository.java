/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.*;
import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.query.NativeSearchQueryBuilder;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Pageable;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.repository.query.NativeSearchQuery;
import com.zqykj.repository.query.Query;
import com.zqykj.util.StreamUtils;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * <h1>  Elasticsearch specific repository
 * implementation. Likely to be used as target within </h1>
 *
 * @author zhangkehou
 * @author machengjun
 */
@Slf4j
@SuppressWarnings({"all"})
public class SimpleElasticsearchRepository implements EntranceRepository {

    private ElasticsearchRestTemplate operations;
    private IndexOperations indexOperations;

    public SimpleElasticsearchRepository(ElasticsearchRestTemplate operations) {
        this.operations = operations;
        this.indexOperations = new ElasticsearchIndexOperations(operations);
    }


    @Override
    public <T, ID> Optional<T> findById(ID id, String routing, @NonNull Class<T> entityClass) throws Exception {

        createIndexAndMapping(entityClass);
        return Optional.ofNullable(
                execute(operations -> operations.get(stringIdRepresentation(id), entityClass, getIndexCoordinates(entityClass), routing)));
    }

    @Override
    public <T> Iterable<T> findAll(String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        int itemCount = (int) this.count(routing, entityClass);

        if (itemCount == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        return this.findAll(PageRequest.of(0, Math.max(1, itemCount)), routing, entityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Page<T> findAll(Pageable pageable, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(pageable).build();

        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates(entityClass)));

        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public <T, ID> Iterable<T> findAllById(Iterable<ID> ids, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(ids, "ids can't be null.");

        List<T> result = new ArrayList<>();
        List<String> stringIds = stringIdsRepresentation(ids);

        if (stringIds.isEmpty()) {
            return result;
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder().withIds(stringIds).withRoute(routing).build();
        List<T> multiGetEntities = execute(operations -> operations.multiGet(query, entityClass, getIndexCoordinates(entityClass)));

        if (multiGetEntities != null) {
            multiGetEntities.forEach(entity -> {

                if (entity != null) {
                    result.add(entity);
                }
            });
        }

        return result;
    }

    private <ID> List<String> stringIdsRepresentation(Iterable<ID> ids) {

        Assert.notNull(ids, "ids can't be null.");

        return StreamUtils.createStreamFromIterator(ids.iterator()).map(this::stringIdRepresentation)
                .collect(Collectors.toList());
    }

    @Nullable
    private <ID> String stringIdRepresentation(@Nullable ID id) {
        return operations.stringIdRepresentation(id);
    }

    @Override
    public <T> long count(String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();
        // noinspection ConstantConditions
        return execute(operations -> operations.count(query, entityClass, getIndexCoordinates(entityClass)));
    }

    @Override
    public <T> T save(T entity, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(entity, "Cannot save 'null' entity.");
        return executeAndRefresh(operations -> operations.save(entity, getIndexCoordinates(entityClass), routing), entityClass);
    }

    public <T> Iterable<T> save(Iterable<T> entities, String routing, Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        return Streamable.of(saveAll(entities, routing, entityClass)).stream().collect(Collectors.toList());
    }

    @Override
    public <T> Iterable<T> saveAll(Iterable<T> entities, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        String indexCoordinates = getIndexCoordinates(entityClass);
        executeAndRefresh(operations -> operations.save(entities, indexCoordinates, routing), entityClass);

        return entities;
    }


    @Override
    public <T, ID> void deleteById(ID id, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(id, "Cannot delete entity with id 'null'.");
        doDelete(id, getIndexCoordinates(entityClass), routing, entityClass);
    }

    private <T, ID> void doDelete(@Nullable ID id, @Nullable String routing, String indexCoordinates, Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        if (id != null) {
            executeAndRefresh(operations -> operations.delete(stringIdRepresentation(id), routing, indexCoordinates), entityClass);
        }
    }

    @Override
    public <T, ID> void deleteAll(Iterable<ID> ids, String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        Assert.notNull(ids, "Cannot delete 'null' list.");

        String indexCoordinates = getIndexCoordinates(entityClass);
        IdsQueryBuilder idsQueryBuilder = idsQuery();
        for (ID id : ids) {
            if (id != null) {
                idsQueryBuilder.addIds(stringIdRepresentation(id));
            }
        }

        if (idsQueryBuilder.ids().isEmpty()) {
            return;
        }

        Query query = new NativeSearchQueryBuilder().withQuery(idsQueryBuilder).build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, indexCoordinates);
            return null;
        }, entityClass);
    }

    @Override
    public <T> void deleteAll(String routing, @NonNull Class<T> entityClass) {

        createIndexAndMapping(entityClass);

        String indexCoordinates = getIndexCoordinates(entityClass);
        Query query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, indexCoordinates);
            return null;
        }, entityClass);
    }

    /**
     * <h2> 刷新索引 </h2>
     */
    public <T> void refresh(Class<T> entityClass) {
        indexOperations.refresh(entityClass);
    }

    /**
     * <h2> 自动构建索引与映射 </h2>
     */
    public <T> void createIndexAndMapping(Class<T> entityClass) {
        try {
            if (operations.shouldCreateIndexAndMapping(entityClass) && !indexOperations.exists(entityClass)) {
                indexOperations.create(entityClass);
                indexOperations.putMapping(entityClass);
            }
        } catch (Exception exception) {
            log.warn("Cannot create index: {}", exception.getMessage());
        }
    }

    private <T> String getIndexCoordinates(@NonNull Class<T> entityClass) {
        return operations.getIndexCoordinatesFor(entityClass);
    }

    @FunctionalInterface
    public interface OperationsCallback<R> {
        @Nullable
        R doWithOperations(ElasticsearchRestTemplate operations);

    }

    @Nullable
    public <R> R execute(OperationsCallback<R> callback) {
        return callback.doWithOperations(operations);
    }

    @Nullable
    public <R, T> R executeAndRefresh(OperationsCallback<R> callback, Class<T> entityClass) {
        R result = callback.doWithOperations(operations);
        refresh(entityClass);
        return result;
    }
}
