# Tổng quan kiến trúc — Ecommerce Microservices (Phase 1)

Demo lab Module 4 Bài 2: Gateway · Auth · Product · Order · Notify.  
Syllabus: [`5_java_m4_bai2_Microservices.md`](../../syllabus/module-4/5_java_m4_bai2_Microservices.md)

## Kiến trúc hệ thống

Hệ thống minh họa: **API Gateway**, JWT ở biên, gọi HTTP giữa service, **Database-per-service**.

```mermaid
flowchart TD
    Client["Client / Postman / Web"]

    Gateway["API Gateway<br/>:8080"]

    Auth["Auth Service<br/>:8081"]
    Product["Product Service<br/>:8082"]
    Order["Order Service<br/>:8083"]
    Notification["Notification Service<br/>:8084"]

    AuthDB[("MongoDB<br/>auth_db")]
    ProductDB[("MongoDB<br/>product_db")]
    OrderDB[("MongoDB<br/>order_db")]
    SMTP["SMTP / Email"]

    Client --> Gateway
    Gateway --> Auth
    Gateway --> Product
    Gateway --> Order
    Auth --> AuthDB
    Product --> ProductDB
    Order --> OrderDB
    Order -->|HTTP REST| Product
    Order -->|HTTP REST| Notification
    Notification --> SMTP
```

## Phụ thuộc giữa các service

```mermaid
flowchart LR
    Client --> Gateway
    Gateway --> Auth
    Gateway --> Product
    Gateway --> Order
    Order --> Product
    Order --> Notification
    Auth --> AuthDB
    Product --> ProductDB
    Order --> OrderDB
    Notification --> SMTP
```

**Cấm dependency ngược:** Notify → Order · Product → Order · Auth → Order.

## Routing qua Gateway

```mermaid
flowchart LR
    Client["Client"]
    Gateway["API Gateway :8080"]
    Auth["Auth :8081"]
    Product["Product :8082"]
    Order["Order :8083"]
    Client --> Gateway
    Gateway -->|/api/auth/**| Auth
    Gateway -->|/api/products/**| Product
    Gateway -->|/api/orders/**| Order
```

Notification **không** public qua Gateway — chỉ Order gọi nội bộ `:8084`.

## Quyết định thiết kế (Phase 1)

### 1. Không Service Discovery

- Gọi HTTP bằng URL trong `application.properties`
- Đủ để học; Phase sau có thể thêm Eureka / K8s

### 2. JWT — pattern B (gần production)

| Chỗ | Việc |
| --- | ---- |
| **Auth** | **Ký / phát** JWT |
| **Gateway** | **Verify** → gắn `X-User-Id`, `X-User-Email`, `X-User-Roles` |
| **Order** | Tin header từ Gateway — **không** jjwt |
| Bảo mật | Gỡ `Authorization` trước khi forward xuống service |

### 3. Database-per-service

| DB | Service | Nội dung |
| -- | ------- | -------- |
| `auth_db` | Auth | users, refresh_tokens |
| `product_db` | Product | products, categories |
| `order_db` | Order | orders (items **embedded**) |
| *(không)* | Notification | Chỉ gửi email |

### 4. Giao tiếp sync (HTTP)

- Order → Product: lấy giá/tên để snapshot  
- Order → Notification: báo gửi email  
- **Lưu ý:** mail lỗi **không** rollback đơn đã lưu (hạn chế Phase 1)

### 5. Product Snapshot

Đơn lưu tên/giá **lúc mua** — không tin `price` từ client; không đọc thẳng `product_db`.

---

## Chi tiết từng service

### API Gateway — cổng `:8080`

- Công nghệ: Spring Cloud Gateway  
- Việc: Route · verify JWT · CORS · Correlation Id  
- Route: `/api/auth/**` · `/api/products/**` · `/api/orders/**`

### Auth Service — `:8081`

- Spring Boot · Security · MongoDB · jjwt  
- DB: `auth_db`  
- API: `POST /api/auth/register|login|logout|refresh`  
- Password: BCrypt

### Product Service — `:8082`

- Spring Boot · MongoDB · `product_db`  
- GET: **public** · POST/PUT/DELETE: **ROLE_ADMIN** (Gateway kiểm)  
- API: `GET/POST /api/products` · `GET/PUT/DELETE /api/products/{id}`

### Order Service — `:8083`

- Spring Boot · Security · MongoDB · `order_db`  
- Tin `X-User-*`; kiểm đơn thuộc user  
- API: `POST/GET /api/orders` · `GET /api/orders/{id}`  
- Client HTTP: `ProductClient` · `NotificationClient`

### Notification Service — `:8084`

- Spring Boot · Spring Mail · **không** Mongo  
- API nội bộ: `POST /internal/notifications/order-success`  
- Lab: `app.mail.enabled=false` → log nội dung thư

---

## Luồng đăng nhập

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant A as Auth Service
    participant DB as auth_db

    C->>G: POST /api/auth/login
    G->>A: Forward
    A->>DB: Tìm user theo email
    DB-->>A: User
    A->>A: BCrypt kiểm password
    A->>A: Ký JWT
    A-->>G: Access + Refresh token
    G-->>C: Authentication response
```

## Luồng tạo đơn (quan trọng nhất)

```mermaid
sequenceDiagram
    participant C as Client
    participant G as API Gateway
    participant O as Order Service
    participant P as Product Service
    participant DB as order_db
    participant N as Notification
    participant SMTP as SMTP

    C->>G: POST /api/orders + JWT
    G->>G: Verify JWT
    G->>O: Forward + X-User-*
    O->>P: GET /api/products/{id}
    P-->>O: name, price, status
    O->>O: Snapshot + tính tổng
    O->>DB: Save Order
    O->>N: POST /internal/notifications/order-success
    N->>SMTP: Gửi mail (hoặc skip)
    N-->>O: 200
    O-->>G: 201 Created
    G-->>C: Order response
```

---

## Công nghệ

| Vai trò | Công nghệ |
| ------- | --------- |
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Maven (mỗi service một `pom.xml`) |
| DB | MongoDB + Spring Data MongoDB |
| Security | Spring Security + JWT (jjwt) |
| Gateway | Spring Cloud Gateway |
| HTTP client | Spring `RestClient` |
| Email | Spring Mail |

## Chạy local

| Service | Port |
| ------- | ---- |
| API Gateway | 8080 |
| Auth | 8081 |
| Product | 8082 |
| Order | 8083 |
| Notification | 8084 |

**Thứ tự start:** MongoDB → Auth → Product → Notification → Order → Gateway  
(`./start-all.sh` / `./stop-all.sh`)

**Mongo URI (như Bài 4):**

```text
mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/{db}?authSource=admin
```

DB: `auth_db` · `product_db` · `order_db`

## Correlation Id

1. Gateway **luôn sinh** `X-Correlation-Id` (không tin header client); trả lại trên response  
2. Mỗi service ghi MDC `cid` → log `[cid=…]`  
3. Order forward header khi gọi Product / Notify  

## Health

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
… 8082 · 8083 · 8084
```

Trước khi debug “đặt hàng lỗi”, kiểm Product / Notify còn `UP`.

## Tài liệu liên quan

- [README](../README.md) — chạy nhanh  
- [api-test.md](./api-test.md) — kịch bản curl  
- Syllabus Bài 2 — lý thuyết + lab  
