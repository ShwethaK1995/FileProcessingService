package com.accolite.data_validation_service.rule;

import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DBInsertRule implements ValidationRule<ReferenceMessage> {

    private final ReferenceRepository repository;

    public DBInsertRule(ReferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public String id() { return "INSERT_NOT_EXIST"; }

    @Override
    public int order() { return 4; }

    @Override
    public List<ValidationError> validate(ReferenceMessage msg) {
        if ("I".equals(msg.getAction())
                && repository.existsById(msg.getCusipId())) {

            return List.of(new ValidationError(
                    "cusipId",
                    "ALREADY_EXISTS",
                    "CUSIP already exists for insert",
                    id()
            ));
        }
        return List.of();
    }
}
