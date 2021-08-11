/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import com.zqykj.infrastructure.util.Direction;
import com.zqykj.infrastructure.util.IndexDirection;
import com.zqykj.infrastructure.util.TypeInformation;
import com.zqykj.annotations.CompoundIndex;
import com.zqykj.annotations.CompoundIndexes;
import com.zqykj.annotations.Document;
import com.zqykj.tldw.aggregate.index.context.AbstractMappingContext;
import com.zqykj.tldw.aggregate.index.mapping.BasicPersistentEntity;
import com.zqykj.tldw.aggregate.index.mapping.PersistentEntity;
import com.zqykj.tldw.aggregate.index.mapping.PersistentProperty;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongoPersistentEntity;
import com.zqykj.tldw.aggregate.index.mongodb.SimpleMongodbPersistentProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.iterators.CollatingIterator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <h2> Mongodb 索引解析器, 用来处理 @CompoundIndex </h2>
 */
@Slf4j
public class MongodbPersistentEntityIndexResolver implements IndexResolver {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private final AbstractMappingContext<? extends SimpleMongoPersistentEntity<?>,
            SimpleMongodbPersistentProperty> mappingContext;

    public MongodbPersistentEntityIndexResolver(AbstractMappingContext<? extends SimpleMongoPersistentEntity<?>,
            SimpleMongodbPersistentProperty> mappingContext) {
        Assert.notNull(mappingContext, "Mapping context must not be null in order to resolve index definitions");
        this.mappingContext = mappingContext;
    }

    @Override
    public Iterable<? extends IndexDefinition> resolveIndexFor(TypeInformation<?> typeInformation) {
        BasicPersistentEntity<?, ?> entity = mappingContext.getRequiredPersistentEntity(typeInformation);
        if (null != entity) {
            return resolveIndexForEntity((SimpleMongoPersistentEntity<?>) entity);
        }
        return CollatingIterator::new;
    }

    public List<IndexDefinitionHolder> resolveIndexForEntity(SimpleMongoPersistentEntity<?> root) {

        Assert.notNull(root, "MongoPersistentEntity must not be null!");
        Document document = root.findAnnotation(Document.class);
        Assert.notNull(document, () -> String
                .format("Entity %s is not a collection root. Make sure to annotate it with @Document!", root.getName()));

        List<IndexDefinitionHolder> indexInformation = new ArrayList<>();
        String collection = root.getCollection();
        indexInformation.addAll(potentiallyCreateCompoundIndexDefinitions("", collection, root));
        // TODO 暂不支持,等待后续开发
//        indexInformation.addAll(potentiallyCreateTextIndexDefinition(root, collection));

        root.doWithProperties(property -> this
                .potentiallyAddIndexForProperty(root, property, indexInformation, new CycleGuard()));

        // TODO 文档引用暂不支持,等待后续开发 (参考spring.data.mongodb MongoPersistentEntityIndexResolver.resolveIndexesForDbrefs() 方法)
//        indexInformation.addAll(resolveIndexesForDbrefs("", collection, root));

        return indexInformation;
    }

    private void potentiallyAddIndexForProperty(SimpleMongoPersistentEntity<?> root, SimpleMongodbPersistentProperty persistentProperty,
                                                List<IndexDefinitionHolder> indexes, CycleGuard guard) {

        try {
            if (persistentProperty.isEntity()) {
                indexes.addAll(resolveIndexForClass(persistentProperty.getTypeInformation().getActualType(),
                        persistentProperty.getFieldName(), CycleGuard.Path.of(persistentProperty), root.getCollection(), guard));
            }

            List<IndexDefinitionHolder> indexDefinitions = createIndexDefinitionHolderForProperty(
                    persistentProperty.getFieldName(), root.getCollection(), persistentProperty);
            if (!indexDefinitions.isEmpty()) {
                indexes.addAll(indexDefinitions);
            }
        } catch (CyclicPropertyReferenceException e) {
            log.info(e.getMessage());
        }
    }

