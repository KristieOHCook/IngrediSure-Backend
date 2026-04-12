package com.ingredisure.api.repository;

import com.ingredisure.api.model.SavedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {
    List<SavedItem> findByUserId(Long userId);
    long countBySafetyVerdict(String safetyVerdict);
    void deleteByIdAndUserId(Long id, Long userId);
}