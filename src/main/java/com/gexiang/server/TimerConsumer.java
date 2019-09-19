package com.gexiang.server;

import com.gexiang.vo.GxTimerRequest;
import com.gexiang.vo.GxTimerResponse;
import reactor.core.publisher.MonoSink;

import java.util.function.Consumer;

public class TimerConsumer implements Consumer<MonoSink<GxTimerResponse>> {
    private final GxTimerRequest request;
    private final WorkerPool<TimerConsumer> workerPool;
    private volatile MonoSink<GxTimerResponse> monoSink;

    public TimerConsumer(GxTimerRequest request, WorkerPool<TimerConsumer> workerPool){
        this.request = request;
        this.workerPool = workerPool;
    }

    @Override
    public void accept(MonoSink<GxTimerResponse> monoSink) {
        this.monoSink = monoSink;
        /**add to queue**/
        workerPool.push(this);
    }

    public GxTimerRequest getRequest(){
        return request;
    }

    public void success(GxTimerResponse response){
        monoSink.success(response);
    }
}
