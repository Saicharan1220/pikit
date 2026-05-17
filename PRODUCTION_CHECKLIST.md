# 🚀 Production-Level Meal Pre-Order System - Complete Checklist

## Phase Overview
This checklist takes you from current REST API foundation to a **production-ready, deployable application** with a simple UI and full deployment pipeline.

---

## ✅ **PHASE 1: Complete REST API (Currently Done)**

- [x] Spring Boot 3 with Java 21
- [x] Generic API response wrapper (ApiResponse<T>)
- [x] Custom exception hierarchy with HTTP mapping
- [x] Global exception handler
- [x] Distributed tracing (Sleuth)
- [x] Database configuration (HikariCP, Hibernate batch)
- [x] Flyway migrations
- [x] Actuator monitoring endpoints
- [x] Logging with rotation
- [x] Jackson JSON optimization
- [x] Swagger/OpenAPI documentation
- [x] Profile-based configuration (dev/prod)

**Status: 95% Complete** ⏳ Estimated Time: Already Done ✅

---

## ✅ **PHASE 2: Complete REST Controllers & Services**

### 2.1 JPA Entity Models
- [ ] **Restaurant Entity** (id, name, location, isActive)
- [ ] **MenuItem Entity** (id, restaurantId, name, price, isAvailable)
- [ ] **Order Entity** (id, restaurantId, status, totalAmount, pickupTime)
- [ ] **OrderItem Entity** (id, orderId, menuItemId, quantity, unitPrice)
- [ ] **DeliverySlot Entity** (id, restaurantId, slotTime, maxCapacity, currentOrders)
- [ ] Add Lombok annotations (@Entity, @Data, @NoArgsConstructor, @AllArgsConstructor, @Builder)
- [ ] Add validation annotations (@NotNull, @NotBlank, @Positive, @Min, @Max)
- [ ] Add JPA annotations (@Column, @Index, @Unique)

**Estimated Time: 2 hours**

### 2.2 Request/Response DTOs
- [ ] CreateRestaurantRequest (name, latitude, longitude)
- [ ] RestaurantResponse (id, name, location)
- [ ] CreateMenuItemRequest (restaurantId, name, price)
- [ ] MenuItemResponse (id, name, price, available)
- [ ] CreateOrderRequest (restaurantId, items[], pickupSlotId)
- [ ] OrderResponse (id, status, totalAmount, items)
- [ ] OrderItemResponse (menuItemId, quantity, unitPrice)
- [ ] OrderStatusResponse (status, message)

**Estimated Time: 1 hour**

### 2.3 Repository Layer (JPA Repositories)
- [ ] RestaurantRepository (findByIsActive, findById)
- [ ] MenuItemRepository (findByRestaurantId, findByIsAvailable)
- [ ] OrderRepository (findByStatus, findByRestaurantId, findByCreatedAtBetween)
- [ ] OrderItemRepository (findByOrderId)
- [ ] DeliverySlotRepository (findByRestaurantIdAndSlotTime, custom queries)
- [ ] Add custom @Query annotations for complex queries
- [ ] Add @Transactional for write operations

**Estimated Time: 2 hours**

### 2.4 Service Layer (Business Logic)
- [ ] **RestaurantService**
  - [ ] createRestaurant(CreateRestaurantRequest) → Restaurant
  - [ ] getRestaurant(id) → RestaurantResponse
  - [ ] getAllRestaurants() → List<RestaurantResponse>
  - [ ] updateRestaurant(id, request)
  - [ ] deleteRestaurant(id)

- [ ] **MenuService**
  - [ ] addMenuItem(restaurantId, CreateMenuItemRequest)
  - [ ] getMenuItems(restaurantId) → List<MenuItemResponse>
  - [ ] updateMenuItem(id, request)
  - [ ] deleteMenuItem(id)

