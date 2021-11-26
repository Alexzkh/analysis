/**
 * @作者 Mcj
 */
package com.zqykj.app.service.interfaze;

import com.zqykj.app.service.vo.fund.FastInFastOutRequest;
import com.zqykj.common.core.ServerResponse;

/**
 * <h1> 战法快进快出 </h1>
 */
public interface IFastInFastOut {

    /**
     * <h2> 快进快出分析 </h2>
     */
    ServerResponse fastInFastOutAnalysis(FastInFastOutRequest request);
}
