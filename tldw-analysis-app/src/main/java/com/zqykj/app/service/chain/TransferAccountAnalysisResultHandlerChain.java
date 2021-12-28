package com.zqykj.app.service.chain;

import com.zqykj.common.request.TransferAccountAnalysisRequest;
import com.zqykj.domain.vo.TransferAccountAnalysisResultVO;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @Description: 初始化调单账号特征分析结果处理器链
 * @Author zhangkehou
 * @Date 2021/12/27
 */
@Component
public class TransferAccountAnalysisResultHandlerChain implements ApplicationContextAware {
    private List<AbstractHandler> chain = new ArrayList<>();

    public void handle(TransferAccountAnalysisResultVO context, TransferAccountAnalysisRequest request) {
        if (context.getPos() < chain.size()) {
            AbstractHandler handler = chain.get(context.getPos());
            // 移动位于处理器链中的位置
            context.setPos(context.getPos() + 1);
            handler.doHandle(request, context, this);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, AbstractHandler> beans = applicationContext.getBeansOfType(AbstractHandler.class);
        chain.addAll(beans.values());
        // 根据处理器的权重，对处理器链中元素进行排序
        chain.sort(Comparator.comparingInt(AbstractHandler::weight));
    }
}
