package com.zqykj.app.service.tools;

import com.zqykj.constant.Constants;
import com.zqykj.infrastructure.util.StringUtils;
import com.zqykj.infrastructure.util.hashing.LongHashFunction;

import java.io.*;

/**
 * @Description: 图操作工具类
 * @Author zhangkehou
 * @Date 2021/12/16
 */
public class AthenaGdbOperations {

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

    /**
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


    /**
     * To string from stream.
     *
     * @param input    stream
     * @param encoding charset of stream
     * @return string
     * @throws IOException io exception
     */
    public static String toString(InputStream input, String encoding) throws IOException {
        if (input == null) {
            return StringUtils.EMPTY;
        }
        return (null == encoding) ? toString(new InputStreamReader(input, Constants.ENCODE))
                : toString(new InputStreamReader(input, encoding));
    }

    /**
     * To string from reader.
     *
     * @param reader reader
     * @return string
     * @throws IOException io exception
     */
    public static String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }

    /**
     * Copy data.
     *
     * @param input  source
     * @param output target
     * @return copy size
     * @throws IOException io exception
     */
    public static long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
