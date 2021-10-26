package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IPeopleAreaStatistics;
import com.zqykj.app.service.strategy.AggregateResultConversionAccessor;
import com.zqykj.common.enums.TacticsTypeEnum;
import com.zqykj.common.request.PeopleAreaRequest;
import com.zqykj.common.response.PeopleAreaReponse;
import com.zqykj.domain.bank.PeopleArea;
import com.zqykj.factory.AggregationRequestParamFactory;
import com.zqykj.factory.QueryRequestParamFactory;
import com.zqykj.parameters.aggregate.AggregationParams;
import com.zqykj.parameters.query.QuerySpecialParams;
import com.zqykj.repository.EntranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 人员地域战法统计业务接口实现类.
 * @Author zhangkehou
 * @Date 2021/10/26
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PeopleAreaStatisticsImpl implements IPeopleAreaStatistics {


    private final EntranceRepository entranceRepository;

    private final AggregationRequestParamFactory aggregationRequestParamFactory;

    private final QueryRequestParamFactory queryRequestParamFactory;

    private final AggregateResultConversionAccessor aggregateResultConversionAccessor;

    @Override
    public List<PeopleAreaReponse>  accessPeopleAreaStatisticsData(PeopleAreaRequest peopleAreaRequest, String caseId) {
        /**
         * 构建人员地域数据聚合统计公共查询请求
         * */
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.
                bulidPeopleAreaAnalysisRequest(peopleAreaRequest, caseId);

        /**
         * 构建人员地域数据聚合统计公共数据聚合请求体
         * */
        AggregationParams aggregationParams = aggregationRequestParamFactory.createPeopleAreaQueryAgg(peopleAreaRequest);

        /**
         * 根据查询请求和聚合请求体,查询elastisearch,并获取到结果
         * */
        List<List<Object>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, PeopleArea.class, "457eea4b3ebe46aabc604b9183a83920");


        /**
         * 根据聚合模块返回的数据,进行封装返回给业务层
         * */
        List<PeopleAreaReponse> responses = (List<PeopleAreaReponse>) aggregateResultConversionAccessor.access(result, caseId, null, TacticsTypeEnum.ASSET_TRENDS);

        return responses;
    }
}
