package com.peerisland.orderManagement.service;


import com.peerisland.orderManagement.model.OrderEntity;
import com.peerisland.orderManagement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    private String clientRequestId;

    @BeforeEach
    void setup() {
        clientRequestId = "REQ-" + System.currentTimeMillis();
    }

    @Test
    void testFullOrderLifecycle() throws Exception {
        // 1️⃣ Create an order
        String createPayload = """
            {
                "productCode": "ABC123",
                "quantity": 2,
                "clientRequestId": "%s"
            }
            """.formatted(clientRequestId);

        String createResponse = mockMvc.perform(post("/api/orders")
                                           .contentType(MediaType.APPLICATION_JSON)
                                           .content(createPayload))
                                       .andExpect(status().isCreated())
                                       .andExpect(jsonPath("$.status").value("CREATED"))
                                       .andReturn()
                                       .getResponse()
                                       .getContentAsString();

        assertThat(createResponse).contains("ABC123");

        // Get created order ID from DB directly for further steps
        List<OrderEntity> allOrders = orderRepository.findAll();
        assertThat(allOrders).hasSize(1);
        Long orderId = allOrders.get(0).getId();

        // 2️⃣ Retrieve order
        mockMvc.perform(get("/api/orders/{id}", orderId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(orderId))
               .andExpect(jsonPath("$.productCode").value("ABC123"));

        // 3️⃣ Cancel order
        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 4️⃣ Fetch status history
        mockMvc.perform(get("/api/orders/{id}/history", orderId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].newStatus").exists());
    }
}
