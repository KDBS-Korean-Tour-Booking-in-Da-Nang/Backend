# KDBS Backend API

Backend API cho hệ thống quản lý tour và booking.

## Mục lục

- [Tạo Voucher](#tạo-voucher)
- [API Endpoints](#api-endpoints)
- [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)

---

## Tạo Voucher

### 1. Voucher giảm giá theo phần trăm (PERCENT)

Voucher giảm 10% cho đơn hàng tối thiểu 2,000,000 VNĐ, áp dụng cho tour ID 1:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "SUMMER10",
    "name": "Giảm 10% mùa hè",
    "discountType": "PERCENT",
    "discountValue": 10,
    "minOrderValue": 2000000,
    "totalQuantity": 100,
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-08-31T23:59:59",
    "status": "ACTIVE",
    "tourIds": [1]
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "SUMMER10",
  "name": "Giảm 10% mùa hè",
  "discountType": "PERCENT",
  "discountValue": 10,
  "minOrderValue": 2000000,
  "totalQuantity": 100,
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-08-31T23:59:59",
  "status": "ACTIVE",
  "tourIds": [1]
}
```

### 2. Voucher giảm giá cố định (FIXED)

Voucher giảm 500,000 VNĐ cho đơn hàng tối thiểu 3,000,000 VNĐ:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "FIXED500K",
    "name": "Giảm 500.000 VNĐ",
    "discountType": "FIXED",
    "discountValue": 500000,
    "minOrderValue": 3000000,
    "totalQuantity": 50,
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-08-31T23:59:59",
    "status": "ACTIVE",
    "tourIds": null
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "FIXED500K",
  "name": "Giảm 500.000 VNĐ",
  "discountType": "FIXED",
  "discountValue": 500000,
  "minOrderValue": 3000000,
  "totalQuantity": 50,
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-08-31T23:59:59",
  "status": "ACTIVE",
  "tourIds": null
}
```

### 3. Voucher áp dụng cho tất cả tour của công ty

Voucher giảm 20% cho tất cả tour của công ty, không giới hạn đơn hàng tối thiểu:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "ALLTOURS20",
    "name": "Giảm 20% cho tất cả tour",
    "discountType": "PERCENT",
    "discountValue": 20,
    "minOrderValue": null,
    "totalQuantity": 200,
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-12-31T23:59:59",
    "status": "ACTIVE",
    "tourIds": null
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "ALLTOURS20",
  "name": "Giảm 20% cho tất cả tour",
  "discountType": "PERCENT",
  "discountValue": 20,
  "minOrderValue": null,
  "totalQuantity": 200,
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-12-31T23:59:59",
  "status": "ACTIVE",
  "tourIds": null
}
```

### 4. Voucher giảm giá lớn (30%)

Voucher giảm 30% cho đơn hàng từ 5,000,000 VNĐ trở lên, áp dụng cho nhiều tour:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "BIG30",
    "name": "Giảm 30% cho đơn hàng lớn",
    "discountType": "PERCENT",
    "discountValue": 30,
    "minOrderValue": 5000000,
    "totalQuantity": 30,
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59",
    "status": "ACTIVE",
    "tourIds": [1, 2, 3]
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "BIG30",
  "name": "Giảm 30% cho đơn hàng lớn",
  "discountType": "PERCENT",
  "discountValue": 30,
  "minOrderValue": 5000000,
  "totalQuantity": 30,
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-12-31T23:59:59",
  "status": "ACTIVE",
  "tourIds": [1, 2, 3]
}
```

### 5. Voucher giảm giá 1,000,000 VNĐ

Voucher giảm 1 triệu đồng cho đơn hàng từ 5,000,000 VNĐ:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "MEGA1M",
    "name": "Giảm 1.000.000 VNĐ",
    "discountType": "FIXED",
    "discountValue": 1000000,
    "minOrderValue": 5000000,
    "totalQuantity": 20,
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59",
    "status": "ACTIVE",
    "tourIds": null
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "MEGA1M",
  "name": "Giảm 1.000.000 VNĐ",
  "discountType": "FIXED",
  "discountValue": 1000000,
  "minOrderValue": 5000000,
  "totalQuantity": 20,
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-12-31T23:59:59",
  "status": "ACTIVE",
  "tourIds": null
}
```

### 6. Voucher khuyến mãi đặc biệt (15%)

Voucher giảm 15% không giới hạn đơn hàng tối thiểu:

```bash
curl -X POST "http://localhost:8080/api/vouchers" \
  -H "Content-Type: application/json" \
  -d '{
    "companyId": 1,
    "code": "SPECIAL15",
    "name": "Khuyến mãi đặc biệt 15%",
    "discountType": "PERCENT",
    "discountValue": 15,
    "minOrderValue": null,
    "totalQuantity": 150,
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-09-30T23:59:59",
    "status": "ACTIVE",
    "tourIds": null
  }'
