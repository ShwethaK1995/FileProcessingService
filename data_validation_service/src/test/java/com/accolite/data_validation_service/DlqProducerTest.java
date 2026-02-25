package com.accolite.data_validation_service;

import com.accolite.data_validation_service.kafka.producer.DlqProducer;
import com.accolite.data_validation_service.model.ValidationFailureEntity;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DlqProducerTest {

    @Mock
    private KafkaTemplate<String, ValidationFailureEntity> kafkaTemplate; // ✅ correct type

    @InjectMocks
    private DlqProducer producer;

    @Test
    void shouldSendToDlqTopicWithCusipKey() {
        ReflectionTestUtils.setField(producer, "dlqTopic", "reference-dlq-topic");

        ReferenceMessage msg = new ReferenceMessage();
        msg.setCusipId("CUS123");

        ValidationFailureEntity event = new ValidationFailureEntity();
        event.setOriginalMessage(msg);

        producer.send(event);

        verify(kafkaTemplate).send(eq("reference-dlq-topic"), eq("CUS123"), eq(event));
    }
}