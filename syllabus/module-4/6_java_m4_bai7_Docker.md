# Bài 7: Docker — Đóng gói ứng dụng Spring Boot

> File: `6_java_m4_bai7_Docker.md`  
> PDF gốc (tham khảo lịch sử): [`java_m4_bai7_Docker.pdf`](../pdf/java_m4_bai7_Docker.pdf)

**Hai lab, làm đúng thứ tự:**

| Lab | App | Mục đích Docker | Demo (làm sau) |
|-----|-----|-----------------|----------------|
| **Lab 1** | Spring Boot **không DB**, vài API dữ liệu cố định | Quen `Dockerfile`, `docker build`, `docker run` | [`demo-bai7-docker-lab1`](../../demo-bai7-docker-lab1) |
| **Lab 2** | **Copy** Mini Project Module 3 Bài 10 | Multi-stage + Compose (app + Mongo + volume + env) | [`demo-bai7-docker`](../../demo-bai7-docker) |

> Nguồn Lab 2: [`demo-bai10-mini-project`](../../../t3h-ltv-java-module-3/demo-bai10-mini-project) · [Bài 10 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)

---

## Mục tiêu bài học

Sau bài này, học viên có thể:

**Chung**

- Giải thích **Docker** giải quyết bài toán môi trường lệch (máy HV / máy GV / server)
- Phân biệt **image**, **container**, **Dockerfile**, **registry**, **volume**, **network**
- Cài **Docker Desktop** và dùng lệnh: `build`, `run`, `ps`, `logs`, `stop`, `rm`

**Lab 1**

- Tạo (hoặc dùng) một Spring Boot **chỉ Web**, vài REST API với **list cố định trong code** — không Mongo, không Security
- Viết **Dockerfile một stage**: copy JAR đã `package` → image JRE → `docker run -p`
- Gọi API từ máy HV (`curl` / trình duyệt) vào container

**Lab 2**

- **Copy** Mini Project Bài 10 sang Module 4 (**không** sửa repo M3, **không** đổi nghiệp vụ)
- Viết **Dockerfile multi-stage** (Maven builder + JRE runtime)
- Giải thích vì sao `localhost` trong container **không** phải Mongo trên máy host
- Chạy **app + Mongo** bằng `docker compose` (volume + `SPRING_DATA_MONGODB_URI`)
- Kiểm tra `/movies`, login admin, `/admin/dashboard`

> **Không nằm trong phạm vi:** Kubernetes / Helm; CI build-push; Docker hóa microservice; viết lại CRUD / Security Mini Project; Atlas như luồng chính (phụ lục).

> **Docker không phải điều kiện để làm microservices.** Lab 1–2 đóng gói **một** app (hello API, rồi monolith Mini Project).

---

## Điều kiện tiên quyết

- **Lab 1:** JDK 17+, Maven / `mvnw`, biết `@RestController` + JSON — ôn [M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) (REST, không cần Mongo hôm nay) hoặc [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) (`/api/v1/...`)
- **Lab 2:** Mini Project đã chạy `./mvnw spring-boot:run` — [Bài 10 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md); URI Mongo [Bài 3 §2](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md); import CSV [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md)
- **Chưa học Docker ở Module 3** — dạy từ đầu

```text
# Không thêm dependency Maven cho Docker.
# Docker đứng ngoài ứng dụng: đóng gói JAR đã có.
```

> **Demo làm sau.** Khi chưa có folder: Lab 1 scaffold Spring Initializr theo §4; Lab 2 copy Bài 10 rồi làm §6–§8.

### Thời lượng gợi ý

| Phần | Thời gian |
|------|-----------|
| Vì sao Docker + thuật ngữ + cài Desktop §1–3 | ~30 phút |
| **Lab 1** — API tĩnh + Dockerfile 1 stage + build/run §4–5 | ~40 phút |
| **Lab 2** — copy Bài 10 + multi-stage + localhost §6–7 | ~40 phút |
| **Lab 2** — Compose + volume + kiểm tra Mini Project §8 | ~30 phút |
| Lỗi thường gặp + phụ lục | ~15 phút |

---

## Nội dung (làm theo thứ tự)

