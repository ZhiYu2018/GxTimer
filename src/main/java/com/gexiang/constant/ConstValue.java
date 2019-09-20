package com.gexiang.constant;

public class ConstValue {
    public final static int APP_ID_MAX_LEN = 32;
    public final static int JOB_ID_MAX_LEN = 64;
    public final static int REQ_GET_TYPE = 0;
    public final static int REQ_POST_TYPE = 1;
    public final static int REQ_PUT_TYPE = 2;
    public final static int DATA_TEXT_TYPE = 1;
    public final static int DATA_JSON_TYPE = 2;
    public final static int DATA_FORM_TYPE = 3;
    public final static int URL_MAX_LEN = 512;
    public final static int BODY_MAX_LEN = 8192;
    public final static int HEADER_MAX_LEN = 1024;
    public final static Integer JOB_IDLE_STATE = Integer.valueOf(0);
    public final static Integer JOB_SUCCESS_STATE = Integer.valueOf(1);
    public final static Integer JOB_FAILED_STATE = Integer.valueOf(-1);
    public final static Integer JOB_PENDDING_STATE = Integer.valueOf(2);
    public final static String TIMER_JOB_NAME = "timer.job.try";
    public final static String TIMER_NOTIFY_NAME = "timer.notify.try";

    public final static int MAX_QUEUE_SIZE = 1000;
    public final static Long DEF_JOB_TIME_LONG = 86400L;
    public final static String DATA_EMPTY = "{NONE}";
}
