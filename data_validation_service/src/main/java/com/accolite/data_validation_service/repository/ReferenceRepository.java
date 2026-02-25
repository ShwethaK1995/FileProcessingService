package com.accolite.data_validation_service.repository;

import com.accolite.data_validation_service.model.ReferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface ReferenceRepository extends JpaRepository<ReferenceEntity, String> {

}

