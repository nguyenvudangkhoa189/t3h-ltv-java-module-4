# Demo Phụ lục 3 — Spring Boot Redis Cache + MongoDB Product

Project demo cho syllabus [`7_java_m4_phuluc3_Redis_Cache.md`](../syllabus/module-4/7_java_m4_phuluc3_Redis_Cache.md).

**MongoDB** = nguồn sự thật Product · **Redis** = cache đọc nhanh (Docker) — tập trung:

| Phần | Class | Syllabus |
|------|-------|----------|
| Redis Docker + Insight | `docker-compose.yml` | §2 |
| Mongo URI + Product `@Document` | `application.properties` · `Product` · `ProductRepository` | §5 (giống demo-bai4) |
| Dependency + Redis + `@EnableCaching` | `pom.xml` · Application | §3 |
| Hello `@Cacheable` | `HelloCacheService` + `HelloCacheController` | §4 |
| Product cache + evict | `ProductService` + `ProductController` | §5 |
| TTL + JSON serializer | `RedisCacheConfig` | §3.4 · quan sát TTL §6 |
| Unit test (mock repo) | `ProductServiceTest`, `HelloCacheServiceTest` | Bài tập |

## Yêu cầu

- JDK 17+
- **MongoDB** đang chạy — URI trong `application.properties` (cùng kiểu lab [demo-bai4-auth](../demo-bai4-auth))
- **Docker Desktop** cho Redis (+ Redis Insight)
- **Không cần** Gmail

## Cấu hình Mongo

Giống Bài 4 — chỉnh URI theo máy (user/pass/`authSource` nếu có):

```properties
spring.data.mongodb.uri=mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/db_java_t3h_module4_phuluc3?authSource=admin
```

> DB riêng `db_java_t3h_module4_phuluc3` — không đụng DB Bài 4. Lần đầu `DataSeeder` tạo 3 product nếu collection trống.

## Chạy Redis + Redis Insight

```bash
cd demo-phuluc3-redis
docker compose up -d
docker exec -it demo-phuluc3-redis redis-cli PING
# → PONG
```

| Service | URL / port | Việc làm |
|---------|------------|----------|
| **Redis** | `localhost:6379` | App Spring (cache) |
| **Redis Insight** | [http://localhost:5540](http://localhost:5540) | GUI xem key cache |

### Kết nối Redis trong Insight (một lần)

> **Lỗi hay gặp:** Host = `localhost` → fail. Insight trong Docker → Host phải là **`redis`**, Port **`6379`**.

## Chạy app

```bash
cd demo-phuluc3-redis/java-springboot-phuluc3
./mvnw spring-boot:run
```

Thứ tự: **Mongo Up** → **Redis Up** → Spring Boot.

## Swagger UI (khuyến nghị lab)

**http://localhost:8080/swagger-ui/index.html**

| Tag | Việc thử nhanh |
|-----|----------------|
| **Hello Cache** | `GET /api/hello-cache` hai lần cùng `name=Khoa` — lần 2 `tookMs` nhỏ |
| **Products** | `GET /api/products` → copy `_id` → `GET /{id}` hai lần → `PUT` → `GET` lại |

> Product id là **Mongo ObjectId** (string), không còn `1`, `2`, `3` cố định.

## API nhanh

| Method | URL | Ghi chú |
|--------|-----|---------|
| GET | `/api/hello-cache?name=Khoa` | Redis cache `hello` |
| GET | `/api/products` | Mongo + cache key `'all'` |
| GET | `/api/products/{id}` | Mongo + cache key `#id` |
| POST | `/api/products` | Lưu Mongo · `@CacheEvict` |
| PUT | `/api/products/{id}` | Cập nhật Mongo · evict |
| DELETE | `/api/products/{id}` | Xóa Mongo · evict |

```bash
# 1) Hello — lần 1 chậm (~2s), lần 2 nhanh (HIT)
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"

# 2) Lấy danh sách + id Mongo
curl -s http://localhost:8080/api/products | python3 -m json.tool

# 3) Thay ID=... bằng _id thật
ID=...
curl -s http://localhost:8080/api/products/$ID
curl -s http://localhost:8080/api/products/$ID

# 4) Update → evict → GET lại
curl -s -X PUT http://localhost:8080/api/products/$ID \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","price":24990000,"description":"Sau khi update"}'
curl -s http://localhost:8080/api/products/$ID

# 5) Xem key Redis
docker exec -it demo-phuluc3-redis redis-cli KEYS '*'
```

## Chạy test

```bash
./mvnw test
# Không cần Redis/Mongo — Mockito mock ProductRepository
```

## Cấu trúc

```
demo-phuluc3-redis/
├── docker-compose.yml                 ← Redis :6379 + Redis Insight :5540 ★
├── README.md
└── java-springboot-phuluc3/
    └── src/main/java/vn/demo/
        ├── DemoPhuluc3RedisApplication.java   ← @EnableCaching ★
        ├── config/
        │   ├── RedisCacheConfig.java          ← TTL + JSON ★
        │   ├── OpenApiConfig.java             ← Swagger UI
        │   └── DataSeeder.java                ← seed Mongo products
        ├── hello/
        │   ├── HelloCacheService.java         ← §4 @Cacheable ★
        │   └── controller/HelloCacheController.java
        ├── product/
        │   ├── model/Product.java             ← @Document(collection="products") ★
        │   ├── dto/ProductRequest.java
        │   ├── repository/ProductRepository.java  ← MongoRepository ★
        │   ├── service/ProductService.java    ← cache + Mongo ★
        │   └── controller/ProductController.java
        └── exception/...
```

## Map syllabus → code / properties

| Syllabus | Demo |
|----------|------|
| §2 Redis Docker | `docker-compose.yml` · Insight host=`redis` |
| Mongo Product | `spring.data.mongodb.uri` · `@Document` · `MongoRepository` |
| §3 Dependency | `starter-data-redis` + `starter-cache` + `starter-data-mongodb` |
| Swagger UI | `/swagger-ui/index.html` |
| §3 `@EnableCaching` | `DemoPhuluc3RedisApplication` |
| §4 Hello cache | `HelloCacheService.greet` |
| §5 Product | `ProductService` (Mongo + cache/evict) |
| §3.4 TTL + JSON | `RedisCacheConfig` |
| Test | Mockito · `spring.cache.type=none` |

## Lỗi hay gặp

| Triệu chứng | Cách xử lý |
|-------------|------------|
| Không connect Mongo | Sửa URI / bật Mongo; cùng kiểu lab Bài 4 |
| `Unable to connect to Redis` | `docker compose up -d` |
| Insight không nối Redis | Host = **`redis`**, không `localhost` |
| Sau PUT vẫn giá cũ | Quên evict — Service đã gắn `@CacheEvict` |
| Muốn seed lại Product | Drop DB `db_java_t3h_module4_phuluc3` hoặc xóa collection `products` |
