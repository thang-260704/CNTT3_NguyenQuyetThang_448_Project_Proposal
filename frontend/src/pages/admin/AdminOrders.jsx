import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../../api/client';
import { formatDate, formatPrice, STATUS_LABELS } from '../../utils/format';
import StatusBadge from '../../components/StatusBadge';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

const FILTERS = ['', 'PENDING', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED'];

export default function AdminOrders() {
  const [params, setParams] = useSearchParams();
  const status = params.get('status') || '';
  const [data, setData] = useState(null);
  const [expanded, setExpanded] = useState(null);
  const [msg, setMsg] = useState({ type: '', text: '' });

  const load = useCallback(() => {
    setData(null);
    api
      .get('/admin/orders', { params: { status: status || undefined, size: 50 } })
      .then((r) => setData(r.data))
      .catch((err) => setMsg({ type: 'error', text: errorMessage(err) }));
  }, [status]);

  useEffect(() => {
    load();
  }, [load]);

  const changeStatus = async (order, next) => {
    try {
      await api.patch(`/admin/orders/${order.id}/status`, { status: next });
      setMsg({ type: 'success', text: `Đơn #${order.id}: ${STATUS_LABELS[order.status]} → ${STATUS_LABELS[next]}` });
      load();
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  return (
    <div className="space-y-4">
      <Alert type={msg.type}>{msg.text}</Alert>
      <div className="flex flex-wrap gap-2">
        {FILTERS.map((f) => (
          <button
            key={f || 'all'}
            type="button"
            onClick={() => setParams(f ? { status: f } : {})}
            className={`rounded-full px-3 py-1 text-sm font-medium ${status === f ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 ring-1 ring-slate-200'}`}
          >
            {f ? STATUS_LABELS[f] : 'Tất cả'}
          </button>
        ))}
      </div>

      {!data ? (
        <Spinner />
      ) : data.content.length === 0 ? (
        <p className="card p-8 text-center text-slate-500">Không có đơn hàng</p>
      ) : (
        <div className="space-y-3">
          {data.content.map((o) => (
            <div key={o.id} className="card p-4">
              <div className="flex flex-col gap-3 md:flex-row md:items-center">
                <button type="button" className="flex-1 text-left" onClick={() => setExpanded(expanded === o.id ? null : o.id)}>
                  <p className="font-semibold">
                    #{o.id} · {o.fullName} <span className="font-normal text-slate-500">({o.username})</span>
                  </p>
                  <p className="text-sm text-slate-500">
                    {formatDate(o.createdAt)} · {o.phone} · {o.items.length} sản phẩm
                  </p>
                </button>
                <StatusBadge status={o.status} />
                <p className="font-bold text-brand-600 md:w-32 md:text-right">{formatPrice(o.totalAmount)}</p>
                <div className="flex gap-2">
                  {o.nextStatuses.map((next) => (
                    <button
                      key={next}
                      type="button"
                      className={next === 'CANCELLED' ? 'btn-danger px-3 py-1' : 'btn-primary px-3 py-1'}
                      onClick={() => changeStatus(o, next)}
                    >
                      {next === 'CANCELLED' ? 'Hủy đơn' : `→ ${STATUS_LABELS[next]}`}
                    </button>
                  ))}
                </div>
              </div>
              {expanded === o.id && (
                <div className="mt-3 space-y-1 border-t pt-3 text-sm">
                  <p>
                    <b>Địa chỉ:</b> {o.address}
                  </p>
                  {o.items.map((i, idx) => (
                    <p key={idx} className="flex justify-between text-slate-600">
                      <span>
                        {i.productName} × {i.quantity}
                      </span>
                      <span>{formatPrice(i.subtotal)}</span>
                    </p>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
