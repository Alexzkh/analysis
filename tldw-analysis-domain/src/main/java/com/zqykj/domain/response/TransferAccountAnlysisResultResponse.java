package com.zqykj.domain.response;

import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import com.zqykj.infrastructure.compare.AbstractMultiLevelSortVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Description: 调单个体详情返回体
 * @Author zhangkehou
 * @Date 2021/12/18
 */
@Data
@Builder
public class TransferAccountAnlysisResultResponse extends AbstractMultiLevelSortVO<TransferAccountAnalysisResultVO> {

    private List<TransferAccountAnalysisResultVO> results;

    @Override
    public List<TransferAccountAnalysisResultVO> provideSortList() {
        return results;
    }

    @Override
    public void updateSortList(List<TransferAccountAnalysisResultVO> list) {
        results = list;
    }
}
