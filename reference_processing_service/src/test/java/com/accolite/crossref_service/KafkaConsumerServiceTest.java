package com.accolite.crossref_service;

import com.accolite.crossref_service.consumer.KafkaConsumerService;
import com.accolite.crossref_service.consumer.KafkaProducerService;
import com.accolite.crossref_service.dto.ReferenceMessage;
import com.accolite.crossref_service.repository.ReferenceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private ReferenceRepository referenceRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    private ReferenceMessage message;

    @BeforeEach
    void setUp() {
        message = new ReferenceMessage();
        message.setCusipId("CUSIP123");
    }

    // Test 1: Null CUSIP should throw exception
    @Test
    void consume_shouldThrowException_whenCusipIdIsNull() {
        message.setCusipId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kafkaConsumerService.consume(message)
        );

        assertEquals("CUSIP ID cannot be null", exception.getMessage());
        verifyNoInteractions(referenceRepository);
        verifyNoInteractions(kafkaProducerService);
    }

    //Test 2: Record exists → action = "U"
    @Test
    void consume_shouldSetActionToU_whenRecordExists() {
        when(referenceRepository.existsById("CUSIP123")).thenReturn(true);

        kafkaConsumerService.consume(message);

        assertEquals("U", message.getAction());
        verify(referenceRepository, times(1)).existsById("CUSIP123");
        verify(kafkaProducerService, times(1)).send(message);
    }

    // Test 3: Record does NOT exist → action = "I"
    @Test
    void consume_shouldSetActionToI_whenRecordDoesNotExist() {
        when(referenceRepository.existsById("CUSIP123")).thenReturn(false);

        kafkaConsumerService.consume(message);

        assertEquals("I", message.getAction());
        verify(referenceRepository, times(1)).existsById("CUSIP123");
        verify(kafkaProducerService, times(1)).send(message);
    }
}

