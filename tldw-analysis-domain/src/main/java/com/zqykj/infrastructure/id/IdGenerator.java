package com.zqykj.infrastructure.id;

import java.util.Map;

/**
 * Id generator.
 */
public interface IdGenerator {

    /**
     * Perform the corresponding initialization operation.
     */
    void init();

    /**
     * current id info.
     *
     * @return current id
     */
    long currentId();

    /**
     * Get next id.
     *
     * @return next id
     */
    long nextId();

    /**
     * Returns information for the current IDGenerator.
     *
     * @return {@link Map}
     */
    Map<Object, Object> info();

}
