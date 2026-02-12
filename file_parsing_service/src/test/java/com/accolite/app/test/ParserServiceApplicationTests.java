package com.accolite.app.test;

import com.accolite.ParserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = ParserServiceApplication.class,
        properties = {
        "file.input.dir=./build/test-input",
        "kafka.input.topic=test-topic"
        }
)
class ParserServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldRequireExpectedProperties() {
        String inputDir = environment.getProperty("file.input.dir");
        String topic = environment.getProperty("kafka.input.topic");

        assertNotNull(inputDir);
        assertFalse(inputDir.isBlank());
        assertNotNull(topic);
        assertFalse(topic.isBlank());
    }

    @Test
    void shouldLoadRequiredBeans() {
        assertNotNull(applicationContext.getBean(com.accolite.service.FileParsingScheduler.class));
        assertNotNull(applicationContext.getBean(com.accolite.kafka.producer.KafkaProducerService.class));
    }
}
