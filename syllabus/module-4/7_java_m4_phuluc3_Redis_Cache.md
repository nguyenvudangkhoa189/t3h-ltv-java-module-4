# Phụ lục 3: Spring Boot Redis Cache — Cache dữ liệu với Redis (Docker)

## Mục tiêu bài học

Sau phụ lục này, học viên có thể:

- Giải thích **cache** khác gì đọc thẳng DB — khi nào dùng, rủi ro dữ liệu cũ (stale)
- Chạy **Redis bằng Docker** (`docker run` / `docker compose`) và kiểm tra bằng `redis-cli`
- Thêm dependency `spring-boot-starter-data-redis` + `spring-boot-starter-cache` và cấu hình `spring.data.redis.*`
- Bật cache bằng `@EnableCaching` và dùng `@Cacheable` / `@CacheEvict` / `@CachePut`
- Cấu hình **TTL** + **JSON serializer** (`RedisCacheConfig`) ngay khi nối Redis — rồi quan sát hết hạn bằng cách đổi `app.cache.*-ttl-seconds`
- Áp dụng tình huống thực tế: **cache chi tiết sản phẩm** (đọc nhiều, ghi ít) + **xoá cache khi cập nhật**
- Nhận biết lỗi thường gặp (Redis chưa chạy, sai host/port, key không khớp, stale data) và cách xử lý mức lab

> **Không nằm trong phạm vi phụ lục này:** Redis Cluster / Sentinel; Redis Streams; Pub/Sub đầy đủ; cache multi-level (Caffeine + Redis); distributed lock (Redisson) chi tiết; Spring Session Redis.

## Điều kiện tiên quyết

- **Module 2 — Bài 4**: Spring Boot project, `@Service`, `application.properties`
- **Module 2 — Bài 5+**: Controller → Service → Repository
- *(Khuyến khích)* **[Module 4 — Bài 7 Docker](./6_java_m4_bai7_Docker.md)**: đã chạy container, biết `docker run` / `docker compose`
- *(Khuyến khích)* **Phụ lục 1 — Scheduled**: quen tách Job/Service; có thể kết hợp job làm mới cache
- *(Khuyến khích)* **[Module 4 — Bài 4 Auth](./4_java_m4_bai4_Authentication_Authorization.md)** / [demo-bai4-auth](../../demo-bai4-auth): đã quen `spring.data.mongodb.uri`, `@Document`, `MongoRepository`
- JDK 17+, Spring Boot 3.x
- **MongoDB** đang chạy (URI trong `application.properties`)
- **Docker Desktop** (hoặc Docker Engine) đang chạy — cho Redis

