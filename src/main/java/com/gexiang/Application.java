package com.gexiang;

import com.gexiang.io.IoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);
    public static void main( String[] args ){
        logger.info("Hello timer!");
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public ReactorResourceFactory resourceFactory() {
        return IoFactory.getInstance().getFactory();
    }

    @Bean
    public WebClient webClient(){
        return IoFactory.getInstance().getWebClient();
    }

    @PreDestroy
    public void  dostory(){
        logger.info("Post dostory ......");
        AppStatus.setbQuit();
    }
}
