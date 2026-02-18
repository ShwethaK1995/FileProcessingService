package com.accolite.data_validation_service;

import com.accolite.data_validation_service.ReferenceEntity;
import com.accolite.data_validation_service.repository.ReferenceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceRepositoryTest {

    private final ReferenceRepository repository;

    public ReferenceRepositoryTest(ReferenceRepository repository) {
        this.repository = repository;
    }


    @Test
    void saveAndFindById_shouldWork() {
        ReferenceEntity e = new ReferenceEntity();
        e.setCusipId("CUS123");
        e.setCountryCode("US");
        e.setDescription("DESC");
        e.setIsin("ISIN123");
        e.setLotSize(BigDecimal.TEN);

        repository.save(e);

        assertTrue(repository.findById("CUS123").isPresent());
    }
}

