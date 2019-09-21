package com.gexiang.io;

import com.gexiang.repository.entity.TimerReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class FxxFunction implements Function<ClientResponse, Mono<? extends Throwable>> {
    private static Logger logger = LoggerFactory.getLogger(FxxFunction.class);
    private final TimerReq timerReq;
    public FxxFunction(TimerReq timerReq){
        this.timerReq = timerReq;
    }
    @Override
    public Mono<FxxException> apply(ClientResponse clientResponse) {
        logger.info("appId:{}, jobId:{} 4xx error", timerReq.getAppId(), timerReq.getJobId());
        return Mono.error(new FxxException(timerReq, clientResponse.statusCode()));
    }
}
