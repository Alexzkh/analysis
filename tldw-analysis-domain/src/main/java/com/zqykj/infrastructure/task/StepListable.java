package com.zqykj.infrastructure.task;

import java.util.List;

/**
 * @Description: 任务执行步骤记录.
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public interface StepListable {

    /**
     * 获取当前执行步骤.
     */
    public int getNowStep();

    /**
     * 设置当前执行步骤.
     */
    public void setNowStep(int step);

    /**
     * 获取任务执行流程.
     */
    public List<String> getStepList();


    /**
     * 设置任务执行流程集.
     */
    public void setStepList(List<String> stepList);
}
