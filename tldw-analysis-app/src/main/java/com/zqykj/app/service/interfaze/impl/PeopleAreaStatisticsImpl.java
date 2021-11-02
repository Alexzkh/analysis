package com.zqykj.app.service.interfaze.impl;

import com.zqykj.app.service.interfaze.IPeopleAreaStatistics;
import com.zqykj.app.service.strategy.AggregateResultConversionAccessor;
import com.zqykj.app.service.strategy.PeopleAreaResultConversionAccessor;
import com.zqykj.common.enums.TacticsTypeEnum;
import com.zqykj.common.request.PeopleAreaDetailRequest;
import com.zqykj.common.request.PeopleAreaRequest;
import com.zqykj.common.response.PeopleAreaReponse;
import com.zqykj.domain.Page;
import com.zqykj.domain.PageRequest;
import com.zqykj.domain.Sort;
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
import java.util.Map;

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

    private final PeopleAreaResultConversionAccessor peopleAreaResultConversionAccessor;

    @Override
    public List<PeopleAreaReponse> accessPeopleAreaStatisticsData(PeopleAreaRequest peopleAreaRequest, String caseId) {
        /**
         * 构建人员地域数据聚合统计公共查询请求
         * */
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.
                bulidPeopleAreaAnalysisRequest(peopleAreaRequest, caseId);

        /**
         * 构建人员地域数据聚合统计公共数据聚合请求体
         * */
        AggregationParams aggregationParams = aggregationRequestParamFactory.createPeopleAreaQueryAgg(peopleAreaRequest);

        aggregationParams.setResultName("peopleArea");

        /**
         * 根据查询请求和聚合请求体,查询elastisearch,并获取到结果
         * */
        Map<String, List<List<Object>>> result = entranceRepository.compoundQueryAndAgg(querySpecialParams, aggregationParams, PeopleArea.class, caseId);


        /**
         * 根据聚合模块返回的数据,进行封装返回给业务层
         * */
        List<PeopleAreaReponse> responses = (List<PeopleAreaReponse>) peopleAreaResultConversionAccessor.access(result.get(aggregationParams.getResultName()), TacticsTypeEnum.PEOPLE_AREA);

        return responses;
    }

    @Override
    public Page<PeopleArea> accessPeopleAreaStatisticsDetail(PeopleAreaDetailRequest peopleAreaDetailRequest, String caseId) {
        QuerySpecialParams querySpecialParams = queryRequestParamFactory.bulidPeopleAreaDetailAnalysisRequest(peopleAreaDetailRequest, caseId);
        PageRequest pageRequest = PageRequest.of(peopleAreaDetailRequest.getQueryRequest().getPaging().getPage(),
                peopleAreaDetailRequest.getQueryRequest().getPaging().getPageSize(),
                peopleAreaDetailRequest.getQueryRequest().getSorting().getOrder().isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC,
                peopleAreaDetailRequest.getQueryRequest().getSorting().getProperty());
        Page<PeopleArea> result = (Page<PeopleArea>) entranceRepository.compoundQueryWithoutAgg(pageRequest, querySpecialParams, PeopleArea.class, caseId);
        return result;
    }
}
