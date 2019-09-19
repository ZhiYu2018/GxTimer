package com.gexiang.vo;

public class GxTimerRequest {
    private String appId;
    private String jobId;
    private int reqType;
    private int dataType;
    private long timeUsed; /**需要尝试的时长，单位秒，0 采用系统默认的时长**/
    private String reqUrl;
    private String cbUrl;
    private String reqBody;
    private String reqHeader;
    public GxTimerRequest(){
        reqType = -1;
        dataType = -1;
        timeUsed = 0;
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

    public int getReqType() {
        return reqType;
    }

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getCbUrl() {
        return cbUrl;
    }

    public void setCbUrl(String cbUrl) {
        this.cbUrl = cbUrl;
    }

    public String getReqBody() {
        return reqBody;
    }

    public void setReqBody(String reqBody) {
        this.reqBody = reqBody;
    }

    public String getReqHeader() {
        return reqHeader;
    }

    public void setReqHeader(String reqHeader) {
        this.reqHeader = reqHeader;
    }
}