> **Demo chuẩn:** [`demo-phuluc3-redis`](../../demo-phuluc3-redis) — **MongoDB** (Product nguồn sự thật) + **Redis cache** qua Docker, đủ Hello cache + Product CRUD + TTL + Swagger UI + unit test Service (mock repo).  
> Chi tiết chạy app / API: [README](../../demo-phuluc3-redis/README.md).  
> **Swagger UI:** sau khi chạy app → [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  
> Mongo URI cùng kiểu [demo-bai4-auth](../../demo-bai4-auth) — DB riêng `db_java_t3h_module4_phuluc3`.

> **Vai trò phụ lục:** Kỹ năng **bổ trợ backend** (tăng tốc đọc dữ liệu nóng), dùng được trong Final Project / microservice nhỏ. Không thay các bài chính Module 4 (REST, Auth, Docker…).

### Thời lượng gợi ý


| Phần                                      | Thời gian |
| ----------------------------------------- | --------- |
| Khái niệm Cache vs DB §1                  | ~15 phút  |
| Redis Docker + kiểm tra `redis-cli` §2    | ~20 phút  |
| Dependency + `@EnableCaching` + `RedisCacheConfig` §3 | ~25 phút |
| Hello `@Cacheable` §4                     | ~20 phút  |
| Ví dụ Product — cache + evict §5          | ~30 phút  |
| Quan sát TTL + lỗi thường gặp §6–7        | ~15 phút  |
| Nâng cao đọc hiểu + bài tập               | ~15 phút  |


## Nội dung (làm theo thứ tự)


| #       | Chủ đề                              | Việc HV làm (tóm tắt)                                                    | Kết quả kiểm tra                         |
| ------- | ----------------------------------- | ------------------------------------------------------------------------ | ---------------------------------------- |
| 1       | Cache vs DB + khi nào dùng          | Đọc / thảo luận                                                          | Nói được hit / miss / stale              |
| 2       | Redis Docker + Redis Insight        | **Tạo** `docker-compose.yml` · **Chạy** · `PING` · mở Insight            | `PONG`; UI :5540 thấy Redis              |
| 3       | Dependency + EnableCaching + TTL/JSON config | **Thêm** starter · properties · `@EnableCaching` · `RedisCacheConfig` | App start; kết nối Redis OK              |
| 4       | Hello `@Cacheable`                  | **Thêm** `HelloCacheService` · **Thêm** `HelloCacheController`           | Lần 2 nhanh hơn lần 1                    |
| 5       | Product Mongo + cache + `@CacheEvict` | **Cấu hình** URI · `@Document` · `MongoRepository` · seed · GET/PUT | Update → GET lấy data mới từ Mongo      |
| 6       | Quan sát / chỉnh TTL                | **Đổi** `app.cache.*-ttl-seconds` · đợi hết hạn · GET lại                | Key tự hết hạn sau N giây                |
| 7       | Lỗi thường gặp                      | Đọc bảng                                                                 | Tự sửa khi app không kết nối Redis       |
| 8       | Nâng cao (đọc hiểu)                 | Optional                                                                 | Biết `@CachePut`, key SpEL, multi-instance |
| Phụ lục | Bài tập · Checklist · Liên kết      | —                                                                        | —                                        |


---

## Kiến trúc lab (demo chuẩn)

```
demo-phuluc3-redis/
├── docker-compose.yml                 ← §2 Redis :6379 + Redis Insight :5540 ★
├── README.md
└── java-springboot-phuluc3/
    └── src/main/java/vn/demo/
        ├── DemoPhuluc3RedisApplication.java   ← @EnableCaching ★ (§3)
        ├── config/
        │   ├── RedisCacheConfig.java          ← §3 TTL + JSON serializer ★
        │   ├── OpenApiConfig.java             ← Swagger UI (lab thử API)
        │   └── DataSeeder.java                ← §5 seed Product
        ├── hello/
        │   ├── HelloCacheService.java         ← §4 @Cacheable giả lập chậm ★
        │   └── controller/HelloCacheController.java
        ├── product/                           ← §5 ví dụ thực tế (Mongo)
        │   ├── model/Product.java             ← @Document(collection="products")
        │   ├── dto/ProductRequest.java        ← class + validation (không dùng record)
        │   ├── repository/ProductRepository.java  ← MongoRepository<Product, String>
        │   ├── service/ProductService.java      ★ @Cacheable / @CacheEvict
        │   └── controller/ProductController.java
        └── exception/
            ├── ResourceNotFoundException.java
            └── RestExceptionHandler.java
```

> Demo dùng **MongoDB** làm nguồn sự thật Product (convention [demo-bai4](../../demo-bai4-auth)). Redis chỉ **cache** bản đọc. Hello cache vẫn giả lập chậm in-process.

```mermaid
flowchart LR
    Client[Client] -->|GET /api/products/1| Ctrl[ProductController]
    Ctrl --> Svc[ProductService]
    Svc -->|1. hỏi cache| Redis[(Redis)]
    Redis -->|HIT| Svc
    Redis -->|MISS| Svc
    Svc -->|MISS: đọc DB giả| Repo[ProductRepository]
    Svc -->|ghi vào cache| Redis
    Client2[Client] -->|PUT /api/products/1| Ctrl2[ProductController]
    Ctrl2 --> Svc2[ProductService]
    Svc2 -->|@CacheEvict| Redis
    Svc2 --> Repo
```



---

## 1. Cache là gì? Vì sao cần Redis?

### 1.1. So với đọc thẳng DB / Repository


|                   | Đọc DB / Repository mỗi lần              | Đọc qua Cache (Redis)                              |
| ----------------- | ---------------------------------------- | -------------------------------------------------- |
| **Ai kích hoạt?** | Client gọi API                           | Client gọi API — **nhưng** Service hỏi cache trước |
| **Đích đến?**     | Luôn vào “DB” (chậm hơn)                 | **Hit** → Redis (nhanh); **Miss** → DB rồi ghi cache |
| **Ví dụ**         | `GET /api/products/1` luôn quét map/DB   | Lần 1 miss; lần 2–N hit cho tới khi evict / hết TTL |


> **Ẩn dụ:** DB = kho hàng ở xa. Cache = **kệ gần quầy** — món bán chạy để sẵn; hết hạn hoặc có hàng mới thì phải dọn / lấy lại từ kho.

```mermaid
flowchart TB
    subgraph miss [Cache MISS]
        A1[GET product/1] --> R1{Redis có key?}
        R1 -->|Không| DB1[Đọc Repository]
        DB1 --> W1[Ghi Redis]
        W1 --> Resp1[Trả JSON]
    end
    subgraph hit [Cache HIT]
        A2[GET product/1] --> R2{Redis có key?}
        R2 -->|Có| Resp2[Trả JSON từ Redis]
    end
```



### 1.2. Thuật ngữ mức lab cần nhớ


| Thuật ngữ        | Ý nghĩa ngắn                                                              |
| ---------------- | ------------------------------------------------------------------------- |
| **Cache HIT**    | Tìm thấy dữ liệu trong Redis → **không** gọi Repository                   |
| **Cache MISS**   | Không có trong Redis → gọi Repository → (thường) **ghi** vào Redis        |
| **Evict**        | **Xoá** entry cache (sau khi update/delete)                               |
| **TTL**          | Time To Live — hết hạn thì tự mất, lần đọc sau sẽ miss lại                |
| **Stale data**   | Cache còn giữ bản cũ trong khi DB đã đổi (quên evict / TTL quá dài)       |


### 1.3. Khi nào dùng cache? (2 ví dụ phụ lục)


| #     | Tình huống                                      | Vì sao dùng Redis Cache?                                      |
| ----- | ----------------------------------------------- | ------------------------------------------------------------- |
| **1** | API “Hello chậm” giả lập I/O nặng               | Thấy rõ lần 2 nhanh hơn — cảm nhận HIT/MISS                   |
| **2** | **Chi tiết sản phẩm** đọc nhiều, cập nhật ít    | Giảm tải DB; sau PUT/DELETE phải **evict** để tránh stale     |


> Cache **không** thay DB. Cache = bản sao tạm để đọc nhanh. Nguồn sự thật vẫn là Repository/DB.

**Việc HV làm ở §1:** Không code — nắm HIT / MISS / Evict / TTL / stale trước khi bật Docker.

---

## 2. Chạy Redis bằng Docker

### Việc học viên làm


| Bước | Hành động                                              | File / ghi chú                          |
| ---- | ------------------------------------------------------ | --------------------------------------- |
| 2.1  | Kiểm tra Docker đang chạy                              | `docker version`                        |
| 2.2  | **Tạo** `docker-compose.yml` (Redis + Redis Insight)   | [`docker-compose.yml`](../../demo-phuluc3-redis/docker-compose.yml) |
| 2.3  | `docker compose up -d`                                 | Redis `:6379` · Insight `:5540`         |
| 2.4  | Kiểm tra `PING` → `PONG` (+ thử `SET`/`GET`)           | `docker exec` + `redis-cli`             |
| 2.5  | Mở Redis Insight · **Add database** host=`redis`       | [http://localhost:5540](http://localhost:5540) |


### Bước 2.1 — Docker sẵn sàng

```bash
docker version
docker compose version
```

> Nếu lệnh lỗi: mở **Docker Desktop**, đợi engine Running, rồi thử lại. Chi tiết Docker cơ bản: [Bài 7](./6_java_m4_bai7_Docker.md).

### Bước 2.2 — `docker-compose.yml` (lab)

Đặt cạnh project demo (hoặc trong `demo-phuluc3-redis/`):

```yaml
services:
  redis:
    image: redis:7-alpine
    container_name: demo-phuluc3-redis
    ports:
      - "6379:6379"
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "PING"]
      interval: 5s
      timeout: 3s
      retries: 5

  redis-insight:
    image: redis/redisinsight:latest
    container_name: demo-phuluc3-redis-insight
    ports:
      - "5540:5540"
    depends_on:
      redis:
        condition: service_healthy
    environment:
      RI_REDIS_HOST: redis
      RI_REDIS_PORT: "6379"
      RI_REDIS_ALIAS: phuluc3-redis
    volumes:
      - redis-insight-data:/data

volumes:
  redis-data:
  redis-insight-data:
```

**Kiến thức mới:**

- Image `redis:7-alpine` — nhẹ, đủ cho lab
- Map `6379:6379` — app Spring trên máy host kết nối `localhost:6379`
- `appendonly yes` — Redis ghi AOF (lab thấy data còn sau restart container; không bắt buộc hiểu sâu)
- **Redis Insight** (`:5540`) — GUI xem key/TTL/value
- **Host trong Insight = `redis`** (tên service), **không** dùng `localhost` — vì Insight cũng là container; `localhost` trỏ về chính nó

### Bước 2.3 — Chạy Redis + Insight

```bash
cd demo-phuluc3-redis
docker compose up -d
docker compose ps
```

→ cả `demo-phuluc3-redis` và `demo-phuluc3-redis-insight` trạng thái `Up`; port `6379` và `5540`.

**Một lệnh thay thế chỉ Redis (không Insight):**

```bash
docker run -d --name demo-phuluc3-redis -p 6379:6379 redis:7-alpine
```

### Bước 2.4 — Kiểm tra bằng `redis-cli`

```bash
docker exec -it demo-phuluc3-redis redis-cli PING
```

→ `PONG`.

Thử ghi / đọc thủ công:

```bash
docker exec -it demo-phuluc3-redis redis-cli SET lab:hello "xin-chao"
docker exec -it demo-phuluc3-redis redis-cli GET lab:hello
```

→ `"xin-chao"`.

### Bước 2.5 — Redis Insight (xem key cho tiện)

1. Mở [http://localhost:5540](http://localhost:5540)
2. **Add Redis database**
3. Host: **`redis`** · Port: **`6379`** · không password  
   - Sai: `localhost` / `127.0.0.1` → Insight không tới được Redis  
   - Đúng: `redis` (DNS nội bộ Docker compose)
4. Test Connection → Add
5. Sau khi gọi API cache (Swagger), refresh Browser trên Insight → thấy key thuộc cache `hello` / `products`

> **Câu chốt:** Spring trên máy host → `localhost:6379`. Insight trong Docker → Host form = **`redis`**.---

## 3. Dependency + cấu hình Redis + `@EnableCaching` + `RedisCacheConfig`

### Việc học viên làm


| Bước | Hành động                                                              | File / ghi chú                |
| ---- | ---------------------------------------------------------------------- | ----------------------------- |
| 3.1  | **Thêm** `starter-data-redis` + `starter-cache` + `starter-data-mongodb` (+ web, validation, lombok, springdoc) | [`pom.xml`](../../demo-phuluc3-redis/java-springboot-phuluc3/pom.xml) |
| 3.2  | **Cập nhật** `spring.data.redis.*` + `spring.cache.type` + TTL properties | [`application.properties`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/resources/application.properties) |
| 3.3  | **Thêm** `@EnableCaching` trên Application                             | [`DemoPhuluc3RedisApplication.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/DemoPhuluc3RedisApplication.java) |
| 3.4  | **Thêm** `RedisCacheConfig` — JSON serializer + TTL theo cache name    | [`RedisCacheConfig.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/config/RedisCacheConfig.java) |
| 3.5  | Chạy app (Redis đã Up)                                                 | `./mvnw spring-boot:run`      |


### Bước 3.1 — Dependency

Trong [`pom.xml`](../../demo-phuluc3-redis/java-springboot-phuluc3/pom.xml):

```xml
<!-- ★ Phụ lục 3: Redis client (Lettuce) + auto-config RedisConnectionFactory -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- ★ Abstraction @Cacheable / @CacheEvict / CacheManager -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

> Boot 3.x mặc định dùng **Lettuce** (không cần thêm Jedis cho lab).  
> Có `starter-cache` + Redis trên classpath → Spring Boot auto-config `RedisCacheManager`. Lab **tinh chỉnh TTL + JSON** ngay ở bước 3.4 (trước khi cache `Product` có `Instant`).

### Bước 3.2 — Properties

Demo chuẩn ([`application.properties`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/resources/application.properties)):

```properties
server.port=8080

# --- Redis (Docker map 6379) ---
spring.data.redis.host=localhost
spring.data.redis.port=6379
# spring.data.redis.password=   # lab không dùng

# --- Cache app ---
spring.cache.type=redis
app.cache.product-ttl-seconds=60
app.cache.hello-ttl-seconds=30
app.product.seed-on-startup=true
```


| Thuộc tính                 | Giá trị lab                                      |
| -------------------------- | ------------------------------------------------ |
| `spring.data.redis.host`   | `localhost` (Redis container publish ra host)    |
| `spring.data.redis.port`   | `6379`                                           |
| `spring.cache.type`        | `redis` — dùng Redis làm CacheManager            |
| `app.cache.*-ttl-seconds`  | TTL từng cache — đọc bởi `RedisCacheConfig`      |


### Bước 3.3 — `@EnableCaching`

```java
@SpringBootApplication
@EnableCaching   // ← bắt buộc; thiếu thì @Cacheable không có hiệu lực
public class DemoPhuluc3RedisApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoPhuluc3RedisApplication.class, args);
    }
}
```

### Bước 3.4 — `RedisCacheConfig` (TTL + JSON) — làm **trước** §5 Product

> **Vì sao sớm?** `Product` có field `Instant`. Serializer mặc định (JDK) dễ lỗi / khó đọc bằng `redis-cli`. Lab dùng JSON + `JavaTimeModule` ngay từ đầu. §6 chỉ **đổi số TTL** để quan sát hết hạn.

```java
@Configuration
public class RedisCacheConfig {  // KHÔNG gắn @EnableCaching ở đây — đã có trên Application

