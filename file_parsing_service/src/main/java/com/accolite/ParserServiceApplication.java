package com.accolite;

import com.accolite.util.ConcurrencyProperties;
import com.accolite.util.FileIngestProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "com.accolite")
@EnableConfigurationProperties({FileIngestProperties.class, ConcurrencyProperties.class})
public class ParserServiceApplication {
    public static void main(String[] args) {
        System.out.println("Spring is starting up..!");
        SpringApplication.run(ParserServiceApplication.class, args);
    }
}
