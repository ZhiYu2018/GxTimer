package com.gexiang.web;

import com.gexiang.io.CDRWriter;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController()
public class MgrController {

    @PostMapping("/cdr/dump")
    public Mono<String> dumpCdr(HttpEntity<String> body){
        CDRWriter.getInstance().dump();
        return Mono.just("{\"status\":200, \"msg\":\"Success\"}");
    }
}
