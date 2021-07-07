package com.zqykj.tldw.aggregate.searching.esclientrhl.util;

/**
 *  constants
 **/
public class Constant {
    /**
     * Default page size without pageable
     */
    public static int DEFALT_PAGE_SIZE = 200;

    /**
     * Completion suggestions by default size.
     */
    public static int COMPLETION_SUGGESTION_SIZE = 10;

    /**
     * Default highlight tag
     */
    public static String HIGHLIGHT_TAG = "";

    /**
     * When creating index mapping, is keyword created by default
     */
    public static boolean DEFAULT_KEYWORDS = true;

    /**
     * Default host
     **/
    public static String DEFAULT_ES_HOST = "127.0.0.1:9200";

    /**
     * Scroll search 2 hour
     **/
    public static long DEFAULT_SCROLL_TIME = 2;

    /**
     * Scroll search default number of items per page
     **/
    public static int DEFAULT_SCROLL_PERPAGE = 100;

    /**
     * Default percentage query specification
     **/
    public static double[] DEFAULT_PERCSEGMENT = {50.0, 95.0, 99.0};

    /**
     * Batch update (New) number of pieces per batch
     **/
    public static int BULK_COUNT = 5000;

    /**
     * The maximum number of aggregate queries returned
     **/
    public static int AGG_RESULT_COUNT = Integer.MAX_VALUE;

    /**
     * Default precisoion threshold
     **/
    public static long DEFAULT_PRECISION_THRESHOLD = 3000L;
}
