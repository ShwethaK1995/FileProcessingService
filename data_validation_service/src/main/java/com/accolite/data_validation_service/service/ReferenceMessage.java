package com.accolite.data_validation_service.service;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ReferenceMessage {

    private String cusipId;
    private String countryCode;
    private String description;
    private String isin;
    private BigDecimal lotSize;
    private String action;
}
