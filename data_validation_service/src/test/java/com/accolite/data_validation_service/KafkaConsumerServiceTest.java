package com.accolite.data_validation_service;

import com.accolite.data_validation_service.kafka.consumer.KafkaConsumerService;
import com.accolite.data_validation_service.kafka.producer.DlqProducer;
import com.accolite.data_validation_service.model.ValidationFailureEntity;
import com.accolite.data_validation_service.service.DataValidationService;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private DataValidationService validationService;

    @Mock
    private DlqProducer dlqProducer;

    @InjectMocks
    private KafkaConsumerService consumer;

    @Test
    void shouldProcessMessageSuccessfully() {
        ReferenceMessage msg = new ReferenceMessage();
        msg.setCusipId("CUS123");

        consumer.consume(msg);

        verify(validationService).process(msg);
        verifyNoInteractions(dlqProducer);
    }

    @Test
    void shouldSendToDlqWhenProcessingFails() {
        ReferenceMessage msg = new ReferenceMessage();
        msg.setCusipId("CUS123");

        doThrow(new IllegalArgumentException("bad"))
                .when(validationService).process(msg);

        consumer.consume(msg);

        verify(validationService).process(msg);

        ArgumentCaptor<ValidationFailureEntity> captor =
                ArgumentCaptor.forClass(ValidationFailureEntity.class);

        verify(dlqProducer).send(captor.capture());

        ValidationFailureEntity event = captor.getValue();
        assertNotNull(event);
        assertNotNull(event.getOriginalMessage());
        assertEquals("CUS123", event.getOriginalMessage().getCusipId());


    }
}