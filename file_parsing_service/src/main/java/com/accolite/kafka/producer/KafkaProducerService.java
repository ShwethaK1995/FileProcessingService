package com.accolite.kafka.producer;

import com.accolite.entity.ParsedRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private final KafkaTemplate<Object, ParsedRecord> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${kafka.input.topic}")
    private String TOPIC ="";

    public KafkaProducerService(KafkaTemplate<Object, ParsedRecord> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRecord(ParsedRecord record) {
        try {
            String json = objectMapper.writeValueAsString(record);
            System.out.println("Sending JSON to Kafka: " + json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        kafkaTemplate.send(TOPIC, record);
    }
}
