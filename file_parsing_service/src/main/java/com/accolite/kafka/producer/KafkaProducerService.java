package com.accolite.kafka.producer;

import com.accolite.entity.ParsedRecord;
import com.accolite.entity.DeadLetterMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, ParsedRecord> recordTemplate;
    private final KafkaTemplate<String, DeadLetterMessage> dltTemplate;

    @Value("${kafka.topic.parser}")
    private String parserTopic;

    @Value("${kafka.topic.dlt}")
    private String dltTopic;

    public KafkaProducerService(KafkaTemplate<String, ParsedRecord> recordTemplate,
                                KafkaTemplate<String, DeadLetterMessage> dltTemplate) {
        this.recordTemplate = recordTemplate;
        this.dltTemplate = dltTemplate;
    }

    public void sendRecord(String key,
                           ParsedRecord record,
                           String fileName,
                           long recordIndex) {

        recordTemplate.send(parserTopic, key, record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        DeadLetterMessage dlt = DeadLetterMessage.of(
                                // rawRecord: use JSON if you can, else record.toString()
                                String.valueOf(record),
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
                        // last resort
                        log.error("DLT publish failed key={} error={}", key, ex.getMessage(), ex);
                    }
                });
    }


}
