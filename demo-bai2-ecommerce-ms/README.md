# Demo Bài 2 — Ecommerce Microservices (Phase 1)

5 Spring Boot độc lập + MongoDB (Docker map `:27017`, 3 DB) + Gateway JWT biên.  
5 service **không** chạy trong Docker · **không** Eureka / Kafka.

Syllabus: [`5_java_m4_bai2_Microservices.md`](../syllabus/module-4/5_java_m4_bai2_Microservices.md)

| Service | Port | DB | Vai trò |
|---------|------|-----|---------|
| api-gateway | 8080 | — | Route · JWT verify · Correlation Id |
| auth-service | 8081 | `auth_db` | Register/Login/Refresh — **ký JWT** |
| product-service | 8082 | `product_db` | Catalog CRUD |
| order-service | 8083 | `order_db` | Đơn + snapshot · tin `X-User-*` |
| notification-service | 8084 | — | Email internal · **không** qua Gateway |

## Yêu cầu

- JDK 17+, Maven
- MongoDB trên **Docker**, map `localhost:27017`  
  User: `root` · Password: `DBVWiYdDoMnfWmK` · `authSource=admin`

```bash
docker start mongo-t3h 2>/dev/null || docker run -d --name mongo-t3h -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=DBVWiYdDoMnfWmK \
  mongo:7
```

## Chạy

```bash
./start-all.sh    # kiem tra :27017 (Docker) roi Auth → Product → Notify → Order → Gateway
./stop-all.sh     # chi tat Spring Boot, khong stop Mongo Docker
```

Health: `curl -s localhost:8080/actuator/health` (lặp 8081–8084).

Log (`./start-all.sh`): `logs/ms-auth.log`, `logs/ms-product.log`, `logs/ms-notify.log`, `logs/ms-order.log`, `logs/ms-gateway.log`.

## Seed

| Email | Password | Role |
|-------|----------|------|
| user@example.com | user123 | ROLE_USER |
| admin@example.com | admin123 | ROLE_ADMIN |

## Thử nhanh

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"user123"}' | jq -r .accessToken)

PID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')

curl -s -i -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"items\":[{\"productId\":\"$PID\",\"quantity\":1}]}"
# X-Correlation-Id do Gateway sinh — xem header response, rồi tìm [cid=…] trên logs/
```

Tài liệu: [docs/architecture-overview.md](docs/architecture-overview.md) · [docs/api-test.md](docs/api-test.md)
