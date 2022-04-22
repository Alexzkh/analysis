package com.zqykj.app.service.strategy;

import com.zqykj.common.enums.TacticsTypeEnum;
import com.zqykj.common.response.PeopleAreaResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @Description: 人员地域数据解析（一个入参，一个出参）
 * @Author zhangkehou
 * @Date 2021/10/28
 */
@Component
public class PeopleAreaResultConversionAccessor {


    /**
     * 解析人员地域返回的result.
     */
    private static final Function<List<List<Object>>, List<PeopleAreaResponse>> PEOPLE_AREA_CONVERSION =
            lists -> {
                List<PeopleAreaResponse> responses = new ArrayList<>();

                lists.forEach(rowData -> {
                    PeopleAreaResponse peopleAreaResponse = PeopleAreaResponse.builder()
                            .region((String) rowData.get(0))
                            .number((Long) rowData.get(1))
                            .build();
                    responses.add(peopleAreaResponse);
                });
                return responses;
            };

    /**
     * 静态映射表.
     */
    private static final Map<TacticsTypeEnum, Function> functionMap = new ConcurrentHashMap<>();

    static {


        /**
         * 人员地域的结果解析.
         * */
        functionMap.put(TacticsTypeEnum.PEOPLE_AREA, PEOPLE_AREA_CONVERSION);
    }


    /**
     * 用于获取将标准对象转换为业务对象返回给界面.
     *
     * @param toBeParsedResult: 待转换的结果.未转化成业务对象的结果。
     * @param tacticsTypeEnum:  战法类型
     * @return: Object
     **/
    public Object access(List<List<Object>> toBeParsedResult, TacticsTypeEnum tacticsTypeEnum) {
        if (functionMap.containsKey(tacticsTypeEnum)) {
            return functionMap.get(tacticsTypeEnum).apply(toBeParsedResult);
        }
        return null;
    }
}
