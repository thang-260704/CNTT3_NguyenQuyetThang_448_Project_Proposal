// NFR-01 & NFR-03: 50 người dùng đồng thời, thời gian phản hồi API < 500ms
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8081';

export const options = {
  vus: 50,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
  },
};

export default function () {
  const list = http.get(`${BASE}/api/products?page=0&size=12`);
  check(list, { 'danh sách 200': (r) => r.status === 200 });
  const search = http.get(`${BASE}/api/products?keyword=ao&size=12`);
  check(search, { 'tìm kiếm 200': (r) => r.status === 200 });
  const detail = http.get(`${BASE}/api/products/1`);
  check(detail, { 'chi tiết 200': (r) => r.status === 200 });
  sleep(0.5);
}