    @Value("${app.cache.product-ttl-seconds:60}")
    private long productTtlSeconds;

    @Value("${app.cache.hello-ttl-seconds:30}")
    private long helloTtlSeconds;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration("hello",
                        cacheConfiguration(Duration.ofSeconds(helloTtlSeconds)))
                .withCacheConfiguration("products",
                        cacheConfiguration(Duration.ofSeconds(productTtlSeconds)));
    }

    private RedisCacheConfiguration cacheConfiguration(Duration ttl) {
        // 1) ObjectMapper hỗ trợ Instant (JavaTime)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2) Serializer JSON — dễ xem bằng redis-cli hơn JDK serialization
        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        // 3) Gắn TTL + serializer; không cache null
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();
    }
}
```

**Kiến thức mới — TTL + JSON serializer:**

- **TTL**: hết hạn → lần đọc sau là MISS (lưới an toàn khi quên evict)
- **`GenericJackson2JsonRedisSerializer`**: value dạng JSON; cần `JavaTimeModule` cho `Instant`
- **`RedisCacheManagerBuilderCustomizer`**: gắn TTL **riêng** cho cache `hello` và `products`

### Bước 3.5 — Chạy app

```bash
# Terminal 1 — Redis
cd demo-phuluc3-redis
docker compose up -d

