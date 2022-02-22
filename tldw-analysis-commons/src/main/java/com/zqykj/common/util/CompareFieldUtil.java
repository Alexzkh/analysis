/**
 * @作者 Mcj
 */
package com.zqykj.common.util;

import com.zqykj.common.util.overrideclass.BeanComparator;
import org.apache.commons.collections4.comparators.ComparatorChain;

import java.util.Comparator;
import java.util.List;

/**
 * <h1> 根据多字段动态排序 </h1>
 */
public class CompareFieldUtil {

    /**
     * <h2> 排序 </h2>
     *
     * @param sortData  需要排序的数据
     * @param isReverse 是否降序排序
     * @param fields    排序字段
     */
    public static <T> void sort(List<T> sortData, boolean isReverse, String... fields) {

        ComparatorChain<T> chain = new ComparatorChain<>();
        for (String field : fields) {
            chain.addComparator(new BeanComparator<>(field, Comparator.nullsLast(Comparator.naturalOrder())), isReverse);
        }
        sortData.sort(chain);
    }
}
