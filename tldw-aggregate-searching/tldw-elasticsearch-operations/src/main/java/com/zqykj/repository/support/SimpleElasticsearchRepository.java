/**
 * @作者 Mcj
 */
package com.zqykj.repository.support;

import com.zqykj.core.*;
import com.zqykj.core.aggregation.AggregatedPage;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.query.NativeSearchQueryBuilder;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Pageable;
import com.zqykj.repository.ElasticsearchRepository;
import com.zqykj.repository.EntranceRepository;
import com.zqykj.repository.query.NativeSearchQuery;
import com.zqykj.repository.query.Query;
import com.zqykj.util.StreamUtils;
import com.zqykj.util.Streamable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
public class SimpleElasticsearchRepository<T, ID> implements ElasticsearchRepository<T, ID>, EntranceRepository<T, ID> {

    private ElasticsearchRestTemplate operations;
    private IndexOperations indexOperations;

    private Class<T> entityClass;
    private ElasticsearchEntityInformation<T, ID> entityInformation;

    public SimpleElasticsearchRepository(ElasticsearchEntityInformation<T, ID> metadata,
                                         ElasticsearchRestTemplate operations) {
        this.operations = operations;

        Assert.notNull(metadata, "ElasticsearchEntityInformation must not be null!");

        this.entityInformation = metadata;
        this.entityClass = this.entityInformation.getJavaType();
        this.indexOperations = operations.indexOps(this.entityClass);

        try {
            if (shouldCreateIndexAndMapping() && !indexOperations.exists()) {
                indexOperations.createOrRollover();
                indexOperations.putMapping(entityClass);
            }
        } catch (Exception exception) {
            log.warn("Cannot create index: {}", exception.getMessage());
        }
    }

    private boolean shouldCreateIndexAndMapping() {

        final ElasticsearchPersistentEntity<?> entity = operations.getElasticsearchConverter().getMappingContext()
                .getRequiredPersistentEntity(entityClass);
        return entity.isCreateIndexAndMapping();
    }

    @Override
    public Optional<T> findById(ID id, String routing) throws Exception {
        return Optional.ofNullable(
                execute(operations -> operations.get(stringIdRepresentation(id), entityClass, getIndexCoordinates(), routing)));
    }

    @Override
    public Iterable<T> findAll(String routing) {
        int itemCount = (int) this.count(routing);

        if (itemCount == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        return this.findAll(PageRequest.of(0, Math.max(1, itemCount)), routing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page<T> findAll(Pageable pageable, String routing) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(pageable).build();
        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexCoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids, String routing) {
        Assert.notNull(ids, "ids can't be null.");

        List<T> result = new ArrayList<>();
        List<String> stringIds = stringIdsRepresentation(ids);

        if (stringIds.isEmpty()) {
            return result;
        }

        NativeSearchQuery query = new NativeSearchQueryBuilder().withIds(stringIds).withRoute(routing).build();
        List<T> multiGetEntities = execute(operations -> operations.multiGet(query, entityClass, getIndexCoordinates()));

        if (multiGetEntities != null) {
            multiGetEntities.forEach(entity -> {

                if (entity != null) {
                    result.add(entity);
                }
            });
        }

        return result;
    }

    private List<String> stringIdsRepresentation(Iterable<ID> ids) {

        Assert.notNull(ids, "ids can't be null.");

        return StreamUtils.createStreamFromIterator(ids.iterator()).map(this::stringIdRepresentation)
                .collect(Collectors.toList());
    }

    private @Nullable
    String stringIdRepresentation(@Nullable ID id) {
        return operations.stringIdRepresentation(id);
    }

    @Override
    public long count(String routing) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();
        // noinspection ConstantConditions
        return execute(operations -> operations.count(query, entityClass, getIndexCoordinates()));
    }

    @Override
    public <S extends T> S save(S entity, String routing) {
        Assert.notNull(entity, "Cannot save 'null' entity.");
        return executeAndRefresh(operations -> operations.save(entity, getIndexCoordinates(), routing));
    }

    public <S extends T> Iterable<S> save(Iterable<S> entities, String routing) {
        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        return Streamable.of(saveAll(entities, routing)).stream().collect(Collectors.toList());
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities, String routing) {

        Assert.notNull(entities, "Cannot insert 'null' as a List.");

        String indexCoordinates = getIndexCoordinates();
        executeAndRefresh(operations -> operations.save(entities, indexCoordinates, routing));

        return entities;
    }


    @Override
    public void deleteById(ID id, String routing) {
        Assert.notNull(id, "Cannot delete entity with id 'null'.");
        doDelete(id, getIndexCoordinates(), routing);
    }

    @Override
    public void delete(T entity, String routing) {
        Assert.notNull(entity, "Cannot delete 'null' entity.");

        doDelete(extractIdFromBean(entity), operations.getEntityRouting(entity), getIndexCoordinates());
    }

    private void doDelete(@Nullable ID id, @Nullable String routing, String indexCoordinates) {

        if (id != null) {
            executeAndRefresh(operations -> operations.delete(stringIdRepresentation(id), routing, indexCoordinates));
        }
    }

    @Nullable
    private ID extractIdFromBean(T entity) {
        return entityInformation.getId(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities, String routing) {
        Assert.notNull(entities, "Cannot delete 'null' list.");

        String indexCoordinates = getIndexCoordinates();
        IdsQueryBuilder idsQueryBuilder = idsQuery();
        for (T entity : entities) {
            ID id = extractIdFromBean(entity);
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
        });
    }

    @Override
    public void deleteAll(String routing) {

        String indexCoordinates = getIndexCoordinates();
        Query query = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
                .withRoute(routing)
                .build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, indexCoordinates);
            return null;
        });
    }

    public void refresh() {
        indexOperations.refresh();
    }

    private String getIndexCoordinates() {
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
    public <R> R executeAndRefresh(OperationsCallback<R> callback) {
        R result = callback.doWithOperations(operations);
        refresh();
        return result;
    }
}
