# Bài 2: Microservices — Kiến trúc, giao tiếp, Gateway & thiết kế biên dịch vụ

## Mục tiêu bài học

Sau bài này, học viên có thể:

- Giải thích **microservice** khác gì **một ứng dụng nguyên khối (monolith)**
- Nêu **lợi ích / chi phí** và biết **khi nào chưa nên** tách nhiều service
- Phân biệt giao tiếp **đồng bộ (REST)** và **bất đồng bộ (hàng đợi / sự kiện)**
- Hiểu vì sao **không nên dùng chung một database** giữa các service
- Nắm ý **mỗi service sở hữu dữ liệu của mình**; JWT chỉ ở mức *token mang danh tính* (làm chi tiết ở Bài 4)
- Phân biệt **API Gateway** (cổng vào hệ thống) và **Filter/Interceptor** (trong một app)
- Biết vì sao cần **timeout / retry cẩn thận**; và vì sao **không còn một `@Transactional` bao hai database**
- **Thiết kế trên giấy** hệ thống phòng khám: cắt service, sync/async, rủi ro

> **Không làm trong bài này:** viết JWT/Spring Security đầy đủ (→ **Bài 4 Auth**); cài Kafka/RabbitMQ; triển khai nhiều container lên cloud (→ Bài 7–8).  
> Các mục **Mở rộng** cuối mỗi phần: tự đọc — *là gì / dùng để làm gì / dùng khi nào* — **không** bắt buộc thi/lab.

## Điều kiện tiên quyết

- **[Bài 1 — REST](./java_m4_bai1_RESTful_API.md)**: API REST, versioning, idempotent, `@Async` ≠ hàng đợi thật, logging
- **[M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md)**: REST CRUD, DTO
- **[M3 Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md)** §8: `@Transactional` trong **một** MongoDB
- *(Khuyến khích)* **[M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md)** Bearer token; **[M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)** Authn vs Authz
- JDK 17+, Spring Boot 3.x — **lab chính không cần tạo project** (thiết kế whiteboard / giấy)

> **Lab chính:** thiết kế phòng khám (§7).  
> **Demo 2 service:** sẽ bổ sung sau (`demo-bai2-microservices`).

> **Trên lớp:** §0 → §1 → §2 → §3 → §4 → §5 (phần lõi) → lab §7.  
> **Mở rộng + §6 đầy đủ:** HV tự đọc hoặc GV chọn 1–2 ý soi lab.

### Thời lượng gợi ý


| Phần | Thời gian |
| ---- | --------- |
| Bridge §0 | ~5 phút |
| Monolith vs microservice §1 | ~15 phút |
| Giao tiếp sync / async §2 | ~20 phút |
| Sở hữu dữ liệu + JWT khái niệm §3 | ~10–15 phút |
| API Gateway §4 | ~15 phút |
| Chịu lỗi + nhất quán đơn giản §5 | ~15–20 phút |
| Lab phòng khám §7 | ~35–45 phút |


## Nội dung (làm theo thứ tự)


| # | Chủ đề | Việc HV làm | Kiểm tra |
| - | ------ | ----------- | -------- |
| 0 | Bridge | Đọc bảng “đã biết” | Biết xem lại bài nào |
| 1 | Monolith vs MS | Ví dụ app quen | 3 lợi ích + 3 chi phí + khi **chưa** tách |
| 2 | Sync / async | Chọn cách cho 3 tình huống | Không đề xuất “dùng chung DB” làm chuẩn |
| 3 | Sở hữu dữ liệu + token | Vẽ luồng login → đặt hàng | Nói được “ai sở hữu dữ liệu gì” |
| 4 | Gateway vs Filter | So sánh 2 cột | Chỉ đúng vị trí |
| 5 | Timeout, retry, 2 DB | Case Order → kho chậm | Không nhầm Saga = `@Transactional` 2 DB |
| 6 | Lỗi hay gặp | Chọn 3 lỗi cho lab | Gắn vào thiết kế của mình |
| 7 | Lab phòng khám | Nộp theo rubric | Đủ ownership / sync-async / rủi ro |