- [ ] **OrderService** (MOST IMPORTANT - Complex Business Logic)
  - [ ] createOrder(CreateOrderRequest) → Order
    - [ ] Validate restaurant exists
    - [ ] Validate menu items exist & available
    - [ ] Calculate total amount with pricing strategy
    - [ ] Check slot capacity (AtomicInteger increment)
    - [ ] Create order with state machine (PlacedState)
    - [ ] Publish observer events (notifications)
  - [ ] confirmOrder(orderId) → Order
  - [ ] cancelOrder(orderId) → Order
  - [ ] getOrder(orderId) → OrderResponse
  - [ ] getOrdersByRestaurant(restaurantId) → List<OrderResponse>
  - [ ] getOrdersByStatus(status) → List<OrderResponse>

- [ ] **DeliverySlotService**
  - [ ] getAvailableSlots(restaurantId) → List<DeliverySlotResponse>
  - [ ] bookSlot(restaurantId, slotTime) → boolean
  - [ ] releaseSlot(restaurantId, slotTime) → boolean
  - [ ] generateUpcomingSlots(restaurantId) → automatic

- [ ] **PricingService** (Strategy Pattern)
  - [ ] calculatePrice(orderItems, pricingStrategy) → BigDecimal
  - [ ] Strategies: Regular, VIP, PeakHour

- [ ] **NotificationService** (Observer Pattern)
  - [ ] notifyOrderConfirmed(order) → triggers SMS, Email, Push
  - [ ] notifyOrderReady(order)
  - [ ] notifyOrderCancelled(order)

**Estimated Time: 6 hours**

### 2.5 REST Controllers
- [ ] **RestaurantController** (/api/v1/restaurants)
  - [ ] POST /api/v1/restaurants (createRestaurant)
  - [ ] GET /api/v1/restaurants (getAll)
  - [ ] GET /api/v1/restaurants/{id} (getById)
  - [ ] PUT /api/v1/restaurants/{id} (update)
  - [ ] DELETE /api/v1/restaurants/{id} (delete)

- [ ] **MenuController** (/api/v1/menus)
  - [ ] POST /api/v1/menus (addMenuItem)
  - [ ] GET /api/v1/menus?restaurantId={id} (getByRestaurant)
  - [ ] PUT /api/v1/menus/{id} (updateMenuItem)
  - [ ] DELETE /api/v1/menus/{id} (deleteMenuItem)

- [ ] **OrderController** (/api/v1/orders) - MOST CRITICAL
  - [ ] POST /api/v1/orders (createOrder) - Returns 201 Created
  - [ ] GET /api/v1/orders/{id} (getOrder)
  - [ ] GET /api/v1/orders?status=CONFIRMED (filter)
  - [ ] POST /api/v1/orders/{id}/confirm (confirmOrder)
  - [ ] POST /api/v1/orders/{id}/cancel (cancelOrder)
  - [ ] GET /api/v1/orders/restaurant/{restaurantId} (byRestaurant)

- [ ] **DeliverySlotController** (/api/v1/slots)
  - [ ] GET /api/v1/slots?restaurantId={id} (available)
  - [ ] POST /api/v1/slots/{id}/book (bookSlot)

**Estimated Time: 3 hours**

### 2.6 Add Validation Annotations
- [ ] @Valid on all request DTOs
- [ ] @NotNull, @NotBlank, @Positive, @Min, @Max
- [ ] @Email for customer email (future)
- [ ] Custom validation annotations (@ValidOrderState)

**Estimated Time: 1 hour**

**PHASE 2 Total Time: ~15 hours**

---

## ✅ **PHASE 3: Comprehensive Unit & Integration Tests**

### 3.1 Unit Tests (JUnit 5 + Mockito)
- [ ] **RestaurantServiceTest**
  - [ ] testCreateRestaurant_Success()
  - [ ] testCreateRestaurant_InvalidCoordinates()
  - [ ] testGetRestaurant_NotFound()

- [ ] **OrderServiceTest** (MOST CRITICAL)
  - [ ] testCreateOrder_Success()
  - [ ] testCreateOrder_RestaurantNotFound() → OrderNotFoundException
  - [ ] testCreateOrder_MenuItemNotAvailable()
  - [ ] testCreateOrder_InsufficientSlots() → InsufficientSlotsException
  - [ ] testConfirmOrder_InvalidState() → InvalidOrderStateException
  - [ ] testCancelOrder_Success()
  - [ ] testCalculateTotalPrice_WithPricingStrategy()