    private List<IndexDefinitionHolder> resolveIndexForClass(final TypeInformation<?> type, final String dotPath,
                                                             final CycleGuard.Path path, final String collection, final CycleGuard guard) {

        SimpleMongoPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(type);

        final List<IndexDefinitionHolder> indexInformation = new ArrayList<>();
        indexInformation.addAll(potentiallyCreateCompoundIndexDefinitions(dotPath, collection, entity));

        entity.doWithProperties(property -> this
                .guardAndPotentiallyAddIndexForProperty(property, dotPath, path, collection, indexInformation, guard));

        // TODO 文档引用暂不支持,等待后续开发 (参考spring.data.mongodb MongoPersistentEntityIndexResolver.resolveIndexesForDbrefs() 方法)
//        indexInformation.addAll(resolveIndexesForDbrefs(dotPath, collection, entity));

        return indexInformation;
    }

    private void guardAndPotentiallyAddIndexForProperty(SimpleMongodbPersistentProperty persistentProperty, String dotPath,
                                                        CycleGuard.Path path, String collection, List<IndexDefinitionHolder> indexes, CycleGuard guard) {

        String propertyDotPath = (StringUtils.hasText(dotPath) ? dotPath + "." : "") + persistentProperty.getFieldName();

        CycleGuard.Path propertyPath = path.append(persistentProperty);
        guard.protect(persistentProperty, propertyPath);

        if (persistentProperty.isEntity()) {
            try {
                indexes.addAll(resolveIndexForClass(persistentProperty.getTypeInformation().getActualType(), propertyDotPath,
                        propertyPath, collection, guard));
            } catch (CyclicPropertyReferenceException e) {
                log.info(e.getMessage());
            }
        }

        List<IndexDefinitionHolder> indexDefinitions = createIndexDefinitionHolderForProperty(propertyDotPath, collection,
                persistentProperty);

        if (!indexDefinitions.isEmpty()) {
            indexes.addAll(indexDefinitions);
        }
    }

    private List<IndexDefinitionHolder> createIndexDefinitionHolderForProperty(String dotPath, String collection,
                                                                               SimpleMongodbPersistentProperty persistentProperty) {

        List<IndexDefinitionHolder> indices = new ArrayList<>(2);

        if (persistentProperty.isAnnotationPresent(Indexed.class)) {
            indices.add(createIndexDefinition(dotPath, collection, persistentProperty));
        } else if (persistentProperty.isAnnotationPresent(GeoSpatialIndexed.class)) {
            // TODO 暂不支持,等待后续开发
//            indices.add(createGeoSpatialIndexDefinition(dotPath, collection, persistentProperty));
        }

        if (persistentProperty.isAnnotationPresent(HashIndexed.class)) {
            indices.add(createHashedIndexDefinition(dotPath, collection, persistentProperty));
        }

        return indices;
    }


    /**
     * Creates {@link HashedIndex} wrapped in {@link IndexDefinitionHolder} out of {@link HashIndexed} for a given
     * {@link SimpleMongodbPersistentProperty}.
     *
     * @param dotPath            The properties {@literal "dot"} path representation from its document root.
     * @param collection
     * @param persistentProperty
     * @return
     * @since 2.2
     */
    @Nullable
    protected IndexDefinitionHolder createHashedIndexDefinition(String dotPath, String collection,
                                                                SimpleMongodbPersistentProperty persistentProperty) {
        HashIndexed index = persistentProperty.findAnnotation(HashIndexed.class);
        if (index == null) {
            return null;
        }
        return new IndexDefinitionHolder(dotPath, HashedIndex.hashed(dotPath), collection);
    }

