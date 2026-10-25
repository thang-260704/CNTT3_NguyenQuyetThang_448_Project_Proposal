import { Link } from 'react-router-dom';
import { useCartStore } from '../store/cartStore';
import { formatPrice, PLACEHOLDER_IMAGE } from '../utils/format';

export default function ProductCard({ product, onAdded }) {
  const addItem = useCartStore((s) => s.addItem);
  const outOfStock = product.stock <= 0;

  const handleAdd = () => {
    addItem(product, 1);
    onAdded?.(product);
  };

  return (
    <div className="card flex flex-col overflow-hidden transition hover:-translate-y-0.5 hover:shadow-md">
      <Link to={`/products/${product.id}`} className="block aspect-[4/3] overflow-hidden bg-slate-100">
        <img
          src={product.imageUrl || PLACEHOLDER_IMAGE}
          alt={product.name}
          loading="lazy"
          onError={(e) => {
            e.currentTarget.src = PLACEHOLDER_IMAGE;
          }}
          className="h-full w-full object-cover"
        />
      </Link>
      <div className="flex flex-1 flex-col gap-2 p-4">
        {product.category && (
          <span className="text-xs font-medium uppercase tracking-wide text-slate-400">{product.category.name}</span>
        )}
        <Link to={`/products/${product.id}`} className="line-clamp-2 font-semibold text-slate-800 hover:text-brand-600">
          {product.name}
        </Link>
        <div className="mt-auto flex items-center justify-between pt-2">
          <span className="text-lg font-bold text-brand-600">{formatPrice(product.price)}</span>
          <span className="text-xs text-slate-400">{outOfStock ? 'Hết hàng' : `Còn ${product.stock}`}</span>
        </div>
        <button type="button" className="btn-primary w-full" disabled={outOfStock} onClick={handleAdd}>
          {outOfStock ? 'Hết hàng' : 'Thêm vào giỏ hàng'}
        </button>
      </div>
    </div>
  );
}
