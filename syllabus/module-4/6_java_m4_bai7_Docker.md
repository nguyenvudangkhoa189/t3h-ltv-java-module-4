# Bài 7: Docker — Đóng gói ứng dụng Spring Boot

> File: `6_java_m4_bai7_Docker.md`  
> PDF gốc (tham khảo lịch sử): [`java_m4_bai7_Docker.pdf`](../pdf/java_m4_bai7_Docker.pdf)

**Hai lab, làm đúng thứ tự — chung một thư mục gốc [`demo-bai7-docker`](../../demo-bai7-docker), mỗi lab một project con (`lab1/`, `lab2/`):**

| Lab | App | Mục đích Docker | Demo chuẩn |
|-----|-----|-----------------|------------|
| **Lab 1** | Spring Boot **không DB**, vài API dữ liệu cố định | Quen `Dockerfile`, `docker build`, `docker run` | [`demo-bai7-docker/lab1`](../../demo-bai7-docker/lab1) · [README](../../demo-bai7-docker/lab1/README.md) |
| **Lab 2** | **Copy** Mini Project Module 3 Bài 10 | Multi-stage + Compose (app + Mongo + volume + env) | [`demo-bai7-docker/lab2`](../../demo-bai7-docker/lab2) · [README](../../demo-bai7-docker/lab2/README.md) |

> Nguồn Lab 2: [`demo-bai10-mini-project`](../../../t3h-ltv-java-module-3/demo-bai10-mini-project) · [Bài 10 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)

> **Cách đánh số:** Phần chung (§0–§4, gồm kiến trúc demo) → **Lab 1** đánh số lại từ §1 → **Lab 2** đánh số lại từ §1. Mỗi lab là một phần lớn độc lập.

---

## Mục tiêu bài học

Sau bài này, học viên có thể:

**Chung**

- Giải thích **Docker** giải quyết bài toán môi trường lệch (máy HV / máy GV / server)
- Phân biệt **image**, **container**, **Dockerfile**, **registry**, **volume**, **network**
- Cài **Docker Desktop** và dùng lệnh: `build`, `run`, `ps`, `logs`, `stop`, `rm`
- Chạy nhanh **MongoDB / MySQL** bằng `docker run` (có / không mật khẩu)

**Lab 1**

- Dùng (hoặc tạo) Spring Boot **chỉ Web**, vài REST API với **list cố định trong code** — không Mongo, không Security
- Viết **Dockerfile một stage**: copy JAR đã `package` → image JRE → `docker run -p`
- Gọi API từ máy HV (`curl` / trình duyệt) vào container

**Lab 2**

- Hiểu demo Lab 2 là **bản copy** Mini Project Bài 10 sang Module 4 (**không** sửa repo M3, **không** đổi nghiệp vụ)
- Giải thích **vì sao dùng multi-stage** và viết Dockerfile multi-stage (Maven builder + JRE runtime)
- Giải thích vì sao `localhost` trong container **không** phải Mongo trên máy host
- Chạy **app + Mongo** bằng `docker compose` (volume + `SPRING_DATA_MONGODB_URI`)
- Import CSV + kiểm tra `/movies`, login admin, `/admin/dashboard`

> **Không nằm trong phạm vi:** Kubernetes / Helm; CI build-push; Docker hóa microservice; viết lại CRUD / Security Mini Project; Atlas như luồng chính (phụ lục).

> **Docker không phải điều kiện để làm microservices.** Lab 1–2 đóng gói **một** app (hello API, rồi monolith Mini Project).

---

## Điều kiện tiên quyết

- **Lab 1:** JDK 17+, Maven / `mvnw`, biết `@RestController` + JSON — ôn [M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) (REST) hoặc [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) (`/api/v1/...`)
- **Lab 2:** đã làm Lab 1; Mini Project đã quen — [Bài 10 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md); URI Mongo [Bài 3 §2](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md); import CSV [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md)
- **Chưa học Docker ở Module 3** — dạy từ đầu

```text
# Không thêm dependency Maven cho Docker.
# Docker đứng ngoài ứng dụng: đóng gói JAR đã có.
```

### Thời lượng gợi ý

| Phần | Thời gian |
|------|-----------|
| Phần chung §0–§3 (vì sao + thuật ngữ + cài + `docker run` DB) | ~35 phút |
| **Kiến trúc hai lab** (nói trước khi vào lab) | ~5 phút |
| **Lab 1** §1–§2 | ~40 phút |
| **Lab 2** §1–§4 | ~70 phút |
| Lỗi thường gặp + phụ lục | ~15 phút |

---

## Nội dung (làm theo thứ tự)

| Phần | Chủ đề | Việc HV làm | Kết quả kiểm tra |
|------|--------|-------------|------------------|
| **Chung** §0 | Bridge Module 3 | Đọc bảng | Lab 1 ≠ CRUD phim; Lab 2 không sửa repo M3 |
| **Chung** §1 | Vì sao Docker | Thảo luận | Không gắn bắt buộc với MS |
| **Chung** §2 | Thuật ngữ | Image → N container | Volume / registry / `localhost` đúng |
| **Chung** §3 | Cài Docker + `docker run` Mongo/MySQL | `hello-world` + DB | Engine chạy; DB lên bằng container |
| **Trước lab** | Kiến trúc hai lab (demo chuẩn) | Xem folder + sơ đồ | Biết Lab 1 / Lab 2 khác nhau chỗ nào |
| **Lab 1** §1–§2 | API tĩnh + Dockerfile 1 stage | Demo · `build`/`run` | `GET /api/v1/hello` từ container |
| **Lab 2** §1–§4 | Copy Bài 10 · multi-stage · localhost · Compose | Demo Lab 2 · import | `/movies` + login admin |
| Cuối bài | Lỗi thường gặp · phụ lục | Đọc bảng | Tự sửa port / URI |

