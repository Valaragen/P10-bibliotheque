package com.rudy.bibliotheque.mbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableConfigurationProperties
@RefreshScope
@EnableSwagger2
@EnableDiscoveryClient
@EnableAsync
public class BookApiApplication {
    public static void main( String[] args )
    {
        SpringApplication.run(BookApiApplication.class, args);
    }
}
