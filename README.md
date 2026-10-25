# Rikkei E-Commerce QuickShop (MVP)

[![CI](https://github.com/thang-260704/CNTT3_NguyenQuyetThang_448_Project_Proposal/actions/workflows/ci.yml/badge.svg)](https://github.com/thang-260704/CNTT3_NguyenQuyetThang_448_Project_Proposal/actions/workflows/ci.yml)
[![CD](https://github.com/thang-260704/CNTT3_NguyenQuyetThang_448_Project_Proposal/actions/workflows/cd.yml/badge.svg)](https://github.com/thang-260704/CNTT3_NguyenQuyetThang_448_Project_Proposal/actions/workflows/cd.yml)

Mã dự án: **PRJ-QUICK-REVIEW-PRE-OJT-01**. Đây là website bán hàng tối giản: khách xem sản phẩm, thêm vào giỏ, đặt hàng thanh toán khi nhận (COD), còn admin quản lý sản phẩm và đơn hàng. Dự án có tích hợp AI để sinh mô tả sản phẩm, tạo dữ liệu mẫu và làm chatbot tư vấn.

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS 18 (Vite), Tailwind CSS, Zustand (giỏ hàng lưu LocalStorage), React Router, Axios |
| Backend | Spring Boot 3.4 (Java 17), Spring Security + JWT, Spring Data JPA, Spring AI, Logback/SLF4J |
| Database | MySQL 8 (khi test dùng H2 chế độ MySQL) |
| DevOps | Docker multi-stage, Docker Compose, Nginx reverse proxy, GitHub Actions CI/CD, GHCR |
| Kiểm thử | JUnit 5 + MockMvc, Vitest + Testing Library, smoke test E2E (bash/curl), k6 load test, Lighthouse |

---

## 1. Chạy dự án (chỉ cần Docker)

Máy cần có **Docker Desktop** (hoặc Docker Engine có Compose v2). Không cần cài Java, Node hay MySQL.

```bash
git clone https://github.com/thang-260704/CNTT3_NguyenQuyetThang_448_Project_Proposal.git
cd CNTT3_NguyenQuyetThang_448_Project_Proposal
docker compose up -d --build
```

Lần đầu mất khoảng 3–5 phút để build. Sau đó mở các địa chỉ sau:

| Địa chỉ | Nội dung |
|---|---|
| http://localhost:3000 | Website QuickShop (frontend + `/api` được proxy qua Nginx) |
| http://localhost:8081/api/products | Gọi thẳng REST API của backend |
| http://localhost:8081/actuator/health | Health check của backend |

**Tài khoản demo** (được tạo tự động cùng 16 sản phẩm mẫu):

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Admin | `admin` | `admin123` |
| Khách hàng | `customer` | `customer123` |

Các lệnh thường dùng:

```bash
docker compose ps                 # xem trạng thái (cả 3 container phải "healthy")
docker compose logs -f backend    # xem log Spring Boot
bash scripts/smoke-test.sh        # tự kiểm tra 16 luồng chính (cần curl + jq)
docker compose down               # dừng (thêm -v nếu muốn xóa luôn dữ liệu MySQL)
```

> Nếu cổng 3000, 8081 hoặc 3307 đã bị chiếm, hãy sửa phần `ports` trong `docker-compose.yml`.

### AI (không bắt buộc)

Khi không cấu hình gì thêm, ứng dụng chạy ở **chế độ AI fallback**: mô tả sản phẩm và câu trả lời chatbot được sinh bằng template và tìm kiếm trong CSDL, nên không cần Internet hay API key. Nếu muốn dùng LLM thật qua Spring AI, tạo file `.env` từ `.env.example` rồi chạy lại `docker compose up -d`:

```env
AI_API_KEY=sk-...                 # OpenAI
# Hoặc Gemini (endpoint tương thích OpenAI):
# AI_API_KEY=AIza...
# AI_BASE_URL=https://generativelanguage.googleapis.com
# AI_COMPLETIONS_PATH=/v1beta/openai/chat/completions
# AI_MODEL=gemini-2.0-flash
```

Chế độ AI đang chạy được hiển thị ở trang Admin → Tổng quan, hoặc qua `GET /api/ai/status`.

## 2. Chạy ở chế độ phát triển (không dùng Docker)

```bash
# Terminal 1 – MySQL (hoặc dùng MySQL có sẵn trên máy, user quickshop/quickshop123)
docker compose up -d mysql

# Terminal 2 – Backend tại http://localhost:8080
cd backend
DB_URL="jdbc:mysql://localhost:3307/quickshop?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" ./gradlew bootRun
#   Windows cmd:  set DB_URL=... && gradlew.bat bootRun

# Terminal 3 – Frontend tại http://localhost:5173 (proxy /api sang backend)
cd frontend
npm install
VITE_API_PROXY=http://localhost:8080 npm run dev
```

Chạy test:

```bash
cd backend && ./gradlew test      # 35 test JUnit (unit + integration với H2)
cd frontend && npm test           # 9 test Vitest
```

## 3. Chức năng

| Mã | Chức năng | Vị trí |
|---|---|---|
| FR-01 | Đăng ký, đăng nhập (BCrypt + JWT), phân quyền CUSTOMER/ADMIN, xem/sửa hồ sơ, đổi mật khẩu | `AuthController`, `UserController`, trang Login/Register/Profile |
| FR-02 | Danh sách sản phẩm dạng lưới, tìm kiếm, lọc danh mục, sắp xếp, phân trang, xem chi tiết | `ProductController`, trang Home/ProductDetail |
| FR-03 | Giỏ hàng (Zustand + LocalStorage), checkout COD (họ tên, SĐT, địa chỉ), trừ tồn kho có khóa bi quan, đơn ở trạng thái PENDING, lịch sử đơn | `OrderService`, trang Cart/Checkout/Orders |
| FR-04 | Admin thêm/sửa/xóa (xóa mềm) sản phẩm | `AdminController`, trang Admin → Sản phẩm |
| FR-05 | AI viết mô tả sản phẩm từ tên + danh mục | `POST /api/admin/ai/description`, nút "✨ AI viết mô tả" |
| FR-06 | Admin cập nhật trạng thái đơn PENDING → PROCESSING → SHIPPING → COMPLETED (hoặc CANCELLED, hoàn lại kho) | `OrderStatus`, trang Admin → Đơn hàng |
| AI+ | AI tạo dữ liệu sản phẩm mẫu, chatbot tư vấn sản phẩm (hiểu được cả khoảng giá như "dưới 500k") | `MockDataService`, `ChatWidget` |

## 4. API

Mọi API trả về JSON. Lỗi có cùng một định dạng `{timestamp, status, error, message, path, fieldErrors}`. API cần đăng nhập thì gửi header `Authorization: Bearer <token>`.

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Đăng ký, trả về JWT |
| POST | `/api/auth/login` | Public | Đăng nhập, trả về JWT |
| GET/PUT | `/api/users/me` | Đăng nhập | Xem / cập nhật hồ sơ |
| PUT | `/api/users/me/password` | Đăng nhập | Đổi mật khẩu |
| GET | `/api/products?keyword=&categoryId=&sort=&page=&size=` | Public | Danh sách, tìm kiếm, lọc (sort: `price_asc`, `price_desc`, `name_asc`) |
| GET | `/api/products/{id}` | Public | Chi tiết sản phẩm |
| GET | `/api/categories` | Public | Danh mục |
| POST | `/api/orders` | CUSTOMER/ADMIN | Đặt hàng COD |
| GET | `/api/orders/my`, `/api/orders/{id}` | Đăng nhập | Lịch sử / chi tiết đơn của mình |
| POST/PUT/DELETE | `/api/admin/products[/{id}]` | ADMIN | CRUD sản phẩm |
| GET | `/api/admin/orders?status=` | ADMIN | Danh sách đơn hàng |
| PATCH | `/api/admin/orders/{id}/status` | ADMIN | Cập nhật trạng thái đơn |
| GET | `/api/admin/stats` | ADMIN | Thống kê |
| POST | `/api/admin/ai/description` | ADMIN | AI sinh mô tả |
| POST | `/api/admin/ai/mock-products?count=` | ADMIN | AI tạo sản phẩm mẫu |
| POST | `/api/ai/chat` | Public | Chatbot tư vấn |
| GET | `/api/ai/status` | Public | Chế độ AI đang dùng (`llm` / `fallback`) |

## 5. CI/CD

| Workflow | Kích hoạt | Các bước |
|---|---|---|
| `ci.yml` | Push/PR vào `main`, `dev`, `feature/**` | **backend**: Gradle test + bootJar, upload báo cáo test & JaCoCo → **frontend**: `npm ci`, ESLint, Vitest, Vite build → **integration**: `docker compose up --build`, smoke test E2E, k6 với 50 người dùng đồng thời (ngưỡng p95 < 500ms), Lighthouse đo FCP |
| `cd.yml` | Push vào `main` hoặc tag `v*` | Build và đẩy image `backend`, `frontend` lên GitHub Container Registry |

Quy trình nhánh: mỗi thành viên làm trên `feature/<tên-chức-năng>` tách từ `dev`, mở Pull Request vào `dev` (CI phải xanh mới được merge), cuối sprint merge `dev` vào `main` để chạy CD. Commit message theo chuẩn Conventional Commits (`feat:`, `fix:`, `test:`, `ci:`, `docs:`).

## 6. Yêu cầu phi chức năng

| Mã | Yêu cầu | Cách đáp ứng / kiểm chứng |
|---|---|---|
| NFR-01 | API phản hồi < 500ms | Có index, phân trang, `open-in-view=false`; k6 đặt ngưỡng `p(95)<500` trong CI |
| NFR-02 | FCP < 2s | Vite build + gzip + cache `/assets`; Lighthouse trong CI |
| NFR-03 | Chịu 50 request đồng thời | k6 50 VUs trong 30 giây, tỷ lệ lỗi < 1% |
| NFR-04 | Mã hóa mật khẩu | BCrypt (TC-AUTH-02) |
| NFR-05 | JWT có hạn | Hết hạn sau 24h, token sai/hết hạn trả về 401 (TC-AUTH-07) |
| NFR-06 | CORS | Cấu hình whitelist bằng `CORS_ALLOWED_ORIGINS` |
| NFR-07 | Đóng gói Docker | 3 service trong `docker-compose.yml`, có healthcheck, chạy bằng user non-root |
| NFR-08 | CI tự động | GitHub Actions chạy trên mỗi lần push |
| NFR-09 | Coding convention + log | Java dùng camelCase, cột DB dùng snake_case, Logback/SLF4J |
| NFR-10 | Responsive | Tailwind breakpoints, menu mobile, bảng admin cuộn ngang |
| NFR-11 | Tương thích trình duyệt | Vite build target mặc định (Chrome/Edge/Firefox/Safari bản hiện đại) |

Chi tiết test case và kết quả: [docs/TEST_CASES.md](docs/TEST_CASES.md). Báo cáo đề cương: [docs/Bao_Cao_De_Cuong_QuickShop.docx](docs/Bao_Cao_De_Cuong_QuickShop.docx).

## 7. Cấu trúc thư mục

```
├── backend/                 Spring Boot (Gradle)
│   ├── src/main/java/com/rikkei/quickshop/
│   │   ├── controller/      REST controller
│   │   ├── service/         Nghiệp vụ (auth, sản phẩm, đơn hàng)
│   │   ├── ai/              Spring AI + fallback, mock data
│   │   ├── security/        JWT filter, UserDetails
│   │   ├── entity/ repository/ dto/ exception/ config/
│   └── src/test/            JUnit + MockMvc
├── frontend/                React + Vite
│   └── src/{api,store,components,pages,pages/admin,test}
├── scripts/smoke-test.sh    Kiểm thử E2E qua Nginx
├── tests/load/products.js   Kịch bản k6
├── docker-compose.yml
└── .github/workflows/       ci.yml, cd.yml
```

## 8. Nhóm thực hiện

| Thành viên | Vai trò |
|---|---|
| Thành viên 1 | Team Leader / Backend |
| Thành viên 2 | Backend (Auth, Order) |
| Thành viên 3 | Frontend (Customer) |
| Thành viên 4 | Frontend (Admin) |
| Thành viên 5 | AI Integration |
| Nguyễn Quyết Thắng – B24DTCN448 | QA / DevOps (Docker, CI/CD, kiểm thử) |
