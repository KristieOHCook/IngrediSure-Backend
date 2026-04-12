package com.ingredisure.api.repository;

import com.ingredisure.api.model.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalConditionRepository extends JpaRepository<MedicalCondition, Long> {
    List<MedicalCondition> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}