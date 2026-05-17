# 🍱 Meal Pre-Order & Collect System

A production-grade **Low Level Design (LLD)** implementation of a Meal Pre-Order system — built to demonstrate MAANG-level design thinking for SDE-2 interviews.

## 📌 What This Project Demonstrates

| Concept | Where Used |
|---|---|
| **SOLID Principles** | Every class has a single responsibility; interfaces over concretions |
| **Observer Pattern** | `OrderObserver` → SMS, Push, Email notifiers decoupled from `Order` |
| **State Pattern** | `OrderState` machine with illegal transition enforcement |
| **Strategy Pattern** | `PricingStrategy` → Regular, VIP, PeakHour pricing swapped at runtime |
| **Factory Pattern** | `OrderFactory` encapsulates creation logic |
| **Lazy Loading** | `MenuCache` + lazy field init — DB not hit until menu is needed |
| **Thread Safety** | `AtomicInteger` for slot counts, `ConcurrentHashMap` for caches, `CopyOnWriteArrayList` for observers |
| **Repository Pattern** | Data access abstracted behind interfaces |
| **Builder Pattern** | `Order.Builder` for readable, validated object construction |

## 🗂️ Project Structure

```
src/main/java/com/mealorder/
├── model/          Core domain entities (Rider, Restaurant, Order, Menu, MenuItem)
├── enums/          OrderStatus, NotificationType, OrderType
├── state/          State Pattern — PlacedState, ConfirmedState, PreparingState...
├── observer/       Observer Pattern — OrderObserver interface + notifiers
├── factory/        Factory Pattern — OrderFactory
├── strategy/       Strategy Pattern — PricingStrategy + implementations
├── service/        Business logic — OrderService, NotificationService
├── repository/     Repository Pattern — in-memory implementations
├── cache/          Lazy-loaded MenuCache
├── exception/      Custom domain exceptions
└── util/           GeoPoint, distance calculations
```

## 💡 Key Design Decisions

- **Observer over direct calls**: Adding WhatsApp notifications requires zero changes to Order class
- **State Pattern over if-else**: Illegal state transitions fail at the call site, not silently
- **CopyOnWriteArrayList for observers**: Safe iteration during concurrent add/remove
- **AtomicInteger for slots**: CAS operation — no thread blocking, measurably faster than synchronized at high concurrency

## 📈 Production Improvements (Resume Talking Points)
- Replace in-memory MenuCache with Redis
- Replace synchronous Observer calls with Kafka events
- Add PostgreSQL with idempotency keys on orders
- Add Circuit Breaker (Resilience4j) around notification services
- Distributed slot locking via Redis SETNX
