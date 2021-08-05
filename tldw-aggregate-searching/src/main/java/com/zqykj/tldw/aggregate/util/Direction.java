/**
 * @作者 Mcj
 */
package com.zqykj.tldw.aggregate.util;

/**
 * Enumeration for sort directions.
 */
public enum Direction {

    ASC, DESC;

    /**
     * Enumeration for sort directions.
     * <p>
     * <p>
     * ASC, DESC;
     * <p>
     * /**
     * Returns whether the direction is ascending.
     */
    public boolean isAscending() {
        return this.equals(ASC);
    }

    /**
     * Returns whether the direction is descending.
     *
     * @return
     * @since 1.13
     */
    public boolean isDescending() {
        return this.equals(DESC);
    }
}
