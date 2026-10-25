import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import api, { errorMessage, fieldErrors } from '../api/client';
import { useCartStore } from '../store/cartStore';
import { useAuthStore } from '../store/authStore';
import { formatPrice } from '../utils/format';
import Alert from '../components/Alert';

const PHONE_REGEX = /^(0|\+84)[0-9]{9,10}$/;

export default function Checkout() {
  const { items, clear } = useCartStore();
  const user = useAuthStore((s) => s.user);
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: user?.fullName || '', phone: '', address: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  if (items.length === 0) return <Navigate to="/cart" replace />;

  const validate = () => {
    const e = {};
    if (!form.fullName.trim()) e.fullName = 'Vui lòng nhập họ tên';
    if (!PHONE_REGEX.test(form.phone.trim())) e.phone = 'Số điện thoại không hợp lệ (VD: 0912345678)';
    if (!form.address.trim()) e.address = 'Vui lòng nhập địa chỉ giao hàng';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    setError('');
    try {
      const { data } = await api.post('/orders', {
        fullName: form.fullName.trim(),
        phone: form.phone.trim(),
        address: form.address.trim(),
        items: items.map((i) => ({ productId: i.id, quantity: i.quantity })),
      });
      clear();
      navigate(`/orders/${data.id}`, { replace: true, state: { justPlaced: true } });
    } catch (err) {
      setErrors(fieldErrors(err));
      setError(errorMessage(err, 'Đặt hàng thất bại'));
    } finally {
      setSubmitting(false);
    }
  };

  const field = (name, label, props = {}) => (
    <div>
      <label className="label" htmlFor={name}>
        {label}
      </label>
      <input
        id={name}
        className="input"
        value={form[name]}
        onChange={(e) => setForm({ ...form, [name]: e.target.value })}
        {...props}
      />
      {errors[name] && <p className="field-error">{errors[name]}</p>}
    </div>
  );

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <form onSubmit={submit} className="card space-y-4 p-5 lg:col-span-2" noValidate>
        <h1 className="text-xl font-bold">Thông tin giao hàng</h1>
        <Alert>{error}</Alert>
        {field('fullName', 'Họ tên người nhận', { autoComplete: 'name' })}
        {field('phone', 'Số điện thoại', { inputMode: 'tel', autoComplete: 'tel', placeholder: '0912345678' })}
        {field('address', 'Địa chỉ giao hàng', { autoComplete: 'street-address', placeholder: 'Số nhà, đường, phường, quận, tỉnh' })}
        <div className="rounded-lg border border-brand-100 bg-brand-50 p-3 text-sm">
          <p className="font-semibold text-brand-700">Phương thức thanh toán: COD</p>
          <p className="text-slate-600">Thanh toán bằng tiền mặt khi nhận hàng.</p>
        </div>
        <div className="flex gap-2">
          <Link to="/cart" className="btn-outline">
            ← Giỏ hàng
          </Link>
          <button type="submit" className="btn-primary flex-1" disabled={submitting}>
            {submitting ? 'Đang đặt hàng...' : `Xác nhận đặt hàng · ${formatPrice(total)}`}
          </button>
        </div>
      </form>
      <aside className="card h-fit space-y-3 p-5">
        <h2 className="font-semibold">Đơn hàng ({items.length})</h2>
        {items.map((i) => (
          <div key={i.id} className="flex justify-between gap-2 text-sm">
            <span className="text-slate-600">
              {i.name} × {i.quantity}
            </span>
            <span className="font-medium">{formatPrice(i.price * i.quantity)}</span>
          </div>
        ))}
        <div className="flex justify-between border-t pt-3 font-bold">
          <span>Tổng</span>
          <span className="text-brand-600">{formatPrice(total)}</span>
        </div>
      </aside>
    </div>
  );
}
