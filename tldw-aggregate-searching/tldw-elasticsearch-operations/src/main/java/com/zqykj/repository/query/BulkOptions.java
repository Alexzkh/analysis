/**
 * @作者 Mcj
 */
package com.zqykj.repository.query;

import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.core.TimeValue;
import org.springframework.lang.Nullable;

/**
 * <h1> 批量操作选项 </h1>
 */
public class BulkOptions {

    private static final BulkOptions defaultOptions = builder().build();

    private final @Nullable
    TimeValue timeout;
    private final @Nullable
    WriteRequest.RefreshPolicy refreshPolicy;
    private final @Nullable
    ActiveShardCount waitForActiveShards;
    private final @Nullable
    String pipeline;
    private final @Nullable
    String routingId;

    private BulkOptions(@Nullable TimeValue timeout, @Nullable WriteRequest.RefreshPolicy refreshPolicy,
                        @Nullable ActiveShardCount waitForActiveShards, @Nullable String pipeline, @Nullable String routingId) {
        this.timeout = timeout;
        this.refreshPolicy = refreshPolicy;
        this.waitForActiveShards = waitForActiveShards;
        this.pipeline = pipeline;
        this.routingId = routingId;
    }

    @Nullable
    public TimeValue getTimeout() {
        return timeout;
    }

    @Nullable
    public WriteRequest.RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    @Nullable
    public ActiveShardCount getWaitForActiveShards() {
        return waitForActiveShards;
    }

    @Nullable
    public String getPipeline() {
        return pipeline;
    }

    @Nullable
    public String getRoutingId() {
        return routingId;
    }

    /**
     * Create a new {@link BulkOptionsBuilder} to build {@link BulkOptions}.
     *
     * @return a new {@link BulkOptionsBuilder} to build {@link BulkOptions}.
     */
    public static BulkOptionsBuilder builder() {
        return new BulkOptionsBuilder();
    }

    /**
     * Return default {@link BulkOptions}.
     *
     * @return default {@link BulkOptions}.
     */
    public static BulkOptions defaultOptions() {
        return defaultOptions;
    }

    /**
     * Builder for {@link BulkOptions}.
     */
    public static class BulkOptionsBuilder {

        private @Nullable
        TimeValue timeout;
        private @Nullable
        WriteRequest.RefreshPolicy refreshPolicy;
        private @Nullable
        ActiveShardCount waitForActiveShards;
        private @Nullable
        String pipeline;
        private @Nullable
        String routingId;

        private BulkOptionsBuilder() {
        }

        public BulkOptionsBuilder withTimeout(TimeValue timeout) {
            this.timeout = timeout;
            return this;
        }

        public BulkOptionsBuilder withRefreshPolicy(WriteRequest.RefreshPolicy refreshPolicy) {
            this.refreshPolicy = refreshPolicy;
            return this;
        }

        public BulkOptionsBuilder withWaitForActiveShards(ActiveShardCount waitForActiveShards) {
            this.waitForActiveShards = waitForActiveShards;
            return this;
        }

        public BulkOptionsBuilder withPipeline(String pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        public BulkOptionsBuilder withRoutingId(String routingId) {
            this.routingId = routingId;
            return this;
        }

        public BulkOptions build() {
            return new BulkOptions(timeout, refreshPolicy, waitForActiveShards, pipeline, routingId);
        }
    }
}
