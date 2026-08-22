# Demo Bài 7 — Lab 2: Docker hóa Mini Project (multi-stage + Compose)

Nằm trong [`demo-bai7-docker`](../) · syllabus [`6_java_m4_bai7_Docker.md`](../../syllabus/module-4/6_java_m4_bai7_Docker.md) — **Lab 2**.

**Nguồn:** copy từ Module 3 [`demo-bai10-mini-project`](../../../t3h-ltv-java-module-3/demo-bai10-mini-project) — **không đổi nghiệp vụ**. Chỉ thêm Docker.

> Lab 1: [`../lab1/`](../lab1/)

## Yêu cầu

- JDK 17+ (khi `./mvnw spring-boot:run` trên máy)
- Docker Desktop
- Làm **sau** Lab 1

## Cấu trúc

```
demo-bai7-docker/
└── lab2/
    ├── README.md
    ├── docker-compose.yml                 ← TH1: chưa có Mongo (app + mongo)
    ├── docker-compose.host-mongo.yml      ← TH2: đã có Mongo trên máy (chỉ app)
    ├── scripts/import-movies.sh           ← import vào Mongo container (TH1)
    └── java-springboot-bai7/
        ├── Dockerfile                     ← multi-stage
        ├── scripts/import-movies.sh       ← import vào Mongo localhost (TH2 / máy)
        ├── sample-data/
        └── src/...
```

## Chọn đúng trường hợp Compose

| | **TH1 — Chưa có MongoDB** | **TH2 — Đã có MongoDB** |
|--|---------------------------|-------------------------|
| File | `docker-compose.yml` | `docker-compose.host-mongo.yml` |
| Services | `app` + `mongo` | chỉ `app` |
| Host trong URI | `mongo` (tên service) | `host.docker.internal` |
| Port `:27017` | Compose map ra máy | Dùng Mongo sẵn trên máy — **không** map thêm |
| Import CSV | `./scripts/import-movies.sh` (vào container) | `java-springboot-bai7/scripts/import-movies.sh` hoặc Compass / script M3 |

> Hai file **không** `up` cùng lúc nếu cùng chiếm `:8080`. Chọn **một** trường hợp.

---

## Chạy trên máy (không Docker app)

```bash
cd demo-bai7-docker/lab2/java-springboot-bai7
./mvnw spring-boot:run
```

Cần Mongo local + URI trong `application.properties`. Tài khoản admin: `admin` / `admin123`.

| URL | Ai |
|-----|-----|
| http://localhost:8080/movies | Khách |
| http://localhost:8080/login | — |
| http://localhost:8080/admin/dashboard | ADMIN |

---

## TH1 — Chưa có MongoDB (`docker-compose.yml`)

Tắt process chiếm `:8080` / `:27017` (nếu có).

```bash
cd demo-bai7-docker/lab2
docker compose up --build -d
```

Import CSV vào **Mongo container**:

```bash
chmod +x scripts/import-movies.sh
./scripts/import-movies.sh
# file nhỏ: ./scripts/import-movies.sh java-springboot-bai7/sample-data/mymoviedb-sample.csv
docker compose restart app
```

```bash
docker compose logs -f app
docker compose down          # giữ volume mongo-data
docker compose down -v       # xoá luôn data lab
```

**Vì sao URI dùng `mongo`?** Trong container app, `localhost` = chính container đó. Service Mongo trong Compose có hostname = tên service `mongo`.

---

## TH2 — Đã có MongoDB (`docker-compose.host-mongo.yml`)

Mongo đang chạy trên máy (cài native, hoặc `docker run … -p 27017:27017` như phần chung syllabus §3.2). Dataset `mymoviedb` nên đã có (import bằng script trong `java-springboot-bai7/scripts/` hoặc Compass).

Chỉnh URI trong `docker-compose.host-mongo.yml` nếu user/password/DB khác máy bạn.

```bash
cd demo-bai7-docker/lab2
docker compose -f docker-compose.host-mongo.yml up --build -d
```

```bash
docker compose -f docker-compose.host-mongo.yml logs -f app
docker compose -f docker-compose.host-mongo.yml down
```

**Vì sao `host.docker.internal`?** App trong container không thấy Mongo qua `localhost`. `host.docker.internal` trỏ về máy HV (Docker Desktop; Linux có `extra_hosts: host-gateway` trong file).

Import trên máy (Mongo host), ví dụ:

```bash
cd java-springboot-bai7
chmod +x scripts/import-movies.sh
./scripts/import-movies.sh
```

---

## Chỉ build image app (không Compose)

```bash
cd java-springboot-bai7
docker build -t movie-portal:1.0 .
```

## Liên kết

- Syllabus Lab 2: [`6_java_m4_bai7_Docker.md`](../../syllabus/module-4/6_java_m4_bai7_Docker.md)
- Lab 1: [`../lab1/`](../lab1/)
- Module 3 Bài 10: [syllabus](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)
