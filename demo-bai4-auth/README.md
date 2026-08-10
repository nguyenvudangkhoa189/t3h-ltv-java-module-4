# Demo Bài 4 — Authentication & Authorization (JWT + RBAC Permission)

Project demo cho syllabus [`4_java_m4_bai4_Authentication_Authorization.md`](../syllabus/module-4/4_java_m4_bai4_Authentication_Authorization.md).

REST API **stateless**: login trả JWT (chỉ identity) → mỗi request Bearer → filter **nạp Permission từ Mongo** → `@PreAuthorize("hasAuthority('…')")`.

Khác [M3 Bài 10](../../t3h-ltv-java-module-3/demo-bai10-mini-project) (form login + session + `hasRole`).

## Yêu cầu

- JDK 17+
- MongoDB đang chạy (URI trong `application.properties`)

> Nếu đã chạy lab schema cũ: **drop** database `db_java_t3h_module4_bai4` rồi start lại để seed mới.

## Chạy

```bash
cd demo-bai4-auth/java-springboot-bai4
./mvnw spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui/index.html  
  Login → copy `token` → **Authorize** → dán token (không cần chữ `Bearer `).
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Lần đầu `RbacDataSeeder` tạo permissions / roles / 3 user (BCrypt).

| Username | Password | Role | Permissions chính |
|----------|----------|------|-------------------|
| `admin` | `admin123` | ADMIN | VIEW+CREATE+UPDATE+DELETE+ASSIGN_ROLE |
| `editor` | `editor123` | EDITOR | VIEW+CREATE+UPDATE |
| `alice` | `user123` | USER | chỉ VIEW |

## API

| Method | URL | Auth | Mô tả |
|--------|-----|------|--------|
| POST | `/api/auth/login` | Không | `{username,password}` → `{token}` · sai → **401** |
| GET | `/api/auth/me` | Bearer | `{id, username, email, roles, permissions}` |
| GET | `/api/users` | `USER_VIEW` | Danh sách |
| GET | `/api/users/{id}` | `USER_VIEW` | Chi tiết |
| POST | `/api/users` | `USER_CREATE` | Tạo · **201** |
| PUT | `/api/users/{id}` | `USER_UPDATE` | Cập nhật |
| DELETE | `/api/users/{id}` | `USER_DELETE` | Xóa · **204** · Alice/Editor → **403** |
| POST | `/api/users/{id}/roles` | `USER_ASSIGN_ROLE` | Body `{roleCode}` · chỉ Admin |
| DELETE | `/api/users/{id}/roles/{roleCode}` | `USER_ASSIGN_ROLE` | Gỡ role · chỉ Admin |

```bash
# Login alice
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"user123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

curl -s http://localhost:8080/api/auth/me -H "Authorization: Bearer $TOKEN"

# Alice xem users → 200
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"

# Alice xóa → 403
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE http://localhost:8080/api/users/any-id \
  -H "Authorization: Bearer $TOKEN"
```

## Chạy test

```bash
./mvnw test
```

`JwtServiceTest` không cần Mongo — kiểm JWT chỉ chứa `sub` + `username`.

## Quy ước code (Module 3 / M4)

- Package `vn.demo`; Model suffix `Model`; `Controller → Service → Repository`
- Constructor injection + Lombok `@RequiredArgsConstructor`
- Comment block `// --- ... ---` trong bước quan trọng
- Kiến thức mới (JWT tối giản, load Permission mỗi request, `hasAuthority`): Javadoc chi tiết
- Password: BCrypt; secret JWT qua `app.jwt.secret` / env `JWT_SECRET`

## File chính

| Nhóm | Class |
|------|--------|
| Config | `SecurityConfig`, `OpenApiConfig`, `JwtProperties`, `RbacDataSeeder` |
| Security | `JwtService`, `JwtAuthenticationFilter`, `AuthUserPrincipal` |
| Model | `UserModel`, `RoleModel`, `PermissionModel` |
| API | `AuthController`, `UserController` |
| Service | `AuthService`, `UserService`, `PermissionLoader`, `SecurityUtils` |

## Liên kết

- Syllabus: [`4_java_m4_bai4_Authentication_Authorization.md`](../syllabus/module-4/4_java_m4_bai4_Authentication_Authorization.md)
- Spec gốc: [`module4-bai4-authen-and-autho.md`](../syllabus/pdf/module4-bai4-authen-and-autho.md)
- Ôn form/session/BCrypt: [M3 Bài 10](../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)
