package com.accolite.crossref_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceMessage {

    private String cusipId;
    private String countryCode;
    private String description;
    private String isin;
    private BigDecimal lotSize;
    private String action;


}