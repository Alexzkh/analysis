package com.zqykj.infrastructure.constant;

/**
 * @Description Constant classs
 * @Author zhangkehou
 * @Date 2021/7/19 14:17
 */
public class Constants {

    /**
     * Default page size without pageable
     */
    public static int DEFALT_PAGE_SIZE = 200;

    /**
     * Batch update (New) number of pieces per batch
     **/
    public static int BULK_COUNT = 5000;

    public static final String ENCODE = "UTF-8";
    /**
     * 用户数的 redis key
     */
    public static final String USE_COUNT_REDIS_KEY = "imooc-user-count";


    public static final String NULL = "";

    /**
     * <h2>User HBase Table</h2>
     */
    public class UserTable {

        /**
         * User HBase 表名
         */
        public static final String TABLE_NAME = "pb:user";

        /**
         * 基本信息列族
         */
        public static final String FAMILY_B = "b";

        /**
         * 用户名
         */
        public static final String NAME = "name";

        /**
         * 用户年龄
         */
        public static final String AGE = "age";

        /**
         * 用户性别
         */
        public static final String SEX = "sex";

        /**
         * 额外信息列族
         */
        public static final String FAMILY_O = "o";

        /**
         * 电话号码
         */
        public static final String PHONE = "phone";

        /**
         * 住址
         */
        public static final String ADDRESS = "address";
    }
}
