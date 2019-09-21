package com.gexiang.web;

import com.gexiang.Util.Helper;
import com.gexiang.constant.ConstValue;
import com.gexiang.server.TimerService;
import com.gexiang.vo.GxTimerRequest;
import com.gexiang.vo.GxTimerResponse;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
public class TimerController {
    private static Logger logger = LoggerFactory.getLogger(TimerController.class);
    private final TimerService timerService;

    @Autowired
    public TimerController(TimerService timerService){
        this.timerService = timerService;
    }

    @PostMapping("/submit/job")
    public Mono<GxTimerResponse> submitJob(@RequestBody GxTimerRequest request){
        logger.info("call /submit/job:{}.{} cbUrl:{}", request.getAppId(), request.getJobId(), request.getCbUrl());
        try{
            checkArgs(request);
            return timerService.submitJob(request);
        }catch (IllegalArgumentException e){
            GxTimerResponse response = new GxTimerResponse();
            response.setAppId(request.getAppId());
            response.setJobId(request.getJobId());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMsg(e.getMessage());
            return Mono.just(response);
        }catch (Throwable t) {
            GxTimerResponse response = new GxTimerResponse();
            response.setAppId(request.getAppId());
            response.setJobId(request.getJobId());
            return Mono.just(response);
        }
    }

    @PostMapping("/submit/notify")
    public Mono<GxTimerResponse> submitNotify(@RequestBody GxTimerRequest request){
        logger.info("call /submit/notify:{}.{} cbUrl:{}", request.getAppId(), request.getJobId(), request.getCbUrl());
        try{
            checkArgs(request);
            return timerService.submitNotify(request);
        }catch (IllegalArgumentException e){
            GxTimerResponse response = new GxTimerResponse();
            response.setAppId(request.getAppId());
            response.setJobId(request.getJobId());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setMsg(e.getMessage());
            return Mono.just(response);
        }catch (Throwable t) {
            GxTimerResponse response = new GxTimerResponse();
            response.setAppId(request.getAppId());
            response.setJobId(request.getJobId());
            return Mono.just(response);
        }
    }

    private void checkArgs(GxTimerRequest request){
        Preconditions.checkArgument(Helper.isStrLenLess(request.getAppId(), ConstValue.APP_ID_MAX_LEN), "appId Error");
        Preconditions.checkArgument(Helper.isStrLenLess(request.getJobId(), ConstValue.JOB_ID_MAX_LEN), "jobId Error");
        Preconditions.checkArgument(Helper.isValueRange(request.getReqType(),
                                                         ConstValue.REQ_GET_TYPE,
                                                         ConstValue.REQ_PUT_TYPE), "reqType Error");
        Preconditions.checkArgument(Helper.isValueRange(request.getDataType(),
                                                        ConstValue.DATA_TEXT_TYPE,
                                                        ConstValue.DATA_FORM_TYPE), "dataType Error");
        Preconditions.checkArgument((request.getTimeUsed() >= 0), "timeUsed Error");
        Preconditions.checkArgument(Helper.isStrLenLess(request.getReqUrl(), ConstValue.URL_MAX_LEN), "reqUrl Error");
        Preconditions.checkArgument(!Helper.isStrLenBigger(request.getCbUrl(), ConstValue.URL_MAX_LEN), "cbUrl Error");
        Preconditions.checkArgument(!Helper.isStrLenBigger(request.getReqBody(), ConstValue.BODY_MAX_LEN), "body Error");
        Preconditions.checkArgument(!Helper.isStrLenBigger(request.getReqHeader(), ConstValue.HEADER_MAX_LEN), "header Error");
    }
}
