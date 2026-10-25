import { Link, useNavigate } from 'react-router-dom';
import { useCartStore } from '../store/cartStore';
import { useAuthStore } from '../store/authStore';
import { formatPrice, PLACEHOLDER_IMAGE } from '../utils/format';

export default function Cart() {
  const { items, updateQuantity, removeItem, clear } = useCartStore();
  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  const token = useAuthStore((s) => s.token);
  const navigate = useNavigate();

  if (items.length === 0) {
    return (
      <div className="card flex flex-col items-center gap-4 py-16 text-center">
        <p className="text-lg font-semibold text-slate-700">Giỏ hàng đang trống</p>
        <Link to="/" className="btn-primary">
          Tiếp tục mua sắm
        </Link>
      </div>
    );
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <div className="space-y-3 lg:col-span-2">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-bold">Giỏ hàng ({items.length} sản phẩm)</h1>
          <button type="button" className="text-sm text-slate-500 hover:text-red-600" onClick={clear}>
            Xóa tất cả
          </button>
        </div>
        {items.map((item) => (
          <div key={item.id} className="card flex gap-3 p-3 sm:items-center">
            <img src={item.imageUrl || PLACEHOLDER_IMAGE} alt={item.name} className="h-20 w-20 flex-shrink-0 rounded-lg object-cover" />
            <div className="flex flex-1 flex-col gap-2 sm:flex-row sm:items-center">
              <div className="flex-1">
                <Link to={`/products/${item.id}`} className="font-medium text-slate-800 hover:text-brand-600">
                  {item.name}
                </Link>
                <p className="text-sm text-slate-500">{formatPrice(item.price)}</p>
              </div>
              <div className="flex items-center gap-3">
                <div className="flex items-center rounded-lg border border-slate-300">
                  <button type="button" className="px-3 py-1" onClick={() => updateQuantity(item.id, item.quantity - 1)} aria-label={`Giảm ${item.name}`}>
                    −
                  </button>
                  <span className="w-8 text-center text-sm">{item.quantity}</span>
                  <button type="button" className="px-3 py-1" onClick={() => updateQuantity(item.id, item.quantity + 1)} aria-label={`Tăng ${item.name}`}>
                    +
                  </button>
                </div>
                <span className="w-28 text-right font-semibold">{formatPrice(item.price * item.quantity)}</span>
                <button type="button" className="text-slate-400 hover:text-red-600" onClick={() => removeItem(item.id)} aria-label={`Xóa ${item.name}`}>
                  ✕
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
      <aside className="card h-fit space-y-4 p-5">
        <h2 className="font-semibold">Tóm tắt đơn hàng</h2>
        <div className="flex justify-between text-sm text-slate-600">
          <span>Tạm tính</span>
          <span>{formatPrice(total)}</span>
        </div>
        <div className="flex justify-between text-sm text-slate-600">
          <span>Phí vận chuyển</span>
          <span>Miễn phí</span>
        </div>
        <div className="flex justify-between border-t pt-3 text-lg font-bold">
          <span>Tổng cộng</span>
          <span className="text-brand-600">{formatPrice(total)}</span>
        </div>
        <button
          type="button"
          className="btn-primary w-full"
          onClick={() => navigate(token ? '/checkout' : '/login', { state: { from: '/checkout' } })}
        >
          {token ? 'Tiến hành đặt hàng' : 'Đăng nhập để đặt hàng'}
        </button>
      </aside>
    </div>
  );
}
