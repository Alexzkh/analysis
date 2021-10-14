/**
 * @作者 Mcj
 */
package com.zqykj.common.vo;

public enum Direction {
    ASC, DESC;

    public boolean isAscending() {
        return this.equals(ASC);
    }

    public boolean isDescending() {
        return this.equals(DESC);
    }
}
