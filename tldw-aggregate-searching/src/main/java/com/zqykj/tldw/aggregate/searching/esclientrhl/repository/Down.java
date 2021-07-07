package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;

/**
 * Drill down aggregate analysis return object
 * <p>
 * Drill down means that a group has been divided,
 * for example, by color, and then the data in the group will continue to be grouped again.
 * For example, a color can also be divided into groups of different brands.
 * Finally, aggregation analysis is performed on each minimum granularity group, which is called drill down analysis.
 * </p>
 **/
public class Down {
    /**
     * first class
     **/
    String level_1_key;

    /**
     * second class
     **/
    String level_2_key;

    /**
     * result
     **/
    Object value;

    public String getLevel_1_key() {
        return level_1_key;
    }

    public void setLevel_1_key(String level_1_key) {
        this.level_1_key = level_1_key;
    }

    public String getLevel_2_key() {
        return level_2_key;
    }

    public void setLevel_2_key(String level_2_key) {
        this.level_2_key = level_2_key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Down{" +
                "level_1_key='" + level_1_key + '\'' +
                ", level_2_key='" + level_2_key + '\'' +
                ", value=" + value +
                '}';
    }
}
