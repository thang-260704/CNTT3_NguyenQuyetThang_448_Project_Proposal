import axios from 'axios';
import { useAuthStore } from '../store/authStore';

// Gọi API qua đường dẫn tương đối /api: Nginx (Docker) hoặc Vite proxy (dev) chuyển tiếp tới backend.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 20000,
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401 && useAuthStore.getState().token) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  },
);

export function errorMessage(error, fallback = 'Đã có lỗi xảy ra, vui lòng thử lại') {
  return error?.response?.data?.message || fallback;
}

export function fieldErrors(error) {
  return error?.response?.data?.fieldErrors || {};
}

export default api;