---

## 0. Bridge — đã học, hôm nay không giảng lại


| Đã biết | Xem lại | Hôm nay |
| ------- | ------- | ------- |
| REST, DTO, `/api/v1` | [Bài 1](./java_m4_bai1_RESTful_API.md) · [M3 Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) | REST **giữa hai service** (có độ trễ mạng) |
| Idempotent | Bài 1 | Dùng khi **thử lại** hoặc user bấm 2 lần |
| `@Async` | Bài 1 §3 | Khác **hàng đợi** giữa nhiều máy/process |
| Logging | Bài 1 §4 | Sau này gắn thêm mã theo dõi request (mở rộng) |
| Bearer token PayPal | [M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md) | Token **máy↔PayPal** ≠ token **user** sau login (Bài 4) |
| Login form, Authn/Authz | [M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md) | Cùng ý “ai / được làm gì”; API dùng token |
| `@Transactional` 1 DB | [M3 Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md) | Hai service = hai DB → **không** gói một transaction |


**Hôm nay học mới:** tách ứng dụng thành nhiều service · cách chúng nói chuyện · cổng vào (Gateway) · lỗi mạng · thiết kế phòng khám.

> JWT **viết code** → Bài 4. Bài này chỉ cần hình dung: *sau login, request mang theo “thẻ” (token)*.

---

## 1. Microservice là gì? Khi nào dùng?

### 1.1. Ẩn dụ nhanh

> **Monolith** giống một nhà hàng: bếp, thu ngân, phục vụ **cùng một tòa**, một cửa mở/đóng.  
> **Microservices** giống khu food court: mỗi quán (service) độc lập — sửa quán phở không cần đóng cả khu, nhưng phải có lối đi, biển chỉ dẫn, và nếu một quán hết đồ thì các quán khác vẫn có thể bán (nếu thiết kế tốt).

**Microservice** = một phần mềm **nhỏ**, lo **một mảng nghiệp vụ rõ** (ví dụ chỉ đơn hàng, hoặc chỉ thanh toán), **chạy / deploy riêng**, nói chuyện với phần khác qua **mạng** (API hoặc tin nhắn).

```text
Monolith:      [ Auth + Order + Payment + một DB ]     ← bật/tắt một lần

Nhiều service: [ Auth ]   [ Order ]   [ Payment ]     ← bật/tắt riêng
                  │           │            │
                  └──── API / tin nhắn ────┘
```

> Thuật ngữ hay gặp: **bounded context** ≈ “ranh giới nghiệp vụ” (trong Order, chữ “khách” có thể khác nghĩa với ở Marketing). Ở bài này cứ hiểu: **một service = một việc nghiệp vụ rõ, ít chồng chéo**.

### 1.2. Lợi ích (khi làm đúng)


| Lợi ích | Ví dụ dễ nhớ |
| ------- | ------------ |
| Deploy riêng | Sửa lỗi thanh toán, chỉ deploy Payment |
| Scale chọn chỗ | Flash sale: chỉ tăng máy cho Catalog |
| Team song song | Nhóm A làm Order, nhóm B làm Auth |
| Lỗi bớt lan (nếu có chịu lỗi) | Kho chậm ≠ treo cả trang chủ *nếu* có timeout |


### 1.3. Chi phí (đừng bỏ qua)


| Chi phí | Hệ quả với người mới |
| ------- | -------------------- |
| Nhiều chỗ chạy | Phải biết service nào lỗi |
| Dữ liệu khó “khớp ngay” | Không còn một `@Transactional` bao hết |
| Chậm hơn gọi hàm trong cùng app | Mỗi lần gọi HTTP/tin nhắn tốn thời gian |
| Nhiều API = nhiều cửa | Bảo mật phải nghĩ từng service |
| Vận hành phức tạp | Log, phiên bản API, môi trường…


Thực tế lab/prod nhỏ: nhiều service có thể chạy **nhiều process trên một máy** (hoặc container) — không nhất thiết mua nhiều server. Phần đắt thường là **độ phức tạp**, không chỉ tiền máy.

