package com.zqykj.app.service.chain.handler;

import com.zqykj.app.service.chain.AbstractHandler;
import com.zqykj.app.service.chain.TransferAccountAnalysisResultHandlerChain;
import com.zqykj.common.enums.CharacteristicRatioType;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 具体的处理者--来源处理器
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Component
public class SourceHandler extends AbstractHandler {
    @Override
    protected void doHandle(TransferAccountAnalysisRequest request, TransferAccountAnalysisResultVO context, TransferAccountAnalysisResultHandlerChain chain) {
        if (request.getSource()) {
            BigDecimal bigDecimal = context.getPayOutAmount().divide(context.getTradeTotalAmount(),2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP);
            Integer result = bigDecimal.intValue();
            if (result.compareTo(request.getCharacteristicRatio().getSource()) != -1) {
                context.setAccountCharacteristics("来源 ");
            }
        }
        chain.handle(context,request);
    }

    @Override
    protected int weight() {
        return CharacteristicRatioType.SOURCE.getCode();
    }
}
