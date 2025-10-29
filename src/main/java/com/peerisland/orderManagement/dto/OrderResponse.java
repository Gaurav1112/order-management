package com.peerisland.orderManagement.dto;

import com.peerisland.orderManagement.model.OrderEntity;
import com.peerisland.orderManagement.model.OrderItemEntity;
import com.peerisland.orderManagement.model.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String clientRequestId;
    private String customerName;
    private OrderStatus status;
    private Double totalAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Item {
        private Long id;
        private String sku;
        private String name;
        private Integer quantity;
        private Double price;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static OrderResponse fromEntity(OrderEntity e) {
        OrderResponse r = new OrderResponse();
        r.setId(e.getId());
        r.setClientRequestId(e.getClientRequestId());
        r.setCustomerName(e.getCustomerName());
        r.setStatus(e.getStatus());
        r.setTotalAmount(e.getTotalAmount());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        r.setItems(e.getItems().stream().map(it -> {
            Item i = new Item();
            i.setId(it.getId());
            i.setSku(it.getSku());
            i.setName(it.getName());
            i.setQuantity(it.getQuantity());
            i.setPrice(it.getPrice());
            return i;
        }).collect(Collectors.toList()));
        return r;
    }
}