    @Nullable
    protected IndexDefinitionHolder createIndexDefinition(String dotPath, String collection,
                                                          SimpleMongodbPersistentProperty persistentProperty) {

        Indexed index = persistentProperty.findAnnotation(Indexed.class);

        if (index == null) {
            return null;
        }

        Index indexDefinition = new Index().on(dotPath,
                IndexDirection.ASCENDING.equals(index.direction()) ? Direction.ASC : Direction.DESC);

        if (!index.useGeneratedName()) {
            indexDefinition
                    .named(pathAwareIndexName(index.name(), dotPath, persistentProperty.getOwner(), persistentProperty));
        }

        if (index.unique()) {
            indexDefinition.unique();
        }

        if (index.sparse()) {
            indexDefinition.sparse();
        }

        if (index.background()) {
            indexDefinition.background();
        }

        if (index.expireAfterSeconds() >= 0) {
            indexDefinition.expire(index.expireAfterSeconds(), TimeUnit.SECONDS);
        }

        // TODO 暂不支持,等待后续开发
//        if (StringUtils.hasText(index.expireAfter())) {
//
//            if (index.expireAfterSeconds() >= 0) {
//                throw new IllegalStateException(String.format(
//                        "@Indexed already defines an expiration timeout of %s seconds via Indexed#expireAfterSeconds. Please make to use either expireAfterSeconds or expireAfter.",
//                        index.expireAfterSeconds()));
//            }
//
//            Duration timeout = computeIndexTimeout(index.expireAfter(),
//                    getEvaluationContextForProperty(persistentProperty.getOwner()));
//            if (!timeout.isZero() && !timeout.isNegative()) {
//                indexDefinition.expire(timeout);
//            }
//        }

//        if (StringUtils.hasText(index.partialFilter())) {
//            indexDefinition.partial(evaluatePartialFilter(index.partialFilter(), persistentProperty.getOwner()));
//        }

        return new IndexDefinitionHolder(dotPath, indexDefinition, collection);
    }

    private List<IndexDefinitionHolder> potentiallyCreateCompoundIndexDefinitions(String dotPath, String collection,
                                                                                  SimpleMongoPersistentEntity<?> entity) {

        if (entity.findAnnotation(CompoundIndexes.class) == null && entity.findAnnotation(CompoundIndex.class) == null) {
            return Collections.emptyList();
        }

        return createCompoundIndexDefinitions(dotPath, collection, entity);
    }

    /**
     * Create {@link IndexDefinition} wrapped in {@link IndexDefinitionHolder} for {@link CompoundIndexes} of a given
     * type.
     *
     * @param dotPath            The properties {@literal "dot"} path representation from its document root.
     * @param fallbackCollection
     * @param entity
     * @return
     */
    protected List<IndexDefinitionHolder> createCompoundIndexDefinitions(String dotPath, String fallbackCollection,
                                                                         SimpleMongoPersistentEntity<?> entity) {

        List<IndexDefinitionHolder> indexDefinitions = new ArrayList<>();
        CompoundIndexes indexes = entity.findAnnotation(CompoundIndexes.class);

        if (indexes != null) {
            indexDefinitions = Arrays.stream(indexes.value())
                    .map(index -> createCompoundIndexDefinition(dotPath, fallbackCollection, index, entity))
                    .collect(Collectors.toList());
        }

        CompoundIndex index = entity.findAnnotation(CompoundIndex.class);

        if (index != null) {
            indexDefinitions.add(createCompoundIndexDefinition(dotPath, fallbackCollection, index, entity));
        }

        return indexDefinitions;
    }

    protected IndexDefinitionHolder createCompoundIndexDefinition(String dotPath, String collection, CompoundIndex index,
                                                                  SimpleMongoPersistentEntity<?> entity) {

        CompoundIndexDefinition indexDefinition = new CompoundIndexDefinition(
                resolveCompoundIndexKeyFromStringDefinition(dotPath, index.def(), entity));

        if (!index.useGeneratedName()) {
            indexDefinition.named(pathAwareIndexName(index.name(), dotPath, entity, null));
        }

        if (index.unique()) {
            indexDefinition.unique();
        }

        if (index.sparse()) {
            indexDefinition.sparse();
        }

        if (index.background()) {
            indexDefinition.background();
        }

//        if (StringUtils.hasText(index.partialFilter())) {
//            indexDefinition.partial(evaluatePartialFilter(index.partialFilter(), entity));
//        }

        return new IndexDefinitionHolder(dotPath, indexDefinition, collection);
    }

    private String pathAwareIndexName(String indexName, String dotPath, @Nullable PersistentEntity<?, ?> entity,
                                      @Nullable SimpleMongodbPersistentProperty property) {

        String nameToUse = "";
        if (StringUtils.hasText(indexName)) {

            Object result = evaluate(indexName, new StandardEvaluationContext(null));

            if (result != null) {
                nameToUse = ObjectUtils.nullSafeToString(result);
            }
        }

        if (!StringUtils.hasText(dotPath) || (property != null && dotPath.equals(property.getFieldName()))) {
            return StringUtils.hasText(nameToUse) ? nameToUse : dotPath;
        }

        if (StringUtils.hasText(dotPath)) {

            nameToUse = StringUtils.hasText(nameToUse)
                    ? (property != null ? dotPath.replace("." + property.getFieldName(), "") : dotPath) + "." + nameToUse
                    : dotPath;
        }
        return nameToUse;
    }


