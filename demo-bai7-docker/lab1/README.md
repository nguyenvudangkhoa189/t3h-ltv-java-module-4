# Demo Bài 7 — Lab 1: Docker (API tĩnh, không DB)

Nằm trong [`demo-bai7-docker`](../) · syllabus [`6_java_m4_bai7_Docker.md`](../../syllabus/module-4/6_java_m4_bai7_Docker.md) — **Lab 1**.

App Spring Boot **chỉ Web**: 3 REST API với dữ liệu cố định trong code. Mục tiêu: quen `Dockerfile` một stage, `docker build`, `docker run -p`.

> Lab 1: [`../lab1/`](../lab1/)

## Yêu cầu

- JDK 17+
- Docker Desktop
- **Không cần** MongoDB

## Cấu trúc

```
demo-bai7-docker/
└── lab1/
    ├── README.md
    └── java-springboot-hello/
        ├── Dockerfile              ← 1 stage (COPY jar + JRE)
        ├── .dockerignore
        ├── pom.xml
        └── src/...
```

| File / class | Vai trò |
|--------------|---------|
| `DemoBai7Lab1Application` | Entry Spring Boot |
| `HelloController` | 3 API `/api/v1/...` |
| `CourseDto` | Record DTO |
| `Dockerfile` | Image runtime JRE |

## Chạy trên máy (trước Docker)

```bash
cd demo-bai7-docker/lab1/java-springboot-hello
./mvnw spring-boot:run
```

| Method | URL | Kỳ vọng |
|--------|-----|---------|
| GET | http://localhost:8080/api/v1/hello | `{"message":"Docker Lab 1"}` |
| GET | http://localhost:8080/api/v1/courses | Mảng 3 khoá học |
| GET | http://localhost:8080/api/v1/courses/1 | Một course |
| GET | http://localhost:8080/api/v1/courses/99 | 404 |

```bash
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses
```

Tắt app (Ctrl+C) trước khi `docker run`.

## Docker — Lab 1

```bash
cd demo-bai7-docker/lab1/java-springboot-hello

./mvnw package -DskipTests
docker build -t hello-docker:1.0 .

# Chạy nền (-d): tắt Terminal không tắt container
docker run -d --name hello-lab1 -p 8080:8080 hello-docker:1.0
```

```bash
curl -s http://localhost:8080/api/v1/hello
curl -s http://localhost:8080/api/v1/courses/1
docker logs hello-lab1
docker stop hello-lab1 && docker rm hello-lab1
```

> Không có `-d`: container gắn Terminal — đóng Terminal = container dừng. Có `--rm` thì còn bị xoá luôn.

Port 8080 bận: `-p 8081:8080`.

## Test

```bash
./mvnw test
```

## Liên kết

- Syllabus: [`6_java_m4_bai7_Docker.md`](../../syllabus/module-4/6_java_m4_bai7_Docker.md) — Lab 1
- Lab 2: [`../lab2/`](../lab2/)