| # | Chủ đề | Việc HV làm | Kết quả kiểm tra |
|---|--------|-------------|------------------|
| 0 | Bridge Module 3 | Đọc bảng | Lab 1 không nhầm CRUD phim; Lab 2 không sửa M3 |
| 1 | Vì sao Docker | Thảo luận | Không gắn bắt buộc với MS |
| 2 | Thuật ngữ | Image → N container | Volume / registry / `localhost` đúng |
| 3 | Cài Docker Desktop | `hello-world` | Engine chạy |
| 4–5 | **Lab 1** | Project không DB · Dockerfile 1 stage · `build`/`run` | `GET /api/v1/hello` từ container |
| 6–8 | **Lab 2** | Copy Bài 10 · multi-stage · Compose | `/movies` + login admin |
| 9 | Lỗi thường gặp | Đọc bảng | Tự sửa port / URI |
| Phụ lục | Atlas · bài tập · checklist | — | — |

---

## 0. Bridge Module 3 — hôm nay không giảng lại

| Kỹ năng | Xem lại | Dùng ở lab nào |
|---------|---------|----------------|
| REST `@RestController`, JSON, `/api/...` | [M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) · [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) | **Lab 1** — API tĩnh, không Repository |
| Mini Project (public + admin + Security) | [Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) · [README](../../../t3h-ltv-java-module-3/demo-bai10-mini-project/README.md) | **Lab 2** — copy sang M4 |
| `spring.data.mongodb.uri` | [Bài 3 §2](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) | **Lab 2** — host = tên service Compose / env |
| Import `mymoviedb` | [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md) | **Lab 2** — import vào Mongo container |
| URL `/movies`, `/login`, `admin` / `admin123` | README Bài 10 | **Lab 2** checklist |
| Testcontainers | [Bài 8 §9.3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai8_Unit_Testing.md) | Đọc hiểu — không lab |

**Hôm nay học mới:** Docker Engine, Dockerfile, port mapping; Lab 2 thêm multi-stage, network, volume, Compose, env.

---

## Kiến trúc hai lab (khi có demo)

```
demo-bai7-docker-lab1/                 ← Lab 1 (làm sau)
└── java-springboot-hello/
    ├── Dockerfile                     ← 1 stage, COPY jar
    ├── .dockerignore
    └── src/...                        ← API tĩnh, không Mongo

demo-bai7-docker/                      ← Lab 2 (làm sau): copy M3 Bài 10 + Docker
├── docker-compose.yml
└── java-springboot-bai7/
    ├── Dockerfile                     ← multi-stage
    ├── .dockerignore
    └── src/...                        ← nghiệp vụ giữ nguyên
```

```mermaid
flowchart TB
  subgraph lab1 [Lab 1 — một container]
    B1[Trình duyệt :8080] --> A1[app hello JAR]
  end
  subgraph lab2 [Lab 2 — Compose]
    B2[Trình duyệt :8080] --> A2[app Mini Project]
    A2 -->|mongo:27017| M[(Mongo + volume)]
  end
```

---

## 1. Vì sao dùng Docker?

### 1.1. Bài toán

Cùng một JAR: máy A chạy được, máy B thiếu JDK 17 hoặc sai version. **Lab 1** cho thấy: máy đích **chỉ cần Docker** vẫn gọi được API. **Lab 2** thêm Mongo — lệch URI / chưa cài DB còn phổ biến hơn.

**Dùng Docker:** đóng gói **cách chạy** (JRE + JAR, và ở Lab 2 cả Mongo) thành image/container.

> Docker **không phải** framework Java. Spring Boot **không “cài”** như JDK: deploy truyền thống = JRE + `.jar`. Docker thay “cài JDK trên server” bằng image.

### 1.2. Khi nào nên / chưa cần

| Nên dùng | Chưa bắt buộc |
|----------|----------------|
| Cần môi trường giống nhau (lớp, máy GV, server thử) | Một máy, `mvn spring-boot:run` là đủ |
| Nhiều thành phần (app + DB) — **Lab 2 / Compose** | Bài tập xong là xoá |
| CI: cùng lệnh build/run image | — |

Không dùng tiêu chí “có microservices thì phải Docker”.

### 1.3. So sánh nhanh

| Khía cạnh | Có Docker | Không Docker |
|-----------|-----------|--------------|
| Máy đích | Docker Engine | JDK + (Lab 2) Mongo đúng bản |
| Lặp lại | `docker run` / `compose up` | Cài lại từng thứ |
| Lab 1 | Một lệnh chạy API | Cần JDK trên máy chạy |

---

## 2. Thuật ngữ

