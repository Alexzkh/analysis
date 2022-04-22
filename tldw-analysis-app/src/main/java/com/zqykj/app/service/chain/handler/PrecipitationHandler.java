package com.zqykj.app.service.chain.handler;

import com.zqykj.app.service.chain.AbstractHandler;
import com.zqykj.app.service.chain.TransferAccountAnalysisResultHandlerChain;
import com.zqykj.common.enums.CharacteristicRatioType;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import com.zqykj.infrastructure.util.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 具体的处理者--中转处理器
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Component
public class PrecipitationHandler extends AbstractHandler {
    @Override
    protected void doHandle(TransferAccountAnalysisRequest request, TransferAccountAnalysisResultVO context, TransferAccountAnalysisResultHandlerChain chain) {
        if (request.getPrecipitate()) {
            BigDecimal bigDecimal =context.getCreditsAmount().divide(context.getTradeTotalAmount(),2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP);
            Integer result = bigDecimal.intValue();
            if (result.compareTo(request.getCharacteristicRatio().getPrecipitate()) != -1) {
                String accountCharacteristics = context.getAccountCharacteristics();
                if (StringUtils.isNotEmpty(accountCharacteristics)){
                    StringBuilder temp = new StringBuilder(accountCharacteristics);
                    temp.append("沉淀 ");
                    context.setAccountCharacteristics(temp.toString());
                }else {
                    context.setAccountCharacteristics("沉淀 ");
                }

            }
        }
        chain.handle(context,request);
    }

    @Override
    protected int weight() {
        return CharacteristicRatioType.PRECIPITATE.getCode();
    }
}
