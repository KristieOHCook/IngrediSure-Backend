package com.ingredisure.api.repository;

import com.ingredisure.api.model.User;
import com.ingredisure.api.model.UserCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConditionRepository extends JpaRepository<UserCondition, Long> {
    List<UserCondition> findByUser(User user);
}