# Terminal 2 — Spring Boot
cd demo-phuluc3-redis/java-springboot-phuluc3
./mvnw spring-boot:run
```

> Nếu Redis chưa chạy → lỗi kết nối (`Unable to connect to Redis`). Luôn `docker compose up -d` trước.

---

## 4. Hello Cache — `@Cacheable` và cảm nhận HIT/MISS

### Việc học viên làm


| Bước | Hành động                                           | File / ghi chú        |
| ---- | --------------------------------------------------- | --------------------- |
| 4.1  | **Thêm** `HelloCacheService` giả lập chậm 2 giây    | [`HelloCacheService.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/hello/HelloCacheService.java) |
| 4.2  | **Thêm** API `GET /api/hello-cache`                 | [`HelloCacheController.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/hello/controller/HelloCacheController.java) |
| 4.3  | Gọi 2 lần — so sánh `tookMs` + log MISS             | curl / Postman        |


### Bước 4.1 — HelloCacheService

```java
@Slf4j
@Service
public class HelloCacheService {

    /** Lần đầu MISS (chậm); lần sau HIT (nhanh) nếu cùng name. */
    @Cacheable(cacheNames = "hello", key = "#name")
    public String greet(String name) {
        // 1) Log MISS — thấy dòng này = thân method đang chạy (chưa có trong Redis)
        log.info("[HelloCache] MISS — đang giả lập xử lý chậm cho name={}", name);

        // 2) Giả lập I/O / DB chậm — HIT sẽ bỏ qua đoạn này
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3) Kết quả được Spring Cache ghi vào Redis sau khi method return
        return "Xin chào, " + name + "!";
    }
}
```

**Kiến thức mới — `@Cacheable`:**

- `cacheNames = "hello"` — tên “ngăn cache” (Redis sẽ có key thuộc cache này)
- `key = "#name"` — SpEL: tham số `name` thành một phần key
- Method **chỉ chạy** khi MISS; HIT → trả giá trị đã cache, **không** vào thân method (không sleep)

### Bước 4.2 — Controller

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HelloCacheController {

    private final HelloCacheService helloCacheService;

    @GetMapping("/hello-cache")
    public Map<String, Object> hello(@RequestParam(defaultValue = "HV") String name) {
        // 1) Đo thời gian quanh lời gọi Service (có cache proxy)
        long start = System.currentTimeMillis();
        String message = helloCacheService.greet(name);
        long tookMs = System.currentTimeMillis() - start;
        // 2) Trả message + thời gian — so sánh 2 lần gọi
        return Map.of("message", message, "tookMs", tookMs);
    }
}
```

