/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.data.query.elasticsearch;

import com.zqykj.annotations.Highlight;
import com.zqykj.annotations.HighlightField;
import com.zqykj.annotations.HighlightParameters;
import org.elasticsearch.search.fetch.subphase.highlight.AbstractHighlighterBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * <h1> 高亮查询定义 </h1>
 *
 * @author Mcj
 */
public class HighlightQuery {

    private final HighlightBuilder highlightBuilder;


    public HighlightQuery(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    static class HighlightQueryBuilder {

        public HighlightQuery getHighlightQuery(Highlight highlight, @Nullable Class<?> type) {

            Assert.notNull(highlight, "highlight must not be null");

            HighlightBuilder highlightBuilder = new HighlightBuilder();

            addParameters(highlight.parameters(), highlightBuilder, type);

            for (HighlightField highlightField : highlight.fields()) {
                HighlightBuilder.Field field = new HighlightBuilder.Field(highlightField.name());

                addParameters(highlightField.parameters(), field, type);

                highlightBuilder.field(field);
            }

            return new HighlightQuery(highlightBuilder);
        }

        private void addParameters(HighlightParameters parameters, AbstractHighlighterBuilder<?> builder, Class<?> type) {

            if (StringUtils.hasLength(parameters.boundaryChars())) {
                builder.boundaryChars(parameters.boundaryChars().toCharArray());
            }

            if (parameters.boundaryMaxScan() > -1) {
                builder.boundaryMaxScan(parameters.boundaryMaxScan());
            }

            if (StringUtils.hasLength(parameters.boundaryScanner())) {
                builder.boundaryScannerType(parameters.boundaryScanner());
            }

            if (StringUtils.hasLength(parameters.boundaryScannerLocale())) {
                builder.boundaryScannerLocale(parameters.boundaryScannerLocale());
            }

            if (parameters.forceSource()) { // default is false
                builder.forceSource(parameters.forceSource());
            }

            if (StringUtils.hasLength(parameters.fragmenter())) {
                builder.fragmenter(parameters.fragmenter());
            }

            if (parameters.fragmentSize() > -1) {
                builder.fragmentSize(parameters.fragmentSize());
            }

            if (parameters.noMatchSize() > -1) {
                builder.noMatchSize(parameters.noMatchSize());
            }

            if (parameters.numberOfFragments() > -1) {
                builder.numOfFragments(parameters.numberOfFragments());
            }

            if (StringUtils.hasLength(parameters.order())) {
                builder.order(parameters.order());
            }

            if (parameters.phraseLimit() > -1) {
                builder.phraseLimit(parameters.phraseLimit());
            }

            if (parameters.preTags().length > 0) {
                builder.preTags(parameters.preTags());
            }

            if (parameters.postTags().length > 0) {
                builder.postTags(parameters.postTags());
            }

            if (!parameters.requireFieldMatch()) { // default is true
                builder.requireFieldMatch(parameters.requireFieldMatch());
            }

            if (StringUtils.hasLength(parameters.type())) {
                builder.highlighterType(parameters.type());
            }

            if (builder instanceof HighlightBuilder) {
                HighlightBuilder highlightBuilder = (HighlightBuilder) builder;

                if (StringUtils.hasLength(parameters.encoder())) {
                    highlightBuilder.encoder(parameters.encoder());
                }

                if (StringUtils.hasLength(parameters.tagsSchema())) {
                    highlightBuilder.tagsSchema(parameters.tagsSchema());
                }
            }

            if (builder instanceof HighlightBuilder.Field) {
                HighlightBuilder.Field field = (HighlightBuilder.Field) builder;

                if (parameters.fragmentOffset() > -1) {
                    field.fragmentOffset(parameters.fragmentOffset());
                }

                if (parameters.matchedFields().length > 0) {
                    //
                    field.matchedFields(Arrays.stream(parameters.matchedFields()) //
                            .collect(Collectors.toList()) //
                            .toArray(new String[]{})); //
                }
            }
        }
    }
}
