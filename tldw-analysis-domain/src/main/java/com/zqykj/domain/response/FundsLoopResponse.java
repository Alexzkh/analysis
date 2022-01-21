package com.zqykj.domain.response;


import com.zqykj.domain.vo.FundsLoopResultListVO;
import com.zqykj.infrastructure.compare.AbstractMultiLevelSortVO;
import lombok.Data;

import java.util.List;

/**
 * @Description: 资金回路分析结果响应body
 * @Author zhangkehou
 * @Date 2022/01/18
 */
@Data
public class FundsLoopResponse extends AbstractMultiLevelSortVO<FundsLoopResultListVO> {

    private List<FundsLoopResultListVO> results;

    @Override
    public List<FundsLoopResultListVO> provideSortList() {
        return results;
    }

    @Override
    public void updateSortList(List<FundsLoopResultListVO> list) {
        results = list;
    }
}
