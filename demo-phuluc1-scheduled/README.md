# Demo Phụ lục 1 — Spring Boot `@Scheduled`

Project demo cho syllabus [`java_m4_phuluc1_Scheduled.md`](../syllabus/module-4/java_m4_phuluc1_Scheduled.md).

App **gọn**, **in-memory** (không Mongo/JPA) — tập trung:

| Phần | Class | Syllabus |
|------|-------|----------|
| Hello `fixedRate` | `HelloScheduleJob` | §2 |
| So sánh Rate / Delay (tắt mặc định) | `RateDelayCompareJob` | §3 |
| **Ví dụ 1** khóa account 24h | `AccountLockJob` + `UserAccountService` | §5 + §7 |
| **Ví dụ 2** báo cáo ngày | `DailyReportJob` + `DailyReportService` | §6 |
| Pool thread (tắt mặc định) | `SchedulingConfig` | §9.1 |
| Unit test Service | `UserAccountServiceTest` | Bài tập nâng cao |

## Yêu cầu

- JDK 17+
- **Không cần** MongoDB / Docker

## Chạy app

```bash
cd demo-phuluc1-scheduled/java-springboot-phuluc1
./mvnw spring-boot:run
```

Quan sát console:

1. `[HelloScheduleJob]` mỗi ~10 giây
2. Trong ~30 giây: `[AccountLockJob]` / `[UserAccountService]` khóa `stale@demo.vn`
3. Trong ~2 phút: `[DailyReportJob]` sinh báo cáo ngày hôm qua

## API nhanh

| Method | URL | Mô tả |
|--------|-----|-------|
| GET | `/api/accounts` | Xem status account (trước/sau job khóa) |
| POST | `/api/accounts/{id}/activate` | Giả lập kích hoạt trong hạn |
| GET | `/api/reports/daily` | Mọi snapshot đã sinh |
| GET | `/api/reports/daily/by-date?date=yyyy-MM-dd` | Đọc **một** snapshot (không aggregation) |
| POST | `/api/reports/daily/generate` | Lab: generate ngay (không đợi cron) |
| POST | `/api/reports/daily/generate?date=yyyy-MM-dd` | Generate cho ngày chỉ định |

```bash
# Sau khi app chạy ~30s — stale phải LOCKED, fresh vẫn PENDING
curl -s http://localhost:8080/api/accounts | python3 -m json.tool

# Đọc báo cáo (sau job hoặc gọi generate)
curl -s -X POST "http://localhost:8080/api/reports/daily/generate"
curl -s "http://localhost:8080/api/reports/daily" | python3 -m json.tool
```

## Chạy test

```bash
./mvnw test
# application-test.properties đã tắt scheduling + seed
# UserAccountServiceTest dùng InMemory repo (không Mockito) — gọi thẳng Service
```

## Cấu trúc

```
src/main/java/vn/demo/
├── DemoPhuluc1ScheduledApplication.java   ← @EnableScheduling ★
├── config/
│   ├── DataSeeder.java
│   └── SchedulingConfig.java              ← §9.1 (optional)
├── schedule/
│   ├── HelloScheduleJob.java              ← §2
│   └── RateDelayCompareJob.java           ← §3 (optional)
├── account/
│   ├── model/UserAccount.java
│   ├── repository/UserAccountRepository.java
│   ├── repository/InMemoryUserAccountRepository.java
│   ├── service/UserAccountService.java    ← logic khóa ★
│   ├── schedule/AccountLockJob.java       ← job mỏng ★
│   └── controller/UserAccountController.java
├── report/
│   ├── model/DailyReport.java
│   ├── model/OrderRecord.java
│   ├── repository/ReportDataRepository.java
│   ├── repository/InMemoryReportDataRepository.java
│   ├── service/DailyReportService.java    ← precompute ★
│   ├── schedule/DailyReportJob.java
│   └── controller/DailyReportController.java
└── exception/
    ├── ResourceNotFoundException.java
    └── RestExceptionHandler.java
```

## Map syllabus → code / properties

| Syllabus | Demo |
|----------|------|
| §2 `@EnableScheduling` | `DemoPhuluc1ScheduledApplication` |
| §2 Hello job | `HelloScheduleJob` + `app.hello.*` |
| §3 Rate vs Delay | `RateDelayCompareJob` — bật `app.rate-delay-demo.enabled=true` |
| §4 Cron 6 field | `app.account.lock-cron`, `app.report.daily-cron` |
| §5 Khóa account | `UserAccountService` + `AccountLockJob` + seed 3 user |
| §6 Báo cáo ngày | `DailyReportService` + `DailyReportJob` + seed orders hôm qua |
| §7 Properties | `application.properties` |
| §8 Tắt schedule khi test | `application-test.properties` → `spring.task.scheduling.enabled=false` |
| §9.1 Thread pool | `SchedulingConfig` — bật `app.scheduling.pool.enabled=true` |

## Properties lab vs production

| Key | Lab (mặc định demo) | Gợi ý production |
|-----|---------------------|------------------|
| `app.account.lock-cron` | `0/30 * * * * *` (mỗi 30s) | `0 */15 * * * *` |
| `app.account.lock-after-hours` | `24` | `24` |
| `app.report.daily-cron` | `0 */2 * * * *` (mỗi 2 phút) | `0 0 2 * * *` |
| `app.report.zone` | `Asia/Ho_Chi_Minh` | giữ nguyên |
