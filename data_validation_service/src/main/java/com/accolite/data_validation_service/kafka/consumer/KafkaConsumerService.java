package com.accolite.data_validation_service.kafka.consumer;

import com.accolite.data_validation_service.kafka.producer.DlqProducer;
import com.accolite.data_validation_service.model.ValidationException;
import com.accolite.data_validation_service.model.ValidationFailureEntity;
import com.accolite.data_validation_service.service.ReferenceMessage;
import com.accolite.data_validation_service.service.DataValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final DataValidationService validationService;
    private final DlqProducer dlqProducer;
    @Value("${kafka.topic.reference.dlq}")
    private String dlqtopic;

    @KafkaListener(topics = "${kafka.topic.reference.input}")
    public void consume(ReferenceMessage message) {

        try {

            validationService.process(message);

        } catch (ValidationException ve) {

            ValidationFailureEntity event =
                    new ValidationFailureEntity(
                            message,
                            ve.getErrors(),
                            "VALIDATION",
                            "Validation failed"
                    );

            dlqProducer.send(dlqtopic,event);

        } catch (Exception ex) {

            ValidationFailureEntity event =
                    new ValidationFailureEntity(
                            message,
                            null,
                            "SYSTEM",
                            ex.getMessage()
                    );

            dlqProducer.send(dlqtopic,event);
        }
    }
}
