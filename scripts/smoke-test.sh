#!/usr/bin/env bash
# Smoke test end-to-end qua Nginx (frontend) -> Spring Boot -> MySQL
set -euo pipefail
BASE="${BASE_URL:-http://localhost:3000}"
pass=0; fail=0
check() { # name expected actual
  if [ "$2" == "$3" ]; then echo "  [PASS] $1 (HTTP $3)"; pass=$((pass+1)); else echo "  [FAIL] $1 - mong đợi $2, nhận $3"; fail=$((fail+1)); fi
}
code() { curl -s -o /tmp/body.json -w '%{http_code}' "$@"; }

echo "== Smoke test $BASE =="
check "SPA index.html"            200 "$(code "$BASE/")"
check "SPA fallback /products/1"  200 "$(code "$BASE/products/1")"

check "GET /api/products"         200 "$(code "$BASE/api/products?size=5")"
PID=$(jq -r '[.content[] | select(.stock > 0)][0].id' /tmp/body.json)
check "GET /api/categories"       200 "$(code "$BASE/api/categories")"
check "Tìm kiếm keyword"          200 "$(code "$BASE/api/products?keyword=tai")"

check "Login customer"            200 "$(code -H 'Content-Type: application/json' -d '{"username":"customer","password":"customer123"}' "$BASE/api/auth/login")"
CTOKEN=$(jq -r .token /tmp/body.json)
check "Login sai mật khẩu -> 401" 401 "$(code -H 'Content-Type: application/json' -d '{"username":"customer","password":"wrong"}' "$BASE/api/auth/login")"
check "Đặt hàng không token -> 401" 401 "$(code -H 'Content-Type: application/json' -d '{}' "$BASE/api/orders")"

check "Checkout COD"              201 "$(code -H "Authorization: Bearer $CTOKEN" -H 'Content-Type: application/json' \
  -d "{\"fullName\":\"Smoke Test\",\"phone\":\"0912345678\",\"address\":\"Ha Noi\",\"items\":[{\"productId\":$PID,\"quantity\":1}]}" "$BASE/api/orders")"
OID=$(jq -r .id /tmp/body.json); STATUS=$(jq -r .status /tmp/body.json)
check "Đơn mới ở trạng thái PENDING" PENDING "$STATUS"
check "Lịch sử đơn hàng"          200 "$(code -H "Authorization: Bearer $CTOKEN" "$BASE/api/orders/my")"
check "Customer vào admin -> 403" 403 "$(code -H "Authorization: Bearer $CTOKEN" "$BASE/api/admin/stats")"

check "Login admin"               200 "$(code -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' "$BASE/api/auth/login")"
ATOKEN=$(jq -r .token /tmp/body.json)
check "Admin cập nhật PROCESSING" 200 "$(code -X PATCH -H "Authorization: Bearer $ATOKEN" -H 'Content-Type: application/json' -d '{"status":"PROCESSING"}' "$BASE/api/admin/orders/$OID/status")"
check "AI sinh mô tả"             200 "$(code -H "Authorization: Bearer $ATOKEN" -H 'Content-Type: application/json' -d '{"name":"Loa bluetooth mini"}' "$BASE/api/admin/ai/description")"
check "AI chatbot"                200 "$(code -H 'Content-Type: application/json' -d '{"message":"tai nghe duoi 1 trieu"}' "$BASE/api/ai/chat")"

echo "== Kết quả: $pass PASS, $fail FAIL =="
[ "$fail" -eq 0 ]
