package com.accolite.data_validation_service.model;

import com.accolite.data_validation_service.service.ReferenceMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationFailureEntity {

    private ReferenceMessage originalMessage;
    private List<ValidationError> errors;
    private String failedAt = Instant.now().toString();

    public ValidationFailureEntity(
            ReferenceMessage originalMessage,
            List<ValidationError> errors, String system, String message) {
        this.originalMessage = originalMessage;
        this.errors = errors;
    }

}
