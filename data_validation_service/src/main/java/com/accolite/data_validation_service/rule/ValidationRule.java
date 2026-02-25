package com.accolite.data_validation_service.rule;

import com.accolite.data_validation_service.model.ValidationError;

import java.util.List;

public interface ValidationRule<T>{

    String id();

    int order();

    List<ValidationError> validate(T input);
}
