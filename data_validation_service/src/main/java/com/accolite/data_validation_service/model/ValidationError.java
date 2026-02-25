package com.accolite.data_validation_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ValidationError {

    private final String field;
    private final String code;
    private final String message;
    private final String ruleId;

}