### 1.4. Khi nào **chưa** nên tách?

- Team ít người, sản phẩm còn đổi yêu cầu liên tục  
- Chưa có cách deploy / xem log cơ bản  
- Traffic thấp, **một Spring Boot** vẫn ổn  
- Chưa rõ “ai lo việc gì” → tách sớm dễ thành nhiều process nhưng vẫn dính chặt nhau  

> **Gợi ý khóa học:** giữ **một app, chia package/module rõ** trước; chỉ tách service khi thật sự cần deploy/scale/team riêng.

#### Mở rộng — §1

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **Modular monolith** | Một app deploy, nhưng code chia module rõ (Auth, Order…) | Vừa tổ chức code vừa tránh phức tạp mạng | Team nhỏ / sản phẩm sớm — **ưu tiên trước MS** |
| **Strangler (tách dần)** | Giữ app cũ, “bóc” dần từng phần sang service mới | Giảm rủi ro viết lại toàn bộ | Đang có monolith chạy tốt, muốn tách từng domain |
| **Polyglot** | Mỗi service dùng ngôn ngữ/DB khác nhau | Chọn đúng tool cho đúng việc | Team đủ lớn + lý do kỹ thuật rõ — **đừng** làm cho “cho vui” |
| **Distributed monolith** | Nhiều service nhưng vẫn phải deploy cùng lúc / gọi nhau rối | Tên gọi cảnh báo “tách sai” | Khi thấy MS không mang lại độc lập thật |

---

## 2. Service nói chuyện với nhau thế nào?

### 2.1. Hai kiểu chính cần nhớ

| Kiểu | Ý | Ví dụ |
| ---- | -- | ----- |
| **Đồng bộ (sync)** | Gọi xong **đợi** trả lời mới làm tiếp | `RestClient` gọi Product để lấy tên SP (đã quen [M3 Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md)) |
| **Bất đồng bộ (async)** | Gửi việc / sự kiện, **không** bắt user đợi xong hết | Đặt hàng xong → gửi tin “OrderPlaced” → email/SMS xử lý sau |

```text
User bấm "Đặt hàng" → cần mã đơn ngay        → sync (tạo Order)
                   → gửi email, trừ kho sau  → async (tin nhắn / hàng đợi)
```

| Hỏi nhanh | Chọn |
| --------- | ---- |
| UI cần kết quả **ngay**? | Sync |
| Việc được phép trễ vài giây/phút? | Async |
| Một việc kích hoạt **nhiều** bên (mail + kho + điểm)? | Async dạng sự kiện (Pub/Sub) |

> Nhắc Bài 1: `@Async` trong **cùng một JVM** ≠ hàng đợi giữa nhiều service. Hàng đợi thật (RabbitMQ, Kafka…) sống **ngoài** app — bài này chỉ cần biết **ý tưởng**.

### 2.2. Đừng dùng chung database làm “cách nói chuyện”

Nhiều người nghĩ: “Hai service cùng đọc một Mongo/MySQL cho nhanh.”

Trong microservices đó thường là **sai hướng**:

- Đổi schema của Order dễ làm hỏng Product  
- Không deploy độc lập thật sự  
- Khó biết ai được ghi bảng nào  

```text
❌  Order ──┐
            ├──► một DB dùng chung
    Product─┘

✅  Order  → orders_db
    Product → products_db
    Cần tên sản phẩm? → gọi API Product (hoặc nhận sự kiện cập nhật)
```

Cho phép **tạm** khi đang tách dần từ app cũ — mục tiêu vẫn là **mỗi service một kho dữ liệu** (§3).

### 2.3. REST vẫn là nền (bạn đã biết)

Giữa các service Java trong khóa này, **REST/HTTP + JSON** là lựa chọn mặc định — cùng kỹ năng Bài 1 / M3.