| Thuật ngữ | Nghĩa đúng | Tránh hiểu sai |
|-----------|------------|----------------|
| **Dockerfile** | Công thức tạo image | Không phải “file chạy app” |
| **Image** | Gói chỉ đọc (JRE + JAR + …) | Không phải process đang chạy |
| **Container** | Một lần chạy của image | **Một image → nhiều container** |
| **Registry** | Kho image (Hub, GHCR, ECR…) | Công ty dùng private registry, không “tự tạo Docker Hub” |
| **Volume** | Dữ liệu sống sót khi container tạo lại | Mục đích chính: persist (Mongo, Lab 2) |
| **Network** | Container gọi nhau bằng **tên service** | `localhost` trong container = **chính nó** |

```text
Dockerfile  →  docker build  →  Image  →  docker run / compose  →  Container
```

`EXPOSE 8080` chỉ ghi chú. Máy HV vào được nhờ **`-p 8080:8080`** (Lab 1) hoặc `ports` Compose (Lab 2).

**Lab 1 vs Lab 2 (Dockerfile):**

| | Lab 1 | Lab 2 |
|--|-------|-------|
| Stage | **Một** `FROM` JRE | **Hai:** `builder` (Maven) + `jre` |
| JAR | HV `./mvnw package` trên máy, `COPY` vào image | Build **trong** image |
| DB | Không | Mongo + Compose |

---

## 3. Cài đặt Docker