- [ ] **PricingStrategyTest**
  - [ ] testRegularPricing()
  - [ ] testVIPPricing()
  - [ ] testPeakHourPricing()

- [ ] **NotificationServiceTest**
  - [ ] testNotifyOrderConfirmed_CallsObservers()
  - [ ] Mock SMS, Email, Push observers

- [ ] **GlobalExceptionHandlerTest**
  - [ ] testHandleOrderNotFoundException() → 404
  - [ ] testHandleInvalidOrderStateException() → 400
  - [ ] testHandleValidationException() → fieldErrors

**Target Coverage: 85%+**
**Estimated Time: 4 hours**

### 3.2 Integration Tests (Testcontainers + PostgreSQL)
- [ ] **RestaurantControllerIT**
  - [ ] testCreateRestaurant_E2E()
  - [ ] testGetRestaurant_E2E()
  - [ ] Uses real PostgreSQL in container

- [ ] **OrderControllerIT** (CRITICAL)
  - [ ] testCreateOrder_E2E() → Database persistence
  - [ ] testConfirmOrder_E2E() → State transition
  - [ ] testOrderNotification_E2E() → Observer triggers
  - [ ] Uses Testcontainers.postgresql()

- [ ] **ConcurrencyTest**
  - [ ] testMultipleOrdersOnSameSlot() → Thread-safe
  - [ ] Verify AtomicInteger slot counting
  - [ ] 10 concurrent threads, 1 available slot → 1 succeeds, 9 fail

**Target: 10+ integration tests**
**Estimated Time: 3 hours**

### 3.3 Test Configuration
- [ ] application-test.yml (H2 in-memory)
- [ ] TestDataBuilder (convenient test data creation)
- [ ] @DataJpaTest for repository tests
- [ ] @WebMvcTest for controller tests
- [ ] @SpringBootTest for integration tests

**Estimated Time: 1 hour**

**PHASE 3 Total Time: ~8 hours**

---

## ✅ **PHASE 4: Simple Frontend UI**

### 4.1 Frontend Setup (React)
- [ ] Create `frontend/` directory
- [ ] `npx create-react-app frontend`
- [ ] Install axios (HTTP client)
- [ ] Install react-router (navigation)
- [ ] Install tailwindcss (styling)

**Estimated Time: 30 minutes**

### 4.2 API Client Layer
- [ ] `src/api/apiClient.js` - Axios instance with base URL
- [ ] `src/api/restaurantService.js` - API calls to /restaurants
- [ ] `src/api/orderService.js` - API calls to /orders
- [ ] Handle errors globally (show error toast)

**Estimated Time: 1 hour**

### 4.3 Pages & Components

#### 4.3.1 Restaurant Listing Page
- [ ] Component: RestaurantList.jsx
- [ ] Shows list of active restaurants
- [ ] Cards with: name, location, "View Menu" button
- [ ] Search/filter by name
- [ ] Loading spinner while fetching

#### 4.3.2 Menu Page
- [ ] Component: MenuPage.jsx
- [ ] Shows menu items for selected restaurant
- [ ] Item cards: name, price, "Add to Cart" button
- [ ] Shopping cart (useState)
- [ ] "Checkout" button

#### 4.3.3 Order Creation Page
- [ ] Component: OrderCheckout.jsx
- [ ] Display cart items (quantity × price)
- [ ] Show total amount
- [ ] Select delivery slot dropdown
- [ ] "Place Order" button
- [ ] Validate: items not empty, slot selected

#### 4.3.4 Order Confirmation Page
- [ ] Component: OrderConfirmation.jsx
- [ ] Display: Order ID, status, total amount
- [ ] Items list
- [ ] Pickup time
- [ ] "View Order Details" button
- [ ] "Continue Shopping" button

