package com.accolite.crossref_service.consumer;

import com.accolite.crossref_service.dto.ReferenceMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, ReferenceMessage> kafkaTemplate;

    @Value("${kafka.topic.reference.output}")
    private String topic;

    public void send(ReferenceMessage message) {

        log.info("the json being sent to datavalidation service is: " +message);
        kafkaTemplate.send(topic, message.getCusipId(), message);
    }
}

