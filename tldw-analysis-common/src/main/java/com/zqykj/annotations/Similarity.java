/**
 * @作者 Mcj
 */
package com.zqykj.annotations;

public enum Similarity {

    Default("default"), BM25("BM25"),

    classic("classic"), Boolean("boolean");

    // need to use a custom name because 'boolean' can't be used as enum name
    private final String toStringName;

    Similarity(String name) {
        this.toStringName = name;
    }

    @Override
    public String toString() {
        return toStringName;
    }
}
