# Demo Phụ lục 3 — Spring Boot Redis Cache (Docker)

Project demo cho syllabus [`7_java_m4_phuluc3_Redis_Cache.md`](../syllabus/module-4/7_java_m4_phuluc3_Redis_Cache.md).

App **gọn**, **in-memory Product** (không Mongo/JPA) + **Redis thật qua Docker** — tập trung:

| Phần | Class | Syllabus |
|------|-------|----------|
| Redis Docker | `docker-compose.yml` | §2 |
| Dependency + Redis properties | `pom.xml` + `application.properties` | §3 |
| `@EnableCaching` | `DemoPhuluc3RedisApplication` | §3 |
| Hello `@Cacheable` | `HelloCacheService` + `HelloCacheController` | §4 |
| Product cache + evict | `ProductService` + `ProductController` | §5 |
| TTL + JSON serializer | `RedisCacheConfig` | §3.4 · quan sát TTL §6 |
| Unit test (không Redis) | `ProductServiceTest`, `HelloCacheServiceTest` | Bài tập |

## Yêu cầu

- JDK 17+
- **Docker Desktop** (hoặc Docker Engine) đang chạy
- **Không cần** MongoDB / Gmail

## Chạy Redis (bắt buộc trước app)

```bash
cd demo-phuluc3-redis
docker compose up -d
docker exec -it demo-phuluc3-redis redis-cli PING
# → PONG
```

## Chạy app

```bash
cd demo-phuluc3-redis/java-springboot-phuluc3
./mvnw spring-boot:run
```

## API nhanh

| Method | URL | Cache |
|--------|-----|-------|
| GET | `/api/hello-cache?name=Khoa` | `@Cacheable` cache `hello` |
| GET | `/api/products` | `@Cacheable` key `'all'` |
| GET | `/api/products/{id}` | `@Cacheable` key `#id` |
| POST | `/api/products` | `@CacheEvict` all |
| PUT | `/api/products/{id}` | `@CacheEvict` all |
| DELETE | `/api/products/{id}` | `@CacheEvict` all |

```bash
# 1) Hello — lần 1 chậm (~2s), lần 2 nhanh (HIT)
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"

# 2) Product GET — lần 1 MISS (log Repository), lần 2 HIT
curl -s http://localhost:8080/api/products/1
curl -s http://localhost:8080/api/products/1

# 3) Update → evict → GET lại phải MISS + giá mới
curl -s -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","price":24990000,"description":"Sau khi update"}'
curl -s http://localhost:8080/api/products/1

# 4) Xem key Redis (tùy prefix / serializer)
docker exec -it demo-phuluc3-redis redis-cli KEYS '*'
```

Quan sát:

1. Console: `[HelloCache] MISS` / `[ProductService] MISS` chỉ khi chưa có cache (hoặc vừa evict / hết TTL)
2. `tookMs` của hello lần 2 nhỏ hơn rõ so với lần 1

## Chạy test

```bash
./mvnw test
# Không cần Redis — Mockito / gọi thẳng Service (cache AOP không chạy trong unit test)
```

## Cấu trúc

```
demo-phuluc3-redis/
├── docker-compose.yml                 ← Redis :6379 ★
├── README.md
└── java-springboot-phuluc3/
    └── src/main/java/vn/demo/
        ├── DemoPhuluc3RedisApplication.java   ← @EnableCaching ★
        ├── config/
        │   ├── RedisCacheConfig.java          ← §6 TTL + JSON ★
        │   └── DataSeeder.java                ← seed 3 products
        ├── hello/
        │   ├── HelloCacheService.java         ← §4 @Cacheable ★
        │   └── controller/HelloCacheController.java
        ├── product/
        │   ├── model/Product.java
        │   ├── dto/ProductRequest.java
        │   ├── repository/...
        │   ├── service/ProductService.java    ← §5 cache + evict ★
        │   └── controller/ProductController.java
        └── exception/
            ├── ResourceNotFoundException.java
            └── RestExceptionHandler.java
```

## Map syllabus → code / properties

| Syllabus | Demo |
|----------|------|
| §2 Redis Docker | `docker-compose.yml` · `redis-cli PING` |
| §3 Dependency | `pom.xml` → `starter-data-redis` + `starter-cache` |
| §3 Properties | `application.properties` (`spring.data.redis.*`, `spring.cache.type=redis`) |
| §3 `@EnableCaching` | `DemoPhuluc3RedisApplication` |
| §4 Hello cache | `HelloCacheService.greet` + `GET /api/hello-cache` |
| §5 Product | `ProductService` + `ProductController` |
| §3.4 TTL + JSON | `RedisCacheConfig` |
| §6 Quan sát TTL | Đổi `app.cache.*-ttl-seconds` · restart · GET lại |
| §7 Lỗi thường gặp | README + syllabus bảng lỗi |
| Test tắt Redis | `application-test.properties` (`spring.cache.type=none`) + Mockito |

## Lỗi hay gặp khi chạy lab

| Triệu chứng | Cách xử lý |
|-------------|------------|
| `Unable to connect to Redis` | `docker compose up -d` trước; đúng port `6379` |
| `@Cacheable` không ăn | Kiểm tra `@EnableCaching`; gọi qua bean (không `this.`) |
| Sau PUT vẫn giá cũ | Quên evict — lab đã gắn `@CacheEvict` trên create/update/delete |
| `SerializationException` / lỗi Instant | Dùng `RedisCacheConfig` (JSON + `JavaTimeModule`) |
| Muốn xoá sạch cache lab | `docker exec -it demo-phuluc3-redis redis-cli FLUSHDB` |
