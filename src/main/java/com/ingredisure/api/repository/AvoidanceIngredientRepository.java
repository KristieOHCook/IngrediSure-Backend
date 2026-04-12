package com.ingredisure.api.repository;

import com.ingredisure.api.model.AvoidanceIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvoidanceIngredientRepository extends JpaRepository<AvoidanceIngredient, Long> {
    List<AvoidanceIngredient> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}