package com.accolite.kafka.producer;

import com.accolite.entity.ParsedRecord;
import com.accolite.entity.DeadLetterMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    public void sendRecord(String key, ParsedRecord record) {
        recordTemplate.send(parserTopic, key, record);
    }

    public void sendDLT(String key,DeadLetterMessage msg) {
        dltTemplate.send(dltTopic,key,msg);
    }
}