#### Mở rộng — §2

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **Message Queue** | Hàng đợi tin nhắn (vd. RabbitMQ) | Làm việc nặng / lệch tải, worker lấy dần | Upload video, gửi email hàng loạt |
| **Pub/Sub** | Một sự kiện, nhiều service đăng ký nhận | Phát tán “đã xảy ra việc X” | `OrderPlaced` → mail, kho, analytics |
| **gRPC** | Gọi RPC qua HTTP/2 + protobuf | Nhanh, contract chặt **giữa service nội bộ** | Service–service trong data center — **không** phải mặc định cho browser realtime |
| **GraphQL** | Client mô tả đúng field cần lấy | Giảm over-fetch cho UI phức tạp | Thường ở lớp gần frontend (BFF), ít dùng làm bus mọi service |
| **WebSocket / SSE** | Kênh đẩy realtime lên trình duyệt | Chat, progress bar | UI cần cập nhật liên tục |
| **SOAP** | Giao thức XML cũ | Tích hợp hệ thống legacy | Đối tác bắt buộc (ngân hàng cũ…) — **không** “bảo mật hơn REST” chỉ vì là SOAP |
| **Stream (Kafka…)** | Luồng sự kiện liên tục | Phân tích / pipeline lớn | Telemetry, log nghiệp vụ quy mô lớn |

---

## 3. Ai giữ dữ liệu? Làm sao biết “ai đang gọi”?

### 3.1. Mỗi service sở hữu dữ liệu của mình

| Service (ví dụ shop) | Được phép lưu |
| -------------------- | ------------- |
| User / Auth | Tài khoản, profile cơ bản |
| Product | Tên, giá catalog |
| Order | Đơn, dòng hàng, trạng thái đơn |
| Payment | Giao dịch thanh toán |

Service khác **không** `find` thẳng vào DB của mình — hãy **gọi API** hoặc lắng nghe **sự kiện**.

**Sai hay gặp:** Order copy nguyên document Product “cho tiện” rồi giá bên Product đổi mà Order không biết.

### 3.2. JWT — chỉ cần hình dung (làm kỹ ở Bài 4)

Sau khi login, hệ thống cấp một **token** (thường là JWT). Các request sau gửi kèm:

```http
Authorization: Bearer <token>
```

Service (hoặc Gateway) kiểm tra token còn hạn / chữ ký đúng → biết `user_id`, quyền… **không** bắt login lại từng service.

```text
Trình duyệt → (Gateway) → Order Service
                 Bearer token
                      ↓
              đọc user_id từ token
```

| Đã gặp | Khác gì JWT user? |
| ------ | ----------------- |
| Token PayPal (M3 Bài 9) | Chứng minh **app của mình** gọi PayPal |
| Session form (M3 Bài 10) | Cookie + session server — hợp SSR Thymeleaf |
| JWT sau login API | “Thẻ” của **người dùng** cho nhiều API/service |


**Bảo mật tối thiểu:** không ghi secret vào code; dùng biến môi trường (đã nhắc M3 Bài 9).

### 3.3. Filter / Interceptor trong một app

Trong **một** Spring Boot, trước khi vào Controller thường có lớp kiểm tra (Filter, Interceptor, hoặc Spring Security). Ví dụ: đọc header, kiểm tra token, gắn `userId` vào request.

Đây là việc **trong process** — khác Gateway ở §4.

#### Mở rộng — §3

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **JWKS / khóa quay** | Tập khóa công khai để verify JWT | Nhiều instance Auth đổi khóa an toàn | Hệ thống lớn, nhiều service verify cùng issuer |
| **Refresh token** | Token dài hạn để lấy access token mới | User khỏi login liên tục | App mobile / SPA — thiết kế ở Bài 4 |
| **Read model / bản sao đọc** | Copy dữ liệu cần đọc từ service khác (qua event) | Đỡ gọi sync liên tục | Màn hình cần ghép dữ liệu nhiều nguồn, chấp nhận trễ nhẹ |
| **PII** | Dữ liệu định danh cá nhân (SĐT, CMND, bệnh án…) | Nhắc thiết kế least privilege | Mọi hệ thống có user thật — lab phòng khám §7 |

---

## 4. API Gateway — cổng vào

