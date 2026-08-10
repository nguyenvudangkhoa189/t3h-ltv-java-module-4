# Bài 4: Authentication & Authorization — JWT, Spring Security API & RBAC + Permission

## Mục tiêu bài học

Sau bài này, học viên có thể:

- Ôn lại **Authentication / Authorization**, BCrypt, Bearer đủ để làm lab API
- Phân biệt **stateful** (session) và **stateless** (JWT)
- Hiểu mô hình **RBAC + Permission**: User → Role → Permission; Role chỉ là nhóm quyền
- Biết vì sao JWT **chỉ mang identity** (`sub`, `username`) và **Permission được nạp từ Mongo mỗi request**
- Dùng **`hasAuthority(...)`** / `@PreAuthorize` — **không** dùng `hasRole(...)` để check API
- Phân biệt đúng **401 Unauthorized** và **403 Forbidden**
- Hiện thực lab: `POST /api/auth/login`, `GET /api/auth/me`, CRUD `/api/users` (+ gán / gỡ role)
- Dùng **Swagger UI** (springdoc) để login lấy JWT và **Authorize** thử API có/không đủ Permission

> **Không nằm trong phạm vi:** OAuth2 / OIDC; Keycloak; refresh token chi tiết; CRUD Role/Permission API; PRODUCT_/ORDER_; JWKS / RS256; ABAC.  
> Mục **Mở rộng**: tự đọc — không bắt buộc thi / lab.

## Điều kiện tiên quyết

- **[M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) §4** — Authn vs Authz, form login, session, BCrypt, `hasRole`, `SecurityFilterChain` (SSR)
- **[M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md)** — header `Authorization: Bearer`
- **[Bài 2](./java_m4_bai2_Microservices.md) §3.2** — JWT ở mức hình dung giữa các service
- **[Bài 1](./1_java_m4_bai1_RESTful_API.md)** — REST, DTO, HTTP status cơ bản
- *(Khuyến khích)* **[M3 Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md)** — quan hệ document (reference bằng id)
- JDK 17+, Spring Boot 3.x, Maven, MongoDB

```xml
<!-- Đã quen Module 3 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MỚI — JWT (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- MỚI — springdoc OpenAPI + Swagger UI (test JWT trên trình duyệt) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.13</version>
</dependency>
```

> **Project tham chiếu:** `[demo-bai4-auth](../../demo-bai4-auth)` — [README](../../demo-bai4-auth/README.md).  
> Spec gốc (tham khảo): `[module4-bai4-authen-and-autho.md](../pdf/module4-bai4-authen-and-autho.md)`.  
> Ôn springdoc cơ bản: **[Bài 1](./1_java_m4_bai1_RESTful_API.md)** §5.

### Thời lượng gợi ý


| Phần                                                         | Thời gian    |
| ------------------------------------------------------------ | ------------ |
| **Phần 1** — Giới thiệu & ôn kiến thức                       | ~20–25 phút  |
| **Phần 2** — Stateful/Stateless + cơ chế RBAC + **§2.7 PreAuthorize** | ~35–40 phút  |
| **Phần 3** — Lab: Auth + User (tính năng 1 → 7)              | ~90–120 phút |
| Lỗi thường gặp + checklist nộp                               | ~10–15 phút  |


## Nội dung (làm theo thứ tự)


| #   | Phần   | Chủ đề                                        | Kết quả kiểm tra                                      |
| --- | ------ | --------------------------------------------- | ----------------------------------------------------- |
| 1   | Phần 1 | Authn, Authz, BCrypt, Bearer                  | Phân biệt Authn vs Authz; biết BCrypt & Bearer        |
| 2   | Phần 2 | Session vs JWT; RBAC; **§2.7 JWT → PreAuthorize** | Nói được Role ≠ Permission; JWT chỉ identity; mô tả được `hasAuthority` |
| 3   | Phần 3 | Lab — tổng quan + tính năng 1 → 7             | Checklist cuối bài đạt                                |


---

# Phần 1 — Giới thiệu và ôn lại kiến thức

> Các khái niệm dưới đây **đã học** ở Module 3. Auth thường khó nên bài này **giới thiệu lại đủ để theo lab**, không đào sâu form login / Thymeleaf.  
> Cuối mỗi mục có dòng **Đã học chi tiết tại…**.

## 1.1. Authentication — *Bạn là ai?*

**Authentication** là quá trình xác minh danh tính của người dùng.

Ví dụ: đăng nhập username + mật khẩu → đối chiếu dữ liệu đã lưu → đúng thì **đã xác thực**.

Trong lab API: `POST /api/auth/login` thành công → client nhận **JWT** (access token).

> **Đã học chi tiết tại:** [M3 Bài 10 §4.1](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md).