#### 4.3.5 Order Status Page
- [ ] Component: OrderStatus.jsx
- [ ] Search orders by ID
- [ ] Display: status (PLACED, CONFIRMED, PREPARING, READY, CANCELLED)
- [ ] Real-time status update (polling every 5 seconds)
- [ ] "Cancel Order" button (if cancellable)

**Estimated Time: 4 hours**

### 4.4 Styling
- [ ] Tailwind CSS classes (responsive, mobile-first)
- [ ] Dark mode support
- [ ] Toast notifications (error, success)
- [ ] Loading spinners, skeleton screens

**Estimated Time: 1 hour**

### 4.5 Environment Configuration
- [ ] `.env` file with API_BASE_URL
- [ ] Different URLs for dev/prod
- [ ] Build process (npm run build)

**Estimated Time: 30 minutes**

**PHASE 4 Total Time: ~7 hours**

---

## ✅ **PHASE 5: Docker & Containerization**

### 5.1 Backend Dockerfile
- [ ] Create `Dockerfile` in project root
- [ ] Build stage: Maven build with JDK 21
- [ ] Runtime stage: JRE 21 slim image
- [ ] Multi-stage build (reduces image size by 70%)
- [ ] Expose port 8080
- [ ] Health check endpoint

**Dockerfile Content:**
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Estimated Time: 30 minutes**

### 5.2 Frontend Dockerfile
- [ ] Create `frontend/Dockerfile`
- [ ] Build stage: Node.js 20 build
- [ ] Runtime stage: Nginx serve static files
- [ ] Multi-stage build
- [ ] Expose port 3000

**Estimated Time: 30 minutes**

### 5.3 Docker Compose
- [ ] Create `docker-compose.yml`
- [ ] Services:
  - [ ] backend (meal-preorder-api)
  - [ ] frontend (meal-preorder-ui)
  - [ ] postgres (PostgreSQL 15)
  - [ ] pgadmin (Database UI)
- [ ] Networks: internal communication
- [ ] Volumes: persistent data for PostgreSQL
- [ ] Environment variables for all services

**docker-compose.yml Structure:**
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: mealorder_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
  
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mealorder_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_PROFILES_ACTIVE: prod
    ports:
      - "8080:8080"
    depends_on:
      - postgres
  
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    environment:
      REACT_APP_API_URL: http://backend:8080/api/v1
    ports:
      - "3000:3000"
    depends_on:
      - backend

volumes:
  postgres_data:
