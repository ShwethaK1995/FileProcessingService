package com.accolite.data_validation_service.rule;

import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CusipRule implements ValidationRule<ReferenceMessage> {

    @Override
    public String id() { return "CUSIP_NOT_BLANK"; }

    @Override
    public int order() { return 1; }

    @Override
    public List<ValidationError> validate(ReferenceMessage msg) {
        if (msg.getCusipId() == null || msg.getCusipId().isBlank()) {
            return List.of(new ValidationError(
                    "cusipId",
                    "NOT_BLANK",
                    "CUSIP must not be blank",
                    id()
            ));
        }
        return List.of();
    }
}
