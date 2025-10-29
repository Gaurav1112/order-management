package com.peerisland.orderManagement.repository;

import com.peerisland.orderManagement.model.SchedulerLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerLockRepository extends JpaRepository<SchedulerLock, String> {
}
