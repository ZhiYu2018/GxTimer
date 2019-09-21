package com.gexiang.io;

import com.gexiang.repository.entity.TimerReq;
import org.springframework.http.HttpStatus;

public class FxxException extends Throwable {
    private final TimerReq timerReq;
    private final HttpStatus httpStatus;
    public FxxException(TimerReq timerReq, HttpStatus httpStatus){
        super(String.format("appId:%s, jobId:%s, httpStatus:(%d,%s)",
                            timerReq.getAppId(), timerReq.getJobId(),
                            httpStatus.value(), httpStatus.getReasonPhrase()));
        this.timerReq = timerReq;
        this.httpStatus = httpStatus;
    }

    public TimerReq getTimerReq(){
        return timerReq;
    }

    public HttpStatus getHttpStatus(){
        return httpStatus;
    }
}
