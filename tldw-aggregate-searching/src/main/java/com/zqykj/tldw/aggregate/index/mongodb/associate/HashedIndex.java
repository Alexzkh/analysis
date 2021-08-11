/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zqykj.tldw.aggregate.index.mongodb.associate;

import org.bson.Document;
import org.springframework.util.Assert;

/**
 * {@link IndexDefinition} implementation for MongoDB
 * <a href="https://docs.mongodb.com/manual/core/index-hashed/">Hashed Indexes</a> maintaining entries with hashes of
 * the values of the indexed field.
 */
public class HashedIndex implements IndexDefinition {

    private final String field;

    private HashedIndex(String field) {

        Assert.hasText(field, "Field must not be null nor empty!");
        this.field = field;
    }

    /**
     * Creates a new {@link HashedIndex} for the given field.
     *
     * @param field must not be {@literal null} nor empty.
     * @return new instance of {@link HashedIndex}.
     */
    public static HashedIndex hashed(String field) {
        return new HashedIndex(field);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.core.index.IndexDefinition#getIndexKeys()
     */
    @Override
    public Document getIndexKeys() {
        return new Document(field, "hashed");
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.core.index.IndexDefinition#getIndexOptions()
     */
    @Override
    public Document getIndexOptions() {
        return new Document();
    }
}
