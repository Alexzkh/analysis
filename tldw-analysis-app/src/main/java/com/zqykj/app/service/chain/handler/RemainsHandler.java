package com.zqykj.app.service.chain.handler;

import com.zqykj.app.service.chain.AbstractHandler;
import com.zqykj.app.service.chain.TransferAccountAnalysisResultHandlerChain;
import com.zqykj.common.enums.CharacteristicRatioType;
import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import com.zqykj.infrastructure.util.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @Description: 其他
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Component
public class RemainsHandler extends AbstractHandler {
    @Override
    protected void doHandle(TransferAccountAnalysisRequest request, TransferAccountAnalysisResultVO context, TransferAccountAnalysisResultHandlerChain chain) {
        if (request.getOther()) {
            String accountCharacteristics = context.getAccountCharacteristics();
            if (StringUtils.isEmpty(accountCharacteristics)) {
                context.setAccountCharacteristics("其他");
            }

        }
        chain.handle(context, request);
    }

    @Override
    protected int weight() {
        return CharacteristicRatioType.OTHER.getCode();
    }
}