### 4.1. Vai trò (ẩn dụ)

> Gateway giống **lễ tân tòa nhà**: khách chỉ cần một địa chỉ; lễ tân chỉ đường đúng phòng (service), kiểm soát giờ ra vào, không để ai xông thẳng vào mọi cửa.

| Việc Gateway hay làm | Ví dụ |
| -------------------- | ----- |
| Định tuyến | `/api/v1/orders/**` → Order Service |
| Chính sách chung | HTTPS, giới hạn số lần gọi, CORS |
| Phân tải | Nhiều bản sao cùng một service |


```text
Client →  API Gateway  ─┬─► Product
                        ├─► Order
                        └─► Payment
```

### 4.2. Gateway ≠ Filter trong app


| | API Gateway | Filter / Interceptor |
| - | ----------- | -------------------- |
| Ở đâu? | Trước / ngoài các service | Trong **một** service |
| Lo bao nhiêu app? | Nhiều service | Một app |
| Việc chính | Route + policy toàn cục | Validate/log **local** |


Trong hệ sinh thái Spring hay nghe tên **Spring Cloud Gateway** (biết tên là đủ; cấu hình để sau).

### 4.3. Version API giữa service

Service gọi nhau cũng là “client”. Đổi JSON phá tương thích → version (`/v1` → `/v2`), giữ bản cũ một thời gian — cùng ý [Bài 1](./java_m4_bai1_RESTful_API.md) §1.4.

#### Mở rộng — §4

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **BFF** | Backend cho từng loại frontend | API tối ưu mobile vs web | Mobile cần payload khác web rõ rệt |
| **Auth ở Gateway vs từng service** | Chỗ nào verify JWT | Tập trung vs phòng thủ nhiều lớp | Prod thường: Gateway + service vẫn kiểm (hoặc mã hóa kênh nội bộ) |
| **mTLS** | Hai bên chứng thực bằng chứng chỉ | Service tin đúng service | Mạng nội bộ zero-trust |
| **Rate limiting** | Giới hạn số request | Chống spam / bảo vệ backend | API public hoặc đối tác |

---

## 5. Khi service kia chậm / lỗi — và khi dữ liệu ở hai chỗ

### 5.1. Chuỗi gọi sync dễ kéo đổ

```text
Checkout → Order → Kho (treo 30s) → ...
              ↑
         User đợi → bấm lại → dễ ra hai đơn
```

### 5.2. Ba ý chịu lỗi cần thuộc


| Ý | Nghĩa đơn giản | Ví dụ |
| - | -------------- | ----- |
| **Timeout** | Đợi tối đa X giây rồi thôi | Gọi Kho quá 2s → báo lỗi có kiểm soát |
| **Retry** | Thử lại lỗi **tạm** | Mạng nhấp nháy — nhưng chỉ khi thao tác **làm lại an toàn** (idempotent) |
| **Idempotency** | Gọi 1 lần hay 5 lần → nghiệp vụ như 1 lần | Đặt lịch: cùng `Idempotency-Key` / chặn trùng slot |


> User double-click “Đặt hàng” ≈ retry không kiểm soát → cần thiết kế chống trùng (Bài 1 đã gợi ý Idempotency-Key).

### 5.3. Hai database ≠ một `@Transactional`

Ở [M3 Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md) bạn bọc nhiều bước **trong một Mongo**.

Sang microservices: Order DB và Billing DB **khác nhau** → Spring **không** rollback cả hai giúp bạn như một transaction.

Cách nghĩ đơn giản:

1. Mỗi service commit **phần của mình**  
2. Báo các bên khác bằng **sự kiện** (hoặc gọi API có chủ đích)  
3. Nếu bước sau fail → **sửa lại** (hủy đơn, hoàn tiền…) — gọi là hướng **Saga** ở mức ý tưởng  

```text
1. Order: tạo đơn PENDING (xong ở DB Order)
2. Gửi sự kiện "OrderCreated"
3. Kho: trừ hàng — fail → báo "InventoryFailed"
4. Order: chuyển đơn sang CANCELED
```

