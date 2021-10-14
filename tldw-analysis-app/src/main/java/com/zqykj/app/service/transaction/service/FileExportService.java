package com.zqykj.app.service.transaction.service;

import com.zqykj.app.service.task.SingleSheetExcelFileExportTask;
import com.zqykj.common.response.AggregationResult;
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