```

**Estimated Time: 1 hour**

### 5.4 .dockerignore Files
- [ ] Create `.dockerignore` (exclude node_modules, .git, etc.)
- [ ] Create `frontend/.dockerignore`

**Estimated Time: 15 minutes**

**PHASE 5 Total Time: ~2.5 hours**

---

## ✅ **PHASE 6: CI/CD Pipeline (GitHub Actions)**

### 6.1 GitHub Actions Workflow
- [ ] Create `.github/workflows/ci-cd.yml`
- [ ] Triggers: on push to main/pikit_v01, on pull requests

**Workflow Steps:**
1. **Build & Test**
   - [ ] Checkout code
   - [ ] Setup Java 21
   - [ ] Run tests with Maven (`mvn test`)
   - [ ] Code coverage report (JaCoCo)
   - [ ] Fail if coverage < 80%

2. **Code Quality**
   - [ ] SonarQube scan (optional)
   - [ ] Checkstyle validation
   - [ ] SpotBugs static analysis

3. **Docker Build**
   - [ ] Build backend Docker image
   - [ ] Build frontend Docker image
   - [ ] Tag images with commit SHA

4. **Push to Docker Registry** (Docker Hub or GitHub Container Registry)
   - [ ] Login to registry
   - [ ] Push backend image
   - [ ] Push frontend image

5. **Deploy to Staging** (optional, if using cloud)
   - [ ] Deploy docker-compose to staging server
   - [ ] Run smoke tests
   - [ ] Notify Slack on success/failure

**Estimated Time: 2 hours**

### 6.2 GitHub Actions Configuration
- [ ] Create secrets in GitHub Settings:
  - [ ] DOCKER_USERNAME
  - [ ] DOCKER_PASSWORD
  - [ ] SONAR_TOKEN (optional)
- [ ] Restrict deployments to main branch only
- [ ] Add branch protection: require CI to pass before merging

**Estimated Time: 30 minutes**

**PHASE 6 Total Time: ~2.5 hours**

---

## ✅ **PHASE 7: Cloud Deployment**

### 7.1 Choose Cloud Provider (Pick ONE)

#### Option A: AWS (Recommended for MAANG)
- [ ] **ECR (Elastic Container Registry)**
  - [ ] Create ECR repository for backend image
  - [ ] Create ECR repository for frontend image
  
- [ ] **RDS (PostgreSQL Database)**
  - [ ] Create RDS instance (db.t3.micro for dev)
  - [ ] Set up security groups
  - [ ] Create database and user
  
- [ ] **ECS (Elastic Container Service)**
  - [ ] Create ECS cluster
  - [ ] Create task definitions for backend & frontend
  - [ ] Create services with auto-scaling
  - [ ] Configure load balancer
  
- [ ] **ALB (Application Load Balancer)**
  - [ ] Create ALB
  - [ ] Configure target groups
  - [ ] Set up HTTPS with ACM certificate
  
- [ ] **Auto-Scaling**
  - [ ] Create auto-scaling groups
  - [ ] Scale based on CPU/memory
  - [ ] Min: 2 instances, Max: 10

**Estimated Time: 4 hours**

#### Option B: DigitalOcean (Simpler, Cheaper)
- [ ] Create App Platform project
- [ ] Connect GitHub repository
- [ ] Specify docker-compose.yml
- [ ] Deploy automatically on push
- [ ] Enable auto-scaling

**Estimated Time: 1 hour**

#### Option C: Heroku (Easiest, but Deprecated)
- [ ] Not recommended (Heroku free tier ended)

#### Option D: Google Cloud Run (Serverless)
- [ ] Build and push Docker image to Artifact Registry
- [ ] Deploy backend as Cloud Run service
- [ ] Deploy frontend as Cloud Storage + Cloud CDN
- [ ] Set up Cloud SQL for PostgreSQL
- [ ] Configure API Gateway for routing

**Estimated Time: 2 hours**

### 7.2 Domain & SSL
- [ ] Register domain (GoDaddy, Route 53, etc.)
- [ ] Configure DNS to point to your deployment
- [ ] Set up SSL certificate (Let's Encrypt, ACM)
- [ ] Enforce HTTPS redirect

**Estimated Time: 30 minutes**

### 7.3 Monitoring & Logging
- [ ] CloudWatch (AWS) / Cloud Logging (GCP)
- [ ] Set up alarms for:
  - [ ] High CPU/memory
  - [ ] Error rate > 1%
  - [ ] Response time > 500ms
- [ ] Create dashboards

**Estimated Time: 1 hour**

**PHASE 7 Total Time: 2-4 hours (depends on provider)**

---

## ✅ **PHASE 8: Production Hardening**

### 8.1 Security
- [ ] Add Spring Security (authentication/authorization)
- [ ] Implement API rate limiting
- [ ] Add CORS configuration
- [ ] SQL injection protection (JPA parameterized queries - already done)
- [ ] XSS protection (Content Security Policy headers)
- [ ] CSRF protection (for state-changing requests)
- [ ] Helmet.js for frontend security headers

**Estimated Time: 2 hours**

### 8.2 Performance Optimization
- [ ] Add Redis caching for:
  - [ ] Menu items (invalidate every 1 hour)
  - [ ] Restaurants (invalidate every 30 mins)
  - [ ] Available slots (invalidate every 5 mins)
- [ ] Enable HTTP compression (already done in config)
- [ ] CDN for static assets (frontend images, CSS)
- [ ] Database query optimization (add more indexes)
- [ ] Load testing (JMeter) to verify performance

**Estimated Time: 3 hours**

### 8.3 Backup & Disaster Recovery
- [ ] Automated daily backups of PostgreSQL
- [ ] Test restore procedure monthly
- [ ] Point-in-time recovery enabled
- [ ] Multi-region replication (for critical systems)

**Estimated Time: 1 hour**

### 8.4 Documentation
- [ ] **API Documentation**: Swagger already auto-generated
- [ ] **Deployment Guide**: Step-by-step to deploy on new server
- [ ] **Architecture Decision Records (ADRs)**: Why each decision was made
- [ ] **Runbook**: How to handle common issues
- [ ] **Database Schema Diagram**: Visual representation
- [ ] **Monitoring & Alerting Guide**: How to check health

**Estimated Time: 2 hours**

**PHASE 8 Total Time: ~8 hours**

---

## ✅ **PHASE 9: Load Testing & Performance Benchmarking**

### 9.1 Load Testing Setup
- [ ] Install JMeter or Gatling
- [ ] Create test scenarios:
  - [ ] Create 1000 restaurants concurrently
  - [ ] Create 5000 orders simultaneously
  - [ ] Filter orders by status (concurrent reads)
  - [ ] Ramp up: 0 → 100 users over 5 minutes
  - [ ] Hold: 100 users for 10 minutes
  - [ ] Ramp down: 100 → 0 users over 5 minutes

### 9.2 Performance Benchmarks
- [ ] POST /orders: < 100ms p95, < 200ms p99
- [ ] GET /orders: < 50ms p95, < 100ms p99
- [ ] System handles 1000 concurrent users
- [ ] Database: < 50 connections under load
- [ ] Memory: < 512MB under load
- [ ] Error rate: < 0.1%

### 9.3 Stress Testing
- [ ] Push to 10K concurrent users
- [ ] Identify breaking point
- [ ] Document max sustainable load

**Estimated Time: 2 hours**

**PHASE 9 Total Time: ~2 hours**

---

## 📊 **COMPLETE CHECKLIST SUMMARY**

| Phase | Component | Status | Est. Hours |
|-------|-----------|--------|-----------|
| 1 | REST API Foundation | ✅ DONE | 0 |
| 2 | Controllers & Services | ⏳ TODO | 15 |
| 3 | Testing | ⏳ TODO | 8 |
| 4 | Frontend UI | ⏳ TODO | 7 |
| 5 | Docker & Compose | ⏳ TODO | 2.5 |
| 6 | CI/CD Pipeline | ⏳ TODO | 2.5 |
| 7 | Cloud Deployment | ⏳ TODO | 3 |
| 8 | Security & Hardening | ⏳ TODO | 8 |
| 9 | Load Testing | ⏳ TODO | 2 |
| **TOTAL** | | | **48 hours** |

**Timeline: ~2 weeks of full-time work OR 4-6 weeks part-time**

---

## 🎯 **Deployment Architecture (Final)**

```
GitHub Repository
    ↓
    ├── GitHub Actions CI/CD
    │   ├── Build backend JAR
    │   ├── Run tests (85%+ coverage)
    │   ├── Build Docker images
    │   └── Push to Docker registry
    ↓
