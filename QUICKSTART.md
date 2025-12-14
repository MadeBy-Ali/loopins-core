# Quick Start Guide - Loopins Core Service

## Main Class Location
**Full Class Name**: `com.loopins.core.LoopinsCoreApplication`
**File Path**: `src/main/java/com/loopins/core/LoopinsCoreApplication.java`

## Running in IntelliJ IDEA

### Option 1: Use Pre-configured Run Configuration
1. Open IntelliJ IDEA
2. Look for **"LoopinsCoreApplication"** in the run configurations dropdown (top-right)
3. Click the green play button ▶️ or press `Shift + F10`

### Option 2: Create Run Configuration Manually
1. Go to **Run** → **Edit Configurations**
2. Click **+** → **Spring Boot**
3. Fill in:
   - **Name**: `LoopinsCoreApplication`
   - **Main class**: `com.loopins.core.LoopinsCoreApplication`
   - **Active profiles**: `dev`
   - **Working directory**: `$MODULE_WORKING_DIR$`
4. Click **OK**
5. Click the green play button ▶️

### Option 3: Run from Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Accessing the Application

Once the application starts, you can access:

### Swagger UI (API Documentation)
**URL**: http://localhost:8080/api/swagger-ui.html

This provides:
- ✅ Interactive API documentation
- ✅ Try out endpoints directly from browser
- ✅ View request/response schemas
- ✅ Example values
- ✅ All endpoint descriptions

### OpenAPI Specification
- **JSON**: http://localhost:8080/api/v3/api-docs
- **YAML**: http://localhost:8080/api/v3/api-docs.yaml

### H2 Database Console (Dev Profile)
**URL**: http://localhost:8080/api/h2-console
- **JDBC URL**: `jdbc:h2:mem:loopins_core_dev`
- **Username**: `sa`
- **Password**: (leave empty)

### Health Check
**URL**: http://localhost:8080/api/actuator/health

## Quick API Testing (via Swagger)

1. Open http://localhost:8080/api/swagger-ui.html
2. Look for **"Cart Management"** section
3. Try **POST /api/carts**:
   ```json
   {
     "userId": 1
   }
   ```
4. Try **POST /api/carts/{cartId}/items**:
   ```json
   {
     "productId": "PROD-001",
     "productName": "Wireless Mouse",
     "unitPrice": 150000,
     "quantity": 2
   }
   ```
5. Try **POST /api/orders/checkout**:
   ```json
   {
     "cartId": 1,
     "userId": 1,
     "shippingAddress": "Jl. Sudirman No. 1, Jakarta",
     "originCity": "Jakarta",
     "destinationCity": "Bandung",
     "weightInKg": 0.5
   }
   ```

## Sample Users (Seeded in Dev Profile)

| ID | Username    | Email              | Role     |
|----|-------------|--------------------|----------|
| 1  | john_doe    | john@example.com   | CUSTOMER |
| 2  | jane_smith  | jane@example.com   | CUSTOMER |
| 3  | acme_store  | store@acme.com     | SELLER   |
| 4  | admin       | admin@loopins.com  | ADMIN    |

## Swagger Features

### API Groups
- **Cart Management** - Create and manage shopping carts
- **Checkout** - Checkout process and payment
- **Order Management** - View and manage orders

### Security
Protected endpoints (payment callbacks) show 🔒 icon and require `X-SERVICE-KEY` header.

### Try It Out
Click "Try it out" on any endpoint to:
1. Fill in parameters
2. Click "Execute"
3. See the actual response

## Troubleshooting

### Can't find main class in IntelliJ
1. Go to **File** → **Project Structure**
2. Ensure JDK 17+ is selected
3. Go to **Modules** → Check `src/main/java` is marked as "Sources"
4. Click **File** → **Invalidate Caches** → **Invalidate and Restart**

### Port 8080 already in use
Edit `application-dev.yml` and change:
```yaml
server:
  port: 8081  # or any available port
```

### Swagger not loading
1. Ensure application started successfully
2. Check logs for errors
3. Verify URL: http://localhost:8080/api/swagger-ui.html (note the `/api` prefix)

## Next Steps

1. ✅ Start the application
2. ✅ Open Swagger UI
3. ✅ Explore the APIs
4. ✅ Test cart creation
5. ✅ Test checkout flow
6. 🚀 Integrate with Fulfillment Service (next microservice)

## Environment Profiles

- **dev** - H2 in-memory database, verbose logging, H2 console enabled
- **test** - H2 in-memory for testing
- **default** - PostgreSQL production database

## Tech Stack Summary

- ☕ Java 17
- 🍃 Spring Boot 3.2
- 📊 JPA/Hibernate
- 🐘 PostgreSQL / H2
- 🔄 Flyway migrations
- 🌐 OpenFeign
- 🛡️ Resilience4j
- 📖 SpringDoc OpenAPI 3 (Swagger)
- 🎯 MapStruct
- 🧰 Lombok

## Support

For issues or questions, refer to:
- README.md in project root
- Swagger UI for API documentation
- Application logs in console

