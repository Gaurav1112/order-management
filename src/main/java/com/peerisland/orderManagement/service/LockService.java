package com.peerisland.orderManagement.service;

public interface LockService {
    boolean acquireLock(String lockName, int expirySeconds);
    void releaseLock(String lockName);
}
