/**
 * @author Mcj
 */
package com.zqykj.repository.config;

import com.zqykj.repository.query.QueryLookupStrategy;
import com.zqykj.util.Streamable;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * <h1> RepositoryConfiguration </h1>
 */
public interface RepositoryConfiguration<T extends RepositoryConfigurationSource> {

    /**
     * <h2> 返回在其下扫描存储库的基本包 </h2>
     */
    Streamable<String> getBasePackages();

    /**
     * <h2> Returns the interface name of the repository. </h2>
     */
    String getRepositoryInterface();

    /**
     * Returns the key to resolve a {@link QueryLookupStrategy} from eventually.
     *
     * @see QueryLookupStrategy.Key
     */
    Object getQueryLookupStrategyKey();

    /**
     * Returns the location of the file containing Spring Data named queries.
     */
    Optional<String> getNamedQueriesLocation();

    /**
     * Returns the name of the repository base class to be used or {@literal null} if the store specific defaults shall be
     * applied.
     */
    Optional<String> getRepositoryBaseClassName();

    /**
     * Returns the name of the repository factory bean class to be used.
     */
    String getRepositoryFactoryBeanClassName();

    /**
     * Returns the source of the {@link RepositoryConfiguration}.
     */
    @Nullable
    Object getSource();

    /**
     * Returns the {@link RepositoryConfigurationSource} that backs the {@link RepositoryConfiguration}.
     */
    T getConfigurationSource();

    /**
     * Returns whether to initialize the repository proxy lazily.
     */
    boolean isLazyInit();

    /**
     * Returns whether the repository is the primary one for its type.
     *
     * @return {@literal true} whether the repository is the primary one for its type.
     */
    boolean isPrimary();

    /**
     * Returns the {@link TypeFilter}s to be used to exclude packages from repository scanning.
     */
    Streamable<TypeFilter> getExcludeFilters();
}
