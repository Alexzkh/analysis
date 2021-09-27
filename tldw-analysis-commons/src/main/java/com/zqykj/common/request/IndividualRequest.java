package com.zqykj.common.request;

import lombok.*;

/**
 * @Description: TODO
 * @Author zhangkehou
 * @Date 2021/9/23
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualRequest  {

    private String keyword;
    private SortingRequest sortingRequest;
    private PagingRequest pagingRequest;
    private String field;
    private String value;
    private String caseId;

}
