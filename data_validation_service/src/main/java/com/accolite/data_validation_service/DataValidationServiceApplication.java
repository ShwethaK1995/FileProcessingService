package com.accolite.data_validation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DataValidationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataValidationServiceApplication.class, args);
    }

}