Docker Registry (Docker Hub / ECR)
    ↓
Cloud Provider (AWS/GCP/DigitalOcean)
    ├── Load Balancer
    │   ├── Backend API (ECS/Container Service)
    │   │   ├── 2-10 instances (auto-scaling)
    │   │   └── PostgreSQL RDS
    │   └── Frontend (S3 + CloudFront / Container Service)
    ├── Monitoring (CloudWatch / Cloud Monitoring)
    ├── Logging (CloudWatch Logs / Cloud Logging)
    └── Backups (Automated daily)
    
Client Browser
    ↓
Load Balancer (HTTPS)
    ├── Frontend (React SPA) → 3000
    └── Backend API (Spring Boot) → 8080
```

---

## 🚀 **Quick Start Commands**

### Local Development
```bash
# Terminal 1: Start PostgreSQL
docker-compose up postgres pgadmin

# Terminal 2: Start backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Terminal 3: Start frontend
cd frontend && npm start
```

### Build & Deploy Locally
```bash
# Build Docker images
docker-compose build

# Run entire stack
docker-compose up
```

### Deploy to Cloud (AWS example)
```bash
# Build and push images
mvn clean package
docker build -t meal-preorder:latest .
docker tag meal-preorder:latest <aws-account>.dkr.ecr.<region>.amazonaws.com/meal-preorder:latest
aws ecr get-login-password | docker login --username AWS --password-stdin <aws-account>.dkr.ecr.<region>.amazonaws.com
docker push <aws-account>.dkr.ecr.<region>.amazonaws.com/meal-preorder:latest

