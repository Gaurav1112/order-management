# 🛒 Order Management System (E-Commerce Backend)

A **Spring Boot 3**–based backend for an **E-commerce Order Processing System**.
It supports full order lifecycle management — creation, tracking, updating, and cancellation — along with automated background status transitions and complete audit history.

---

## 🧱 Project Structure

src/
├── main/java/com/peerisland/orderManagement

│ ├── controller/ # REST Controllers

│ ├── service/ # Business logic

│ ├── model/ # Entities

│ ├── dto/ # Data Transfer Objects

│ ├── repository/ # Spring Data JPA Repositories

│ └── config/ # App configuration

└── test/java/... # Unit & Integration Tests


---

## 📚 API Endpoints Overview

| Method | Endpoint | Description |
|---------|-----------|-------------|
| **POST** | `/api/orders` | Create a new order |
| **GET** | `/api/orders/{id}` | Get order details by ID |
| **PUT** | `/api/orders/{id}/status` | Update order status (e.g., from `PENDING` → `SHIPPED`, `DELIVERED`) — state machine enforced |
| **GET** | `/api/orders/{id}/status` | Retrieve current status of an order |
| **POST** | `/api/orders/{id}/cancel` | Cancel an order (allowed only if status is `PENDING`) |
| **GET** | `/api/orders/{id}/history` | Retrieve complete order status history |
| **GET** | `/api/orders?status={status}` | List all orders (optionally filtered by status) |
| **GET** | `/api/orders/track/{id}` | Track real-time order progress and current state |
| **GET** | `/actuator/health` | Check service health (Spring Boot Actuator) |
| **GET** | `/swagger-ui.html` | OpenAPI/Swagger UI to explore and test endpoints |
| **GET** | `/h2-console` | Access H2 in-memory database console |
---

## ⚙️ Tech Stack

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA (Hibernate)**
- **Flyway** (DB migration)
- **H2** (In-memory DB for demo/testing)
- **Lombok**
- **JUnit 5 + Spring Boot Test**
- **Swagger / OpenAPI 3**

---

## 🚀 How to Run Locally

### 1️⃣ Prerequisites
Make sure you have installed:
- JDK **17+**
- Maven **3.9+**

### 2️⃣ Start the Application

mvn spring-boot:run

3️⃣ Access the Application
Service	URL
Swagger UI	http://localhost:8080/swagger-ui.html

H2 Console	http://localhost:8080/h2-console

H2 Credentials:

JDBC URL: jdbc:h2:mem:ordersdb
Username: sa
Password: (blank)

| Feature                           | Description                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| **Optimistic Locking**            | Prevents race conditions on concurrent order updates.                                 |
| **Scheduler Lock (DB-based)**     | Ensures only one instance updates orders from `PENDING → PROCESSING` every 5 minutes. |
| **Flyway Migrations**             | Enables versioned, repeatable, and trackable schema migrations.                       |
| **Idempotency (clientRequestId)** | Prevents duplicate order creation if the same request ID is retried.                  |
| **Status History Tracking**       | Maintains a full audit log of every order status change.                              |
| **Transactional Boundaries**      | Declared per method to ensure data consistency.                                       |

📬 Sample cURL Requests

### 1️⃣ Create Order

curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
        "clientRequestId": "REQ-123",
        "customerName": "John Doe",
        "items": [
          { "sku": "SKU-001", "name": "Laptop", "quantity": 1, "price": 85000 },
          { "sku": "SKU-002", "name": "Mouse", "quantity": 2, "price": 500 }
        ]
      }'

### 2️⃣ Retrieve Order by ID

curl http://localhost:8080/api/orders/1


### 3️⃣ Track Order History

curl http://localhost:8080/api/orders/1/history

### 4️⃣ Cancel Order

curl -X POST http://localhost:8080/api/orders/1/cancel

### ⏱ Background Job

Every 5 minutes, a scheduler updates all PENDING orders to PROCESSING and logs the transition in the order_status_history table.

### 🧩 Flyway Migrations

| Version                                 | Description                                   |
| --------------------------------------- | --------------------------------------------- |
| **V1__create_tables.sql**               | Creates base tables (`orders`, `order_items`) |
| **V2__create_order_status_history.sql** | Adds `order_status_history` table             |
| **V3__insert_sample_orders.sql**        | Inserts demo records                          |


### 📄 OpenAPI / Swagger Documentation

If using Springdoc OpenAPI:
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

| Resource         | URL                                                                            |
| ---------------- | ------------------------------------------------------------------------------ |
| **Swagger UI**   | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **OpenAPI JSON** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)         |


### 🧪 Testing
| Test                     | Description                                                  |
| ------------------------ | ------------------------------------------------------------ |
| **OrderServiceTest**     | Unit tests for core business logic (create, cancel, update). |
| **OrderIntegrationTest** | End-to-end flow: create → get → cancel → history validation. |

### 🤖 Use of AI (Transparency)
| Step                      | What ChatGPT / Cursor Helped With | My Manual Fixes                                           |
| ------------------------- | --------------------------------- | --------------------------------------------------------- |
| **DTO and Entity Design** | Generated base structure          | Adjusted field types, added annotations                   |
| **Repository Interfaces** | Suggested method names            | Verified JPA query correctness                            |
| **Scheduler Logic**       | Suggested cron setup              | Replaced with Spring `@Scheduled` and DB-based lock       |
| **Exception Handling**    | Provided base template            | Customized with `NotFoundException` and response handling |
| **README.md**             | Generated documentation           | Added real URLs, cURL examples, Flyway notes              |
| **Test Templates**        | Generated basic skeleton          | Expanded assertions and mock setups manually              |

# order-management
