package com.accolite.data_validation_service.service;

import com.accolite.data_validation_service.ReferenceEntity;
import com.accolite.data_validation_service.kafka.producer.KafkaProducerService;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.kafka.common.utils.Utils.isBlank;

@Service
@RequiredArgsConstructor
public class DataValidationService {

    private final ReferenceRepository repository;
    private final KafkaProducerService producer;

    @Transactional
    public void process(ReferenceMessage message) {

        validate(message);

        ReferenceEntity entity = convertToEntity(message);
        boolean exists = repository.existsById(message.getCusipId());

        if ("I".equalsIgnoreCase(message.getAction())) {

            if (exists) {
                throw new IllegalArgumentException(
                        "Insert failed: record already exists for CUSIP " + message.getCusipId());
            }

            repository.save(entity);
        }

        else if ("U".equalsIgnoreCase(message.getAction())) {

            if (!exists) {
                throw new IllegalArgumentException(
                        "Update failed: record does not exist for CUSIP " + message.getCusipId());
            }

            repository.save(entity);
        }

        else {
            throw new IllegalArgumentException("Invalid action type: " + message.getAction());
        }

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



    private ReferenceEntity convertToEntity(ReferenceMessage message) {
        ReferenceEntity entity = new ReferenceEntity();
        entity.setCusipId(message.getCusipId());
        entity.setCountryCode(message.getCountryCode());
        entity.setDescription(message.getDescription());
        entity.setIsin(message.getIsin());
        entity.setLotSize(message.getLotSize());
        return entity;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

