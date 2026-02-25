package com.accolite.data_validation_service.kafka.producer;

import com.accolite.data_validation_service.model.ValidationFailureEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DlqProducer {

    private final KafkaTemplate<String, ValidationFailureEntity> kafkaTemplate;

    public DlqProducer(KafkaTemplate<String, ValidationFailureEntity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String topic, ValidationFailureEntity msg) {
        kafkaTemplate.send(topic, msg);
    }
}