package com.zqykj.domain.response;

import com.zqykj.domain.vo.TimeRuleResultListVO;
import com.zqykj.infrastructure.compare.AbstractMultiLevelSortVO;
import lombok.Data;

import java.util.List;

/**
 * @Description: 时间规律结果数据返回body
 * @Author zhangkehou
 * @Date 2021/12/30
 */
@Data
public class TimeRuleResultListResponse extends AbstractMultiLevelSortVO<TimeRuleResultListVO> {


    private List<TimeRuleResultListVO> results;

    @Override
    public List<TimeRuleResultListVO> provideSortList() {
        return results;
    }

    @Override
    public void updateSortList(List<TimeRuleResultListVO> list) {
        this.results = list;
    }
}
