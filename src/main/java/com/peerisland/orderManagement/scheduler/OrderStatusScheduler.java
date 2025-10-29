package com.peerisland.orderManagement.scheduler;

import com.peerisland.orderManagement.service.LockService;
import com.peerisland.orderManagement.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private final OrderService orderService;
    private final LockService dbLockService;
    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private static final String LOCK_NAME = "order-processing-lock";

    public OrderStatusScheduler(OrderService orderService, LockService dbLockService) {
        this.orderService = orderService;
        this.dbLockService = dbLockService;
    }

    @Scheduled(cron = "0 */5 * * * *") // runs every 5 minutes
    public void processPendingOrders() {
        log.info("Attempting to acquire scheduler lock...");

        // Try acquiring lock for 300 seconds (5 minutes)
        boolean acquired = dbLockService.acquireLock(LOCK_NAME, 300);

        if (!acquired) {
            log.info("Another instance is processing orders. Skipping this run.");
            return;
        }

        try {
            log.info("Lock acquired. Processing pending orders...");
            orderService.bumpPendingToProcessingBatch();
        } catch (Exception e) {
            log.error("Error occurred while processing orders", e);
        } finally {
            dbLockService.releaseLock(LOCK_NAME);
            log.info("Lock released.");
        }
    }
}