## 1.2. Authorization — *Bạn được làm gì?*

**Authorization** quyết định sau khi đã biết *ai* đang gọi: được phép thực hiện hành động nào trên tài nguyên nào.

Ví dụ lab:

- Alice (USER) được **xem** danh sách user (`USER_VIEW`) nhưng **không** được xóa (`USER_DELETE`)
- Editor được tạo / sửa user nhưng không xóa
- Admin được mọi quyền quản lý user (+ gán role)

> **Đã học chi tiết tại:** [M3 Bài 10 §4.2](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md).

## 1.3. BCrypt — hash mật khẩu

Không lưu mật khẩu plain text. Dùng `BCryptPasswordEncoder`:

- **Seed / đăng ký:** `encode(rawPassword)` → lưu hash vào Mongo
- **Login:** `matches(rawPassword, hashTrongDb)`

> **Đã học chi tiết tại:** [M3 Bài 10 §4.4](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md).

## 1.4. Bearer token trên REST

Client gửi:

```http
Authorization: Bearer <access_token>
```

- **M3 Bài 9:** Bearer thường là token **máy** (PayPal).
- **Bài này:** Bearer là **JWT của user** sau khi login.

> **Đã học chi tiết tại:** [M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md).

## 1.5. 401 vs 403 (ôn nhanh)


| Status               | Ý nghĩa                                      | Ví dụ lab                                      |
| -------------------- | -------------------------------------------- | ---------------------------------------------- |
| **401 Unauthorized** | Chưa xác thực / JWT thiếu·sai·hết hạn        | Gọi `/api/users` không Bearer                  |
| **403 Forbidden**    | Đã xác thực nhưng **thiếu Permission**       | Alice gọi `DELETE /api/users/{id}`             |


---

# Phần 2 — Stateful / Stateless và cơ chế RBAC + Permission

> Đây là phần **lý thuyết trọng tâm** trước khi code lab. Hiểu phần này rồi mới làm tính năng 3–5.  
> Đặc biệt đọc kỹ **§2.7** (JWT → load Permission → `@PreAuthorize`) trước khi viết filter và `UserController`.

## 2.1. Stateful (Session) vs Stateless (JWT)

| | **Stateful — Session (M3 Bài 10)** | **Stateless — JWT (bài này)** |
|---|-----------------------------------|-------------------------------|
| Sau login | Server tạo session, gửi cookie `JSESSIONID` | Server trả JWT; **không** lưu session |
| Request sau | Browser gửi cookie → server đọc session | Client gửi `Authorization: Bearer` |
| CSRF | Cần bảo vệ (form) | API JWT thường tắt CSRF |
| Scale | Session store / sticky session | Dễ scale ngang (token tự chứa identity) |

Lab này dùng **stateless**: mọi API (trừ login) cần Bearer JWT hợp lệ.

## 2.2. Mô hình RBAC + Permission — nguyên tắc

Lab dùng **RBAC + Permission** (theo spec bài học):

1. **User** có nhiều **Role** (`roleIds`)
2. **Role** có nhiều **Permission** (`permissionIds`)
3. **Permission** là **đơn vị kiểm tra quyền** (field `code`, ví dụ `USER_CREATE`)
4. **Không** dùng `hasRole(...)` trên Controller
5. **Chỉ** dùng `hasAuthority('USER_CREATE')` (và các code tương tự)
6. Role **chỉ** dùng để **nhóm** Permission — Role **không** tham gia trực tiếp vào câu lệnh phân quyền trên API

```mermaid
flowchart LR
  User -->|roleIds| Role
  Role -->|permissionIds| Permission
  Permission -->|code| Authority["GrantedAuthority"]
```

Ví dụ trong code:

```java
@PreAuthorize("hasAuthority('USER_CREATE')")
@PostMapping
public UserResponse create(...) { ... }
```

```java
@PreAuthorize("hasAuthority('USER_DELETE')")
@DeleteMapping("/{id}")
public void delete(@PathVariable String id) { ... }
```

### So với M3 Bài 10

| | M3 Bài 10 | Bài 4 (lab) |
|---|-----------|-------------|
| Check quyền | `hasRole("ADMIN")` → authority `ROLE_ADMIN` | `hasAuthority("USER_DELETE")` → đúng bằng `permission.code` |
| Nguồn quyền | Thường 1 field `role` trên user | Load Role → Permission từ Mongo |
| UI | Form + Thymeleaf | REST JSON |

## 2.3. Schema MongoDB (3 collection)

### `users`

```json
{
  "_id": "...",
  "username": "admin",
  "email": "admin@demo.local",
  "password": "<bcrypt>",
  "enabled": true,
  "roleIds": ["id_cua_role_ADMIN"],
  "createdAt": "...",
  "updatedAt": "..."
}
```