---

# Phần chung — trước khi vào lab

## 0. Bridge Module 3 — hôm nay không giảng lại

| Kỹ năng | Xem lại | Dùng ở lab nào |
|---------|---------|----------------|
| REST `@RestController`, JSON, `/api/...` | [M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) · [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) | **Lab 1** — API tĩnh, không Repository |
| Mini Project (public + admin + Security) | [Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) · [README](../../../t3h-ltv-java-module-3/demo-bai10-mini-project/README.md) | **Lab 2** — bản copy trong M4 |
| `spring.data.mongodb.uri` | [Bài 3 §2](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) | **Lab 2** — host = tên service Compose / env |
| Import `mymoviedb` | [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md) | **Lab 2** — `scripts/import-movies.sh` vào container |
| URL `/movies`, `/login`, `admin` / `admin123` | README Bài 10 | **Lab 2** checklist |
| Testcontainers | [Bài 8 §9.3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai8_Unit_Testing.md) | Đọc hiểu — không lab |

**Hôm nay học mới:** Docker Engine, Dockerfile, port mapping; Lab 2 thêm multi-stage, network, volume, Compose, env.

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

### 3.1. Cài Docker Desktop

1. Tải [Docker Desktop](https://www.docker.com/products/docker-desktop/)  
2. Mở app, đợi engine sẵn sàng (icon cá voi không còn “starting”).  
3. Kiểm tra:

```bash
docker version
docker run --rm hello-world
```

Thấy thông báo Hello từ Docker → Engine đã dùng được.

> macOS / Windows: Desktop chạy Linux VM bên dưới. Lệnh `docker` gõ trên Terminal máy HV.

### 3.2. Luyện `docker run` — MongoDB & MySQL

Trước khi đóng gói Spring Boot, HV luyện **chạy DB bằng image có sẵn trên Docker Hub** (không tự viết Dockerfile). Đây là cách hay dùng khi máy chưa cài Mongo/MySQL native.

**Lệnh chung hay gặp:**

| Cờ | Nghĩa |
|----|--------|
| `-d` | Chạy nền (detached) |
| `--name` | Đặt tên container |
| `-p host:container` | Map cổng ra máy HV |
| `-e KEY=value` | Biến môi trường (user/password…) |
| `-v tên:/đường/trong/container` | Volume persist dữ liệu |

Dừng / xoá khi xong lab:

```bash
docker stop mongo-lab mysql-lab
docker rm mongo-lab mysql-lab
# (tuỳ chọn) xoá volume nếu đã tạo: docker volume rm mongo-lab-data
```

#### A. MongoDB

**Cách 1 — Không đặt mật khẩu** (lab nhanh, chỉ máy học, **không** dùng production):

```bash
docker run -d \
  --name mongo-lab \
  -p 27017:27017 \
  mongo:7
```

- Không có user/password → kết nối: `mongodb://localhost:27017/ten_db`
- Spring: `spring.data.mongodb.uri=mongodb://localhost:27017/db_java_t3h_module3`

**Cách 2 — Có mật khẩu** (khớp demo Module 3/4):

```bash
docker run -d \
  --name mongo-lab \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=DBVWiYdDoMnfWmK \
  -v mongo-lab-data:/data/db \
  mongo:7
```

- URI: `mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/ten_db?authSource=admin`
- Volume `mongo-lab-data`: tắt container rồi chạy lại — **không mất** dữ liệu

Kiểm tra:

```bash
docker ps
docker logs mongo-lab
# Có mật khẩu:
docker exec -it mongo-lab mongosh -u root -p DBVWiYdDoMnfWmK --authenticationDatabase admin
# Không mật khẩu:
docker exec -it mongo-lab mongosh
```

#### B. MySQL

**Cách 1 — Không đặt mật khẩu root** (lab; MySQL image vẫn thường yêu cầu biến — dùng password rỗng qua `MYSQL_ALLOW_EMPTY_PASSWORD`):

```bash
docker run -d \
  --name mysql-lab \
  -p 3306:3306 \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
  -e MYSQL_DATABASE=demo_db \
  mysql:8
```

- Kết nối: `jdbc:mysql://localhost:3306/demo_db` · user `root` · **không** password  
- Chỉ dùng học trên máy cá nhân

**Cách 2 — Có mật khẩu** (khuyến nghị khi luyện gần thực tế):

```bash
docker run -d \
  --name mysql-lab \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=demo_db \
  -v mysql-lab-data:/var/lib/mysql \
  mysql:8
```

- User `root` / password `root` · DB sẵn `demo_db`  
- Volume giữ dữ liệu giữa các lần `docker start`

Kiểm tra:

```bash
docker ps
docker logs mysql-lab
# Đợi vài giây MySQL init xong rồi:
docker exec -it mysql-lab mysql -uroot -proot -e "SHOW DATABASES;"
# (cách không mật khẩu: mysql -uroot -e "SHOW DATABASES;")
```

#### So sánh nhanh hai lựa chọn

| | Không mật khẩu | Có mật khẩu |
|--|----------------|-------------|
| Mục đích | Lab / thử nhanh trên máy HV | Gần cấu hình team / demo M3–M4 |
| Rủi ro | Ai cùng mạng máy có thể vào DB | An toàn hơn trên máy dùng chung |
| URI Spring | Không có `user:pass@` | Có user/pass + (Mongo) `authSource=admin` |
| Production | **Không** dùng | Bắt buộc có auth (+ không hardcode trên slide công khai) |

> **Lab 1** không cần Mongo/MySQL.  
> **Lab 2** dùng Mongo (Compose) với **có mật khẩu** — cùng bộ `root` / `DBVWiYdDoMnfWmK` như các demo Module 3/4. Phần §3.2 giúp HV quen `docker run` trước khi viết Dockerfile cho app.

---

## 4. Kiến trúc hai lab (demo chuẩn) — nói trước khi vào lab

> Mục đích mục này: HV **nhìn tổng thể** hai project demo và khác biệt chính **trước** khi làm từng bước Lab 1 / Lab 2.  
> Không code ở đây — chỉ định hướng folder, file Docker, và sơ đồ chạy.

Mỗi lab = **một project con** trong [`demo-bai7-docker`](../../demo-bai7-docker):

| | Lab 1 | Lab 2 |
|--|-------|-------|
| Folder | [`lab1/`](../../demo-bai7-docker/lab1) | [`lab2/`](../../demo-bai7-docker/lab2) |
| App | API tĩnh, **không DB** | Mini Project (copy M3 Bài 10) + Mongo |
| Dockerfile | **1 stage** (COPY JAR đã `package`) | **Multi-stage** (build trong Docker) |
| Chạy | `docker build` + `docker run -p` | `docker compose up` (app + mongo) |

```
demo-bai7-docker/                         ← thư mục gốc demo Bài 7
├── README.md
├── lab1/                                 ← Lab 1 (project riêng)
│   ├── README.md
│   └── java-springboot-hello/
│       ├── Dockerfile                    ← 1 stage, COPY jar
│       ├── .dockerignore
│       ├── pom.xml
│       └── src/main/java/vn/demo/
│           ├── DemoBai7Lab1Application.java
│           ├── controller/HelloController.java
│           └── dto/CourseDto.java
└── lab2/                                 ← Lab 2 (project riêng): copy M3 Bài 10 + Docker
    ├── README.md
    ├── docker-compose.yml                ← TH1: chưa có Mongo (app + mongo)
    ├── docker-compose.host-mongo.yml     ← TH2: đã có Mongo trên máy (chỉ app)
    ├── scripts/import-movies.sh
    └── java-springboot-bai7/
        ├── Dockerfile                    ← multi-stage
        ├── .dockerignore
        ├── sample-data/                  ← CSV mymoviedb
        └── src/...                       ← nghiệp vụ giữ nguyên
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

**Thứ tự làm:** xong phần chung §0–§4 → vào **Lab 1** (§1–§2) → rồi **Lab 2** (§1–§4). Không làm Lab 2 trước Lab 1.

---

# Lab 1 — Spring Boot không DB: quen Dockerfile, build, run

> Mục tiêu: HV **thấy vòng lặp** package → image → container → gọi API. Chưa Mongo, chưa Compose, chưa multi-stage.  
> **Demo chuẩn:** [`demo-bai7-docker/lab1`](../../demo-bai7-docker/lab1)  
> **Đánh số trong Lab 1 bắt đầu lại từ §1.**

## 1. Project API dữ liệu cố định

### 1.1. Cách làm đề xuất

| Cách | Việc HV làm |
|------|-------------|
| **A — Dùng demo (khuyến nghị)** | Mở `demo-bai7-docker/lab1/java-springboot-hello`, đọc code, chạy §1.3 |
| **B — Tự scaffold** | Spring Initializr: Boot 3.x, Java 17, **chỉ** `spring-boot-starter-web` → làm bảng §1.2 |

`groupId` `vn.demo`, `artifactId` `demo-bai7-docker-lab1`. Không Mongo, không Security, không Thymeleaf.

### 1.2. Bản đồ công việc — file / class Lab 1

| File / class | Tạo / Cập nhật | Việc cần làm | Để làm gì |
|--------------|----------------|--------------|-----------|
| `pom.xml` | **Tạo** | Parent Boot 3.5.x + `spring-boot-starter-web` (+ test) | App tối thiểu |
| `DemoBai7Lab1Application` | **Tạo** | `@SpringBootApplication` + `main` | Entry |
| `dto/CourseDto` | **Tạo** | `record CourseDto(int id, String name, String level)` | DTO JSON |
| `controller/HelloController` | **Tạo** | 3 endpoint bảng dưới + list `COURSES` tĩnh | API kiểm tra Docker |
| `application.properties` | **Tạo** | `spring.application.name`, `server.port=8080` | Cấu hình tối thiểu |
| `HelloApiSmokeTest` | **Tạo** (demo có sẵn) | MockMvc 3 case | Chắc app trước Docker |
| `Dockerfile` | **Tạo** ở Lab 1 §2 | 1 stage JRE | Đóng gói |
| `.dockerignore` | **Tạo** ở Lab 1 §2 | Bỏ IDE / git | Context gọn |

### 1.3. API tối thiểu (giữ 3 endpoint)

| Method | Path | Trả về |
|--------|------|--------|
| GET | `/api/v1/hello` | `{ "message": "Docker Lab 1" }` |
| GET | `/api/v1/courses` | Mảng 3 khoá học hard-code |
| GET | `/api/v1/courses/{id}` | Một phần tử hoặc 404 |

Code chuẩn (khớp demo):

```java
@RestController
@RequestMapping("/api/v1")
public class HelloController {

	// --- Danh sách cố định trong code (thay DB) ---
	private static final List<CourseDto> COURSES = List.of(
			new CourseDto(1, "Java Spring Boot", "M3"),
			new CourseDto(2, "REST & Docker", "M4"),
			new CourseDto(3, "Mini Project Movie", "M3 Bài 10"));

	@GetMapping("/hello")
	public Map<String, String> hello() {
		// --- Trả message cố định — dùng curl sau docker run ---
		return Map.of("message", "Docker Lab 1");
	}

	@GetMapping("/courses")
	public List<CourseDto> courses() {
		return COURSES;
	}

	@GetMapping("/courses/{id}")
	public ResponseEntity<CourseDto> course(@PathVariable int id) {
		// --- Tìm trong list tĩnh; không thấy → 404 ---
		return COURSES.stream()
				.filter(c -> c.id() == id)
				.findFirst()
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
```

Chạy máy **trước** Docker:

```bash
cd demo-bai7-docker/lab1/java-springboot-hello
./mvnw spring-boot:run
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses
```

Tắt app (Ctrl+C) rồi mới `docker run` — giải phóng cổng 8080.

> Không giảng lại REST/DTO. Ôn [M4 Bài 1](./1_java_m4_bai1_RESTful_API.md) nếu muốn `/api/v1`.

---

## 2. Dockerfile một stage — build và run

### 2.1. Bản đồ công việc — Docker Lab 1

| Bước | File / lệnh | Tạo / Cập nhật | Việc HV làm |
|------|-------------|----------------|-------------|
| 1 | Terminal | — | `./mvnw package -DskipTests` → có `target/*.jar` |
| 2 | `.dockerignore` | **Tạo** | Bỏ `.idea/`, `.git/`… (**không** nuốt hết `target/*.jar`) |
| 3 | `Dockerfile` | **Tạo** | 1 stage: `FROM jre` → `COPY target/*.jar` → `ENTRYPOINT` |
| 4 | Terminal | — | `docker build -t hello-docker:1.0 .` |
| 5 | Terminal | — | `docker run -d --name hello-lab1 -p 8080:8080 hello-docker:1.0` (có `-d` = chạy nền) |
| 6 | Terminal | — | `curl` 3 API; `docker ps` / `logs` / `stop` / `rm` |

### 2.2. Package JAR trên máy HV

```bash
./mvnw package -DskipTests
ls target/*.jar
```

### 2.3. `.dockerignore` (Lab 1)

Cách đơn giản (khớp demo):

```gitignore
.idea/
.vscode/
*.iml
.git/
.gitignore
```

> Lab 1 **cần** JAR trong context → **không** ghi `target/` vào ignore. (Lab 2 mới ignore `target/` vì build trong image.)

### 2.4. Dockerfile (một stage)

Tên file đúng **`Dockerfile`**, cùng thư mục `pom.xml` (xem demo: `java-springboot-hello/Dockerfile`):

```dockerfile
# Lab 1 — một stage: máy HV đã package JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Dòng | Ý nghĩa |
|------|---------|
| `FROM …-jre` | Image chạy chỉ cần JRE 17 (không JDK/Maven) |
| `COPY target/*.jar` | Đưa fat JAR Spring Boot vào image |
| `EXPOSE 8080` | Metadata — **không** mở port ra máy HV |
| `ENTRYPOINT` | Process chính = `java -jar` |

HV phải `package` **trước** `docker build`. Máy chỉ **chạy** container thì không cần JDK.

### 2.5. Build image và chạy container

```bash
docker build -t hello-docker:1.0 .
docker images

# Cách A — chạy NỀN (khuyến nghị khi muốn tắt Terminal / mở cửa sổ khác curl)
docker run -d --name hello-lab1 -p 8080:8080 hello-docker:1.0

# Cách B — chạy GẮN Terminal (log in ngay cửa sổ này; đóng Terminal = container dừng)
# docker run --name hello-lab1 -p 8080:8080 hello-docker:1.0
```

| Cờ | Nghĩa |
|----|--------|
| `-d` | **Detached** — chạy nền, không chiếm Terminal; tắt Terminal **không** tắt container |
| `-t hello-docker:1.0` | Tên + tag image (khi `build`) |
| `-p 8080:8080` | Máy HV 8080 → container 8080 |
| `--name` | Dễ `logs` / `stop` |
| `--rm` | (tuỳ chọn) tự xoá container khi **dừng**; hay dùng với Cách B tạm thời |

> **Vì sao tắt Terminal lại mất container?**  
> Không có `-d`, `docker run` gắn process app với session Terminal (foreground). Đóng cửa sổ → OS gửi tín hiệu dừng process → container dừng. Có `--rm` thì container còn bị **xoá** luôn. Muốn chạy rồi đóng Terminal: luôn thêm **`-d`**.

Cửa sổ khác (sau khi đã `run -d`):

```bash
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses/1
docker ps
docker logs hello-lab1
docker stop hello-lab1
docker rm hello-lab1
```

**Xong Lab 1** khi 3 API trả đúng như lúc `spring-boot:run`, nhưng process nằm trong container.

Port 8080 bận: `-p 8081:8080` → `http://localhost:8081/api/v1/hello`.

---

# Lab 2 — Docker hóa Mini Project Bài 10 (multi-stage + Compose)

> Làm **sau** Lab 1. App có Mongo + Thymeleaf + Security.  
> **Demo chuẩn:** [`demo-bai7-docker/lab2`](../../demo-bai7-docker/lab2) — đã copy sẵn từ M3 Bài 10 + Dockerfile + Compose.  
> **Đánh số trong Lab 2 bắt đầu lại từ §1.**

## 1. Bản copy Mini Project trong Module 4

**Không** dockerize folder trong repo Module 3. Demo M4 đã copy; HV **dùng demo** hoặc tự copy theo bảng dưới.

### 1.1. Bản đồ công việc — chuẩn bị Lab 2

| File / thành phần | Tạo / Cập nhật | Việc cần làm | Để làm gì |
|-------------------|----------------|--------------|-----------|
| Folder `demo-bai7-docker/lab2/` | **Có sẵn** (hoặc copy tay) | Copy từ `demo-bai10-mini-project` → đổi tên thư mục Maven thành `java-springboot-bai7` | Tách khỏi repo M3 |
| `pom.xml` | **Cập nhật** | `artifactId` / `<name>` → `demo-bai7-docker` | Đúng module 4 |
| `DemoBai7DockerApplication` | **Cập nhật** (đổi tên từ Bài 10) | Đổi tên class entry; **không** đổi nghiệp vụ | Entry Lab 2 |
| `application.properties` | **Cập nhật** | Giữ URI localhost cho `mvn run`; ghi chú env Compose ghi đè | Dev máy + Docker |
| Toàn bộ Controller/Service/… | **Giữ** | Không sửa CRUD / Security / Dashboard | Tập trung Docker |

Tự copy (nếu không dùng demo sẵn):

```bash
cp -R t3h-ltv-java-module-3/demo-bai10-mini-project \
      t3h-ltv-java-module-4/demo-bai7-docker/lab2
# Đổi tên thư mục java-springboot-bai10 → java-springboot-bai7, artifactId, Application class
```

Kiểm tra copy còn chạy trên máy (Mongo local — có thể dùng lệnh §3.2 phần chung):

```bash
cd demo-bai7-docker/lab2/java-springboot-bai7
./mvnw spring-boot:run
```

URI Mongo: [Bài 3 M3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md). Bước này **chưa** Compose.

---

## 2. Dockerfile multi-stage

### 2.1. Vì sao dùng multi-stage? *(đọc kỹ)*

Lab 1: HV đã có JAR trên máy → Dockerfile **một stage** chỉ cần JRE + `COPY jar`.  
Lab 2 (và thực tế CI): thường muốn **một lệnh** `docker build` là ra image chạy được — **không** phụ thuộc máy HV đã `mvn package` sẵn, **không** nhồi Maven/JDK vào image đem lên server.

**Vấn đề nếu chỉ một stage “build + chạy” cùng lúc:**

```dockerfile
# ❌ Không khuyến nghị cho production
FROM maven:3.9.9-eclipse-temurin-17
COPY . /app
WORKDIR /app
RUN ./mvnw package -DskipTests
CMD ["java", "-jar", "target/app.jar"]
```

| Hệ quả | Chi tiết |
|--------|----------|
| Image **rất nặng** | Còn nguyên Maven, JDK, cache dependency, mã nguồn — thường **hàng trăm MB → GB**, trong khi chạy app chỉ cần JRE + JAR |
| **Bề mặt tấn** rộng hơn | Tool build / compiler không cần có trên server runtime |
| Lộ thêm context | Source, `.git`, file tạm build dễ bị mang theo nếu không cẩn thận |
| Máy chạy / registry tốn kém | Pull chậm, lưu trữ tốn, cold start kém hơn |

**Ý tưởng multi-stage:** tách **hai giai trường** trong cùng một Dockerfile:

```text
Stage 1 — builder (nặng, tạm):  Maven + JDK  →  biên dịch ra JAR
Stage 2 — runtime (nhẹ, giữ lại): chỉ JRE + file JAR copy từ stage 1
```

Docker **chỉ giữ stage cuối** làm image kết quả (trừ khi bạn đặt tên/tag stage trung gian). Stage builder giống “xưởng lắp ráp”: dùng xong, **không giao xưởng cho khách** — chỉ giao sản phẩm (JAR).

```mermaid
flowchart LR
  subgraph s1 [Stage 1 — builder]
    Pom[pom.xml] --> Dep[dependency:go-offline]
    Dep --> Src[src]
    Src --> Jar[target/*.jar]
  end
  subgraph s2 [Stage 2 — runtime]
    Jre[eclipse-temurin:17-jre] --> App[app.jar]
  end
  Jar -->|COPY --from=builder| App
```

**Lợi ích cụ thể với Spring Boot Lab 2:**

| Lợi ích | Giải thích |
|---------|------------|
| Image runtime **nhỏ** | Chỉ JRE + fat JAR — đủ chạy `java -jar` |
| Build **tái lập** | Cùng Dockerfile trên máy HV, máy GV, CI → cùng cách biên dịch |
| Cache layer tốt | `COPY pom.xml` + `go-offline` **trước** `COPY src` → sửa Java không tải lại hết dependency |
| Tách biệt build / run | Server / máy chỉ chạy không cần cài Maven |
| An toàn hơn một chút | Ít tool thừa trong image cuối |

**So với Lab 1 (một stage):**

| | Lab 1 — 1 stage | Lab 2 — multi-stage |
|--|-----------------|---------------------|
| Ai build JAR? | Máy HV (`./mvnw package`) | Stage `builder` trong Docker |
| Image cần gì? | JRE + JAR | JRE + JAR (Maven **không** vào image cuối) |
| Khi nào đủ? | Học `build`/`run`, app nhỏ | App thật / CI / không muốn phụ thuộc JDK trên máy chạy |
| Đổi giá | Phải package trước mỗi lần đổi code | Lần build đầu chậm hơn (tải Maven image + dependency) |

> **Không phải lúc nào cũng bắt buộc multi-stage.** Lab 1 cố ý dùng 1 stage để HV nắm vòng lặp. Lab 2 chuyển multi-stage vì Mini Project + hướng làm việc gần team (build trong Docker).

### 2.2. Bản đồ công việc — multi-stage

| Bước | File / lệnh | Tạo / Cập nhật | Việc HV làm |
|------|-------------|----------------|-------------|
| 1 | `.dockerignore` | **Tạo** | Ignore `target/`, IDE, `sample-data/`… |
| 2 | `Dockerfile` | **Tạo** | Stage `builder` (Maven) + stage JRE |
| 3 | Terminal | — | `docker build -t movie-portal:1.0 .` (trong `java-springboot-bai7`) |
| 4 | (Tuỳ chọn) | — | `docker run` một mình → **fail Mongo** nếu URI `localhost` — cầu nối Lab 2 §3 |

### 2.3. `.dockerignore` (Lab 2)

```gitignore
target/
.idea/
.vscode/
*.iml
.git/
.gitignore
sample-data/
scripts/
```

`target/` ignore được vì **Maven build JAR trong image**. Không bắt buộc `package` trước `docker build` (khác Lab 1).

### 2.4. Dockerfile multi-stage (khớp demo)

```dockerfile
# --- Stage 1: build JAR (xưởng — không phải image cuối) ---
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: chạy (image kết quả — chỉ JRE + JAR) ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Lệnh | Việc |
|------|------|
| `FROM … AS builder` | Đặt tên stage 1 = `builder` để stage 2 tham chiếu |
| `COPY pom.xml` rồi `go-offline` | Cache dependency khi chỉ sửa Java |
| `FROM eclipse-temurin:17-jre` | **Stage mới** — không kế thừa Maven từ stage 1 |
| `COPY --from=builder …` | Chỉ lấy JAR từ stage 1; bỏ hết JDK/Maven/source khỏi image cuối |

Không dùng `--no-cache` mặc định. Windows: CRLF / `chmod +x mvnw`.

```bash
cd demo-bai7-docker/lab2/java-springboot-bai7
docker build -t movie-portal:1.0 .
```

Có thể so sánh kích thước (sau khi đã từng build Lab 1 / Lab 2):

```bash
docker images hello-docker movie-portal
# Image multi-stage runtime gần với Lab 1 (JRE+JAR), dù quá trình build có Maven
```

---

## 3. Vì sao `localhost` Mongo hỏng? *(kiến thức mới — đọc kỹ)*

URI mặc định trong `application.properties`:

```properties
spring.data.mongodb.uri=mongodb://root:DBVWiYdDoMnfWmK@localhost:27017/db_java_t3h_module3_bai_7?authSource=admin
```

- `./mvnw spring-boot:run`: `localhost` = **máy HV** (Mongo cài trên máy / `docker run` map `:27017` như phần chung §3.2)
- Trong container app: `localhost` = **chính container app** — không có process `mongod`

**Mỗi container có localhost riêng.** Muốn app nói chuyện với Mongo container → **Docker network** + hostname = **tên service** (Compose: `mongo`).

| Cách | Khi nào | Host trong URI |
|------|---------|----------------|
| `host.docker.internal` | Mongo đã có trên máy (TH2 Compose) | Thay `localhost` |
| Mongo **cùng network** Compose | Máy chưa có Mongo (TH1 Compose) | Tên service `mongo` |
| Atlas | Phụ lục | `…mongodb.net` |

**Không bake mật khẩu vào image.** Spring Boot đọc biến môi trường `SPRING_DATA_MONGODB_URI` và **ghi đè** property (relaxed binding). Đổi URI **không** cần rebuild image.

```bash
docker run --name movie-portal -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI='mongodb://root:DBVWiYdDoMnfWmK@mongo:27017/db_java_t3h_module3_bai_7?authSource=admin' \
  movie-portal:1.0
```

Tên `mongo` chỉ resolve khi **cùng Docker network** → Compose (Lab 2 §4). Chạy image một mình lúc này thường **fail Mongo** — đúng bài học.

---

## 4. Docker Compose — hai trường hợp Mongo

> Chọn **một** trong hai. Không `up` cả hai file cùng lúc (cùng `:8080`).

| | **TH1 — Chưa có MongoDB** | **TH2 — Đã có MongoDB trên máy** |
|--|---------------------------|----------------------------------|
| File demo | `docker-compose.yml` | `docker-compose.host-mongo.yml` |
| Services | `app` + `mongo` | chỉ `app` |
| Host trong URI | `mongo` (tên service Compose) | `host.docker.internal` |
| Port `:27017` | Compose map ra máy | Dùng Mongo sẵn — không map thêm |
| Import CSV | `lab2/scripts/import-movies.sh` → container | Script/Compass trên máy host |
| Khi nào dùng | Máy HV chưa cài Mongo / muốn lab “full stack” | Đã có Mongo (M3) hoặc `docker run mongo` §3.2 |

### 4.1. Bản đồ công việc — Compose

| Bước | File / lệnh | Tạo / Cập nhật | Việc HV làm |
|------|-------------|----------------|-------------|
| 1 | Chọn TH1 hoặc TH2 | — | Xem bảng trên |
| 2 | `docker-compose.yml` và/hoặc `docker-compose.host-mongo.yml` | **Tạo** | Theo trường hợp |
| 3 | Terminal | — | Tắt `:8080`; TH1 còn cần trống `:27017` |
| 4 | Terminal | — | `compose up --build` (đúng file) |
| 5 | Import CSV | **Chạy** | TH1: script vào container · TH2: import trên host |
| 6 | Trình duyệt | — | `/movies`, login `admin`/`admin123`, `/admin/dashboard` |
| 7 | Terminal | — | `compose down` (TH1: `-v` nếu muốn xoá volume) |

### 4.2. TH1 — Chưa có MongoDB (`docker-compose.yml`)

Đặt ở `demo-bai7-docker/lab2`. User/password: `root` / `DBVWiYdDoMnfWmK`.

```yaml
services:
  mongo:
    image: mongo:7
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: DBVWiYdDoMnfWmK
    volumes:
      - mongo-data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--quiet", "--eval", "db.adminCommand('ping')"]
      interval: 5s
      timeout: 5s
      retries: 10
      start_period: 10s

  app:
    build:
      context: ./java-springboot-bai7
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://root:DBVWiYdDoMnfWmK@mongo:27017/db_java_t3h_module3_bai_7?authSource=admin
    depends_on:
      mongo:
        condition: service_healthy

volumes:
  mongo-data:
```

| Chi tiết | Ý nghĩa |
|----------|---------|
| Hostname `mongo` | DNS nội bộ Compose = tên service |
| `volumes: mongo-data` | Persist khi `compose down` (mất khi `down -v`) |
| `healthcheck` + `condition: service_healthy` | App chờ Mongo **ready** |
| `SPRING_DATA_MONGODB_URI` | Ghi đè `application.properties` |

```bash
cd demo-bai7-docker/lab2
docker compose up --build -d
chmod +x scripts/import-movies.sh && ./scripts/import-movies.sh
docker compose restart app
```

### 4.3. TH2 — Đã có MongoDB (`docker-compose.host-mongo.yml`)

Mongo đã chạy trên máy (native hoặc container map `:27017`). **Không** tạo service `mongo` trong Compose — tránh đụng port / hai Mongo.

```yaml
services:
  app:
    build:
      context: ./java-springboot-bai7
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    extra_hosts:
      - "host.docker.internal:host-gateway"
    environment:
      SPRING_DATA_MONGODB_URI: mongodb://root:DBVWiYdDoMnfWmK@host.docker.internal:27017/db_java_t3h_module3_bai_7?authSource=admin
```

| Chi tiết | Ý nghĩa |
|----------|---------|
| `host.docker.internal` | Từ trong container trỏ về **máy HV** (Docker Desktop) |
| `extra_hosts: host-gateway` | Linux / Compose v2 — tương đương host |
| Không service `mongo` | Dùng Mongo đã có; chỉnh URI nếu user/pass/DB khác |

Dataset: import trên host (Compass, hoặc `java-springboot-bai7/scripts/import-movies.sh`).

```bash
cd demo-bai7-docker/lab2
docker compose -f docker-compose.host-mongo.yml up --build -d
docker compose -f docker-compose.host-mongo.yml logs -f app
docker compose -f docker-compose.host-mongo.yml down
```

> Nhắc lại Lab 2 §3: trong container, `localhost` ≠ Mongo trên máy. TH1 dùng tên service `mongo`; TH2 dùng `host.docker.internal`.

### 4.4. Import CSV (theo trường hợp)

Ôn CSV / `@Field`: [Bài 7 M3 §3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md).

**TH1** — vào Mongo container:

```bash
./scripts/import-movies.sh
# file nhỏ: ./scripts/import-movies.sh java-springboot-bai7/sample-data/mymoviedb-sample.csv
```

**TH2** — vào Mongo trên máy:

```bash
cd java-springboot-bai7
./scripts/import-movies.sh
```

Seeder admin: `admin` / `admin123` — [README Bài 10](../../../t3h-ltv-java-module-3/demo-bai10-mini-project/README.md).

### 4.5. Checklist Lab 2 xong

| URL | Ai | Kỳ vọng |
|-----|-----|---------|
| http://localhost:8080/movies | Khách | Public (đã import `mymoviedb`) |
| http://localhost:8080/login | — | Form login |
| http://localhost:8080/admin/dashboard | `admin` / `admin123` | Dashboard D1–D6 |

```bash
# TH1
docker compose logs -f app
docker compose down          # giữ volume
docker compose down -v       # xoá data lab

# TH2
docker compose -f docker-compose.host-mongo.yml down
```

Tắt Lab 1 (`hello-lab1`) trước nếu còn chiếm `:8080`.

---

# Cuối bài — dùng chung hai lab

## Lỗi thường gặp

| Hiện tượng | Hay gặp ở | Hướng xử lý |
|------------|-----------|-------------|
| `COPY target/*.jar` fail | Lab 1 | Chưa `./mvnw package`; `.dockerignore` nuốt `target/` |
| API 8080 không vào | Lab 1–2 | App IDE còn chạy; quên `-p` / `ports` |
| `Connection refused` Mongo | Lab 2 | URI `localhost`; sai tên service; Mongo chưa healthy |
| Port `27017` bind fail | Lab 2 TH1 / §3.2 | Mongo local đang chạy — dùng **TH2** (`host-mongo`) hoặc `docker stop` Mongo cũ |
| Sai file Compose / URI | Lab 2 | TH1 cần host `mongo`; TH2 cần `host.docker.internal` — không nhầm file |
| Build fail `mvnw` | Lab 2 | CRLF Windows; thiếu `chmod +x` |
| Đổi URI không có hiệu lực | Lab 2 | Quên `environment` / `-e`; không cần rebuild nếu chỉ đổi env |
| `/movies` trống | Lab 2 | Chưa `./scripts/import-movies.sh`; sai DB/volume |
| Login admin fail | Lab 2 | Volume mới, seeder chưa chạy — xem `logs app` |
| Build chậm | Lab 2 | `--no-cache`; COPY `src` trước `pom.xml` |
| Không hiểu vì sao image vẫn nhỏ dù có Maven trong Dockerfile | Lab 2 | Maven nằm ở **stage builder**; image cuối chỉ stage JRE — xem Lab 2 §2.1 |

---

## Nâng cao (đọc hiểu — không lab)

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

**Phần chung §3.2**

4. Chạy Mongo **không** mật khẩu rồi **có** mật khẩu; viết đúng hai URI Spring tương ứng.  
5. (Tuỳ chọn) Chạy MySQL có mật khẩu, `SHOW DATABASES` bằng `docker exec`.

**Lab 2**

6. Nêu **ba lý do** dùng multi-stage thay vì một stage Maven+JDK để chạy app.  
7. Vẽ: trình duyệt → port host → app → hostname `mongo`.  
8. `compose down` không `-v` rồi `up`: data còn không? Thử `-v` (lab).  
9. (Tuỳ chọn) `docker compose logs app` khi lỗi URI.

---

## Phụ lục C — Checklist

**Phần chung**

- [ ] `docker version` + `hello-world`  
- [ ] `docker run` Mongo (không MK và/hoặc có MK)  
- [ ] (Tuỳ chọn) `docker run` MySQL  

**Lab 1**

- [ ] App không DB, 3 API tĩnh chạy trên máy (`demo-bai7-docker/lab1`)  
- [ ] `./mvnw package` + Dockerfile 1 stage  
- [ ] `docker build -t hello-docker:1.0 .`  
- [ ] `docker run -p 8080:8080` · `curl /api/v1/hello` và `/courses`  
- [ ] `ps` / `logs` / `stop` / `rm`

**Lab 2**

- [ ] Dùng / hiểu bản **copy** Mini Project trong `demo-bai7-docker/lab2` (không sửa repo M3)  
- [ ] Giải thích được **vì sao multi-stage**  
- [ ] Dockerfile **multi-stage** build OK  
- [ ] Giải thích được fail `localhost` Mongo  
- [ ] Chọn đúng Compose: **TH1** (chưa Mongo) hoặc **TH2** (đã có Mongo)  
- [ ] `docker compose up` theo file đã chọn  
- [ ] Import CSV đúng chỗ (container vs host)  
- [ ] `/movies` + login admin  
- [ ] URI qua env, không bake secret  

---

## Phụ lục D — Liên kết

- Docker Desktop: <https://www.docker.com/products/docker-desktop/>  
- Dockerfile / multi-stage: <https://docs.docker.com/build/building/multi-stage/>  
- Compose: <https://docs.docker.com/compose/>  
- Image Mongo: <https://hub.docker.com/_/mongo> · MySQL: <https://hub.docker.com/_/mysql>  
- Temurin: <https://hub.docker.com/_/eclipse-temurin>  
- Spring Boot Externalized Config: <https://docs.spring.io/spring-boot/reference/features/external-config.html>  
- Demo gốc: [`demo-bai7-docker`](../../demo-bai7-docker)  
- Demo Lab 1: [`demo-bai7-docker/lab1`](../../demo-bai7-docker/lab1)  
- Demo Lab 2: [`demo-bai7-docker/lab2`](../../demo-bai7-docker/lab2)  
- [M4 Bài 1 REST](./1_java_m4_bai1_RESTful_API.md) · [M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) · [M3 Bài 3 URI](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) · [M3 Bài 7 import](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai7_Database_Query_To_FrontEnd.md)
