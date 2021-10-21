package com.zqykj.app.service.transaction.service;

import com.zqykj.app.service.interfaze.IAssetTrendsTactics;
import com.zqykj.app.service.task.SingleSheetExcelFileExportTask;
import com.zqykj.common.request.AssetTrendsRequest;
import com.zqykj.common.response.AggregationResult;
import com.zqykj.common.response.AssetTrendsResponse;
import com.zqykj.infrastructure.core.ServerResponse;
import com.zqykj.infrastructure.task.Task;
import com.zqykj.infrastructure.task.TaskManagerService;
import com.zqykj.infrastructure.task.TaskStatus;
import com.zqykj.infrastructure.util.ExcelFileNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 文件导出服务类
 * @Author zhangkehou
 * @Date 2021/10/14
 */
@Service
public class FileExportService {


    @Autowired
    private TaskManagerService taskManagerService;

    @Autowired
    private IAssetTrendsTactics iAssetTrendsTactics;


    //todo 参数中缺少查询数据请求体

    /**
     * 交易统计下载
     *
     * @param caseId: 案件编号
     * @return: com.zqykj.infrastructure.core.ServerResponse<java.lang.String>
     **/
    public ServerResponse<String> transactionStatisticsDownload(String caseId) throws Exception {

        //todo 获取统计结果的数据
        List<AggregationResult> datas = new ArrayList<>();
        ServerResponse<String> result = new ServerResponse<>();
        SingleSheetExcelFileExportTask task = getSingleSheetExcelFileExportTaskByStatisAccount(datas);
        taskManagerService.taskHandle(task);
        String taskId = task.getTaskId();
        result.setData(taskId);
        return result;
    }


    /**
     * 交易统计数据下载任务.
     *
     * @param datas: 数据集合
     * @return: com.zqykj.app.service.task.SingleSheetExcelFileExportTask
     **/
    private SingleSheetExcelFileExportTask getSingleSheetExcelFileExportTaskByStatisAccount(List<AggregationResult> datas) {
        return new SingleSheetExcelFileExportTask<AggregationResult>(ExcelFileNameUtil.getExcelFileName("交易统计分析"),
                "交易统计分析", datas) {
            @Override
            protected String[] data2Array(AggregationResult data) {
                String[] result = new String[14];
                if (null != data) {
                    // todo 数据与表头映射
                }
                return result;
            }

            @Override
            protected String[] getSheetHeaders() {
                String[] result = {"开户名称", "开户证件号码", "开户银行", "账号", "交易卡号", "交易总次数", "交易总金额", "入账次数", "入账金额", "出账次数",
                        "出账金额", "交易净和", "最早交易时间", "最晚交易时间"};
                return result;
            }
        };
    }


    /**
     *
     * @param caseId:          案件编号.
     * @param downloadRequest: 查询下载内容请求体.
     * @return: com.zqykj.infrastructure.core.ServerResponse<java.lang.String>
     **/
    public ServerResponse<String> assetStatisticsDownload(String caseId, AssetTrendsRequest downloadRequest)
            throws Exception {
        ServerResponse<String> response = new ServerResponse<>();
        List<AssetTrendsResponse> assetDetailInfos = iAssetTrendsTactics.accessAssetTrendsTacticsResult(caseId, downloadRequest);
        SingleSheetExcelFileExportTask task = new SingleSheetExcelFileExportTask<AssetTrendsResponse>(
                ExcelFileNameUtil.getExcelFileName("资产趋势"), "资产趋势", assetDetailInfos) {
            @Override
            protected String[] data2Array(AssetTrendsResponse data) {
                String[] result = new String[5];
                result[0] = data.getDate();
                result[1] = data.getTotalTransactionMoney().toString();
                result[2] = data.getTotalExpenditure().toString();
                result[3] = data.getTotolIncome().toString();
                result[4] = data.getTransactionNet().toString();
                return result;
            }

            @Override
            protected String[] getSheetHeaders() {
                String timeDesc;
                String time;
                if ("y".equals(downloadRequest.getDateType())) {
                    timeDesc = "年份";
                    time = "年";
                } else if ("q".equals(downloadRequest.getDateType())) {
                    timeDesc = "季度";
                    time = "季";
                } else {
                    timeDesc = "月份";
                    time = "月";
                }
                String[] result = {timeDesc, "交易总额", "支出总额", "收入总额", "本" + time + "净额"};
                return result;
            }
        };
        taskManagerService.taskHandle(task);
        String taskId = task.getTaskId();
        response.setData(taskId);
        return response;
    }


    /**
     * 根据任务id获取任务执行到结果.
     *
     * @param taskId: 任务id
     * @return: com.zqykj.infrastructure.core.ServerResponse<com.zqykj.infrastructure.task.Task < ?>>
     **/
    public ServerResponse<Task<?>> getTaskById(String taskId) throws Exception {
        Task<?> task = null;
        ServerResponse<Task<?>> response = new ServerResponse();
        task = taskManagerService.getTask(taskId);
        response.setData(task);
        if (task.getStatus() == TaskStatus.Created || task.getStatus() == TaskStatus.Waiting
                || task.getStatus() == TaskStatus.Executing) {
            response.setCode(100);
        }
        return response;
    }
}