# Deploy via CloudFormation / ECS
aws ecs update-service --cluster meal-preorder --service meal-preorder-backend --force-new-deployment
```

---

## 📝 **Next Steps**

1. **Start Phase 2**: Create entities and DTOs (2 hours)
2. **Commit frequently**: Every 2-3 hours for clean git history
3. **Test as you build**: Write tests alongside features
4. **Document decisions**: Add ADRs in `docs/adr/` folder
5. **Get feedback**: Share progress with senior engineers

---

## 💾 **File Structure (Final)**

```
pikit/
├── src/main/java/com/mealorder/
│   ├── MealOrderApplication.java
│   ├── api/
│   │   ├── controller/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── RestaurantController.java
│   │   │   ├── MenuController.java
│   │   │   ├── OrderController.java
│   │   │   └── DeliverySlotController.java
│   │   ├── dto/
│   │   │   ├── ApiResponse.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── RestaurantRequest/Response.java
│   │   │   ├── OrderRequest/Response.java
│   │   │   └── ...
│   │   └── util/
│   │       └── TraceIdProvider.java
│   ├── model/
│   │   ├── Restaurant.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── DeliverySlot.java
│   ├── service/
│   │   ├── RestaurantService.java
│   │   ├── MenuService.java
│   │   ├── OrderService.java
│   │   ├── DeliverySlotService.java
│   │   ├── PricingService.java
│   │   └── NotificationService.java
│   ├── repository/
│   │   ├── RestaurantRepository.java
│   │   ├── MenuItemRepository.java
│   │   ├── OrderRepository.java
│   │   ├── OrderItemRepository.java
│   │   └── DeliverySlotRepository.java
│   └── exception/
│       ├── BaseException.java
│       ├── OrderNotFoundException.java
│       ├── InvalidOrderStateException.java
│       ├── RestaurantNotFoundException.java
│       └── InsufficientSlotsException.java
├── src/test/java/com/mealorder/
│   ├── service/
│   │   ├── OrderServiceTest.java
│   │   └── ...
│   ├── controller/
│   │   ├── OrderControllerIT.java
│   │   └── ...
│   └── integration/
│       └── E2ETests.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-prod.yml
│   ├── application-test.yml
│   └── db/migration/
│       ├── V1__initial_schema.sql
│       ├── V2__add_more_indexes.sql
│       └── ...
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   │   ├── RestaurantList.jsx
│   │   │   ├── MenuPage.jsx
│   │   │   ├── OrderCheckout.jsx
│   │   │   ├── OrderConfirmation.jsx
│   │   │   └── OrderStatus.jsx
│   │   ├── api/
│   │   │   ├── apiClient.js
│   │   │   ├── restaurantService.js
│   │   │   └── orderService.js
│   │   ├── components/
│   │   │   ├── Header.jsx
│   │   │   ├── CartSummary.jsx
│   │   │   ├── LoadingSpinner.jsx
│   │   │   └── ...
│   │   └── App.jsx
│   └── package.json
├── .github/workflows/
│   └── ci-cd.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── PRODUCTION_CHECKLIST.md
└── docs/
    ├── ARCHITECTURE.md
    ├── API_DOCUMENTATION.md
    ├── DEPLOYMENT_GUIDE.md
    └── adr/
        ├── 0001-use-spring-boot-3.md
        ├── 0002-global-exception-handler.md
        └── ...
```

---

**Good luck! You've got this! 🚀**
