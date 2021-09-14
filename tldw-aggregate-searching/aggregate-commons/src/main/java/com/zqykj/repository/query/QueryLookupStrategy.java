/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import com.zqykj.repository.core.NamedQueries;
import com.zqykj.repository.core.RepositoryMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * <h1> Strategy interface for which way to lookup {@link RepositoryQuery}s. </h1>
 */
public interface QueryLookupStrategy {

    public static enum Key {

        CREATE, USE_DECLARED_QUERY, CREATE_IF_NOT_FOUND;

        /**
         * <h2> Returns a strategy key from the given XML value. </h2>
         *
         * @param xml
         * @return a strategy key from the given XML value
         */
        @Nullable
        public static Key create(String xml) {

            if (!StringUtils.hasText(xml)) {
                return null;
            }

            return valueOf(xml.toUpperCase(Locale.US).replace("-", "_"));
        }
    }

    /**
     * <h2> Resolves a {@link RepositoryQuery} from the given {@link QueryMethod} that can be executed afterwards. </h2>
     *
     * @param method       will never be {@literal null}.
     * @param metadata     will never be {@literal null}.
     * @param namedQueries will never be {@literal null}.
     */
    RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata,
                                 NamedQueries namedQueries);
}
