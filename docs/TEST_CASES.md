# Test case & kết quả kiểm thử

Kết quả lấy từ GitHub Actions (workflow **CI**, nhánh `main`, ngày 25/10/2026). Mọi test chạy tự động mỗi lần push.

## 1. Tổng hợp

| Nhóm | Số test | Pass | Fail | Tỷ lệ |
|---|---|---|---|---|
| Backend JUnit (unit + integration) | 35 | 35 | 0 | 100% |
| Frontend Vitest | 9 | 9 | 0 | 100% |
| Smoke test E2E qua Docker Compose | 16 | 16 | 0 | 100% |
| Hiệu năng (k6, Lighthouse) | 4 | 4 | 0 | 100% |
| **Tổng** | **64** | **64** | **0** | **100%** (yêu cầu ≥ 80%) |

## 2. Test chức năng tự động

| Mã | Nội dung | File test | Kết quả |
|---|---|---|---|
| TC-AI-01 | Admin sinh mô tả sản phẩm bằng AI (FR-05) | `AiApiTest` | PASS |
| TC-AI-02 | Khách hàng không dùng được AI sinh mô tả của admin (403) | `AiApiTest` | PASS |
| TC-AI-03 | Chatbot gợi ý đúng sản phẩm theo từ khóa, không cần đăng nhập | `AiApiTest` | PASS |
| TC-AI-04 | Chatbot lọc theo khoảng giá (dưới 200k) | `AiApiTest` | PASS |
| TC-AI-05 | Admin tạo mock data sản phẩm bằng AI | `AiApiTest` | PASS |
| TC-AUTH-01 | Đăng ký thành công trả về JWT và role CUSTOMER | `AuthApiTest` | PASS |
| TC-AUTH-02 | Mật khẩu được lưu dạng BCrypt, không phải plaintext (NFR-04) | `AuthApiTest` | PASS |
| TC-AUTH-03 | Đăng ký trùng tên đăng nhập trả về 409 | `AuthApiTest` | PASS |
| TC-AUTH-04 | Đăng ký thiếu dữ liệu / email sai định dạng trả về 400 kèm lỗi từng trường | `AuthApiTest` | PASS |
| TC-AUTH-05 | Đăng nhập đúng tài khoản admin trả về role ADMIN | `AuthApiTest` | PASS |
| TC-AUTH-06 | Đăng nhập sai mật khẩu trả về 401 | `AuthApiTest` | PASS |
| TC-AUTH-07 | Gọi API cần đăng nhập không có token / token giả trả về 401 (NFR-05) | `AuthApiTest` | PASS |
| TC-AUTH-08 | Xem và cập nhật hồ sơ cá nhân | `AuthApiTest` | PASS |
| TC-AUTH-09 | Đổi mật khẩu: sai mật khẩu cũ 400, đúng thì đăng nhập được bằng mật khẩu mới | `AuthApiTest` | PASS |
| TC-ORD-01 | Đặt hàng COD tạo đơn PENDING, tính đúng tổng tiền và trừ tồn kho (FR-03) | `OrderApiTest` | PASS |
| TC-ORD-02 | Đặt vượt tồn kho trả về 409 và không trừ kho | `OrderApiTest` | PASS |
| TC-ORD-03 | Chưa đăng nhập không đặt hàng được (401) | `OrderApiTest` | PASS |
| TC-ORD-04 | Checkout thiếu thông tin / số điện thoại sai / giỏ rỗng trả về 400 | `OrderApiTest` | PASS |
| TC-ORD-05 | Khách chỉ xem được lịch sử đơn của chính mình | `OrderApiTest` | PASS |
| TC-ORD-06 | Admin cập nhật trạng thái PENDING → PROCESSING → SHIPPING → COMPLETED (FR-06) | `OrderApiTest` | PASS |
| TC-ORD-07 | Chuyển trạng thái sai quy trình (PENDING → COMPLETED) trả về 400 | `OrderApiTest` | PASS |
| TC-ORD-08 | Hủy đơn (CANCELLED) hoàn lại tồn kho | `OrderApiTest` | PASS |
| TC-ORD-09 | Khách hàng không gọi được API quản lý đơn của admin (403) | `OrderApiTest` | PASS |
| TC-ORD-10 | Admin xem thống kê tổng quan | `OrderApiTest` | PASS |
| TC-PROD-01 | Khách vãng lai xem danh sách sản phẩm có phân trang | `ProductApiTest` | PASS |
| TC-PROD-02 | Tìm kiếm theo từ khóa không phân biệt hoa thường | `ProductApiTest` | PASS |
| TC-PROD-03 | Lọc sản phẩm theo danh mục | `ProductApiTest` | PASS |
| TC-PROD-04 | Xem chi tiết sản phẩm; id không tồn tại trả về 404 | `ProductApiTest` | PASS |
| TC-PROD-05 | Khách hàng gọi API admin bị từ chối 403; chưa đăng nhập 401 | `ProductApiTest` | PASS |
| TC-PROD-06 | Admin thêm - sửa - xóa sản phẩm (FR-04) | `ProductApiTest` | PASS |
| TC-PROD-07 | Admin tạo sản phẩm với giá âm bị từ chối 400 | `ProductApiTest` | PASS |
| TC-UNIT-02 | Chuẩn hóa tiếng Việt có dấu về không dấu | `AiFallbackTest` | PASS |
| TC-UNIT-03 | Nhận diện khoảng giá trong câu hỏi | `AiFallbackTest` | PASS |
| TC-UNIT-04 | Mô tả fallback chứa tên sản phẩm và lời kêu gọi mua hàng | `AiFallbackTest` | PASS |
| TC-UNIT-01 | Quy tắc chuyển trạng thái đơn hàng | `OrderStatusTest` | PASS |
| TC-FE-08 | hiển thị ảnh, tên, giá và nút thêm vào giỏ | `ProductCard.test.jsx` | PASS |
| TC-FE-09 | sản phẩm hết hàng thì khóa nút thêm vào giỏ | `ProductCard.test.jsx` | PASS |
| TC-FE-01 | thêm sản phẩm mới vào giỏ | `cartStore.test.js` | PASS |
| TC-FE-02 | thêm trùng sản phẩm thì cộng dồn số lượng, không vượt tồn kho | `cartStore.test.js` | PASS |
| TC-FE-03 | sửa số lượng giới hạn trong khoảng 1..tồn kho | `cartStore.test.js` | PASS |
| TC-FE-04 | xóa sản phẩm và tính tổng tiền | `cartStore.test.js` | PASS |
| TC-FE-05 | giỏ hàng được lưu vào LocalStorage | `cartStore.test.js` | PASS |
| TC-FE-06 | định dạng giá tiền Việt Nam | `format.test.js` | PASS |
| TC-FE-07 | có nhãn tiếng Việt cho đủ 5 trạng thái đơn hàng | `format.test.js` | PASS |

