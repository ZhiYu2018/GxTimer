package com.gexiang.io;

import com.alibaba.fastjson.JSON;
import com.gexiang.constant.ConstValue;
import com.gexiang.repository.entity.TimerReq;
import com.gexiang.server.WorkerPool;
import com.gexiang.vo.GxResult;
import com.gexiang.vo.GxTimeCbRequest;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class IoFactory {
    private static Logger logger = LoggerFactory.getLogger(IoFactory.class);
    private static class LazyHolder {
        static final IoFactory INSTANCE = new IoFactory();
    }

    private ReactorResourceFactory factory;
    private WebClient webClient;

    private IoFactory(){
        logger.info("factory init");
        factory = new ReactorResourceFactory();
        factory.setUseGlobalResources(false);
        factory.setConnectionProvider(ConnectionProvider.fixed("fixed"));
        factory.setLoopResources(LoopResources.create("webflux-http"));

        Function<HttpClient, HttpClient> mapper = client -> {
            client.tcpConfiguration(c -> { c.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); return c;});
            client.tcpConfiguration(c -> c.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10)).addHandlerLast(new WriteTimeoutHandler(10))));
            return client;
        };

        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(factory, mapper);
        webClient = WebClient.builder().clientConnector(connector).build();
    }

    public static IoFactory getInstance(){
        return LazyHolder.INSTANCE;
    }

    public ReactorResourceFactory getFactory(){
        return factory;
    }

    public WebClient getWebClient(){
        return webClient;
    }

    public void forward(TimerReq req, WorkerPool<GxResult<TimerReq, Object>> workerPool){
        switch (req.getReqType().intValue()){
            case ConstValue.REQ_GET_TYPE: {
                get(req, workerPool);
                break;
            }
            default:{
                post(req.getReqType().intValue(), req, workerPool);
            }
        }
    }

    public void callBack(GxTimeCbRequest cbRequest, String url){
        Mono<Void> result = webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                     .syncBody(cbRequest).retrieve().bodyToMono(Void.class);
        result.subscribe(new CbSubscriber(cbRequest));
    }

    private void get(TimerReq req, WorkerPool<GxResult<TimerReq, Object>> workerPool){
        WebClient.RequestHeadersUriSpec uriSpec = webClient.get();
        uriSpec.uri(req.getReqUrl());
        if(!ConstValue.DATA_EMPTY.equals(req.getReqHeader())){
            try{
                Map<String, Object> map = JSON.parseObject(req.getReqHeader(), new HashMap<>().getClass());
                for(Map.Entry<String, Object> kv: map.entrySet()){
                    uriSpec.header(kv.getKey(), kv.getValue().toString());
                }
            }catch (Throwable t){
                logger.warn("Handle header exception:{}", t.getMessage());
            }
        }

        Mono<String> result  = uriSpec.retrieve().onStatus(h -> { return  h.is4xxClientError();}, new FxxFunction(req))
                .onStatus(h ->{ return h.is5xxServerError(); }, new FxxFunction(req)).bodyToMono(String.class);
        result.subscribe(new IoSubscriber(req, workerPool));
    }

    private void post(int type,TimerReq req, WorkerPool<GxResult<TimerReq, Object>> workerPool){
        WebClient.RequestBodyUriSpec uriSpec;
        if(type == ConstValue.REQ_POST_TYPE) {
            uriSpec = webClient.post();
        }else{
            uriSpec = webClient.put();
        }
        uriSpec.uri(req.getReqUrl());
        if(!ConstValue.DATA_EMPTY.equals(req.getReqBody())) {
            MediaType mediaType;
            if(req.getDataType().intValue() == ConstValue.DATA_TEXT_TYPE){
                mediaType = MediaType.TEXT_PLAIN;
            }else if(req.getDataType().intValue() == ConstValue.DATA_JSON_TYPE){
                mediaType = MediaType.APPLICATION_JSON;
            }else{
                mediaType = MediaType.APPLICATION_FORM_URLENCODED;
            }
            uriSpec.contentType(mediaType);
            uriSpec.syncBody(req.getReqBody());
        }

        if(!ConstValue.DATA_EMPTY.equals(req.getReqHeader())){
            try{
                Map<String, Object> map = JSON.parseObject(req.getReqHeader(), new HashMap<>().getClass());
                for(Map.Entry<String, Object> kv: map.entrySet()){
                    uriSpec.header(kv.getKey(), kv.getValue().toString());
                }
            }catch (Throwable t){
                logger.warn("Handle header exception:{}", t.getMessage());
            }
        }

        Mono<String> result  = uriSpec.retrieve().onStatus(h -> { return  h.is4xxClientError();}, new FxxFunction(req))
                .onStatus(h ->{ return h.is5xxServerError(); }, new FxxFunction(req)).bodyToMono(String.class);
        result.subscribe(new IoSubscriber(req, workerPool));
    }
}