### Bước 4.3 — Kiểm tra

```bash
# Lần 1 — MISS (~2000ms+), log có "MISS"
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"

# Lần 2 — HIT (vài ms), KHÔNG có log MISS
curl -s "http://localhost:8080/api/hello-cache?name=Khoa"

# Tên khác — MISS lại
curl -s "http://localhost:8080/api/hello-cache?name=Lan"
```

Xem key trong Redis (tên key phụ thuộc serializer / prefix — lab đã cấu hình JSON ở §3.4):

```bash
docker exec -it demo-phuluc3-redis redis-cli KEYS '*'
```

### Quy tắc vàng (mức cơ bản)


| Quy tắc                                   | Chi tiết                                              |
| ----------------------------------------- | ----------------------------------------------------- |
| Có `@EnableCaching`                       | Thiếu → annotation cache **không** chạy               |
| Gọi qua **Spring bean**                   | `this.greet(...)` cùng class → **không** qua proxy    |
| Redis phải Up                             | Không kết nối được → lỗi khi HIT/MISS                 |
| Method cache được nên **không** side-effect nặng | Đừng `@Cacheable` lên method vừa ghi DB vừa gửi mail |


---

## 5. Ví dụ thực tế — Cache chi tiết sản phẩm

### Việc học viên làm (thứ tự tạo file)


| Bước | Hành động                                                 | File / ghi chú                    |
| ---- | --------------------------------------------------------- | --------------------------------- |
| 5.0  | **Cấu hình** `spring.data.mongodb.uri` (+ tắt Redis repos) | `application.properties` (như Bài 4) |
| 5.1  | Hiểu luồng GET cache / PUT evict (Mongo = nguồn sự thật)  | Không code                        |
| 5.2  | **Thêm** `Product` `@Document` + `ProductRequest` + `MongoRepository` | `product/model/`* · `dto/`* · `repository/`* |
| 5.3  | **Thêm** `DataSeeder` (3 sản phẩm khi collection trống)   | [`DataSeeder.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/config/DataSeeder.java) |
| 5.4  | **Thêm** `ProductService` với `@Cacheable` / `@CacheEvict`| [`ProductService.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/product/service/ProductService.java) |
| 5.5  | **Thêm** `ProductController` + exception handler          | `ProductController` · `exception/`* |
| 5.6  | `GET /api/products` lấy `_id` → GET by id 2 lần → PUT → GET | Chứng minh cache + evict          |


### 5.1. Nghiệp vụ

1. `GET /api/products/{id}` — đọc nhiều → **cache** kết quả
2. `PUT /api/products/{id}` / `DELETE` — đổi nguồn sự thật → **evict** cache id đó (và/hoặc cả list)
3. `POST` tạo mới — evict cache danh sách (nếu có cache `products:all`)

