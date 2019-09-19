package com.gexiang;

import com.gexiang.server.TimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    @PreDestroy
    public void  dostory(){
        logger.info("Post dostory ......");
        ContextAware.getBean(TimerService.class).stop();
    }
}