### `roles`

```json
{
  "_id": "...",
  "code": "ADMIN",
  "name": "Administrator",
  "description": "",
  "permissionIds": ["id_USER_VIEW", "id_USER_CREATE", "..."]
}
```

### `permissions`

```json
{
  "_id": "...",
  "code": "USER_CREATE",
  "name": "Create User",
  "description": ""
}
```

> **Bridge:** quan hệ **reference bằng id** — [M3 Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md). Lab không embed toàn bộ Permission vào User.

## 2.4. JWT chỉ mang identity — vì sao?

### Payload lab

```json
{
  "sub": "<userMongoId>",
  "username": "admin"
}
```

**Không** lưu Role hay Permission trong JWT.

### Mỗi request bảo vệ

1. Parse JWT → verify chữ ký + `exp`
2. Lấy `sub` → load **User** từ Mongo
3. Load **Roles** theo `roleIds`
4. Load **Permissions** theo `permissionIds` của các role
5. Convert mỗi `permission.code` → `new SimpleGrantedAuthority(code)`
6. Gắn `Authentication` vào `SecurityContext`
7. Controller / `@PreAuthorize` đọc authorities

> Chi tiết từng bước (hai lớp 401/403, code mẫu filter + PreAuthorize, ví dụ Admin vs Alice): **[§2.7](#27-từ-jwt-đến-preauthorize--cơ-chế-chi-tiết)**.

```mermaid
sequenceDiagram
  participant C as Client
  participant F as JwtFilter
  participant DB as MongoDB
  participant Ctrl as Controller
  C->>F: Bearer JWT
  F->>F: Verify signature and exp
  F->>DB: Load User by sub
  F->>DB: Load Roles then Permissions
  F->>F: Build GrantedAuthority list
  F->>Ctrl: SecurityContext
  Ctrl->>Ctrl: PreAuthorize hasAuthority
```

### Lợi ích quan trọng (nhớ câu này)

Đổi Permission gắn trên Role trong Mongo → **request kế tiếp đã có hiệu lực**, **không** cần user đăng nhập lại — vì token không “đóng băng” danh sách quyền.

### Đánh đổi

Mỗi request authenticated phải **đọc Mongo** (User + Role + Permission). Lab chấp nhận để học rõ cơ chế; production có thể cache ngắn hạn (ngoài phạm vi bài).

## 2.5. Luồng Authentication (login)

```text
Login (username/password)
  → Verify BCrypt
  → Load User
  → (tuỳ chọn) kiểm tra enabled
  → Generate JWT (chỉ sub + username)
  → Return Access Token
```

**Lưu ý:** lúc login **không bắt buộc** nhét Permission vào JWT. Permission chỉ cần khi **xử lý request** sau (filter).

## 2.6. GrantedAuthority trong lab

Sau khi load Permission, các code ví dụ:

```text
USER_VIEW
USER_CREATE
USER_UPDATE
USER_DELETE
USER_ASSIGN_ROLE
```

Convert:

```java
new SimpleGrantedAuthority(permission.getCode())
```

Đưa vào `Collection<? extends GrantedAuthority>` của principal.

> **Không** thêm tiền tố `ROLE_` vào permission code. `hasAuthority('USER_VIEW')` khớp đúng chuỗi `USER_VIEW`.  
> `hasRole('ADMIN')` của Spring tự tìm `ROLE_ADMIN` — **lab không dùng** kiểu check đó trên API User.

## 2.7. Từ JWT đến `@PreAuthorize` — cơ chế chi tiết

> **Vị trí giảng:** học phần này **trước** khi code tính năng 3 (filter) và tính năng 5 (`@PreAuthorize`).  
> Lab: filter = `[JwtAuthenticationFilter](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/security/JwtAuthenticationFilter.java)`; check API = `[UserController](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/controller/UserController.java)`.

Học viên hay hỏi: *“Annotation trên Controller kiểm tra quyền ở đâu? Token đâu có Permission?”*  
Trả lời ngắn: **JWT chỉ nói *ai*; filter nạp Permission vào `SecurityContext`; `@PreAuthorize` so khớp chuỗi authority đã nạp.**

### Hai lớp kiểm tra (đừng nhầm)

| Lớp | Nơi cấu hình | Câu hỏi | Kết quả nếu fail |
| --- | ------------ | ------- | ---------------- |
| **A — URL / Filter chain** | `SecurityConfig` → `authenticated()` | Đã xác thực chưa? (có JWT hợp lệ + đã set context) | **401** |
| **B — Method** | `@PreAuthorize("hasAuthority('…')")` trên Controller | Đã xác thực rồi — **có đúng Permission chưa?** | **403** |

Ví dụ `POST /api/users`:

1. Lớp A: thiếu / sai JWT → **401** (chưa vào method)
2. Lớp A OK → vào method; lớp B: không có `USER_CREATE` → **403** (method **không** chạy)
3. Cả hai OK → `UserService.create(...)` chạy

`@EnableMethodSecurity` trên `SecurityConfig` **bật** lớp B. Quên annotation này → `@PreAuthorize` **không chạy** (chỉ còn lớp A).

### Bước 1 — Client gửi request kèm JWT

```http
POST /api/users HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

JWT payload lab **chỉ** có identity, ví dụ:

```json
{ "sub": "65f1…", "username": "admin" }
```

**Không** có `USER_CREATE` trong token.

### Bước 2 — `JwtAuthenticationFilter`: verify JWT rồi **load Permission code**

Filter chạy **trước** Controller (đăng ký bằng `addFilterBefore(...)`):

1. Đọc header `Authorization`; không phải `Bearer …` → bỏ qua (không set context) → sau đó lớp A trả **401**
2. `JwtService.parseClaims(token)` — sai chữ ký / hết hạn → không set context → **401**
3. Lấy `sub` = user id → `userRepository.findById(sub)`
4. `PermissionLoader.loadPermissionCodes(user.roleIds)`:
   - `roleIds` → documents `roles`
   - gom `permissionIds` → documents `permissions`
   - lấy field **`code`** (`USER_VIEW`, `USER_CREATE`, …)
5. Tạo `AuthUserPrincipal` với:

```java
new SimpleGrantedAuthority("USER_CREATE")  // mỗi code một authority
```

6. Gắn vào `SecurityContextHolder`:

```java
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(authentication);
```

Sau bước này, “bộ nhớ tạm của request” đã biết: *user này là ai* + *được những Permission code nào*.

### Bước 3 — Spring gọi Controller; `@PreAuthorize` đọc authorities

Trên method:

```java
@PreAuthorize("hasAuthority('USER_CREATE')")
@PostMapping
public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
    return userService.create(request);
}
```

Khi có `@EnableMethodSecurity`, Spring bọc lời gọi method bằng **method security interceptor** (trước khi body method chạy):

1. Lấy `Authentication` hiện tại từ `SecurityContextHolder`
2. Đánh giá biểu thức SpEL: `hasAuthority('USER_CREATE')`
3. So khớp: trong `authentication.getAuthorities()`, có phần tử nào `.getAuthority()` **bằng đúng** `"USER_CREATE"` không?
4. **Có** → cho vào method  
   **Không** → `AccessDeniedException` → `AccessDeniedHandler` → JSON **403**

Controller **không** tự viết `if (roles.contains("ADMIN"))`. Annotation + authorities đã nạp từ Mongo quyết định.

### Sơ đồ gộp (filter → PreAuthorize)

```mermaid
sequenceDiagram
  participant C as Client
  participant F as JwtAuthenticationFilter
  participant DB as MongoDB
  participant SC as SecurityContext
  participant MS as MethodSecurity
  participant UC as UserController

  C->>F: POST /api/users + Bearer JWT
  F->>F: Verify signature and exp
  F->>DB: User by sub then Roles then Permissions
  F->>F: SimpleGrantedAuthority per permission.code
  F->>SC: setAuthentication principal plus authorities
  Note over SC: Lớp A authenticated OK
  F->>MS: Tiếp chain tới DispatcherServlet
  MS->>MS: hasAuthority USER_CREATE
  alt Thiếu USER_CREATE
    MS-->>C: 403 Forbidden
  else Đủ quyền
    MS->>UC: create method chạy
    UC-->>C: 201 Created
  end
