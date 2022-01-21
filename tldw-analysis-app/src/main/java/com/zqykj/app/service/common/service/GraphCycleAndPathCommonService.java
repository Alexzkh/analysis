package com.zqykj.app.service.common.service;

import com.zqykj.app.service.factory.builder.query.fund.GraphResultConversionFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.common.constant.Constants;
import com.zqykj.common.vo.GraphCycleAndPathCommonParamVO;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 回路图和路径图公有方法
 * @Author zhangkehou
 * @Date 2022/1/21
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GraphCycleAndPathCommonService {


    private final EntranceRepository entranceRepository;

    private final GraphResultConversionFactory graphResultConversionFactory;

    /**
     * 获取每一条路径下的对应总表数据的id,用于获取详情使用
     *
     * @param pat:是图服务返回的数据
     * @return: java.util.List<java.lang.String> 总表id的集合
     **/
    public List<String> getRowIds(List<LinkedHashMap<String, Object>> pat) {
        List list = pat.stream().map(dataRows -> dataRows.get(Constants.AthenaGdbConstants.DATA_ROWS)).collect(Collectors.toList());
        List<String> rowIdsResult = new ArrayList<>();
        list.stream().forEach(rowIds -> {
            List<LinkedHashMap<String, Object>> row = (List<LinkedHashMap<String, Object>>) rowIds;
            row.stream().forEach(rowId -> {
                String id = String.valueOf((long) rowId.get(Constants.AthenaGdbConstants.DATA_ROWS_ID));
                rowIdsResult.add(id);
            });
        });
        return rowIdsResult;
    }


    /**
     * 获取收尾节点的公共方法
     *
     * @param node:    传入的收尾节点
     * @param request: 图路径和图回路公共参数
     * @param caseId:  案件编号
     * @return: com.zqykj.domain.Page<com.zqykj.domain.bank.BankTransactionFlow>
     **/
    public Page<BankTransactionFlow> getHeadAndTailNode(LinkedHashMap<String, Object> node, GraphCycleAndPathCommonParamVO request, String caseId) {

        // 获取头尾节点的数据集合
        List<LinkedHashMap<String, Object>> dataRows = (List<LinkedHashMap<String, Object>>) node.get(Constants.AthenaGdbConstants.DATA_ROWS);
        List<String> rowIds = dataRows.stream().map(m -> String.valueOf((long) m.get(Constants.AthenaGdbConstants.DATA_ROWS_ID))).collect(Collectors.toList());
        // 根据获取到的总表id的集合构建查询总表的参数
        QuerySpecialParams querySpecialParams = graphResultConversionFactory.accessGraphResultConversion(request.getDateRange(), request.getFund(), request.getOperator()
                , caseId, rowIds);
        PageRequest pageRequest = new PageRequest(0, 1, Sort.by(Sort.Direction.DESC, FundTacticsAnalysisField.TRADING_TIME));
        // 执行查询操作，并获取到分页结果
        Page<BankTransactionFlow> bankTransactionFlows = entranceRepository.findAll(pageRequest, caseId, BankTransactionFlow.class, querySpecialParams);

        return bankTransactionFlows;
    }
}
