package com.gexiang.server;

import com.gexiang.vo.GxTimerRequest;
import com.gexiang.vo.GxTimerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TimerService {
    private static final int JOB_RATE = 250;
    private static final int NOTIFY_RATE = 500;
    private static final int MAX_QUEUE_SIZE = 10000;
    private static Logger logger = LoggerFactory.getLogger(TimerService.class);
    private WorkerPool<TimerConsumer> jobWorker;
    private WorkerPool<TimerConsumer> notifyWorker;

    public TimerService(){
        int cpuNum = Runtime.getRuntime().availableProcessors();
        logger.info("Cpu num:{}", cpuNum);
        jobWorker = new WorkerPool<>(cpuNum, JOB_RATE, MAX_QUEUE_SIZE, "", this::handleJob);
        notifyWorker = new WorkerPool<>(2, NOTIFY_RATE, MAX_QUEUE_SIZE, "", this::handleNotify);
    }

    public void stop(){
        jobWorker.setStop();
        notifyWorker.setStop();
    }

    public Mono<GxTimerResponse> submitJob(GxTimerRequest request){
        return Mono.create(new TimerConsumer(request, jobWorker));
    }

    public Mono<GxTimerResponse> submitNotify(GxTimerRequest request){
        return Mono.create(new TimerConsumer(request, notifyWorker));
    }

    private void handleJob(TimerConsumer timerConsumer){
        GxTimerResponse response = new GxTimerResponse();
        response.setAppId(timerConsumer.getRequest().getAppId());
        response.setJobId(timerConsumer.getRequest().getJobId());
        response.setStatus(200);
        response.setMsg("OK");
        timerConsumer.success(response);
    }

    private void handleNotify(TimerConsumer timerConsumer){
        GxTimerResponse response = new GxTimerResponse();
        response.setAppId(timerConsumer.getRequest().getAppId());
        response.setJobId(timerConsumer.getRequest().getJobId());
        response.setStatus(200);
        response.setMsg("OK");
        timerConsumer.success(response);
    }
}
