# Hướng dẫn test API — Ecommerce MS Phase 1

## Điều kiện trước khi test

1. MongoDB đang chạy; Auth/Product đã seed user + sản phẩm  
2. Chạy `./start-all.sh` (hoặc start đủ 5 service)  
3. Có `curl` và `jq` (khuyến nghị)

> **Id sản phẩm:** Seed Mongo tạo **ObjectId ngẫu nhiên** — không dùng `product-001`.  
> Luôn lấy id thật từ `GET /api/products`.

## Tài khoản seed

| Email | Mật khẩu | Role |
| ----- | -------- | ---- |
| `user@example.com` | `user123` | ROLE_USER |
| `admin@example.com` | `admin123` | ROLE_ADMIN |

## E2E nhanh (nên chạy trước)

```bash
# 1) Đăng nhập → accessToken
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"user123"}' | jq -r .accessToken)

# 2) Lấy id sản phẩm thật từ catalog
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')
echo "PRODUCT_ID=$PRODUCT_ID"

# 3) Đặt hàng — Correlation Id do Gateway sinh (xem header response)
curl -s -i -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}]}"
```

**Kỳ vọng:** HTTP **201**, `status` ≈ `CONFIRMED`, `items` có snapshot tên/giá.  
Response có `X-Correlation-Id` (UUID). Tìm cùng UUID trên `logs/ms-gateway.log`, `ms-order.log`, `ms-product.log` (`[cid=…]`).

---

## Kịch bản 1: Đăng ký

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "testuser@example.com",
    "password": "testpass123",
    "fullName": "Test User"
  }' | jq
```

## Kịch bản 2: Đăng nhập user

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"user123"}' | jq
```

Lưu `accessToken` vào biến `TOKEN` như mục E2E nhanh.

## Kịch bản 3: Đăng nhập admin

```bash
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"admin123"}' | jq -r .accessToken)
```

Roles gồm `ROLE_ADMIN`.

## Kịch bản 4: Xem danh sách sản phẩm (public)

```bash
curl -s http://localhost:8080/api/products | jq
```

Phản hồi: mảng sản phẩm (id dạng ObjectId Mongo, ví dụ `"67a1b2c3…"`).

## Kịch bản 5: Chi tiết một sản phẩm (public)

```bash
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')
curl -s "http://localhost:8080/api/products/$PRODUCT_ID" | jq
```

## Kịch bản 6: Tạo sản phẩm (ADMIN)

```bash
# Lấy categoryId từ một SP có sẵn
CATEGORY_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].categoryId')

curl -s -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{
    \"name\": \"San pham moi\",
    \"description\": \"Mo ta test\",
    \"price\": 999000,
    \"categoryId\": \"$CATEGORY_ID\"
  }" | jq
```

**Kỳ vọng:** **201**. Dùng token user thường → **403**.

## Kịch bản 7: Tạo đơn hàng

```bash
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')

curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":2}]}" | jq
```

**Kỳ vọng:** **201**; `items` có `productName` / `price` snapshot (không tin giá từ client).  
Mail: mặc định `app.mail.enabled=false` → xem log Notification.

## Kịch bản 8: Đơn của tôi

```bash
curl -s http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Kịch bản 9: Chi tiết đơn

```bash
ORDER_ID=$(curl -s http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id')

curl -s "http://localhost:8080/api/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Kịch bản 10: Refresh token

```bash
# Lưu refreshToken lúc login vào REFRESH_TOKEN
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}" | jq
```

---

## Trường hợp lỗi

### JWT sai → 401

```bash
curl -s -o /dev/null -w "%{http_code}\n" \
  http://localhost:8080/api/orders \
  -H 'Authorization: Bearer invalid-token'
```

### Thiếu Authorization → 401

```bash
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')
curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d "{\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}]}"
```

### User thường tạo sản phẩm → 403

```bash
curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"X","price":100000,"categoryId":"any"}'
```

### Sản phẩm không tồn tại → lỗi gọi ngoài (thường 502 / 400 tùy handler)

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"items":[{"productId":"non-existent-product","quantity":1}]}' | jq
```

### quantity = 0 → 400

```bash
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":0}]}" | jq
```

---

## Kiểm tra Health

```bash
for p in 8080 8081 8082 8083 8084; do
  echo -n ":$p → "
  curl -s "http://localhost:$p/actuator/health"
  echo
done
```

## Correlation Id

Gateway **luôn sinh** UUID — client **không** gửi header. Đọc id trên response rồi grep log.

```bash
PRODUCT_ID=$(curl -s http://localhost:8080/api/products | jq -r '.[0].id')
curl -s -i -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d "{\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":1}]}"
```

Copy `X-Correlation-Id` từ header response; tìm cùng chuỗi trên `logs/ms-gateway.log`, `ms-order.log`, `ms-product.log`, `ms-notify.log` (`[cid=…]`).

## Notification (SMTP)

Mặc định tắt mail — chỉ log nội dung thư. Bật (tuỳ chọn):

```bash
export MAIL_ENABLED=true
# hoặc app.mail.enabled=true trong application.properties
export MAIL_USERNAME=...
export MAIL_PASSWORD=...
```

## Sản phẩm seed (tên — id lấy từ API)

- iPhone 17 Pro  
- Samsung Galaxy S26  
- MacBook Pro M5  
- Premium T-Shirt  
- Java Programming Guide  