    @Nullable
    private static Object evaluate(String value, EvaluationContext evaluationContext) {

        Expression expression = PARSER.parseExpression(value, ParserContext.TEMPLATE_EXPRESSION);
        if (expression instanceof LiteralExpression) {
            return value;
        }

        return expression.getValue(evaluationContext, Object.class);
    }

    private org.bson.Document resolveCompoundIndexKeyFromStringDefinition(String dotPath, String keyDefinitionString,
                                                                          PersistentEntity<?, ?> entity) {

        if (!StringUtils.hasText(dotPath) && !StringUtils.hasText(keyDefinitionString)) {
            throw new RuntimeException("Cannot create index on root level for empty keys.");
        }

        if (!StringUtils.hasText(keyDefinitionString)) {
            return new org.bson.Document(dotPath, 1);
        }

        Object keyDefToUse = evaluate(keyDefinitionString, new StandardEvaluationContext(null));

        org.bson.Document dbo = (keyDefToUse instanceof org.bson.Document) ? (org.bson.Document) keyDefToUse
                : org.bson.Document.parse(ObjectUtils.nullSafeToString(keyDefToUse));

        if (!StringUtils.hasText(dotPath)) {
            return dbo;
        }

        org.bson.Document document = new org.bson.Document();

        for (String key : dbo.keySet()) {
            document.put(dotPath + "." + key, dbo.get(key));
        }
        return document;
    }

    public static class CyclicPropertyReferenceException extends RuntimeException {

        private static final long serialVersionUID = -3762979307658772277L;

        private final String propertyName;
        private final @Nullable
        Class<?> type;
        private final String dotPath;

        public CyclicPropertyReferenceException(String propertyName, @Nullable Class<?> type, String dotPath) {

            this.propertyName = propertyName;
            this.type = type;
            this.dotPath = dotPath;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Throwable#getMessage()
         */
        @Override
        public String getMessage() {
            return String.format("Found cycle for field '%s' in type '%s' for path '%s'", propertyName,
                    type != null ? type.getSimpleName() : "unknown", dotPath);
        }
    }

    /**
     * Implementation of {@link IndexDefinition} holding additional (property)path information used for creating the
     * index. The path itself is the properties {@literal "dot"} path representation from its root document.
     *
     * @author Christoph Strobl
     * @since 1.5
     */
    public static class IndexDefinitionHolder implements IndexDefinition {

        private final String path;
        private final IndexDefinition indexDefinition;
        private final String collection;

        /**
         * Create
         *
         * @param path
         */
        public IndexDefinitionHolder(String path, IndexDefinition definition, String collection) {

            this.path = path;
            this.indexDefinition = definition;
            this.collection = collection;
        }

        public String getCollection() {
            return collection;
        }

        /**
         * Get the {@literal "dot"} path used to create the index.
         *
         * @return
         */
        public String getPath() {
            return path;
        }

