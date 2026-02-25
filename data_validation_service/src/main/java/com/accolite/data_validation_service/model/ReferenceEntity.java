package com.accolite.data_validation_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "reference_entity")
@Getter
@Setter
@NoArgsConstructor
public class ReferenceEntity {
    @Id
    @Column(name = "cusip_id", nullable = false)
    private String cusipId;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "description")
    private String description;

    @Column(name = "isin")
    private String isin;

    @Column(name = "lot_size")
    private BigDecimal lotSize;

    @Column(name = "indicator")
    private String indicator;
}

