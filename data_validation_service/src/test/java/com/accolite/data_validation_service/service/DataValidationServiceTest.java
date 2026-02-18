package com.accolite.data_validation_service.service;

import com.accolite.data_validation_service.ReferenceEntity;
import com.accolite.data_validation_service.kafka.producer.KafkaProducerService;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import com.accolite.data_validation_service.service.DataValidationService;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataValidationServiceTest {

    @Mock
    private ReferenceRepository repository;

    @Mock
    private KafkaProducerService producer;

    @InjectMocks
    private DataValidationService service;

    private ReferenceMessage baseValid(String action) {
        ReferenceMessage m = new ReferenceMessage();
        m.setCusipId("CUS123");
        m.setCountryCode("US");
        m.setDescription("DESC");
        m.setIsin("ISIN123");
        m.setLotSize(BigDecimal.TEN);
        m.setAction(action);
        return m;
    }

    @Test
    void actionI_shouldInsertWhenNotExists() {
        ReferenceMessage m = baseValid("I");
        when(repository.existsById("CUS123")).thenReturn(false);

        service.process(m);

        verify(repository).save(any(ReferenceEntity.class));
        verify(producer).send(m);
    }

    @Test
    void actionI_shouldFailWhenAlreadyExists() {
        ReferenceMessage m = baseValid("I");
        when(repository.existsById("CUS123")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));

        verify(repository, never()).save(any());
        verify(producer, never()).send(any());
    }

    @Test
    void actionU_shouldUpdateWhenExists() {
        ReferenceMessage m = baseValid("U");
        when(repository.existsById("CUS123")).thenReturn(true);

        service.process(m);

        verify(repository).save(any(ReferenceEntity.class));
        verify(producer).send(m);
    }

    @Test
    void actionU_shouldFailWhenNotExists() {
        ReferenceMessage m = baseValid("U");
        when(repository.existsById("CUS123")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));

        verify(repository, never()).save(any());
        verify(producer, never()).send(any());
    }

    @Test
    void invalidAction_shouldFail() {
        ReferenceMessage m = baseValid("X");
        when(repository.existsById("CUS123")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));

        verify(repository, never()).save(any());
        verify(producer, never()).send(any());
    }

    // ---- Field validation tests ----

    @Test
    void nullMessage_shouldFail() {
        assertThrows(IllegalArgumentException.class, () -> service.process(null));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void blankCusip_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setCusipId("  ");

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void blankIsin_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setIsin(null);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void nullLotSize_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setLotSize(null);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void nonPositiveLotSize_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setLotSize(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void blankCountryCode_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setCountryCode("");

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }

    @Test
    void blankAction_shouldFail() {
        ReferenceMessage m = baseValid("I");
        m.setAction(" ");

        assertThrows(IllegalArgumentException.class, () -> service.process(m));
        verifyNoInteractions(repository, producer);
    }
}