        /**
         * Get the {@literal raw} {@link IndexDefinition}.
         *
         * @return
         */
        public IndexDefinition getIndexDefinition() {
            return indexDefinition;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.mongodb.core.index.IndexDefinition#getIndexKeys()
         */
        @Override
        public org.bson.Document getIndexKeys() {
            return indexDefinition.getIndexKeys();
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.mongodb.core.index.IndexDefinition#getIndexOptions()
         */
        @Override
        public org.bson.Document getIndexOptions() {
            return indexDefinition.getIndexOptions();
        }
    }

    /**
     * {@link CycleGuard} holds information about properties and the paths for accessing those. This information is used
     * to detect potential cycles within the references.
     *
     * @author Christoph Strobl
     * @author Mark Paluch
     */
    static class CycleGuard {

        private final Set<String> seenProperties = new HashSet<>();

        /**
         * Detect a cycle in a property path if the property was seen at least once.
         *
         * @param property The property to inspect
         * @param path     The type path under which the property can be reached.
         * @throws RuntimeException in case a potential cycle is detected.
         * @see Path#isCycle()
         */
        void protect(SimpleMongodbPersistentProperty property, Path path) {

            String propertyTypeKey = createMapKey(property);
            if (!seenProperties.add(propertyTypeKey)) {

                if (path.isCycle()) {
                    throw new CyclicPropertyReferenceException(property.getFieldName(), property.getOwner().getType(),
                            path.toCyclePath());
                }
            }
        }

        private String createMapKey(SimpleMongodbPersistentProperty property) {
            return ClassUtils.getShortName(property.getOwner().getType()) + ":" + property.getFieldName();
        }

        /**
         * Path defines the full property path from the document root. <br />
         * A {@link Path} with {@literal spring.data.mongodb} would be created for the property {@code Three.mongodb}.
         *
         * <pre>
         * <code>
         * &#64;Document
         * class One {
         *   Two spring;
         * }
         *
         * class Two {
         *   Three data;
         * }
         *
         * class Three {
         *   String mongodb;
         * }
         * </code>
         * </pre>
         *
         * @author Christoph Strobl
         * @author Mark Paluch
         */
        static class Path {

            private static final Path EMPTY = new Path(Collections.emptyList(), false);

            private final List<PersistentProperty<?>> elements;
            private final boolean cycle;

            private Path(List<PersistentProperty<?>> elements, boolean cycle) {
                this.elements = elements;
                this.cycle = cycle;
            }

            /**
             * @return an empty {@link Path}.
             * @since 1.10.8
             */
            static Path empty() {
                return EMPTY;
            }

            /**
             * Creates a new {@link Path} from the initial {@link PersistentProperty}.
             *
             * @param initial must not be {@literal null}.
             * @return the new {@link Path}.
             * @since 1.10.8
             */
            static Path of(PersistentProperty<?> initial) {
                return new Path(Collections.singletonList(initial), false);
            }

            /**
             * Creates a new {@link Path} by appending a {@link PersistentProperty breadcrumb} to the path.
             *
             * @param breadcrumb must not be {@literal null}.
             * @return the new {@link Path}.
             * @since 1.10.8
             */
            Path append(PersistentProperty<?> breadcrumb) {

                List<PersistentProperty<?>> elements = new ArrayList<>(this.elements.size() + 1);
                elements.addAll(this.elements);
                elements.add(breadcrumb);

                return new Path(elements, this.elements.contains(breadcrumb));
            }

            /**
             * @return {@literal true} if a cycle was detected.
             * @since 1.10.8
             */
            public boolean isCycle() {
                return cycle;
            }

            /*
             * (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return this.elements.isEmpty() ? "(empty)" : toPath(this.elements.iterator());
            }

            /**
             * Returns the cycle path truncated to the first discovered cycle. The result for the path
             * {@literal foo.bar.baz.bar} is {@literal bar -> baz -> bar}.
             *
             * @return the cycle path truncated to the first discovered cycle.
             * @since 1.10.8
             */
            String toCyclePath() {

                if (!cycle) {
                    return "";
                }

                for (int i = 0; i < this.elements.size(); i++) {

                    int index = indexOf(this.elements, this.elements.get(i), i + 1);

                    if (index != -1) {
                        return toPath(this.elements.subList(i, index + 1).iterator());
                    }
                }

                return toString();
            }

            private static <T> int indexOf(List<T> haystack, T needle, int offset) {

                for (int i = offset; i < haystack.size(); i++) {
                    if (haystack.get(i).equals(needle)) {
                        return i;
                    }
                }

                return -1;
            }

            private static String toPath(Iterator<PersistentProperty<?>> iterator) {

                StringBuilder builder = new StringBuilder();
                while (iterator.hasNext()) {

                    builder.append(iterator.next().getName());
                    if (iterator.hasNext()) {
                        builder.append(" -> ");
                    }
                }

                return builder.toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;

                Path that = (Path) o;

                if (this.cycle != that.cycle) {
                    return false;
                }
                return ObjectUtils.nullSafeEquals(this.elements, that.elements);
            }

            @Override
            public int hashCode() {
                int result = ObjectUtils.nullSafeHashCode(elements);
                result = 31 * result + (cycle ? 1 : 0);
                return result;
            }
        }
    }
}
