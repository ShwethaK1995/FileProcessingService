package com.accolite.data_validation_service.service;

import com.accolite.data_validation_service.engine.ValidatorEngine;
import com.accolite.data_validation_service.kafka.producer.KafkaProducerService;
import com.accolite.data_validation_service.model.ReferenceEntity;
import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.model.ValidationException;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataValidationServiceTest {

    @Mock private ReferenceRepository repository;
    @Mock private KafkaProducerService producer;
    @Mock private ValidatorEngine<ReferenceMessage> validatorEngine;

    @InjectMocks private DataValidationService service;

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
    void process_whenValid_shouldSaveAndPublish() {
        ReferenceMessage m = baseValid("I");

        // engine passes (does nothing)
        doNothing().when(validatorEngine).validateOrThrow(m);

        service.process(m);

        verify(validatorEngine).validateOrThrow(m);
        verify(repository).save(any(ReferenceEntity.class));

        verify(producer).send(m);
        verifyNoMoreInteractions(producer);
    }

    @Test
    void process_whenValidationFails_shouldNotSaveOrPublish() {
        ReferenceMessage m = baseValid("I");

        doThrow(new ValidationException(List.of(
                new ValidationError("cusipId", "NOT_BLANK", "CUSIP must not be blank", "CUSIP_NOT_BLANK")
        ))).when(validatorEngine).validateOrThrow(m);

        assertThrows(ValidationException.class, () -> service.process(m));

        verify(validatorEngine).validateOrThrow(m);
        verifyNoInteractions(repository, producer);
    }

    @Test
    void process_nullMessage_shouldFail() {
        assertThrows(IllegalArgumentException.class, () -> service.process(null));
        verifyNoInteractions(validatorEngine, repository, producer);
    }
}