## 3. Smoke test E2E (`scripts/smoke-test.sh`)

Chạy qua Nginx (http://localhost:3000) sau khi `docker compose up --build`: trang SPA và fallback route, danh sách/danh mục/tìm kiếm sản phẩm, đăng nhập đúng/sai, đặt hàng khi chưa đăng nhập (401), checkout COD (201, trạng thái PENDING), lịch sử đơn, customer vào admin (403), admin đăng nhập, chuyển đơn sang PROCESSING, AI sinh mô tả, chatbot. Kết quả: **16 PASS, 0 FAIL**.

## 4. Kiểm thử phi chức năng

| Mã | Yêu cầu | Công cụ | Kết quả đo | Đánh giá |
|---|---|---|---|---|
| NFR-01 | API < 500ms | k6 (`tests/load/products.js`) | avg 9.0ms, p90 11.8ms, p95 28.4ms, max 476.3ms | PASS |
| NFR-03 | 50 request đồng thời | k6, 50 VUs × 30s | 8.583 request (282 req/s), lỗi 0.00%, checks 100% | PASS |
| NFR-02 | FCP < 2s | Lighthouse (desktop) | FCP 375ms, LCP 606ms, Accessibility 95 | PASS |
| NFR-04 | BCrypt | TC-AUTH-02 | Mật khẩu lưu dạng `$2a$...` | PASS |
| NFR-05 | JWT có hạn | TC-AUTH-07 | Token sai/thiếu → 401, hết hạn sau 24h | PASS |
| NFR-07 | Docker | CI job integration | 3 container healthy | PASS |
| NFR-08 | CI tự động | GitHub Actions | Chạy trên mỗi push | PASS |
| NFR-10 | Responsive | Kiểm tra thủ công (DevTools 375px / 768px / 1280px) | Menu mobile, lưới 1–4 cột | PASS |
