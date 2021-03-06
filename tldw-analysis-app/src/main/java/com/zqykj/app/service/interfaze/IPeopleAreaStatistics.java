package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.PeopleAreaDetailRequest;
import com.zqykj.common.request.PeopleAreaRequest;
import com.zqykj.common.response.PeopleAreaResponse;
import com.zqykj.domain.Page;
import com.zqykj.domain.bank.PeopleArea;

import java.util.List;

/**
 * @Description: 人员地域数据业务接口
 * @Author zhangkehou
 * @Date 2021/10/26
 */
public interface IPeopleAreaStatistics {


    /**
     * 用于获取人员地域数据。
     *
     * @param peopleAreaRequest: 人员地域数据请求体
     * @param caseId:
     * @return: com.zqykj.common.response.PeopleAreaResponse
     **/
    List<PeopleAreaResponse> accessPeopleAreaStatisticsData(PeopleAreaRequest peopleAreaRequest, String caseId);


    /**
     * @param peopleAreaDetailRequest: 人员地域详情请求体
     * @param caseId:
     * @return: java.util.List<com.zqykj.domain.bank.PeopleArea>
     **/
    Page<PeopleArea> accessPeopleAreaStatisticsDetail(PeopleAreaDetailRequest peopleAreaDetailRequest, String caseId);
}