```mermaid
sequenceDiagram
    participant C as Client
    participant Ctrl as ProductController
    participant Svc as ProductService
    participant Redis as Redis
    participant Repo as ProductRepository

    C->>Ctrl: GET /api/products/1
    Ctrl->>Svc: getById(1)
    Svc->>Redis: tìm key products::1
    alt HIT
        Redis-->>Svc: Product
    else MISS
        Svc->>Repo: findById(1)
        Repo-->>Svc: Product
        Svc->>Redis: lưu Product
    end
    Svc-->>C: 200 Product

    C->>Ctrl: PUT /api/products/1
    Ctrl->>Svc: update(...)
    Svc->>Repo: save
    Svc->>Redis: EVICT products::1
    Svc-->>C: 200 Product mới
```



### 5.2. Model / DTO / Repository (Mongo — giống demo-bai4)

```properties
# application.properties — chỉnh URI theo máy (authSource=admin nếu có user)
spring.data.mongodb.uri=mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/db_java_t3h_module4_phuluc3?authSource=admin
# Có Mongo + Redis starter → tránh quét nhầm repository
spring.data.redis.repositories.enabled=false
```

- `Product`: `@Document(collection = "products")`, `@Id String id`, `name`, `price`, `description`, `updatedAt`
- `ProductRequest`: **class** + Lombok + validation (convention module-3/phuluc)
- `ProductRepository extends MongoRepository<Product, String>` — **không** còn in-memory
- `DataSeeder`: seed 3 product khi `count() == 0` và `app.product.seed-on-startup=true`
- Không tìm thấy → `ResourceNotFoundException("Product", id)` → HTTP **404**
- **Id API** = Mongo ObjectId (string) — lấy từ `GET /api/products`, không còn `1/2/3` cố định

### 5.3. ProductService — cache đọc, evict khi ghi (Mongo)

```java
@Cacheable(cacheNames = "products", key = "#id")
public Product getById(String id) {
    // 1) Log MISS — đang đọc Mongo
    log.info("[ProductService] MISS — đọc Mongo id={}", id);
    return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
}

@CacheEvict(cacheNames = "products", allEntries = true)
public Product update(String id, ProductRequest request) {
    // 1) Đọc thẳng Mongo (không qua getById)
    Product existing = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    // 2) Gán field · 3) save Mongo · @CacheEvict xoá cache
    existing.setName(request.getName());
    existing.setPrice(request.getPrice());
    existing.setDescription(request.getDescription());
    existing.setUpdatedAt(Instant.now());
    return productRepository.save(existing);
}
```

