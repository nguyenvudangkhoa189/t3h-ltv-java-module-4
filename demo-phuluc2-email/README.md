# Demo Phụ lục 2 — Spring Boot Email (Gmail SMTP)

Project demo cho syllabus [`java_m4_phuluc2_Email.md`](../syllabus/module-4/java_m4_phuluc2_Email.md).

App **gọn**, **in-memory** (không Mongo/JPA) — tập trung:

| Phần | Class | Syllabus |
|------|-------|----------|
| Dependency + Gmail SMTP | `pom.xml` + `application*.properties` | §2 |
| Gửi text / HTML | `MailService` + `MailController` | §3–§4 |
| Chào mừng sau đăng ký | `WelcomeMailService` + `AccountService` | §5 |
| `@Async` (không chặn HTTP) | `@EnableAsync` + `AsyncConfig` | §5.4 |
| Unit test (mock SMTP) | `MailServiceTest`, `AccountServiceTest` | Bài tập |

## Yêu cầu

- JDK 17+
- Tài khoản **Gmail** + **App Password** (bật xác minh 2 bước)
- **Không cần** MongoDB / Docker

## Cấu hình Gmail (một lần)

1. Bật [2-Step Verification](https://myaccount.google.com/security)
2. Tạo [App Password](https://myaccount.google.com/apppasswords) (Mail / Other → `SpringBoot Lab`)
3. Sửa `src/main/resources/application.properties`:

```properties
spring.mail.username=ban@gmail.com
spring.mail.password=abcdefghijklmnop
app.mail.from=ban@gmail.com
```

## Chạy app

```bash
cd demo-phuluc2-email/java-springboot-phuluc2
./mvnw spring-boot:run
```

## API nhanh

| Method | URL | Mô tả |
|--------|-----|-------|
| POST | `/api/mail/hello?to=...` | Gửi **text** (đồng bộ — chờ SMTP) |
| POST | `/api/mail/hello-html?to=...` | Gửi **HTML** (đồng bộ) |
| POST | `/api/accounts/register` | Đăng ký → gửi welcome **@Async** → `202` |
| GET | `/api/accounts` | Liệt kê account đã đăng ký |

```bash
# 1) Hello text — thay email người nhận
curl -s -X POST "http://localhost:8080/api/mail/hello?to=nguoinhan@gmail.com"

# 2) Hello HTML
curl -s -X POST "http://localhost:8080/api/mail/hello-html?to=nguoinhan@gmail.com"

# 3) Đăng ký → 202 ngay; mail chào gửi nền (xem log async-*)
curl -s -X POST http://localhost:8080/api/accounts/register \
  -H "Content-Type: application/json" \
  -d '{"email":"nguoinhan@gmail.com","displayName":"Khoa","password":"secret1"}'

# 4) Liệt kê
curl -s http://localhost:8080/api/accounts | python3 -m json.tool
```

Quan sát:

1. Console: `[MailService]` / `[WelcomeMail]` với thread `async-*` sau register
2. Hộp thư người nhận: Inbox hoặc **Spam**

## Chạy test

```bash
./mvnw test
# Không cần Gmail — Mockito mock JavaMailSender / WelcomeMailService
```

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoPhuluc2EmailApplication.java   ← @EnableAsync ★
├── config/
│   └── AsyncConfig.java               ← §5.4 thread pool async-*
├── mail/
│   ├── MailService.java               ← §3–4 text + HTML ★
│   ├── WelcomeMailService.java        ← §5 @Async welcome ★
│   └── controller/MailController.java ← lab thử gửi
├── account/
│   ├── model/UserAccount.java
│   ├── dto/RegisterRequest.java
│   ├── dto/RegisterResponse.java
│   ├── repository/UserAccountRepository.java
│   ├── repository/InMemoryUserAccountRepository.java
│   ├── service/AccountService.java    ← register + gọi welcome ★
│   └── controller/AccountController.java
└── exception/
    ├── ConflictException.java
    ├── ResourceNotFoundException.java
    └── RestExceptionHandler.java
```

## Map syllabus → code / properties

| Syllabus | Demo |
|----------|------|
| §2 Dependency mail | `pom.xml` → `spring-boot-starter-mail` |
| §2 Gmail SMTP | `application.properties` (`spring.mail.*`) |
| §3 Hello text | `MailService.sendText` + `POST /api/mail/hello` |
| §4 Hello HTML | `MailService.sendHtml` + `POST /api/mail/hello-html` |
| §5 Welcome sau đăng ký | `WelcomeMailService` + `AccountService.register` |
| §5.4 `@Async` | `@EnableAsync` + `AsyncConfig` + `@Async` trên `sendWelcome` |
| §6 Lỗi thường gặp | README + syllabus bảng lỗi |
| Test tắt SMTP thật | `MailServiceTest` / `AccountServiceTest` (Mockito) |

## Lỗi hay gặp khi chạy lab

| Triệu chứng | Cách xử lý |
|-------------|------------|
| `Authentication failed` | Dùng **App Password** 16 ký tự, không phải mật khẩu Gmail |
| Không thấy App passwords | Bật 2FA trước |
| Mail vào Spam | Bình thường lần đầu — kiểm tra Spam |
| Request `/hello` chậm | SMTP đồng bộ — đúng; so với `/register` (202 + async) |
