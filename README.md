# Order & Inventory Service

A RESTful Spring Boot service for managing products and orders, including an asynchronous fulfilment workflow and real-time inventory updates.

## 🚀 Overview

This service provides:

- Product creation, update, and inventory tracking
- Order creation with atomic stock reservation
- Async fulfilment processing (100–300ms delay)
- Validation, error handling and test coverage
- H2 in-memory database
- Actuator metrics

## 📦 Prerequisites

- **Java 17**
- **Maven 3.8+**

## ▶️ Run Application

```bash
mvn spring-boot:run
```

## 📡 API Endpoints

### Products

```bash
# Create product
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json"   -d '{"sku":"P001","name":"Laptop","price":999.99,"availableQuantity":10}'

# Get product by SKU
curl http://localhost:8080/products/P001

# Update product (PATCH)
curl -X PATCH http://localhost:8080/products/P001 \
  -H "Content-Type: application/json"   -d '{"name":"Updated Laptop","price":899.99}'

# Get low-stock products
curl "http://localhost:8080/products/low-stock?threshold=5"
```

### Orders

```bash
# Create order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json"   -d '{
    "customerEmail":"customer@example.com",
    "items":[{"sku":"P001","quantity":2}]
  }'

# Get order by ID
curl http://localhost:8080/orders/1

# Get orders by status
curl "http://localhost:8080/orders?status=FULFILLED"
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=OrderServiceTest
```

## 🗄️ H2 Database Console

Visit:  
`http://localhost:8080/h2-console`

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** *(empty)*

## ⭐ Key Features

- ✅ Product inventory management  
- ✅ Order creation with atomic stock reservation
- ✅ Async fulfilment workflow  
- ✅ Validation & error handling  
- ✅ Unit and integration tests  
- ✅ Actuator metrics (`/actuator/metrics`)
