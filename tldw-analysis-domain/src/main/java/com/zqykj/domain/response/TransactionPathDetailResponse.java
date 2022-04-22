package com.zqykj.domain.response;

import com.zqykj.domain.vo.TransactionPathDetailResultVO;
import com.zqykj.infrastructure.compare.AbstractMultiLevelSortVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Description: 交易路径详情返回体
 * @Author zhangkehou
 * @Date 2021/12/18
 */
@Data
@Builder
public class TransactionPathDetailResponse extends AbstractMultiLevelSortVO<TransactionPathDetailResultVO> {

    private List<TransactionPathDetailResultVO> results;

    @Override
    public List<TransactionPathDetailResultVO> provideSortList() {
        return results;
    }

    @Override
    public void updateSortList(List<TransactionPathDetailResultVO> list) {
        results = list;
    }
}