Trên lớp **không code** Saga — chỉ cần nói được: *“Hai DB thì phải có kế hoạch bù trừ / trạng thái, không ảo tưởng một `@Transactional` thần kỳ.”*

#### Mở rộng — §5

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **Circuit breaker** | “Cầu dao”: thấy callee lỗi nhiều thì ngắt tạm | Tránh đua request vào service đang chết | Dependency hay 5xx / timeout hàng loạt (thư viện: Resilience4j) |
| **Bulkhead** | Tách pool thread/kết nối theo dependency | Một bên chậm không chiếm hết tài nguyên | Nhiều HTTP client trong một app |
| **Eventual consistency** | Dữ liệu khớp **sau một lúc**, không tức thì | Chấp nhận trễ có kiểm soát | Email chưa gửi ngay sau đặt hàng vẫn OK |
| **Saga (orchestration)** | Một “nhạc trưởng” điều phối bước | Luồng dài, cần nhìn một chỗ | Quy trình phức tạp hơn kiểu event qua lại |
| **Outbox** | Ghi event **cùng transaction** với dữ liệu nghiệp vụ, worker gửi sau | Tránh mất event khi crash giữa commit và publish | Bắt đầu dùng message thật sự giữa service |
| **Correlation ID** | Một mã theo request, mọi service log kèm | Debug xuyên service | Từ 2+ service trở lên |
| **Liveness / Readiness** | Health check “còn sống / sẵn sàng nhận traffic” | Orchestrator/Gateway biết đưa request đi đâu | Deploy container (Bài Docker) — Actuator `/actuator/health` |

---

## 6. Lỗi hay gặp (checklist ngắn)

Dùng khi làm lab §7 — chọn **3 lỗi** và nói cách tránh trên thiết kế của bạn.

| Nhóm | Lỗi | Cách nghĩ lại |
| ---- | --- | ------------- |
| Thiết kế | Tách quá vụn / ranh giới mờ | Một service = một việc rõ |
| Thiết kế | Dùng chung DB làm đích | Mỗi service một kho (§2–3) |
| Giao tiếp | Mọi thứ đều sync REST | Side-effect → async |
| Giao tiếp | Retry tạo đơn trùng | Idempotent / khóa nghiệp vụ |
| Dữ liệu | Order lưu full Product | Gọi API hoặc event; ownership rõ |
| Bảo mật | Tin “mạng nội bộ không cần auth” | Vẫn cần token/quyền |
| Bảo mật | API trả thừa field (cả hash mật khẩu) | DTO tối thiểu (M3) |
| Vận hành | Đổi JSON phá tương thích | Version API |
| Vận hành | Không biết lỗi ở service nào | Log có mã request (mở rộng §5) |

---

## 7. Thực hành — thiết kế microservice cho phòng khám

### 7.1. Đề bài (tóm tắt)

- Bệnh nhân: đăng ký/đăng nhập, đặt/hủy lịch, hồ sơ cá nhân  
- Bác sĩ / nhân viên: đăng nhập, quản lý lịch, xem cuộc hẹn  
- Hồ sơ bệnh án: ghi chú, đơn thuốc, xét nghiệm — **chỉ** người được phép  
- Thanh toán: hóa đơn, ghi nhận thanh toán; nhắc lịch (email/SMS)  
- Admin: xem tổng hợp đúng quyền  

> Hồ sơ bệnh án / SĐT là dữ liệu nhạy cảm: **ai sở hữu, ai được đọc** phải rõ trên sơ đồ.

### 7.2. Hai cách cắt service (để thảo luận)


| Cách 1 | Cách 2 |
| ------ | ------ |
| **Auth** — login, token, vai trò | **User** — BN, bác sĩ, admin, vai trò |
| **Patient** — hồ sơ BN + bệnh án | **Appointment** — lịch, đặt/hủy, nhắc |
| **Staff & Appointment** — NV + lịch + nhắc | **Medical Record** — chẩn đoán, đơn, XN |
| **Billing** — hóa đơn, thanh toán | **Billing** — hóa đơn, thanh toán |
| **Notification** — email/SMS | **Notification** — cảnh báo, nhắc |


