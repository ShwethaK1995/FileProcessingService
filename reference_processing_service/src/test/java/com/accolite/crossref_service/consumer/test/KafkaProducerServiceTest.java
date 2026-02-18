package com.accolite.crossref_service.consumer.test;

import com.accolite.crossref_service.consumer.KafkaProducerService;
import com.accolite.crossref_service.dto.ReferenceMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private ReferenceMessage message;

    @BeforeEach
    void setUp() {
        message = new ReferenceMessage();
        message.setCusipId("CUSIP123");

        // Manually inject the topic value (since @Value won't work in unit test)
        ReflectionTestUtils.setField(
                kafkaProducerService,
                "topic",
                "test-topic"
        );
    }

    @Test
    void send_shouldCallKafkaTemplateWithCorrectParameters() {
        kafkaProducerService.send(message);

        verify(kafkaTemplate, times(1))
                .send("test-topic", "CUSIP123", message);
    }
}

