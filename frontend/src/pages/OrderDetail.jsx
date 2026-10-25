import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import api, { errorMessage } from '../api/client';
import { formatDate, formatPrice, PLACEHOLDER_IMAGE, STATUS_LABELS } from '../utils/format';
import StatusBadge from '../components/StatusBadge';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';

const STEPS = ['PENDING', 'PROCESSING', 'SHIPPING', 'COMPLETED'];

export default function OrderDetail() {
  const { id } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get(`/orders/${id}`)
      .then((r) => setOrder(r.data))
      .catch((err) => setError(errorMessage(err, 'Không tìm thấy đơn hàng')));
  }, [id]);

  if (error) return <Alert>{error}</Alert>;
  if (!order) return <Spinner />;

  const stepIndex = STEPS.indexOf(order.status);

  return (
    <div className="mx-auto max-w-3xl space-y-4">
      {location.state?.justPlaced && (
        <Alert type="success">Đặt hàng thành công! Đơn hàng của bạn đang chờ xác nhận. Cảm ơn bạn đã mua sắm tại QuickShop.</Alert>
      )}
      <div className="card space-y-4 p-5">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h1 className="text-xl font-bold">Đơn hàng #{order.id}</h1>
          <StatusBadge status={order.status} />
        </div>
        <p className="text-sm text-slate-500">Đặt lúc {formatDate(order.createdAt)}</p>

        {order.status !== 'CANCELLED' && (
          <ol className="grid grid-cols-4 gap-2 text-center text-xs">
            {STEPS.map((s, i) => (
              <li key={s} className={`rounded-lg px-1 py-2 ${i <= stepIndex ? 'bg-brand-500 text-white' : 'bg-slate-100 text-slate-500'}`}>
                {STATUS_LABELS[s]}
              </li>
            ))}
          </ol>
        )}

        <div className="grid gap-1 rounded-lg bg-slate-50 p-3 text-sm">
          <p>
            <b>Người nhận:</b> {order.fullName} · {order.phone}
          </p>
          <p>
            <b>Địa chỉ:</b> {order.address}
          </p>
          <p>
            <b>Thanh toán:</b> {order.paymentMethod} (thanh toán khi nhận hàng)
          </p>
        </div>

        <div className="divide-y">
          {order.items.map((i, idx) => (
            <div key={idx} className="flex items-center gap-3 py-3">
              <img src={i.imageUrl || PLACEHOLDER_IMAGE} alt={i.productName} className="h-14 w-14 rounded-lg object-cover" />
              <div className="flex-1">
                <p className="font-medium">{i.productName}</p>
                <p className="text-sm text-slate-500">
                  {formatPrice(i.unitPrice)} × {i.quantity}
                </p>
              </div>
              <p className="font-semibold">{formatPrice(i.subtotal)}</p>
            </div>
          ))}
        </div>
        <div className="flex justify-between border-t pt-3 text-lg font-bold">
          <span>Tổng cộng</span>
          <span className="text-brand-600">{formatPrice(order.totalAmount)}</span>
        </div>
      </div>
      <Link to="/orders" className="btn-outline">
        ← Tất cả đơn hàng
      </Link>
    </div>
  );
}