1. [Docker Desktop](https://www.docker.com/products/docker-desktop/)  
2. Đợi engine sẵn sàng.  
3. Kiểm tra:

```bash
docker version
docker run --rm hello-world
```

---

# Lab 1 — Spring Boot không DB: quen Dockerfile, build, run

> Mục tiêu: HV **thấy vòng lặp** package → image → container → gọi API. Chưa Mongo, chưa Compose, chưa multi-stage.

## 4. Tạo project API dữ liệu cố định

### 4.1. Scaffold

Spring Initializr (hoặc IDE): **Spring Boot 3.x**, **Java 17**, dependency chỉ **`spring-boot-starter-web`**.  
Không Mongo, không Security, không Thymeleaf.

Gợi ý: `groupId` `vn.demo`, `artifactId` `demo-bai7-docker-lab1`.

### 4.2. API tối thiểu (list trong code)

Ví dụ — HV được đổi tên field, **giữ 3 endpoint**:

| Method | Path | Trả về |
|--------|------|--------|
| GET | `/api/v1/hello` | `{ "message": "Docker Lab 1" }` |
| GET | `/api/v1/courses` | Mảng 3–5 khoá học hard-code |
| GET | `/api/v1/courses/{id}` | Một phần tử hoặc 404 |

```java
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    public record CourseDto(int id, String name, String level) {}

    private static final List<CourseDto> COURSES = List.of(
            new CourseDto(1, "Java Spring Boot", "M3"),
            new CourseDto(2, "REST & Docker", "M4"),
            new CourseDto(3, "Mini Project Movie", "M3 Bài 10")
    );

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Docker Lab 1");
    }

    @GetMapping("/courses")
    public List<CourseDto> courses() {
        return COURSES;
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<CourseDto> course(@PathVariable int id) {
        return COURSES.stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

Chạy máy trước khi Docker:

```bash
./mvnw spring-boot:run
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses
```

Tắt app (giải phóng cổng 8080) rồi mới `docker run`.

> Không giảng lại REST/DTO. Ôn [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) nếu muốn `/api/v1`.

## 5. Dockerfile một stage — build và run

### 5.1. Package JAR trên máy HV

```bash
./mvnw package -DskipTests
ls target/*.jar
```

### 5.2. `.dockerignore`

```gitignore
target/
.idea/
.vscode/
*.iml
.git
.gitignore
```

Lab 1 **cần** file JAR trong `target/` → **bỏ `target/` khỏi ignore** *hoặc* ignore hết `target/` trừ `*.jar`:

```gitignore
.idea/
.vscode/
*.iml
.git
src/
!target/*.jar
```

Cách đơn giản cho buổi đầu: **không** ignore `target/*.jar` — vẫn ignore `.idea`, `.git`.

### 5.3. Dockerfile (một stage)

Tên file đúng **`Dockerfile`**, cùng thư mục `pom.xml`:

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

HV phải `package` **trước** `docker build`. Máy build cần JDK; máy **chỉ chạy** container thì không cần.

### 5.4. Build image và chạy container

```bash
docker build -t hello-docker:1.0 .
docker images
docker run --name hello-lab1 -p 8080:8080 hello-docker:1.0
```

| Cờ | Nghĩa |
|----|--------|
| `-t hello-docker:1.0` | Tên + tag image |
| `-p 8080:8080` | Máy HV 8080 → container 8080 |
| `--name` | Dễ `logs` / `stop` |

Cửa sổ khác:

```bash
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses/1
docker ps
docker logs hello-lab1
docker stop hello-lab1
docker rm hello-lab1
```

**Xong Lab 1** khi 3 API trả đúng như lúc `spring-boot:run`, nhưng process nằm trong container.

Đổi port nếu 8080 bận: `-p 8081:8080` rồi gọi `http://localhost:8081/api/v1/hello`.

---

# Lab 2 — Docker hóa Mini Project Bài 10 (multi-stage + Compose)

> Làm **sau** Lab 1. App có Mongo + Thymeleaf + Security — Dockerfile 1 stage + `localhost` **sẽ thiếu**. Ở đây học multi-stage, network, volume, Compose.

## 6. Copy Mini Project sang Module 4

**Không** dockerize folder trong repo Module 3.

```bash
# Đường dẫn chỉnh theo máy
cp -R t3h-ltv-java-module-3/demo-bai10-mini-project \
      t3h-ltv-java-module-4/demo-bai7-docker
```

Đổi `artifactId` / `spring.application.name` (vd. `demo-bai7-docker`).  
Chạy một lần trên máy (Mongo local như Bài 10):

```bash
cd demo-bai7-docker/java-springboot-bai7
./mvnw spring-boot:run
```

URI Mongo: [Bài 3 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md). Bước này **chưa** Compose.

## 7. Dockerfile multi-stage

### 7.1. `.dockerignore`

```gitignore
target/
.idea/
.vscode/
*.iml
.git
.gitignore
```

`target/` ignore được vì **Maven build JAR trong image**. Không bắt buộc `package` trước `docker build` (khác Lab 1).

### 7.2. Multi-stage

```dockerfile
# --- Stage 1: build JAR ---
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: chạy ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Lệnh | Việc |
|------|------|
| `FROM … AS builder` | Maven + JDK 17 để biên dịch |
| `COPY pom.xml` rồi `go-offline` | Cache dependency |
| `FROM eclipse-temurin:17-jre` | Image chạy nhỏ, không Maven |
| `COPY --from=builder` | Chỉ lấy JAR sang stage 2 |

Không dùng `--no-cache` mặc định. Windows: CRLF / `chmod +x mvnw` như trên.

```bash
docker build -t movie-portal:1.0 .
```

### 7.3. Vì sao `localhost` Mongo hỏng?

URI Bài 10 dạng `mongodb://…@localhost:27017/…`.

- `spring-boot:run`: `localhost` = **máy HV**
- Trong container: `localhost` = **container app** — không có `mongod`

| Cách | Khi nào | Host trong URI |
|------|---------|----------------|
| `host.docker.internal` | Mongo vẫn trên máy, Docker Desktop | Thay `localhost` |
| Mongo **cùng network** Compose | **Lab 2 chuẩn** | Tên service `mongo` |
| Atlas | Phụ lục | `…mongodb.net` |

**Không bake mật khẩu vào image** — dùng env:

```bash
docker run --name movie-portal -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI='mongodb://root:secret@mongo:27017/db_java_t3h_module3_bai_7?authSource=admin' \
  movie-portal:1.0
```

Tên `mongo` chỉ resolve khi **cùng Docker network** → Compose.

Chạy image Mini Project **một mình** lúc này thường **fail Mongo** — đúng bài học, sang §8.

## 8. Docker Compose — app + Mongo

File `docker-compose.yml` ở **gốc** `demo-bai7-docker` (cạnh thư mục Maven):

```yaml
services:
  mongo:
    image: mongo:7
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: example
    volumes:
      - mongo-data:/data/db

  app:
    build:
      context: ./java-springboot-bai7
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://root:example@mongo:27017/db_java_t3h_module3_bai_7?authSource=admin
    depends_on:
      - mongo

volumes:
  mongo-data:
```

- Volume: `compose down` rồi `up` — data còn (trừ `down -v`)
- Lab: một bộ user/password; không commit secret production (có thể `.env`)

```bash
cd demo-bai7-docker
docker compose up --build
```

Import CSV `mymoviedb` vào **Mongo container** — cùng ý [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md); README demo (làm sau) sẽ ghi một lệnh cụ thể.

Seeder admin Bài 10: `admin` / `admin123` — [README Bài 10](../../../t3h-ltv-java-module-3/demo-bai10-mini-project/README.md).

### Checklist Lab 2 xong

| URL | Ai | Kỳ vọng |
|-----|-----|---------|
| http://localhost:8080/movies | Khách | Public (đã import `mymoviedb`) |
| http://localhost:8080/login | — | Form login |
| http://localhost:8080/admin/dashboard | `admin` / `admin123` | Dashboard D1–D6 |

```bash
docker compose logs -f app
docker compose down          # giữ volume
docker compose down -v       # xoá data lab
```

Tắt Lab 1 (`hello-lab1`) trước nếu còn chiếm `:8080`.

---

## 9. Lỗi thường gặp

| Hiện tượng | Hay gặp ở | Hướng xử lý |
|------------|-----------|-------------|
| `COPY target/*.jar` fail | Lab 1 | Chưa `./mvnw package`; `.dockerignore` nuốt hết `target/` |
| API 8080 không vào | Lab 1–2 | App IDE còn chạy; quên `-p` / `ports` |
| `Connection refused` Mongo | Lab 2 | URI `localhost`; sai tên service; Mongo chưa ready (`depends_on` ≠ ready) |
| Build fail `mvnw` | Lab 2 | CRLF Windows; thiếu `chmod +x` |
| Đổi URI không có hiệu lực | Lab 2 | Quên `environment` / `-e`; không cần rebuild nếu chỉ đổi env |
| `/movies` trống | Lab 2 | Chưa import CSV đúng DB/volume |
| Login admin fail | Lab 2 | Volume mới, seeder chưa chạy — xem log |
| Build chậm | Lab 2 | `--no-cache`; COPY `src` trước `pom.xml` |

---

## 10. Nâng cao (đọc hiểu — không lab)

- **Buildpacks:** `./mvnw spring-boot:build-image`
- User không root trong image runtime
- **Testcontainers** ([M3 Bài 8 §9.3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai8_Unit_Testing.md)) — Docker lúc **test**, khác đóng gói app
- Registry + CI: `build` → `push` → `pull`
- Nhiều service Spring: **không** mục tiêu Bài 7

---

## Phụ lục A — MongoDB Atlas (tuỳ chọn, Lab 2)

Không thay Compose. Network Access + URI qua env, không bake vào image / không dạy lại Compass.

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI='mongodb+srv://USER:PASS@cluster.../db?retryWrites=true&w=majority' \
  movie-portal:1.0
```

Mini Project cần `mymoviedb` — import CSV, không tạo tay vài document.

---

## Phụ lục B — Bài tập

**Lab 1**

1. Đổi `-p 8081:8080`, gọi đúng URL, giải thích hai số.  
2. Sửa message `/hello`, `package` + `build` lại, `run` — chứng minh phải **image mới**.  
3. `docker run` **hai** container cùng image, map `8080` và `8081` — một image, hai container.

**Lab 2**

4. Vẽ: trình duyệt → port host → app → hostname `mongo`.  
5. `compose down` không `-v` rồi `up`: data còn không? Thử `-v` (lab).  
6. (Tuỳ chọn) `docker compose logs app` khi lỗi URI.

---

## Phụ lục C — Checklist

**Lab 1**

- [ ] `hello-world`  
- [ ] App không DB, 3 API tĩnh chạy trên máy  
- [ ] `./mvnw package` + Dockerfile 1 stage  
- [ ] `docker build -t hello-docker:1.0 .`  
- [ ] `docker run -p 8080:8080` · `curl /api/v1/hello` và `/courses`  
- [ ] `ps` / `logs` / `stop` / `rm`

**Lab 2**

- [ ] Copy Bài 10 sang M4 (không sửa repo M3)  
- [ ] Dockerfile **multi-stage**  
- [ ] Giải thích được fail `localhost` Mongo  
- [ ] `docker compose up` app + Mongo + volume  
- [ ] `/movies` + login admin  
- [ ] URI qua env, không bake secret  

---

## Phụ lục D — Liên kết

- Docker Desktop: <https://www.docker.com/products/docker-desktop/>  
- Dockerfile: <https://docs.docker.com/reference/dockerfile/>  
- Compose: <https://docs.docker.com/compose/>  
- Temurin: <https://hub.docker.com/_/eclipse-temurin>  
- Spring Boot Externalized Config: <https://docs.spring.io/spring-boot/reference/features/external-config.html>  
- [M4 Bài 1 REST](./1_java_m4_bai1_RESTful_API.md) · [M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) · [M3 Bài 3 URI](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) · [M3 Bài 7 import](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md)