```

### Ví dụ cụ thể: Admin vs Alice gọi `POST /api/users`

| | Admin | Alice (USER) |
|---|--------|--------------|
| JWT | Hợp lệ, `sub` = id admin | Hợp lệ, `sub` = id alice |
| Permission load từ Mongo | có `USER_CREATE` | **không** có `USER_CREATE` (chỉ `USER_VIEW`) |
| Lớp A `authenticated()` | OK | OK |
| `@PreAuthorize("hasAuthority('USER_CREATE')")` | khớp → vào method | không khớp → **403** |

Alice **đã login** (không phải 401) nhưng **thiếu Permission** → đúng nghĩa Authorization fail.

### Liên hệ class trong demo


| Bước | Class | Việc làm |
| ---- | ----- | -------- |
| Verify JWT | `JwtService` | Parse / chữ ký / `exp` |
| Load code | `PermissionLoader` | `roleIds` → permission `code` |
| Gói authorities | `AuthUserPrincipal` | `SimpleGrantedAuthority(code)` |
| Gắn context | `JwtAuthenticationFilter` | `SecurityContextHolder.setAuthentication` |
| Bật method security | `SecurityConfig` | `@EnableMethodSecurity` |
| Check trên API | `UserController` | `@PreAuthorize("hasAuthority('…')")` |


### Tóm tắt một câu (học thuộc)

**JWT chứng minh identity → Filter nạp Permission code vào authorities → `@PreAuthorize("hasAuthority('USER_CREATE')")` so khớp chuỗi đó trước khi chạy method.**

## 2.8. Quy tắc triển khai (Implementation Rules)

1. `BCryptPasswordEncoder` cho mật khẩu
2. JWT **chỉ** phục vụ Authentication (xác định *ai*)
3. Permission **luôn** nạp từ Mongo khi xử lý request có Bearer
4. Mọi kiểm tra phân quyền API User dùng `hasAuthority(...)` / `@PreAuthorize`
5. **Không** hardcode Role name trong Controller để quyết định được/không được gọi API
6. Role chỉ là tập Permission; check API dựa trên Permission
7. API trả JSON; 401/403 thống nhất
8. Nhớ bật `@EnableMethodSecurity` — nếu không, `@PreAuthorize` không có hiệu lực

---

# Phần 3 — Lab

## 3.0. Tổng quan lab

### 3.0.1. Mục tiêu lab

Xây REST API **stateless**:

1. Đăng nhập → nhận JWT (identity)
2. Gọi API kèm `Authorization: Bearer <token>`
3. Filter nạp Permission từ Mongo → `@PreAuthorize` trên User API

**Project tham chiếu:** `[demo-bai4-auth](../../demo-bai4-auth)`.

### 3.0.2. Các tính năng (theo thứ tự)


| #     | Tính năng                    | Học viên đạt được gì                                      |
| ----- | ---------------------------- | --------------------------------------------------------- |
| **1** | Chuẩn bị project             | Boot 3 + Java 17 + Mongo + JWT + springdoc deps           |
| **2** | Model + seed RBAC            | 3 collection; ADMIN / EDITOR / USER                       |
| **3** | JWT + Security (stateless)   | Filter load DB → authorities; `@EnableMethodSecurity`     |
| **4** | Auth API                     | `POST /api/auth/login`, `GET /api/auth/me`                |
| **5** | User API + PreAuthorize      | CRUD + assign/remove role                                 |
| **6** | Exception JSON               | 401 / 403 / 400 / 404 / 409                               |
| **7** | Swagger UI + JWT Authorize   | Mở UI công khai; nút Authorize dán token để Try it out    |


### 3.0.3. Ma trận Permission & Role

**Permissions (user-mgmt):**

| Code               | Ý nghĩa              |
| ------------------ | -------------------- |
| `USER_VIEW`        | Xem danh sách / chi tiết user |
| `USER_CREATE`      | Tạo user             |
| `USER_UPDATE`      | Cập nhật user        |
| `USER_DELETE`      | Xóa user             |
| `USER_ASSIGN_ROLE` | Gán / gỡ role        |


| Role   | Permissions |
| ------ | ----------- |
| ADMIN  | cả 5        |
| EDITOR | VIEW + CREATE + UPDATE (**không** DELETE, **không** ASSIGN_ROLE) |
| USER   | chỉ VIEW    |


### 3.0.4. Tài khoản seed


| User   | Password    | Role   |
| ------ | ----------- | ------ |
| admin  | `admin123`  | ADMIN  |
| editor | `editor123` | EDITOR |
| alice  | `user123`   | USER   |


### 3.0.5. Contract API


| Method | Path                         | Quyền                | Ghi chú                          |
| ------ | ---------------------------- | -------------------- | -------------------------------- |
| POST   | `/api/auth/login`            | public               | Body `{username,password}` → `{token}` |
| GET    | `/api/auth/me`               | authenticated        | `{id, username, email, roles, permissions}` |
| GET    | `/api/users`                 | `USER_VIEW`          | Danh sách                        |
| GET    | `/api/users/{id}`            | `USER_VIEW`          | Chi tiết                         |
| POST   | `/api/users`                 | `USER_CREATE`        | Tạo user (kèm roleCodes tùy chọn) |
| PUT    | `/api/users/{id}`            | `USER_UPDATE`        | Cập nhật email / enabled / password |
| DELETE | `/api/users/{id}`            | `USER_DELETE`        | Xóa                              |
| POST   | `/api/users/{id}/roles`      | `USER_ASSIGN_ROLE`   | Body `{roleCode}`                |
| DELETE | `/api/users/{id}/roles/{roleCode}` | `USER_ASSIGN_ROLE` | Gỡ role                    |


### 3.0.6. Package (convention Module 3 / M4)

```text
vn.demo/
  config/          SecurityConfig, OpenApiConfig, JwtProperties, RbacDataSeeder
  controller/      AuthController, UserController
  dto/
  model/           UserModel, RoleModel, PermissionModel
  repository/
  security/        JwtService, JwtAuthenticationFilter, AuthUserPrincipal, ...
  service/         AuthService, UserService, PermissionLoader, ...
  exception/