Bạn được chọn Cách 1, 2, hoặc **Cách 3 tự đề xuất**.

### 7.3. Câu hỏi gợi ý (thảo luận nhóm)


| Hỏi | Gợi ý hướng |
| --- | ----------- |
| Gộp Staff + Appointment (Cách 1) lợi/hại? | Ít gọi mạng; dễ phình “ôm quá nhiều việc” |
| SĐT bệnh nhân thuộc service nào? | Thường User/liên hệ — không copy lung tung sang mọi service |
| SMS fail sau khi đặt lịch OK? | Lịch giữ; SMS thử lại (async) |
| Double-click đặt lịch? | Chống trùng slot / Idempotency-Key |
| Notification đọc DB bệnh án? | **Không** — chỉ nhận sự kiện cần thiết (id lịch, giờ, kênh) |
| Shared DB cho cả hệ? | Không phải mục tiêu kiến trúc |


### 7.4. Nộp bài

1. Sơ đồ service + mũi tên **sync** / **async**  
2. Bảng: service → dữ liệu sở hữu → vài API (tên đường dẫn)  
3. Viết ngắn luồng **đặt lịch**  
4. **3 rủi ro** từ §6 + cách giảm trên thiết kế của bạn  
5. *(Khuyến khích)* 2–3 câu về dữ liệu nhạy cảm  

Chấm theo **Rubric (Phụ lục E)**.

### 7.5. Demo 2 service — sẽ có sau

Khi có repo `demo-bai2-microservices`: hai app khác port, gọi REST + header token + (tuỳ chọn) correlation id.  
Ý tưởng tạm: Auth stub `:8081`, Appointment `:8082`. Chi tiết JWT → Bài 4.

#### Mở rộng — §7

| Chủ đề | Là gì? | Dùng để làm gì? | Dùng khi nào? |
| ------ | ------ | --------------- | ------------- |
| **Tách dần (Strangler) phòng khám** | Giữ app monolith, tách Billing/Notification trước | Giảm rủi ro big-bang | Đã có hệ thống phòng khám một khối |
| **Pháp lý dữ liệu sức khỏe** | Quy định bảo vệ dữ liệu (tùy quốc gia) | Biết *vì sao* least privilege quan trọng | Dự án thật — lab chỉ cần ownership + DTO tối thiểu |

---

## 8. Hiểu sai thường gặp


| Hiểu sai | Chốt lại |
| -------- | -------- |
| Tách mọi class thành service | Sai — tách theo **nghiệp vụ**, không theo file |
| Dùng chung DB cho nhanh | Không phải đích của MS |
| `@Async` = Kafka | Chỉ nền trong một app |
| Phải code JWT dài ở bài này | Bài 4 |
| SOAP an toàn hơn REST | Bảo mật = HTTPS + auth + thiết kế dữ liệu |
| gRPC để web realtime | Browser thường WS/SSE; gRPC nội bộ |
| `@Transactional` bao 2 service | Không — cần sự kiện / bù trừ |
| Có Gateway rồi service khỏi auth | Vẫn nên kiểm soát quyền |


---

## Tóm tắt (phần lõi)


| Ý | Nhớ một câu |
| - | ----------- |
| Microservice | Nhiều phần deploy riêng, nói chuyện qua mạng |
| Khi chưa tách | Team nhỏ / chưa rõ việc → một app chia module |
| Sync / async | Cần ngay → sync; việc phụ → async |
| Shared DB | Tránh làm đích |
| Ownership | Mỗi service một kho dữ liệu |
| JWT | Thẻ danh tính — lab ở Bài 4 |
| Gateway | Cổng vào; Filter nằm trong từng app |
| Chịu lỗi | Timeout + retry có idempotent |
| Hai DB | Không một transaction thần kỳ |
| Lab | Thiết kế phòng khám theo rubric |


---

## Phụ lục

### A. Bài tập

