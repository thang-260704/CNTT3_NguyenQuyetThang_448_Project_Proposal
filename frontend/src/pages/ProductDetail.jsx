import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api, { errorMessage } from '../api/client';
import { useCartStore } from '../store/cartStore';
import { formatPrice, PLACEHOLDER_IMAGE } from '../utils/format';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const addItem = useCartStore((s) => s.addItem);
  const [product, setProduct] = useState(null);
  const [qty, setQty] = useState(1);
  const [error, setError] = useState('');
  const [added, setAdded] = useState(false);

  useEffect(() => {
    setProduct(null);
    setError('');
    api
      .get(`/products/${id}`)
      .then((r) => setProduct(r.data))
      .catch((err) => setError(errorMessage(err, 'Không tìm thấy sản phẩm')));
  }, [id]);

  if (error) {
    return (
      <div className="space-y-4">
        <Alert>{error}</Alert>
        <Link to="/" className="btn-outline">
          ← Quay lại cửa hàng
        </Link>
      </div>
    );
  }
  if (!product) return <Spinner />;

  const outOfStock = product.stock <= 0;

  const add = (goToCart) => {
    addItem(product, qty);
    if (goToCart) navigate('/cart');
    else {
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    }
  };

  return (
    <div className="space-y-4">
      <Link to="/" className="text-sm text-slate-500 hover:text-brand-600">
        ← Tất cả sản phẩm
      </Link>
      <div className="card grid gap-6 p-4 md:grid-cols-2 md:p-6">
        <img
          src={product.imageUrl || PLACEHOLDER_IMAGE}
          alt={product.name}
          onError={(e) => {
            e.currentTarget.src = PLACEHOLDER_IMAGE;
          }}
          className="aspect-[4/3] w-full rounded-xl object-cover"
        />
        <div className="flex flex-col gap-4">
          {product.category && <span className="text-sm font-medium uppercase text-slate-400">{product.category.name}</span>}
          <h1 className="text-2xl font-bold text-slate-900">{product.name}</h1>
          <p className="text-3xl font-extrabold text-brand-600">{formatPrice(product.price)}</p>
          <p className={`text-sm font-medium ${outOfStock ? 'text-red-600' : 'text-emerald-600'}`}>
            {outOfStock ? 'Tạm hết hàng' : `Còn ${product.stock} sản phẩm`}
          </p>
          <p className="whitespace-pre-line leading-relaxed text-slate-600">{product.description}</p>

          {!outOfStock && (
            <div className="mt-auto space-y-3">
              <div className="flex items-center gap-3">
                <span className="text-sm text-slate-600">Số lượng</span>
                <div className="flex items-center rounded-lg border border-slate-300">
                  <button type="button" className="px-3 py-1.5" onClick={() => setQty(Math.max(1, qty - 1))} aria-label="Giảm">
                    −
                  </button>
                  <span className="w-10 text-center" data-testid="qty">
                    {qty}
                  </span>
                  <button type="button" className="px-3 py-1.5" onClick={() => setQty(Math.min(product.stock, qty + 1))} aria-label="Tăng">
                    +
                  </button>
                </div>
              </div>
              <div className="flex flex-col gap-2 sm:flex-row">
                <button type="button" className="btn-outline flex-1" onClick={() => add(false)}>
                  {added ? '✓ Đã thêm' : 'Thêm vào giỏ hàng'}
                </button>
                <button type="button" className="btn-primary flex-1" onClick={() => add(true)}>
                  Mua ngay
                </button>
              </div>
              <p className="text-xs text-slate-400">Thanh toán khi nhận hàng (COD) · Đổi trả trong 7 ngày</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