```

### 3.0.7. Thứ tự code gợi ý

**1 → 2 (model) → 3 → 2 (seed) → 4 → 5 → 6 → 7**

Seed cần `PasswordEncoder` từ `SecurityConfig` (tính năng 3). Tính năng 7 có thể làm ngay sau tính năng 3 (permitAll Swagger) + bổ sung `OpenApiConfig` khi đã có login.

---

## 3.1. Tính năng 1 — Chuẩn bị project

### Giới thiệu

Tạo / mở project Spring Boot 3.5, Java **17**, dependency web + mongo + security + validation + jjwt + lombok + **springdoc**.

### File cần tạo / cập nhật


| #   | File                           | Tạo / Cập nhật | Việc cần làm                                              |
| --- | ------------------------------ | -------------- | --------------------------------------------------------- |
| 1   | Project `demo-bai4-auth`       | **Tạo / dùng** | Boot 3.5, Java 17                                         |
| 2   | `pom.xml`                      | **Cập nhật**   | Dependency như mục *Điều kiện tiên quyết* (gồm springdoc) |
| 3   | `DemoBai4AuthApplication.java` | **Tạo mới**    | `@SpringBootApplication` + `@ConfigurationPropertiesScan` |
| 4   | `application.properties`       | **Tạo mới**    | Mongo URI + `app.jwt.*`                                   |


```properties
spring.data.mongodb.uri=mongodb://localhost:27017/db_java_t3h_module4_bai4
app.jwt.secret=${JWT_SECRET:demo-bai4-lab-secret-change-me-32chars-min}
app.jwt.expiration-ms=3600000
```

**Kiểm tra:** `./mvnw -q -DskipTests compile`.

---

## 3.2. Tính năng 2 — Model, Repository và seed

### Schema → Model


| Collection    | Model             | Field chính |
| ------------- | ----------------- | ----------- |
| `users`       | `UserModel`       | username, email, password, enabled, roleIds, createdAt, updatedAt |
| `roles`       | `RoleModel`       | code, name, description, permissionIds |
| `permissions` | `PermissionModel` | code, name, description |


### Công việc

1. Viết 3 Model + 3 Repository (`findByUsername`, `findByCode`, `findByIdIn`, …)
2. Viết `RbacDataSeeder` (idempotent; BCrypt) — **cần** bean `PasswordEncoder`

### File


| #   | File               | Vai trò |
| --- | ------------------ | ------- |
| 1   | 3 `*Model`         | Lưu RBAC |
| 2   | 3 `*Repository`    | Truy vấn |
| 3   | `RbacDataSeeder`   | Seed permission → role → 3 user |


> Tham chiếu: `[RbacDataSeeder.java](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/config/RbacDataSeeder.java)`.

**Kiểm tra:** sau khi có tính năng 3 và start app → Mongo có `users`, `roles`, `permissions`.

> Nếu đã seed schema lab cũ: **drop database** `db_java_t3h_module4_bai4` rồi chạy lại.

---

## 3.3. Tính năng 3 — JWT và bảo vệ API (stateless)

### Giới thiệu

- `JwtService`: phát hành / parse JWT (**chỉ** `sub` + `username` + `exp`)
- `JwtAuthenticationFilter`: Bearer → verify → **load User + Roles + Permissions từ Mongo** → `SecurityContext`
- `SecurityConfig`: STATELESS, CSRF off, `POST /api/auth/login` + **Swagger/OpenAPI** `permitAll`, còn lại authenticated; JSON 401/403; **`@EnableMethodSecurity`**
- `PermissionLoader` (hoặc tương đương): gom permission codes từ `roleIds`

### Ý chính `SecurityFilterChain`

1. CSRF disable; `SessionCreationPolicy.STATELESS`
2. `POST /api/auth/login` → `permitAll`
3. **Swagger UI / OpenAPI** → `permitAll` (nếu không, mở UI bị **401** vì `anyRequest().authenticated()`):
   - `/swagger-ui.html`, `/swagger-ui/**`
   - `/v3/api-docs`, `/v3/api-docs/**`
4. Các URL khác → `authenticated()` (chi tiết quyền ở `@PreAuthorize`)
5. `addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
    .requestMatchers(
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs",
        "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated())
```

> Tham chiếu: `[SecurityConfig.java](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/config/SecurityConfig.java)`.

### File


| #   | File                         | Vai trò |
| --- | ---------------------------- | ------- |
| 1   | `JwtProperties`              | Bind `app.jwt.*` |
| 2   | `JwtService`                 | HS256 issue / parse |
| 3   | `AuthUserPrincipal`          | UserDetails / principal + authorities = permission codes |
| 4   | `PermissionLoader`           | roleIds → Set\<permission code\> |
| 5   | `JwtAuthenticationFilter`    | Mỗi request nạp quyền từ DB |
| 6   | `SecurityConfig`             | Chain + PasswordEncoder + EnableMethodSecurity + **permitAll Swagger** |


**Kiểm tra:** `./mvnw test`; `GET /api/auth/me` không token → **401** JSON; mở `http://localhost:8080/swagger-ui/index.html` **không** bị 401.

> **Ôn lại cơ chế:** filter vừa viết chính là **Bước 2** ở [§2.7](#27-từ-jwt-đến-preauthorize--cơ-chế-chi-tiết) — sau bước này `SecurityContext` đã có danh sách `GrantedAuthority` (permission code).

---

## 3.4. Tính năng 4 — Auth API

### Công việc

1. `AuthService.login` — BCrypt → `JwtService.generateToken(userId, username)`
2. `AuthController` — `POST /api/auth/login`, `GET /api/auth/me`
3. `/me` trả roles + permissions (load từ DB qua principal / loader) — tiện debug lab

### File


| #   | File             | Vai trò |
| --- | ---------------- | ------- |
| 1   | `LoginRequest` / `LoginResponse` / `MeResponse` | DTO |
| 2   | `AuthService`    | Login |
| 3   | `AuthController` | Endpoint |


**Kiểm tra:** login `admin`/`admin123` → token; `/me` với Bearer → thấy permissions gồm `USER_DELETE`.

---

## 3.5. Tính năng 5 — User API + `@PreAuthorize`

### Giới thiệu

Gắn Permission lên từng endpoint User. Annotation **không tự load** quyền từ Mongo — nó chỉ **đọc** authorities mà filter đã đặt vào `SecurityContext` ([§2.7](#27-từ-jwt-đến-preauthorize--cơ-chế-chi-tiết)).

```java
@PreAuthorize("hasAuthority('USER_CREATE')")  // so khớp chuỗi authority đã nạp
@PostMapping
public UserResponse create(...) { ... }
```

Cần `@EnableMethodSecurity` (tính năng 3). Thiếu → annotation bị bỏ qua.

### Công việc

1. `UserService` — CRUD + `assignRole` / `removeRole`
2. `UserController` — gắn đúng `@PreAuthorize("hasAuthority('...')")` theo bảng 3.0.5
3. Không hardcode `if (role == ADMIN)` trong Controller

### File


| #   | File              | Vai trò |
| --- | ----------------- | ------- |
| 1   | DTO User          | Request / Response |
| 2   | `UserService`     | Nghiệp vụ |
| 3   | `UserController`  | REST + PreAuthorize |


**Kiểm tra gợi ý:**

1. Alice `GET /api/users` → **200**; `DELETE` → **403** (đã login nhưng thiếu `USER_DELETE` — đúng lớp B ở §2.7)
2. Editor `POST /api/users` → **201**; `DELETE` → **403**
3. Admin `DELETE` → **200/204**; `POST .../roles` → **200**

> Debug nhanh: gọi `GET /api/auth/me` xem `permissions[]` có chứa đúng code mà `@PreAuthorize` yêu cầu không.
---

## 3.6. Tính năng 6 — Exception JSON

### Công việc

1. `UnauthorizedException` → 401 (login sai)
2. `ForbiddenException` → 403 (nếu ném từ service; method security cũng trả 403 qua AccessDeniedHandler)
3. Not found → 404; conflict username/email → 409; `@Valid` → 400
4. `SecurityConfig`: EntryPoint 401 / AccessDeniedHandler 403 JSON

> Tham chiếu: `[RestExceptionHandler.java](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/exception/RestExceptionHandler.java)`.

---

## 3.7. Tính năng 7 — Swagger UI + cấu hình test JWT

### Giới thiệu

Bài 1 đã dùng springdoc để xem API. Bài này cần thêm **hai việc** vì có Spring Security + JWT:

1. **Security:** cho phép truy cập Swagger/OpenAPI không cần token (đã làm ở tính năng 3)
2. **OpenApiConfig:** khai báo `SecurityScheme` Bearer JWT → nút **Authorize** trên UI để dán token và Try it out

Không có bước 2 thì UI mở được nhưng mỗi request vẫn phải copy header thủ công — khó kiểm tra 401/403 theo role.

### Công việc cần làm

1. Đảm bảo `pom.xml` có `springdoc-openapi-starter-webmvc-ui` (tính năng 1)
2. Đảm bảo `SecurityConfig` `permitAll` các path Swagger (tính năng 3)
3. Viết `OpenApiConfig`:
   - `SecurityScheme` kiểu `HTTP` + `scheme("bearer")` + `bearerFormat("JWT")`
   - `addSecurityItem` áp dụng scheme mặc định cho API
4. (Khuyến nghị) `@SecurityRequirements` **rỗng** trên `POST /api/auth/login` — login không bắt Authorize trên Swagger
5. Restart app → test theo checklist bên dưới

### File


| #   | File            | Tạo / Cập nhật | Việc cần làm                                      | Vai trò        |
| --- | --------------- | -------------- | ------------------------------------------------- | -------------- |
| 1   | `OpenApiConfig` | **Tạo mới**    | `OpenAPI` + `Components` + `bearerAuth`            | Nút Authorize  |
| 2   | `SecurityConfig`| **Đã có**      | `permitAll` `/swagger-ui/**`, `/v3/api-docs/**`    | Mở UI không 401 |
| 3   | `AuthController`| **Cập nhật**   | `@SecurityRequirements` trên `/login`              | Login không cần token trên UI |


```java
@Bean
OpenAPI demoBai4OpenApi() {
    return new OpenAPI()
        .info(new Info().title("Module 4 — Bài 4 Auth API").version("v1"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

> Tham chiếu: `[OpenApiConfig.java](../../demo-bai4-auth/java-springboot-bai4/src/main/java/vn/demo/config/OpenApiConfig.java)`.

### Cách test trên Swagger UI

1. Mở http://localhost:8080/swagger-ui/index.html
2. `POST /api/auth/login` với `admin` / `admin123` → copy field `token`
3. Bấm **Authorize** → dán token (**không** gõ chữ `Bearer `) → Authorize → Close
4. `GET /api/auth/me` → thấy `permissions` gồm `USER_DELETE`
5. Đăng nhập lại bằng `alice` / `user123`, Authorize token mới → `DELETE /api/users/{id}` → **403**

**Kiểm tra:** UI mở không 401; sau Authorize, API có khóa xanh gửi kèm Bearer; Alice thiếu quyền → 403.

---

## Checklist nộp bài


| ☐ | Hạng mục | Tính năng |
| - | -------- | --------- |
| ☐ | 1. Project compile, Mongo URI đúng; có springdoc | 1 |
| ☐ | 2. Seed 3 user / 3 role / 5 permission | 2 |
| ☐ | 3. JWT chỉ `sub`+`username`; filter load Permission từ DB; Swagger `permitAll` | 3 |
| ☐ | 4. Login + `/me` | 4 |
| ☐ | 5. User CRUD + assign role; đúng 401/403 theo ma trận | 5 |
| ☐ | 6. Lỗi trả JSON thống nhất | 6 |
| ☐ | 7. Swagger UI + Authorize JWT (Try it out Alice/Admin) | 7 |


## Lỗi thường gặp

| Triệu chứng | Nguyên nhân thường gặp | Cách xử |
| ----------- | ---------------------- | ------- |
| 401 mọi API kể cả có token | Filter không set SecurityContext; secret sai; token hết hạn | Log parse; kiểm `app.jwt.secret` |
| 403 dù là admin | Quên `@EnableMethodSecurity`; authority không khớp code (thừa `ROLE_`) | In `/me` permissions; so `@PreAuthorize` |
| Seed không chạy / data cũ | Đã có user `admin` từ schema cũ | Drop DB lab rồi start lại |
| `hasRole` vẫn 403 | Lab dùng permission code, không phải `ROLE_ADMIN` | Đổi sang `hasAuthority('USER_…')` |
| Swagger UI bị 401 | Quên `permitAll` `/swagger-ui/**`, `/v3/api-docs/**` | Sửa `SecurityConfig` (tính năng 3 / 7) |
| Không thấy nút Authorize | Chưa khai báo `SecurityScheme` Bearer trong `OpenApiConfig` | Thêm tính năng 7 |
| Authorize rồi vẫn 401 | Dán nhầm cả chữ `Bearer ` + token; hoặc token hết hạn | Chỉ dán chuỗi JWT; login lại |


## Mở rộng (không bắt buộc)

- `POST /api/auth/refresh`
- CRUD Role / Permission API
- Cache Permission theo userId (TTL ngắn)
- Thêm PRODUCT_/ORDER_ permissions như spec đầy đủ

## Liên kết ôn

- Form login + session + BCrypt: [M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)
- Bearer: [M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md)
- Spec gốc: [module4-bai4-authen-and-autho.md](../pdf/module4-bai4-authen-and-autho.md)