```

**JSON Body:**
```json
{
  "companyId": 1,
  "code": "SPECIAL15",
  "name": "Khuyến mãi đặc biệt 15%",
  "discountType": "PERCENT",
  "discountValue": 15,
  "minOrderValue": null,
  "totalQuantity": 150,
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-09-30T23:59:59",
  "status": "ACTIVE",
  "tourIds": null
}
```

---

## Giải thích các trường

| Trường | Loại | Mô tả | Ví dụ |
|--------|------|-------|-------|
| `companyId` | Integer | ID của công ty sở hữu voucher | `1` |
| `code` | String | Mã voucher (phải unique trong cùng company) | `"SUMMER10"` |
| `name` | String | Tên voucher | `"Giảm 10% mùa hè"` |
| `discountType` | Enum | Loại giảm giá: `PERCENT` hoặc `FIXED` | `"PERCENT"` |
| `discountValue` | BigDecimal | Giá trị giảm giá:<br>- Nếu PERCENT: phần trăm (10 = 10%)<br>- Nếu FIXED: số tiền (500000 = 500.000 VNĐ) | `10` hoặc `500000` |
| `minOrderValue` | BigDecimal | Đơn hàng tối thiểu để áp dụng voucher (null = không giới hạn) | `2000000` hoặc `null` |
| `totalQuantity` | Integer | Tổng số lượng voucher | `100` |
| `startDate` | LocalDateTime | Ngày bắt đầu hiệu lực | `"2025-06-01T00:00:00"` |
| `endDate` | LocalDateTime | Ngày kết thúc hiệu lực | `"2025-08-31T23:59:59"` |
| `status` | Enum | Trạng thái: `ACTIVE` hoặc `INACTIVE` | `"ACTIVE"` |
| `tourIds` | List\<Long\> | Danh sách tour ID được áp dụng:<br>- `null` hoặc `[]`: áp dụng cho tất cả tour của công ty<br>- `[1, 2, 3]`: chỉ áp dụng cho các tour được chỉ định | `[1]` hoặc `null` |

---

## Lưu ý quan trọng

1. **Mã voucher (`code`) phải unique** trong cùng một công ty (`companyId`)
2. **`discountType`**:
   - `PERCENT`: `discountValue` là phần trăm (ví dụ: 10 = 10%)
   - `FIXED`: `discountValue` là số tiền cố định (ví dụ: 500000 = 500.000 VNĐ)
3. **`tourIds`**:
   - Nếu `null` hoặc `[]`: voucher áp dụng cho **tất cả tour** của công ty
   - Nếu có giá trị: voucher chỉ áp dụng cho **các tour được chỉ định**
4. **`minOrderValue`**:
   - Nếu `null`: không giới hạn đơn hàng tối thiểu
   - Nếu có giá trị: đơn hàng phải đạt giá trị này mới được áp dụng voucher
5. **`totalQuantity`**: Số lượng voucher ban đầu, hệ thống sẽ tự động quản lý `remainingQuantity`

---

## API Endpoints

### Voucher

- `POST /api/vouchers` - Tạo voucher mới
- `GET /api/vouchers` - Lấy tất cả voucher
- `GET /api/vouchers/company/{companyId}` - Lấy voucher theo company ID
- `GET /api/vouchers/preview-all/{bookingId}` - Preview tất cả voucher available cho booking ⭐
- `POST /api/vouchers/preview` - Preview một voucher cụ thể (⚠️ DEPRECATED)
- `POST /api/vouchers/apply` - Áp dụng voucher vào booking

### Booking

- `POST /api/booking` - Tạo booking mới
- `GET /api/booking/id/{bookingId}` - Lấy thông tin booking
- `POST /api/booking/payment` - Thanh toán booking (có thể kèm voucher)

---

## Hướng dẫn sử dụng

### Luồng sử dụng voucher trong booking:

1. **Tạo booking**: `POST /api/booking`
2. **Lấy danh sách voucher available**: `GET /api/vouchers/preview-all/{bookingId}`
3. **Áp dụng voucher** (nếu muốn): `POST /api/vouchers/apply`
4. **Thanh toán**: `POST /api/booking/payment`

### Ví dụ luồng hoàn chỉnh:

```bash
# 1. Tạo booking
curl -X POST "http://localhost:8080/api/booking" \
  -H "Content-Type: application/json" \
  -d '{...}'

# 2. Lấy danh sách voucher available (bookingId = 1)
curl -X GET "http://localhost:8080/api/vouchers/preview-all/1"

# 3. Áp dụng voucher (nếu muốn)
curl -X POST "http://localhost:8080/api/vouchers/apply" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "voucherCode": "SUMMER10"
  }'

# 4. Thanh toán
curl -X POST "http://localhost:8080/api/booking/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": 1,
    "userEmail": "user@example.com",
    "voucherCode": "SUMMER10"
  }'
```

---

## Tài liệu chi tiết

Xem file `VOUCHER_TEST_REQUESTS.md` để biết thêm chi tiết về các API và test cases.

---

**Lưu ý**: Thay đổi `companyId` và `tourIds` trong các ví dụ trên cho phù hợp với dữ liệu thực tế của bạn.

