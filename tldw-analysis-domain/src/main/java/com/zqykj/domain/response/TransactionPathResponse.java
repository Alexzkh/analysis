package com.zqykj.domain.response;


import com.zqykj.domain.vo.TransactionPathResultVO;
import com.zqykj.infrastructure.compare.AbstractMultiLevelSortVO;
import lombok.Data;

import java.util.List;

/**
 * @Description: 交易路径分析响应body
 * @Author zhangkehou
 * @Date 2021/12/10
 */
@Data
public class TransactionPathResponse extends AbstractMultiLevelSortVO<TransactionPathResultVO> {

    private List<TransactionPathResultVO> results;

    @Override
    public List<TransactionPathResultVO> provideSortList() {
        return results;
    }

    @Override
    public void updateSortList(List<TransactionPathResultVO> list) {
        results = list;
    }
}
