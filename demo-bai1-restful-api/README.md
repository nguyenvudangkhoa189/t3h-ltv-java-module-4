# Demo Bài 1 — REST design, MapStruct / ModelMapper, `@Async`, Logging, springdoc

Project demo cho syllabus [`java_m4_bai1_RESTful_API.md`](../syllabus/module-4/java_m4_bai1_RESTful_API.md).

App **in-memory** (không Mongo/JPA) — tập trung kiến thức mới Module 4 Bài 1:

| Phần | Class / path | Syllabus |
|------|----------------|----------|
| Version `/api/v1` + CRUD + search/page | `EmployeeRestController` | §1, §5, §6 |
| MapStruct (chuẩn) | `EmployeeMapper` + `EmployeeService` | §2.3 |
| ModelMapper (so sánh) | `EmployeeModelMapperService` + `ModelMapperCompareController` | §2.2 |
| `@Async` + 202 | ``async/WelcomeEmailService` + `POST .../welcome` | §3 |
| Sync chậm (so sánh) | `POST .../welcome-sync` | §3.3.1 |
| Logging | `logging.level.vn.demo` + log trong Service | §4 |
| springdoc | Swagger UI + `@Tag` / `@Operation` | §5 |
| Exception | `RestExceptionHandler` (404/400/409) | ôn M3 |

## Yêu cầu

- JDK 17+
- **Không cần** MongoDB / Docker

## Chạy app

```bash
cd demo-bai1-restful-api/java-springboot-bai1
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Seed sẵn 3 employee (`a@company.com`, `b@company.com`, `c@company.com`).

## API nhanh

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/v1/employees?keyword=&role=&page=0&size=10&sort=name,asc` | Search + pagination |
| GET | `/api/v1/employees/{id}` | Chi tiết |
| POST | `/api/v1/employees` | Tạo (201) — MapStruct |
| PUT | `/api/v1/employees/{id}` | Cập nhật |
| DELETE | `/api/v1/employees/{id}` | Xóa (204) |
| POST | `/api/v1/employees/{id}/welcome` | `@Async` → **202** |
| POST | `/api/v1/employees/{id}/welcome-sync` | Sync chậm ~3s → 200 (lab so sánh) |
| GET | `/api/v1/employees/compare/model-mapper/{id}` | Lab ModelMapper |
| POST | `/api/v1/employees/compare/model-mapper` | Lab ModelMapper create |

```bash
# Search developers
curl -s "http://localhost:8080/api/v1/employees?role=developer&page=0&size=5" | python3 -m json.tool

# Welcome async — trả 202 ngay; xem log thread async-*
curl -s -o /dev/null -w "%{http_code}\n" -X POST "http://localhost:8080/api/v1/employees/1/welcome"
```

## Chạy test

```bash
./mvnw test
```

## Cấu trúc (theo kiến thức bài học)

| Package | Mục | Class chính |
|---------|-----|-------------|
| `employee/` | §1 REST + CRUD | `EmployeeRestController`, `EmployeeService` |
| `mapping/` | §2 MapStruct / ModelMapper | `EmployeeMapper`, `ModelMapperCompareController` |
| `async/` | §3 `@Async` | `AsyncConfig`, `WelcomeEmailService` |
| `logging/` | §4 Logging | `HttpRequestLoggingFilter` |
| `openapi/` | §5 springdoc | `OpenApiConfig` |
| `exception/` | ôn M3 | `RestExceptionHandler` |
| `config/` | seed | `DataSeeder` |

```
src/main/java/vn/demo/
├── DemoBai1RestApplication.java
├── employee/     model, dto, repository, service, controller
├── mapping/      EmployeeMapper, MapperConfig, ModelMapper* 
├── async/        AsyncConfig, WelcomeEmailService
├── logging/      HttpRequestLoggingFilter, package-info
├── openapi/      OpenApiConfig
├── exception/
└── config/       DataSeeder
```

## Liên kết

- Syllabus: [`java_m4_bai1_RESTful_API.md`](../syllabus/module-4/java_m4_bai1_RESTful_API.md)
- Ôn Module 3: REST + DTO + `PagedResponse` (Bài 3)
