package com.gexiang.vo;

import org.springframework.http.HttpStatus;

public class GxTimerResponse {
    private String appId;
    private String jobId;
    private int    status; /**200 成功，其他的失败**/
    private String msg;    /**失败原因**/

    public GxTimerResponse(){
        status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        msg    = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
    }

    public GxTimerResponse(String appId, String jobId, int status, String msg) {
        this.appId = appId;
        this.jobId = jobId;
        this.status = status;
        this.msg = msg;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
