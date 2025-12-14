# Loopins Core Service

A Spring Boot microservice that owns the business domain and order lifecycle for the Loopins marketplace.

## Architecture Overview

This is the **Core Service** of a two-microservice marketplace architecture:

- **Core Service** (this service): Owns business domain and order lifecycle
- **Fulfillment Service**: Owns all external integrations (payment, shipping, email)

### Key Principles

- Each service owns its own database (no shared database)
- Core Service does NOT call external APIs directly
- Fulfillment Service does NOT modify Core's database
- Communication via REST (Feign) with support for future async events
- Circuit breaker pattern for resilience

## Features

- Cart management (create, add items, remove items)
- Order checkout with shipping calculation
- Payment initiation and confirmation
- Order lifecycle management (DRAFT → CREATED → PAYMENT_PENDING → PAID → SHIPPED → COMPLETED)
- Service-to-service API key authentication
- Idempotent payment callbacks

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- PostgreSQL (production) / H2 (development)
- Flyway (database migrations)
- OpenFeign (service communication)
- Resilience4j (circuit breaker, retry, timeout)
- MapStruct (DTO mapping)
- Lombok

## API Endpoints

### Cart Controller

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/carts` | Create a new cart |
| GET | `/api/carts/{cartId}` | Get cart by ID |
| GET | `/api/carts/user/{userId}` | Get active cart for user |
| POST | `/api/carts/{cartId}/items` | Add item to cart |
| DELETE | `/api/carts/{cartId}/items/{itemId}` | Remove item from cart |
| PATCH | `/api/carts/{cartId}/items/{itemId}?quantity=X` | Update item quantity |
| DELETE | `/api/carts/{cartId}/items` | Clear all items from cart |

### Checkout Controller

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders/checkout` | Checkout cart and create order |
| POST | `/api/orders/{orderId}/retry-payment` | Retry payment for an order |

### Order Controller

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/orders/{orderId}` | Get order by ID |
| GET | `/api/orders/user/{userId}` | Get orders for user (paginated) |
| POST | `/api/orders/{orderId}/payment-confirmed` | Confirm payment (protected) |
| POST | `/api/orders/{orderId}/payment-failed` | Mark payment failed (protected) |
| POST | `/api/orders/{orderId}/cancel` | Cancel order |
| POST | `/api/orders/{orderId}/ship` | Mark order as shipped |
| POST | `/api/orders/{orderId}/complete` | Mark order as completed |

## Order Status Flow

```
DRAFT → CREATED → PAYMENT_PENDING → PAID → SHIPPED → COMPLETED
                        ↓
                  PAYMENT_FAILED
                        ↓
                    CANCELLED
```

## Database Schema

```sql
users (id, username, email, role, created_at, updated_at)
cart (id, user_id, status, created_at, updated_at)
cart_item (id, cart_id, product_id, product_name, unit_price, quantity)
orders (id, user_id, cart_id, status, subtotal, shipping_fee, total_amount, ...)
order_item (id, order_id, product_id, product_name, unit_price, quantity)
payment_callback_log (id, order_id, callback_reference, callback_type, payload)
```

## Running the Application

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (for production)
- IntelliJ IDEA (recommended)

### Using IntelliJ IDEA

The project includes a pre-configured run configuration:

1. Open the project in IntelliJ IDEA
2. The run configuration should appear automatically as **"LoopinsCoreApplication"**
3. Click the green play button or press `Shift + F10`
4. The application will start with the `dev` profile (H2 database)

**Main Class**: `com.loopins.core.LoopinsCoreApplication`

If the run configuration doesn't appear:
1. Go to **Run** → **Edit Configurations**
2. Click **+** → **Spring Boot**
3. Set **Main class** to: `com.loopins.core.LoopinsCoreApplication`
4. Set **Active profiles** to: `dev`
5. Click **OK**

### Development Mode (H2 Database)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Access H2 Console: http://localhost:8080/api/h2-console

### Production Mode (PostgreSQL)

```bash
# Set environment variables
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export FULFILLMENT_SERVICE_URL=http://fulfillment-service:8081
export FULFILLMENT_API_KEY=your_api_key
export SERVICE_API_KEY=your_service_key

mvn spring-boot:run
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | loopins |
| `DB_PASSWORD` | Database password | loopins |
| `FULFILLMENT_SERVICE_URL` | Fulfillment service URL | http://localhost:8081 |
| `FULFILLMENT_API_KEY` | API key for Fulfillment service | default-api-key |
| `SERVICE_API_KEY` | API key for incoming service calls | core-service-secret-key |

## Service-to-Service Security

Protected endpoints require `X-SERVICE-KEY` header:
- `POST /api/orders/{orderId}/payment-confirmed`
- `POST /api/orders/{orderId}/payment-failed`

## Example Usage

### 1. Create Cart

```bash
curl -X POST http://localhost:8080/api/carts \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

### 2. Add Item to Cart

```bash
curl -X POST http://localhost:8080/api/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "Wireless Mouse",
    "unitPrice": 150000,
    "quantity": 2
  }'
```

### 3. Checkout

```bash
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "cartId": 1,
    "userId": 1,
    "shippingAddress": "Jl. Sudirman No. 1, Jakarta",
    "originCity": "Jakarta",
    "destinationCity": "Bandung",
    "weightInKg": 0.5
  }'
```

### 4. Confirm Payment (Service-to-Service)

```bash
curl -X POST http://localhost:8080/api/orders/ORDER-XXXX/payment-confirmed \
  -H "Content-Type: application/json" \
  -H "X-SERVICE-KEY: dev-service-key" \
  -d '{
    "paymentReference": "PAY-123",
    "callbackReference": "CB-456",
    "status": "SUCCESS"
  }'
```

## Resilience Configuration

The service uses Resilience4j for fault tolerance:

- **Circuit Breaker**: Opens after 50% failure rate (10 calls window)
- **Retry**: 3 attempts with exponential backoff
- **Timeout**: 10 seconds per call

## Monitoring

Health and metrics available via Spring Actuator:
- Health: `GET /api/actuator/health`
- Metrics: `GET /api/actuator/metrics`
- Circuit Breakers: `GET /api/actuator/circuitbreakers`

## License

MIT License

