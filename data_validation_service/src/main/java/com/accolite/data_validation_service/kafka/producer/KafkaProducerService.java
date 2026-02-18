package com.accolite.data_validation_service.kafka.producer;

import com.accolite.data_validation_service.service.ReferenceMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @Value("${kafka.topic.reference.output}")
    private String topic;

    public void send(ReferenceMessage message) {
        kafkaTemplate.send(topic, message.getCusipId(), message);
    }
}
