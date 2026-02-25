package com.accolite.kafka.producer;

import com.accolite.entity.DeadLetterMessage;
import com.accolite.entity.ParsedRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, ParsedRecord> recordTemplate;
    private final KafkaTemplate<String, DeadLetterMessage> dltTemplate;
    private final ObjectMapper objectMapper;


    @Value("${kafka.topic.parser}")
    private String parserTopic;

    @Value("${kafka.topic.dlt}")
    private String dltTopic;

    public KafkaProducerService(KafkaTemplate<String, ParsedRecord> recordTemplate,
                                KafkaTemplate<String, DeadLetterMessage> dltTemplate, ObjectMapper objectMapper) {
        this.recordTemplate = recordTemplate;
        this.dltTemplate = dltTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendRecord(String key,
                           ParsedRecord record,
                           String fileName,
                           long recordIndex) {

        recordTemplate.send(parserTopic, key, record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        DeadLetterMessage dlt = DeadLetterMessage.of(
                                safeJson(record),
                                "PUBLISH_FAILED: " + ex.getMessage(),
                                fileName,
                                recordIndex
                        );
                        sendDLT(key, dlt);
                    }
                });
    }

    public void sendDLT(String key, DeadLetterMessage msg) {
        dltTemplate.send(dltTopic, key, msg)
                .whenComplete((r, ex) -> {
                    if (ex != null) {
                        // last resort (DLT publish should almost never fail)
                        log.error("DLT publish failed key={} error={}", key, ex.getMessage(), ex);
                    }
                });
    }

    private String safeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}

