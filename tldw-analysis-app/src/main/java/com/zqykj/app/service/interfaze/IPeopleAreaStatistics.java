package com.zqykj.app.service.interfaze;

import com.zqykj.common.request.PeopleAreaRequest;
import com.zqykj.common.response.PeopleAreaReponse;

import java.util.List;

/**
 * @Description: 人员地域数据业务接口
 * @Author zhangkehou
 * @Date 2021/10/26
 */
public interface IPeopleAreaStatistics {


    /**
     * 用于过去人员地域数据。
     *
     * @param peopleAreaRequest: 人员地域数据请求体
     * @param caseId:
     * @return: com.zqykj.common.response.PeopleAreaReponse
     **/
    List<PeopleAreaReponse> accessPeopleAreaStatisticsData(PeopleAreaRequest peopleAreaRequest, String caseId);
}
