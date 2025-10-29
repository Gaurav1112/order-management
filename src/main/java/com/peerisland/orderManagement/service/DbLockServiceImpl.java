package com.peerisland.orderManagement.service;

import com.peerisland.orderManagement.model.SchedulerLock;
import com.peerisland.orderManagement.repository.SchedulerLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbLockServiceImpl implements LockService {

    private final SchedulerLockRepository schedulerLockRepository;

    // Try to acquire lock
    @Transactional
    @Override
    public boolean acquireLock(String lockName, int expirySeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirySeconds, ChronoUnit.SECONDS);

        Optional<SchedulerLock> existingLock = schedulerLockRepository.findById(lockName);

        if (existingLock.isPresent()) {
            SchedulerLock lock = existingLock.get();
            if (!lock.isExpired()) {
                log.info("Lock '{}' is still active until {}", lockName, lock.getExpiresAt());
                return false;
            }
            // Expired → update it
            lock.setLockedAt(now);
            lock.setExpiresAt(expiresAt);
            schedulerLockRepository.save(lock);
            log.info("Re-acquired expired lock '{}'", lockName);
            return true;
        } else {
            // No lock → create new one
            SchedulerLock newLock = SchedulerLock.builder()
                                                 .lockName(lockName)
                                                 .lockedAt(now)
                                                 .expiresAt(expiresAt)
                                                 .build();

            schedulerLockRepository.save(newLock);
            log.info("Successfully acquired new lock '{}'", lockName);
            return true;
        }
    }

    // Release lock
    @Transactional
    @Override
    public void releaseLock(String lockName) {
        schedulerLockRepository.deleteById(lockName);
        log.info("Released lock '{}'", lockName);
    }
}