1. **Bắt buộc:** Nộp §7.4 theo Rubric E.  
2. **Bắt buộc:** 5–7 câu so sánh Cách 1 / 2 / 3 (team 3–4 người giả định).  
3. **Bắt buộc:** Giải thích vì sao không `@Transactional` bao Order DB + Billing DB (dựa M3 Bài 5).  
4. **Mở rộng:** Vẽ hủy lịch + Notification async; đọc bảng Mở rộng §5 (Outbox, circuit breaker).  
5. **Mở rộng:** Khi có demo 2 service — chạy và thử có/không token.

### B. Checklist nộp

- [ ] Sơ đồ có sync / async  
- [ ] Ownership rõ — không Shared DB đích  
- [ ] Luồng đặt lịch: chỗ user đợi / chỗ nền  
- [ ] Có idempotency hoặc chống trùng lịch  
- [ ] Có ít nhất một ý timeout/retry/chịu lỗi  
- [ ] (Khuyến khích) PII / bệnh án  
- [ ] Không nộp bài JWT dài thay thiết kế  

### C. Glossary


| Từ | Một dòng |
| -- | -------- |
| **Monolith** | Một ứng dụng deploy một cục |
| **Microservice** | Nhiều dịch vụ nhỏ, deploy riêng |
| **Bounded context** | Ranh giới nghiệp vụ rõ |
| **Sync / Async** | Đợi trả lời ngay / xử lý sau |
| **API Gateway** | Cổng vào hệ thống nhiều service |
| **Idempotent** | Gọi nhiều lần ≈ một lần về nghiệp vụ |
| **Saga** | Chuỗi bước + cách sửa khi fail (ý tưởng) |
| **Outbox** | (Mở rộng) Ghi event cùng lúc với dữ liệu để không mất tin |


### D. Đọc thêm

- [Microsoft — Microservices](https://learn.microsoft.com/en-us/azure/architecture/guide/architecture-styles/microservices)  
- [Strangler Fig](https://learn.microsoft.com/en-us/azure/architecture/patterns/strangler-fig)  
- [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html)  
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)  
- [Resilience4j](https://resilience4j.readme.io/)  
- [JWT Introduction](https://www.jwt.io/introduction) — đọc concept; code ở Bài 4  

### E. Rubric lab (/100)


| Tiêu chí | Điểm | Đạt khi |
| -------- | ---- | ------- |
| Ownership | 25 | Mỗi service có dữ liệu; Medical Record không bị Billing/Notification “ôm” DB |
| Sync vs async | 20 | Đặt lịch: phần hiện UI sync; nhắc SMS/email async (hoặc giải thích khác có lý) |
| Luồng đặt lịch | 15 | Có bước rõ; SMS fail không “xóa” lịch nếu chọn async |
| Chịu lỗi / idempotent | 15 | Ít nhất một cơ chế cụ thể |
| 3 rủi ro áp dụng | 15 | Gắn thiết kế của mình, có cách giảm |
| Trình bày | 10 | Đọc được ≤ 3 phút |


**Trừ nhanh:** Shared DB đích (−15); nhầm `@Async` = MQ (−5); không ghi sync/async (−10).

### F. Handoff Bài 4 Auth

- Mở Bài 4: tiếp §3 bài này — **làm** JWT + phân quyền  
- Authn/Authz form: đã có [M3 Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)

### G. Liên kết nội bộ khóa học

- [Bài 1 REST](./java_m4_bai1_RESTful_API.md) · [Phụ lục 1 Scheduled](./java_m4_phuluc1_Scheduled.md)  
- Bài 4 Auth (tài liệu hiện có): [`java_m4_bai4_Authentication_Authorization.pdf`](../pdf/java_m4_bai4_Authentication_Authorization.pdf)  
- M3: [Bài 3](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai3_MongoDB_Spring_1.md) · [Bài 5](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai5_Relationship_in_MongoDB.md) · [Bài 9](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai9_Online_Payment.md) · [Bài 10](../../../t3h-ltv-java-module-3/syllabus/module-3/java_m3_bai10_Mini_Project.md)  
