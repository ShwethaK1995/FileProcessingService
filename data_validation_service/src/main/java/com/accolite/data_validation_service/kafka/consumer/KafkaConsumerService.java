package com.accolite.data_validation_service.kafka.consumer;

import com.accolite.data_validation_service.kafka.producer.DlqProducer;
import com.accolite.data_validation_service.service.ReferenceMessage;
import com.accolite.data_validation_service.service.DataValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final DataValidationService validationService;
    private final DlqProducer dlqProducer;

    @KafkaListener(
            topics = "${kafka.topic.reference.input}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ReferenceMessage message) {

        try {
            validationService.process(message);
        }
        catch (Exception ex) {
                String cusip = (message != null) ? message.getCusipId() : "null";
                String action = (message != null) ? message.getAction() : "null";

                log.error("DLQ due to error. cusipId={}, action={}, reason={}",
                        cusip, action, ex.getMessage(), ex);

                dlqProducer.send(message);
        }
    }
}
