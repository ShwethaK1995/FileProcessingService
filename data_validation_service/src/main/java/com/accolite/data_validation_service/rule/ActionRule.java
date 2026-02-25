package com.accolite.data_validation_service.rule;

import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActionRule implements ValidationRule<ReferenceMessage> {

    @Override
    public String id() { return "ACTION_ALLOWED"; }

    @Override
    public int order() { return 3; }

    @Override
    public List<ValidationError> validate(ReferenceMessage msg) {
        if (!List.of("I", "U").contains(msg.getAction())) {
            return List.of(new ValidationError(
                    "action",
                    "INVALID_VALUE",
                    "Action must be I or U",
                    id()
            ));
        }
        return List.of();
    }
}