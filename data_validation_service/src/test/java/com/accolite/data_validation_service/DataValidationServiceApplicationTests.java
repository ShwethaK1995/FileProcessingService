package com.accolite.data_validation_service;

import com.accolite.data_validation_service.service.DataValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataValidationServiceApplicationTests {

    @Autowired
    private DataValidationService validationService;

    /**
     * Loads full Spring context
     */
    @Test
    void contextLoads() {
        assertNotNull(validationService);
    }

    /**
     * Covers main() method
     */
    @Test
    void mainMethodRuns() {
        SpringApplication app = new SpringApplication(DataValidationServiceApplication.class);
        assertDoesNotThrow(() -> app.run("--spring.main.web-application-type=none").close());
    }
}
