package com.accolite.data_validation_service.engine;


import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.model.ValidationException;
import com.accolite.data_validation_service.rule.ValidationRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ValidatorEngine<T> {

    private final List<ValidationRule<T>> rules;

    public ValidatorEngine(List<ValidationRule<T>> rules) {
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(ValidationRule::order))
                .toList();
    }

    public void validateOrThrow(T input) {
        List<ValidationError> errors = new ArrayList<>();

        for (ValidationRule<T> rule : rules) {
            errors.addAll(rule.validate(input));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
