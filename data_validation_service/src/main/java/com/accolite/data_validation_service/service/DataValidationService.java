package com.accolite.data_validation_service.service;

import com.accolite.data_validation_service.model.ReferenceEntity;
import com.accolite.data_validation_service.engine.ValidatorEngine;
import com.accolite.data_validation_service.kafka.producer.KafkaProducerService;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataValidationService {

    private final ReferenceRepository repository;
    private final KafkaProducerService producer;
    private final ValidatorEngine<ReferenceMessage> validatorEngine;


    public DataValidationService(ReferenceRepository repository, KafkaProducerService producer, ValidatorEngine<ReferenceMessage> validatorEngine) {
        this.repository = repository;
        this.producer = producer;
        this.validatorEngine = validatorEngine;
    }

    @Transactional
    public void process(ReferenceMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }

        validatorEngine.validateOrThrow(message);

        ReferenceEntity entity = convertToEntity(message);
        repository.save(entity);

        producer.send(message);
    }

    void validate(ReferenceMessage message) {

        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        if (isBlank(message.getCusipId())) {
            throw new IllegalArgumentException("CUSIP ID cannot be empty");
        }

        if (isBlank(message.getIsin())) {
            throw new IllegalArgumentException("ISIN cannot be empty");
        }

        if (message.getLotSize() == null) {
            throw new IllegalArgumentException("Lot size cannot be null");
        }

        if (message.getLotSize().signum() <= 0) {
            throw new IllegalArgumentException("Lot size must be greater than zero");
        }

        if (isBlank(message.getCountryCode())) {
            throw new IllegalArgumentException("Country code cannot be empty");
        }

        if (isBlank(message.getAction())) {
            throw new IllegalArgumentException("Action cannot be empty");
        }

        if (!("I".equalsIgnoreCase(message.getAction())
                || "U".equalsIgnoreCase(message.getAction()))) {
            throw new IllegalArgumentException("Action must be I or U");
        }
    }



    private ReferenceEntity convertToEntity(ReferenceMessage msg) {
        ReferenceEntity entity;

        if ("U".equalsIgnoreCase(msg.getAction())) {

            // Update case
            entity = repository.findById(msg.getCusipId())
                    .orElseThrow(() ->
                            new RuntimeException("CUSIP not found for update: " + msg.getCusipId()));

        } else if ("I".equalsIgnoreCase(msg.getAction())) {

            // Insert case
            entity = new ReferenceEntity();
            entity.setCusipId(msg.getCusipId());

        } else {
            throw new RuntimeException("Unsupported action: " + msg.getAction());
        }

        // Common field mapping
        entity.setCountryCode(msg.getCountryCode());
        entity.setDescription(msg.getDescription());
        entity.setIsin(msg.getIsin());
        entity.setLotSize(msg.getLotSize());

        return entity;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

