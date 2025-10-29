package com.peerisland.orderManagement.controller;

import com.peerisland.orderManagement.dto.CreateOrderRequest;
import com.peerisland.orderManagement.dto.OrderResponse;
import com.peerisland.orderManagement.dto.OrderStatusHistoryDto;
import com.peerisland.orderManagement.dto.UpdateStatusRequest;
import com.peerisland.orderManagement.model.OrderStatus;
import com.peerisland.orderManagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for creating, tracking, updating and cancelling orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create a new order",
               description = "Creates a new order. If `clientRequestId` is provided, the operation is idempotent.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Order created successfully",
                                content = @Content(schema = @Schema(implementation = OrderResponse.class))),
                   @ApiResponse(responseCode = "400", description = "Invalid request data")
               })
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        log.info("📦 Received create order request for customer: {}", req.getCustomerName());
        OrderResponse resp = orderService.createOrder(req);
        log.info("✅ Order created successfully with ID: {}, Status: {}", resp.getId(), resp.getStatus());
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Get order by ID", description = "Fetch order details using its ID.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(
        @Parameter(description = "Order ID", example = "101") @PathVariable Long id) {
        log.info("🔍 Fetching order by ID: {}", id);
        OrderResponse response = orderService.getById(id);
        log.info("✅ Found order: ID={}, Status={}", response.getId(), response.getStatus());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List all orders",
               description = "Retrieve all orders with optional pagination and status filter.")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(
        @Parameter(description = "Page number (optional)") @RequestParam(required = false) Integer page,
        @Parameter(description = "Page size (optional)") @RequestParam(required = false) Integer size,
        @Parameter(description = "Filter by order status (optional)") @RequestParam(required = false) OrderStatus status) {
        log.info("📜 Listing orders (page={}, size={}, status={})", page, size, status);
        List<OrderResponse> responses = orderService.listAll(page, size, status);
        log.info("✅ Found {} orders.", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Cancel order (DELETE)",
               description = "Cancels an order — allowed only when status is PENDING.")
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        log.info("❌ Cancel order request received for ID: {}", id);
        OrderResponse response = orderService.cancelOrder(id);
        log.info("✅ Order {} cancelled successfully", id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order status",
               description = "Updates order status (e.g., from PENDING to SHIPPED). State machine enforced in service.")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateStatusRequest req) {
        log.info("🔄 Updating order {} status to {}", id, req.getStatus());
        OrderResponse response = orderService.updateStatus(id, req.getStatus());
        log.info("✅ Order {} status updated to {}", id, response.getStatus());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel order (POST alternative)",
               description = "Alternate endpoint for cancelling orders (via POST). Returns structured response for errors.")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        log.info("🚫 Received cancel request (POST) for order {}", orderId);
        try {
            orderService.cancelOrder(orderId);
            log.info("✅ Order {} cancelled successfully", orderId);
            return ResponseEntity.ok(Map.of("message", "Order " + orderId + " cancelled successfully"));
        } catch (IllegalStateException e) {
            log.warn("⚠️ Conflict cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            log.warn("❗ Order {} not found for cancellation", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Order not found"));
        } catch (Exception e) {
            log.error("💥 Unexpected error cancelling order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Track order (status + history)",
               description = "Fetches both current order status and its complete status history.")
    @GetMapping("/{id}/track")
    public ResponseEntity<Map<String, Object>> track(@PathVariable Long id) {
        log.info("📦 Tracking order {}", id);
        OrderResponse order = orderService.getById(id);
        List<OrderStatusHistoryDto> history = orderService.getStatusHistory(id);
        Map<String, Object> out = new HashMap<>();
        out.put("order", order);
        out.put("history", history);
        return ResponseEntity.ok(out);
    }

    @Operation(summary = "Get order status history",
               description = "Returns only the status history of a given order.")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderStatusHistoryDto>> history(@PathVariable Long id) {
        log.info("📜 Fetching status history for order {}", id);
        return ResponseEntity.ok(orderService.getStatusHistory(id));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("💥 Unhandled exception in OrderController: {}", ex.getMessage(), ex);
        Map<String, Object> error = new HashMap<>();
        error.put("error", ex.getClass().getSimpleName());
        error.put("message", ex.getMessage());
        error.put("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
