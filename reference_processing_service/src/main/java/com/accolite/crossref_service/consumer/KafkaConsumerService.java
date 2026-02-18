package com.accolite.crossref_service.consumer;

import com.accolite.crossref_service.dto.ReferenceMessage;
import com.accolite.crossref_service.repository.ReferenceRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ReferenceRepository referenceRepository;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(
            topics = "${kafka.topic.reference.input}",
            groupId = "ref-service-group-v3"
    )
    public void consume(ReferenceMessage message) {

        log.info("The recieved kafka message from parser-service is: "+message);

        if (message.getCusipId() == null) {
            throw new IllegalArgumentException("CUSIP ID cannot be null");
        }

        boolean exists = referenceRepository.existsById(message.getCusipId());

        message.setAction(exists ? "U" : "I");

        kafkaProducerService.send(message);
    }
}

