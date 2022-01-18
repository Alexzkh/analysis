package com.zqykj.infrastructure.compare;

import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Transient;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zqykj.domain.Sort.Direction.ASC;

/**
 * @Description: 排序vo
 * @Author zhangkehou
 * @Date 2021/12/15
 */
public abstract class AbstractMultiLevelSortVO<T extends BaseCompareBean> {

    Logger logger = LoggerFactory.getLogger(AbstractMultiLevelSortVO.class);

    //子类提供实现方法，通常就是返回需要比较的集合
    @Transient
    public abstract List<T> provideSortList();

    //子类提供实现方法，通常就是覆盖子类中的集合
    @Transient
    public abstract void updateSortList(List<T> list);

    @Transient
    public List<T> doOrderOrPaging(String orderField, Sort.Direction orderType) {
        return doOrderOrPaging(new PageRequest(0, 25, Sort.by(orderType, orderField)));
    }

    @Transient
    public List<T> doOrderOrPaging(Integer page, Integer size, String orderField, String orderType) {
        return doOrderOrPaging(new PageRequest(page, size, Sort.by(orderType.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, orderField)));
    }

    @Transient
    public List<T> doOrderOrPaging(PageRequest req) {
        String orderField = req.getSort().getOrders().get(0).getProperty();
        Sort.Direction orderType = req.getSort().getOrders().get(0).getDirection();
        //取子类中的集合
        Stream<T> tickerStream = provideSortList().stream();
        Comparator<T> comparator = (o1, o2) -> doCompare(orderField, o1, o2, ASC.equals(orderType));
        if (!ASC.equals(orderType)) {
            comparator = comparator.reversed();
        }
        tickerStream = tickerStream.sorted(comparator);
        Integer pageSize = req.getPageSize();
        Integer pageNum = req.getPageNumber();
        tickerStream = tickerStream.skip(pageSize * (pageNum - 1)).limit(pageSize);

        List<T> list = tickerStream.collect(Collectors.toList());
        //更新子类中的集合
        updateSortList(list);
        return list;
    }


    @Transient
    public int doCompare(String orderField, T o1, T o2, boolean asc) {
        try {
            /*
                空边界之一
                先解决bean为空的情况，原则：无论升序还是降序，空bean无需比较，直接往后放
             */
            if (o1 == null || o1.abnormal()) {
                return thisValuePutBehind(asc);
            }
            if (o2 == null || o2.abnormal()) {
                return thisValuePutForward(asc);
            }
            Comparable v1 = o1.tryBestToFindCompareValue(orderField);
            Comparable v2 = o2.tryBestToFindCompareValue(orderField);
            return compareThinkAboutNull(v1, v2, asc);
        } catch (Exception e) {
            logger.error("获取指定比较字段失败，放弃比较。 orderField {} e {}", orderField, ExceptionUtils.getRootCause(e));
            //若仍有意外发生，也先往后放
            return thisValuePutBehind(asc);
        }
    }

    /**
     * 根据排序方式 把当前值往前放
     *
     * @param asc
     * @return
     */
    private int thisValuePutForward(boolean asc) {
        return asc ? -1 : 1;
    }

    /**
     * 根据排序方式 把当前值往后放
     *
     * @param asc
     * @return
     */
    private int thisValuePutBehind(boolean asc) {
        return asc ? 1 : -1;
    }

    private int compareThinkAboutNull(Comparable v1, Comparable v2, boolean asc) {
        /*
            空边界之二
            解决最后真正比较字段的空问题，原则相同
         */
        if (v1 == null) {
            return thisValuePutBehind(asc);
        }
        if (v2 == null) {
            return thisValuePutForward(asc);
        }
        return v1.compareTo(v2);
    }

}
