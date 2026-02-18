package com.accolite.data_validation_service;

import com.accolite.data_validation_service.kafka.producer.DlqProducer;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DlqProducerTest {

    @Mock
    private KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @InjectMocks
    private DlqProducer producer;

    @Test
    void shouldSendToDlqTopicWithCusipKey() {
        ReflectionTestUtils.setField(producer, "dlqTopic", "reference-dlq-topic");

        ReferenceMessage msg = new ReferenceMessage();
        msg.setCusipId("CUS123");

        producer.send(msg);

        verify(kafkaTemplate).send("reference-dlq-topic", "CUS123", msg);
    }
}

