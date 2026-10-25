import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { errorMessage } from '../api/client';
import { formatDate, formatPrice } from '../utils/format';
import StatusBadge from '../components/StatusBadge';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';

export default function Orders() {
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get('/orders/my')
      .then((r) => setOrders(r.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <Alert>{error}</Alert>;
  if (!orders) return <Spinner />;

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold">Lịch sử đơn hàng</h1>
      {orders.length === 0 ? (
        <div className="card py-12 text-center text-slate-500">
          Bạn chưa có đơn hàng nào.{' '}
          <Link to="/" className="font-semibold text-brand-600">
            Mua sắm ngay
          </Link>
        </div>
      ) : (
        orders.map((o) => (
          <Link key={o.id} to={`/orders/${o.id}`} className="card flex flex-col gap-2 p-4 hover:shadow-md sm:flex-row sm:items-center">
            <div className="flex-1">
              <p className="font-semibold">Đơn hàng #{o.id}</p>
              <p className="text-sm text-slate-500">
                {formatDate(o.createdAt)} · {o.items.length} sản phẩm
              </p>
            </div>
            <StatusBadge status={o.status} />
            <p className="font-bold text-brand-600 sm:w-36 sm:text-right">{formatPrice(o.totalAmount)}</p>
          </Link>
        ))
      )}
    </div>
  );
}
