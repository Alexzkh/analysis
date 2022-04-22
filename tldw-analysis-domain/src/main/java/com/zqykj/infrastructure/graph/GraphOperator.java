package com.zqykj.infrastructure.graph;

import com.zqykj.infrastructure.util.hashing.LongHashFunction;

/**
 * @Description: 图操作
 * @Author zhangkehou
 * @Date 2021/12/10
 */
public class GraphOperator {


    public static final String PATH_URI="/graph/%s/allpaths";


    /**
     * 生成边Id，规则：哈希(fkeyid + "-" + tokeyid)
     *
     * @param fkeyid  起点keyId
     * @param tokeyid 终点点keyid
     * @return
     */
    public static Long edgeIdHashcode(long fkeyid, long tokeyid) {
        String str = fkeyid + "-" + tokeyid;
        return LongHashFunction.xx3().hashChars(str);
    }

    /** d
     * 顶点keyId生成规则
     *
     * @param schema     图方案key
     * @param gid        图Id（caseID）
     * @param eletypeKey 实体对象Key
     * @param id         实体主键
     * @return
     */
    public static long createKeyId(String schema, long gid, String eletypeKey, String id) {
        return LongHashFunction.xx().hashChars(schema + "_" + gid + "_" + eletypeKey + "_" + id);
    }



}
