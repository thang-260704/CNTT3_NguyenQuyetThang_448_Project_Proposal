import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../../api/client';
import { formatPrice, STATUS_LABELS } from '../../utils/format';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [aiMode, setAiMode] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/stats').then((r) => setStats(r.data)).catch((err) => setError(errorMessage(err)));
    api.get('/ai/status').then((r) => setAiMode(r.data.mode)).catch(() => setAiMode(''));
  }, []);

  if (error) return <Alert>{error}</Alert>;
  if (!stats) return <Spinner />;

  const cards = [
    { label: 'Sản phẩm đang bán', value: stats.totalProducts, to: '/admin/products' },
    { label: 'Tổng số đơn hàng', value: stats.totalOrders, to: '/admin/orders' },
    { label: 'Doanh thu (đơn hoàn thành)', value: formatPrice(stats.completedRevenue) },
  ];

  return (
    <div className="space-y-5">
      <div className="grid gap-4 sm:grid-cols-3">
        {cards.map((c) => {
          const body = (
            <div className="card p-5">
              <p className="text-sm text-slate-500">{c.label}</p>
              <p className="mt-1 text-2xl font-extrabold text-slate-900">{c.value}</p>
            </div>
          );
          return c.to ? (
            <Link key={c.label} to={c.to} className="hover:opacity-90">
              {body}
            </Link>
          ) : (
            <div key={c.label}>{body}</div>
          );
        })}
      </div>
      <div className="card p-5">
        <h2 className="mb-3 font-semibold">Đơn hàng theo trạng thái</h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
          {Object.entries(stats.ordersByStatus).map(([status, count]) => (
            <Link key={status} to={`/admin/orders?status=${status}`} className="rounded-lg bg-slate-50 p-3 text-center hover:bg-slate-100">
              <p className="text-2xl font-bold">{count}</p>
              <p className="text-xs text-slate-500">{STATUS_LABELS[status]}</p>
            </Link>
          ))}
        </div>
      </div>
      {aiMode && (
        <p className="text-xs text-slate-500">
          Chế độ AI: <b>{aiMode === 'llm' ? 'Spring AI + Chat Model' : 'Fallback nội bộ (chưa cấu hình AI_API_KEY)'}</b>
        </p>
      )}
    </div>
  );
}
