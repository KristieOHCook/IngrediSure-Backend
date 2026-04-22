package com.ingredisure.api.repository;

import com.ingredisure.api.model.User;
import com.ingredisure.api.model.UserAvoidance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvoidanceRepository extends JpaRepository<UserAvoidance, Long> {
    List<UserAvoidance> findByUser(User user);
}

