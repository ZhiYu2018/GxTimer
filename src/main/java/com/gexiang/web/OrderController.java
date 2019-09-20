package com.gexiang.web;

import com.gexiang.vo.GxTimeCbRequest;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
public class OrderController {
    @PostMapping("order/notify")
    public Mono<String> notify(HttpEntity<String> body){
        return Mono.just("{\"status\":200, \"msg\":\"Success\"}");
    }

    @PostMapping("order/cb")
    public Mono<String> cb(@RequestBody GxTimeCbRequest body){
        return Mono.just("{\"status\":200, \"msg\":\"Success\"}");
    }
}
