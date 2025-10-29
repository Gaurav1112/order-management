package com.peerisland.orderManagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "customerName is required")
    private String customerName;

    /**
     * Optional idempotency key supplied by client to allow safe retries.
     */
    private String clientRequestId;

    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @NotBlank(message = "sku is required")
        private String sku;

        @NotBlank(message = "name is required")
        private String name;

        @Positive(message = "quantity must be > 0")
        private Integer quantity;

        @Positive(message = "price must be > 0")
        private Double price;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }


}

