package com.accolite.data_validation_service.kafka.producer;

import com.accolite.data_validation_service.service.ReferenceMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DlqProducer {

    private final KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @Value("${kafka.topic.reference.dlq}")
    private String dlqTopic;

    public void send(ReferenceMessage message) {
        kafkaTemplate.send(dlqTopic, message.getCusipId(), message);
    }
}

