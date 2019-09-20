package com.gexiang.vo;

public class GxTimeCbRequest {
    private String appId;
    private String jobId;
    private int    status; /**200 成功，其他的失败**/
    private String msg;    /**失败原因**/
    private String body;   /**第三方返回的内容**/

    public GxTimeCbRequest() {
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
