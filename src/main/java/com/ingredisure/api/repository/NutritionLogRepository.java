package com.ingredisure.api.repository;

import com.ingredisure.api.model.NutritionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutritionLogRepository extends JpaRepository<NutritionLog, Long> {
    List<NutritionLog> findByUserIdAndLogDate(Long userId, LocalDate date);
    List<NutritionLog> findByUserIdOrderByLogDateDesc(Long userId);
}
