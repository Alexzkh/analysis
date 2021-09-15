/**
 * @作者 Mcj
 */
package com.zqykj.core.convert;

import com.zqykj.core.document.Document;
import com.zqykj.core.mapping.ElasticsearchPersistentEntity;
import com.zqykj.core.mapping.ElasticsearchPersistentProperty;
import com.zqykj.coverter.EntityConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * <h1> ElasticsearchConverter </h1>
 */
public interface ElasticsearchConverter
        extends EntityConverter<ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty, Object, Document> {

    default String convertId(Object idValue) {

        Assert.notNull(idValue, "idValue must not be null!");

        if (!getConversionService().canConvert(idValue.getClass(), String.class)) {
            return idValue.toString();
        }

        return getConversionService().convert(idValue, String.class);
    }

    /**
     * <h2> 将对象映射到一个 {@link Document}. </h2>
     *
     * @param source the object to map      要映射的对象
     * @return will not be {@literal null}.
     */
    default Document mapObject(@Nullable Object source) {

        Document target = Document.create();

        if (source != null) {
            write(source, target);
        }
        return target;
    }
}
