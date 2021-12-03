package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.factory.builder.query.fund.FundTrackingQueryBuilderFactory;
import com.zqykj.app.service.field.FundTacticsAnalysisField;
import com.zqykj.app.service.interfaze.IFundTracking;
import com.zqykj.common.request.FundTrackingRequest;
import com.zqykj.common.request.GraduallyTrackingRequest;
import com.zqykj.common.response.FundTrackingResponse;
import com.zqykj.common.response.GraduallyTrackingResponse;
import com.zqykj.common.vo.TrackingNode;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageImpl;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
import com.zqykj.domain.bank.BankTransactionFlow;
import com.zqykj.parameters.FieldSort;
import com.zqykj.parameters.Pagination;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 资金追踪业务实现类
 * @Author zhangkehou
 * @Date 2021/11/29
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FundTrackingImpl implements IFundTracking {

    private final EntranceRepository entranceRepository;

    private final FundTrackingQueryBuilderFactory fundTrackingQueryBuilderFactory;

    @Value("${global.chunkSize:1000}")
    private int globalChunkSize;


    @Override
    public Page<FundTrackingResponse> accessFundTrackingResult(FundTrackingRequest request, String caseId) {

        QuerySpecialParams querySpecialParams = fundTrackingQueryBuilderFactory.accessFundTrackingList(request, caseId);
        Integer page = request.getPaging().getPage() - 1;
        Integer pageSize = request.getPaging().getPageSize();
        querySpecialParams.setPagination(new Pagination(page * pageSize, pageSize));
        querySpecialParams.setSort(new FieldSort(FundTacticsAnalysisField.TRADING_TIME, Sort.Direction.DESC.name()));
        PageRequest pageRequest = new PageRequest(page, pageSize, Sort.by(Sort.Direction.DESC, "trading_time"));
        Page<BankTransactionFlow> page1 = entranceRepository.findAll(pageRequest, caseId, BankTransactionFlow.class, querySpecialParams);
        List<FundTrackingResponse> result = new ArrayList<>();
        // 解析elasticsearch返回结果
        page1.getContent().stream().forEach(bankTransactionFlow -> {
                    FundTrackingResponse fundTrackingResponse = FundTrackingResponse.builder()
                            .loanFlag(bankTransactionFlow.getLoanFlag())
                            .oppositeCard(bankTransactionFlow.getTransactionOppositeCard())
                            .oppositeName(bankTransactionFlow.getTransactionOppositeName())
                            .queryCard(bankTransactionFlow.getQueryCard())
                            .queryName(bankTransactionFlow.getCustomerName())
                            .tradingTime(bankTransactionFlow.getTradingTime())
                            .tradingType(bankTransactionFlow.getTransactionType())
                            .transactionMoney(new BigDecimal(bankTransactionFlow.getTransactionMoney()))
                            .transactionSummary(bankTransactionFlow.getTransactionSummary())
                            .build();
                    result.add(fundTrackingResponse);

                }

        );
        Page<FundTrackingResponse> responses = new PageImpl<>(result, pageRequest, (long) page1.getTotalPages());
        return responses;
    }

    /**
     * attention:
     * 1、时间间隔:追踪节点开始往后的时间间隔范围内的时间。
     * 2、金额偏差:追踪节点的交易金额±(追踪节点的交易金额*金额偏差)
     * 3、结果:时间间隔范围内正序的交易，并且在满足金额偏差范围内前10000个节点
     */
    @Override
    public GraduallyTrackingResponse accessGraduallyTrackingResult(GraduallyTrackingRequest request, String caseId) throws Exception {
        QuerySpecialParams querySpecialParams = fundTrackingQueryBuilderFactory.accessFundTrackingResult(request, caseId);

        querySpecialParams.setSort(new FieldSort(FundTacticsAnalysisField.TRADING_TIME, Sort.Direction.DESC.name()));
        Page<BankTransactionFlow> page1 = entranceRepository.findAll(new PageRequest(0, globalChunkSize, Sort.by(Sort.Direction.ASC, "trading_time")), caseId, BankTransactionFlow.class, querySpecialParams);

        BigDecimal bigDecimal = request.getNext().getAmount().multiply(new BigDecimal((double) request.getAmountDeviation() / 100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal upper = request.getNext().getAmount().add(bigDecimal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal down = request.getNext().getAmount().subtract(bigDecimal).setScale(2, RoundingMode.HALF_UP);
        List<TrackingNode> trackingNodes = new ArrayList<>();
        BigDecimal temp = new BigDecimal(0);

        for (BankTransactionFlow value : page1.getContent()) {
            temp = temp.add(new BigDecimal(value.getTransactionMoney()));

            /**
             * 当金额累加的金额在金额偏差的上线以下时，此时追踪的节点才奏效。
             * */
            if (temp.compareTo(upper) == -1) {
                TrackingNode trackingNode = TrackingNode.builder()
                        .tradingTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value.getTradingTime()))
                        .cardNumber(value.getTransactionOppositeCard())
                        .name(value.getTransactionOppositeName())
                        .amount(new BigDecimal(value.getTransactionMoney()))
                        .build();
                trackingNodes.add(trackingNode);
            } else {
                break;
            }

        }

        /**
         * 当累加的金额范围小于金额偏差的下限也是算没有追踪到数据
         * */
        if (temp.compareTo(down) == -1) {
            trackingNodes = new ArrayList<>();
        }

        return GraduallyTrackingResponse.builder()
                .start(request.getStart())
                .next(request.getNext())
                .trackingNodes(trackingNodes)
                .build();
    }
}
