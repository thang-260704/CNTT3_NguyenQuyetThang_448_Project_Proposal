import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="py-20 text-center">
      <p className="text-6xl font-extrabold text-brand-500">404</p>
      <p className="mt-2 text-slate-600">Trang bạn tìm không tồn tại.</p>
      <Link to="/" className="btn-primary mt-6">
        Về trang chủ
      </Link>
    </div>
  );
}