> Full `create` / `findAll` / `delete` xem demo [`ProductService.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/product/service/ProductService.java).

**Kiến thức mới — `@CacheEvict`:**

- `allEntries = true` — xoá **toàn bộ** cache `products` (lab đơn giản, tránh sót key `'all'` vs `#id`)
- Production tinh hơn: `@Caching` kết hợp evict `key = "#id"` **và** `key = "'all'"` — xem §8

> **Câu chốt:** Đọc = `@Cacheable`. Ghi/xoá = **nhớ evict**. Quên evict = user thấy giá cũ (stale).

### 5.4. API + Controller

| Method   | URL                     | Cache                         |
| -------- | ----------------------- | ----------------------------- |
| `GET`    | `/api/products`         | `@Cacheable` key `'all'`      |
| `GET`    | `/api/products/{id}`    | `@Cacheable` key `#id`        |
| `POST`   | `/api/products`         | `@CacheEvict` all → `201`     |
| `PUT`    | `/api/products/{id}`    | `@CacheEvict` all             |
| `DELETE` | `/api/products/{id}`    | `@CacheEvict` all → `204`     |


Controller mỏng — chỉ gọi Service + `@Valid` trên `ProductRequest` (xem demo [`ProductController.java`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/product/controller/ProductController.java)).

**Kiểm tra:**

```bash
# 1) Lấy danh sách + Mongo _id
curl -s http://localhost:8080/api/products

# 2) Thay ID bằng _id thật — lần 1 MISS (log Mongo), lần 2 HIT
curl -s http://localhost:8080/api/products/ID
curl -s http://localhost:8080/api/products/ID

# 3) Cập nhật giá → evict
curl -s -X PUT http://localhost:8080/api/products/ID \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","price":24990000,"description":"Sau khi update"}'

# 4) GET lại — phải MISS rồi thấy price mới (đồng thời document Mongo đã đổi)
curl -s http://localhost:8080/api/products/ID
```

---

## 6. Quan sát / chỉnh TTL (config đã có ở §3.4)

### Việc học viên làm


| Bước | Hành động                                                    | File / ghi chú           |
| ---- | ------------------------------------------------------------ | ------------------------ |
| 6.1  | Ôn lại vì sao cần TTL (lưới an toàn khi quên evict)          | Đọc                      |
| 6.2  | **Đổi** `app.cache.product-ttl-seconds` (vd. `15`) · restart | `application.properties` |
| 6.3  | GET → HIT → đợi hết TTL → GET lại thấy MISS                  | curl + log               |


### 6.1. Vì sao cần TTL?

- Evict khi ghi **đúng** nhưng không phải lúc nào cũng bắt được mọi đường ghi (admin tool, script, service khác)
- TTL = **lưới an toàn**: dữ liệu tối đa cũ N giây rồi tự miss lại
- Lab: TTL ngắn (15–60s) để quan sát dễ
- Code cấu hình: đã làm ở **§3.4** [`RedisCacheConfig`](../../demo-phuluc3-redis/java-springboot-phuluc3/src/main/java/vn/demo/config/RedisCacheConfig.java) — §6 chỉ **đổi số** và quan sát

### 6.2–6.3. Chỉnh TTL và kiểm tra

```properties
# Lab quan sát hết hạn nhanh
app.cache.product-ttl-seconds=15
```

```bash
curl -s http://localhost:8080/api/products/1   # MISS
curl -s http://localhost:8080/api/products/1   # HIT
# đợi > 15 giây
curl -s http://localhost:8080/api/products/1   # MISS lại (log Repository)
```

> Muốn xoá sạch cache lab giữa các lần thử:  
> `docker exec -it demo-phuluc3-redis redis-cli FLUSHDB`

---

## 7. Lỗi thường gặp


| Triệu chứng                                      | Nguyên nhân                                                | Cách xử lý                                              |
| ------------------------------------------------ | ---------------------------------------------------------- | ------------------------------------------------------- |
| `Unable to connect to Redis` / timeout           | Container chưa Up / sai port                               | `docker compose up -d`; đúng `localhost:6379`           |
| App start fail vì Redis                          | Redis chưa chạy trước app                                  | Bật Redis trước; hoặc profile test tắt cache            |
| `@Cacheable` không có hiệu lực                   | Thiếu `@EnableCaching` / gọi `this.` cùng class            | Thêm annotation; gọi qua bean inject                    |
| Luôn MISS                                        | Mỗi lần key khác nhau (SpEL sai) / TTL = 0                 | Log `#id`; kiểm tra `KEYS *` trên redis-cli             |
| Luôn thấy data cũ sau PUT                        | Quên `@CacheEvict` / evict nhầm cache name                 | Evict đúng `cacheNames`; lab dùng `allEntries = true`   |
| `SerializationException`                         | Không serialize được object                                | Dùng `GenericJackson2JsonRedisSerializer` (§6)          |
| HIT nhưng JSON lỗi / class không khớp            | Đổi field model sau khi đã cache                           | Evict / flush Redis: `docker exec ... redis-cli FLUSHDB`|
| Test tích hợp bị phụ thuộc Redis                 | Test cần Redis thật                                        | `spring.cache.type=none` trong `application-test` hoặc Testcontainers (nâng cao) |


```properties
# --- application-test.properties (gợi ý) ---
spring.cache.type=none
app.product.seed-on-startup=false
```

---

## 8. Nâng cao (đọc hiểu — không bắt buộc lab đủ)

### Việc học viên làm: chỉ đọc


| Hướng                         | Khi nào nghĩ tới                                                  |
| ----------------------------- | ----------------------------------------------------------------- |
| **`@CachePut`**               | Luôn chạy method **và** cập nhật cache (vd. sau update trả entity)|
| **`@Caching`**                | Evict đồng thời `key = "#id"` và `key = "'all'"`                  |
| **Key generator / prefix**    | Nhiều service chung Redis — tránh đụng key                        |
| **Caffeine (local) + Redis**  | Cache 2 tầng — giảm round-trip Redis                              |
| **Scheduled + warm cache**    | Job Phụ lục 1 preload product hot vào Redis                       |
| **Redis password / ACL**      | Production — `requirepass` + `spring.data.redis.password`         |
| **Nhiều instance app**        | Chung một Redis → cache dùng chung; nhớ evict vẫn đúng            |


Ví dụ đọc hiểu — evict tinh hơn:

```java
@Caching(evict = {
        @CacheEvict(cacheNames = "products", key = "#id"),
        @CacheEvict(cacheNames = "products", key = "'all'")
})
public Product update(Long id, ProductRequest request) { ... }
```

> Final Project nhỏ: **`@Cacheable` + `@CacheEvict` + TTL + Redis Docker** là đủ. Chưa bắt buộc Cluster / Redisson.

---

## Tóm tắt


| Khái niệm                         | Ý chính                                                         |
| --------------------------------- | --------------------------------------------------------------- |
| Cache vs DB                       | Kệ gần quầy vs kho — HIT/MISS/stale                             |
| Redis Docker                      | `redis:7-alpine` · port `6379` · `PING` → `PONG`                |
| `starter-data-redis` + `starter-cache` | Client Redis + abstraction `@Cacheable`                     |
| Mongo Product                         | `@Document` + `MongoRepository` — nguồn sự thật             |
| `@EnableCaching`                  | Bật cơ chế cache của Spring                                     |
| `RedisCacheConfig` (§3.4)         | TTL theo cache name + JSON + `JavaTimeModule`                   |
| `@Cacheable`                      | Đọc: miss thì chạy method + ghi cache                           |
| `@CacheEvict`                     | Ghi/xoá: dọn cache tránh stale                                  |
| Quan sát TTL (§6)                 | Đổi `app.cache.*-ttl-seconds` → hết hạn → MISS                  |
| Ví dụ Product                     | GET cache; PUT/POST/DELETE evict                                |


---

## Phụ lục

### Bài tập

1. **Docker Redis:** `docker compose up -d` → `redis-cli PING` → `PONG`.
2. **Hello cache:** Gọi `GET /api/hello-cache?name=Khoa` hai lần — lần 1 chậm, lần 2 nhanh; giải thích HIT/MISS.
3. **Product:** `GET /api/products` lấy `_id` → `GET /{id}` hai lần — lần 2 không còn log MISS.
4. **Evict:** `PUT` đổi `price` → `GET` lại phải thấy giá mới (và có log MISS); kiểm tra document trên Mongo đã đổi.
5. **TTL:** Đặt `app.cache.product-ttl-seconds=10` — restart — sau 10s+ lần GET tiếp theo là MISS.
6. **redis-cli:** Sau vài GET, chạy `KEYS *` — mô tả bạn thấy gì (không cần thuộc hết format key).
7. **(Nâng cao):** Đổi `@CacheEvict(allEntries = true)` sang `@Caching` evict `#id` + `'all'`.
8. **(Nâng cao — đọc hiểu):** 2 instance Spring Boot dùng chung 1 Redis — sau khi instance A `PUT`, instance B `GET` có thấy data mới không? Vì sao?

### Checklist trước khi hoàn thành

- [ ] Giải thích được Cache khác đọc DB; biết HIT / MISS / Evict / TTL / stale
- [ ] Chạy được Redis bằng Docker; `PING` → `PONG`
- [ ] Có `starter-data-redis` + `starter-cache` + `starter-data-mongodb`
- [ ] `spring.data.mongodb.uri` đúng; Product lưu/đọc được trên Mongo
- [ ] `spring.data.redis.host/port` đúng; `spring.cache.type=redis`
- [ ] Có `@EnableCaching` và thấy Hello cache lần 2 nhanh hơn
- [ ] Product: GET được cache; PUT/DELETE có evict
- [ ] Có cấu hình TTL + JSON (`RedisCacheConfig` §3.4) và đã thử đổi TTL (§6)
- [ ] Biết xử lý Redis chưa chạy / quên `@EnableCaching` / stale vì quên evict
- [ ] Đã chạy được demo [`demo-phuluc3-redis`](../../demo-phuluc3-redis)

### Câu hỏi ôn tập

1. Vì sao cache **không** thay thế được database?
2. `@Cacheable` khác `@CacheEvict` chỗ nào? Khi nào bắt buộc dùng Evict?
3. App Spring và Redis chạy quan hệ thế nào khi dùng Docker ở phụ lục này?
4. TTL giúp gì nếu đôi khi quên evict?
5. Gọi `this.getById(id)` trong cùng `ProductService` có qua cache không? Vì sao?

Đáp án gợi ý (câu 1–5)

1. Cache là bản sao tạm để đọc nhanh; nguồn sự thật, giao dịch, truy vấn phức tạp vẫn thuộc DB/Repository.
2. `@Cacheable` tối ưu **đọc** (miss mới chạy method). `@CacheEvict` **xoá** cache khi dữ liệu nguồn đổi — bắt buộc sau create/update/delete (hoặc chấp nhận stale tới khi hết TTL).
3. Redis chạy **container riêng** publish `6379`; Spring trên host là **client** kết nối `localhost:6379` — không đóng gói Redis vào cùng process JVM.
4. TTL giới hạn thời gian tối đa dữ liệu cũ tồn tại; hết hạn → miss → đọc lại nguồn mới.
5. Không — AOP proxy chỉ chặn gọi từ **bean khác**. `this.` bỏ qua proxy → không HIT/MISS cache.

### Liên kết tham khảo

- **Demo chuẩn:** [`demo-phuluc3-redis`](../../demo-phuluc3-redis) · [README](../../demo-phuluc3-redis/README.md)
- [Spring Boot — Caching](https://docs.spring.io/spring-boot/reference/io/caching.html)
- [Spring Data Redis — Cache](https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html)
- [Redis — Docker Official Image](https://hub.docker.com/_/redis)
- Module 4 — [Bài 7 Docker](./6_java_m4_bai7_Docker.md)
- Module 4 — [Phụ lục 1 Scheduled](./2_java_m4_phuluc1_Scheduled.md) — warm cache / job dọn dữ liệu
- Module 4 — [Phụ lục 2 Email](./3_java_m4_phuluc2_Email.md)
- Module 4 — Bài 10 Final Project (`syllabus/pdf/java_m4_bai10_Final_Project.pdf`)
