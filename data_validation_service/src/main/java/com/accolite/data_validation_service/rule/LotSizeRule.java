package com.accolite.data_validation_service.rule;

import com.accolite.data_validation_service.model.ValidationError;
import com.accolite.data_validation_service.service.ReferenceMessage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LotSizeRule implements ValidationRule<ReferenceMessage> {

    @Override
    public String id() { return "LOT_SIZE_POSITIVE"; }

    @Override
    public int order() { return 2; }

    @Override
    public List<ValidationError> validate(ReferenceMessage msg) {
        if (msg.getLotSize() == null || msg.getLotSize().signum() <= 0) {
            return List.of(new ValidationError(
                    "lotSize",
                    "POSITIVE",
                    "Lot size must be positive",
                    id()
            ));
        }
        return List.of();
    }
}
