package com.accolite.data_validation_service;

import com.accolite.data_validation_service.kafka.producer.KafkaProducerService;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService producer;

    @Test
    void shouldSendToOutputTopicWithCusipKey() {
        ReflectionTestUtils.setField(producer, "topic", "validation-topic");

        ReferenceMessage msg = new ReferenceMessage();
        msg.setCusipId("CUS123");

        producer.send(msg);

        verify(kafkaTemplate).send("validation-topic", "CUS123", msg);
    }
}

