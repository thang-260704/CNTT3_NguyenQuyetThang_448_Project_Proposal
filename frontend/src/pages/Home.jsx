import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api, { errorMessage } from '../api/client';
import ProductCard from '../components/ProductCard';
import Spinner from '../components/Spinner';
import Alert from '../components/Alert';

const PAGE_SIZE = 12;

export default function Home() {
  const [params, setParams] = useSearchParams();
  const keyword = params.get('keyword') || '';
  const categoryId = params.get('categoryId') || '';
  const sort = params.get('sort') || '';
  const page = Number(params.get('page') || 0);

  const [input, setInput] = useState(keyword);
  const [categories, setCategories] = useState([]);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data)).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    setInput(keyword);
  }, [keyword]);

  useEffect(() => {
    setLoading(true);
    setError('');
    api
      .get('/products', {
        params: { keyword: keyword || undefined, categoryId: categoryId || undefined, sort: sort || undefined, page, size: PAGE_SIZE },
      })
      .then((r) => setData(r.data))
      .catch((err) => setError(errorMessage(err, 'Không tải được danh sách sản phẩm')))
      .finally(() => setLoading(false));
  }, [keyword, categoryId, sort, page]);

  const update = (changes) => {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([k, v]) => (v === '' || v === undefined || v === null ? next.delete(k) : next.set(k, v)));
    if (!('page' in changes)) next.delete('page');
    setParams(next);
  };

  const onAdded = (product) => {
    setToast(`Đã thêm "${product.name}" vào giỏ hàng`);
    setTimeout(() => setToast(''), 2500);
  };

  return (
    <div className="space-y-6">
      <section className="rounded-2xl bg-gradient-to-r from-brand-600 to-rose-400 px-6 py-10 text-white sm:px-10">
        <h1 className="text-2xl font-extrabold sm:text-4xl">Mua sắm nhanh, thanh toán khi nhận hàng</h1>
        <p className="mt-2 max-w-xl text-brand-50">Hàng chính hãng, giao nhanh toàn quốc. Cần tư vấn? Hỏi trợ lý AI ở góc phải màn hình.</p>
        <form
          className="mt-6 flex max-w-xl gap-2"
          onSubmit={(e) => {
            e.preventDefault();
            update({ keyword: input.trim() });
          }}
        >
          <input
            className="input border-0 text-slate-800"
            placeholder="Tìm sản phẩm theo tên..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            aria-label="Từ khóa tìm kiếm"
          />
          <button type="submit" className="btn bg-slate-900 text-white hover:bg-slate-800">
            Tìm kiếm
          </button>
        </form>
      </section>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className={`rounded-full px-4 py-1.5 text-sm font-medium ${!categoryId ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 ring-1 ring-slate-200'}`}
            onClick={() => update({ categoryId: '' })}
          >
            Tất cả
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`rounded-full px-4 py-1.5 text-sm font-medium ${String(c.id) === categoryId ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 ring-1 ring-slate-200'}`}
              onClick={() => update({ categoryId: c.id })}
            >
              {c.name}
            </button>
          ))}
        </div>
        <select className="input sm:w-48" value={sort} onChange={(e) => update({ sort: e.target.value })} aria-label="Sắp xếp">
          <option value="">Mới nhất</option>
          <option value="price_asc">Giá tăng dần</option>
          <option value="price_desc">Giá giảm dần</option>
          <option value="name_asc">Tên A-Z</option>
        </select>
      </div>

      {keyword && (
        <p className="text-sm text-slate-600">
          Kết quả cho “<b>{keyword}</b>”: {data?.totalElements ?? 0} sản phẩm{' '}
          <button type="button" className="text-brand-600 underline" onClick={() => update({ keyword: '' })}>
            Xóa tìm kiếm
          </button>
        </p>
      )}

      <Alert>{error}</Alert>
      {loading ? (
        <Spinner />
      ) : data?.content?.length ? (
        <div className="grid grid-cols-2 gap-3 sm:gap-5 md:grid-cols-3 lg:grid-cols-4">
          {data.content.map((p) => (
            <ProductCard key={p.id} product={p} onAdded={onAdded} />
          ))}
        </div>
      ) : (
        !error && <p className="py-16 text-center text-slate-500">Không tìm thấy sản phẩm phù hợp.</p>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button type="button" className="btn-outline" disabled={page === 0} onClick={() => update({ page: page - 1 })}>
            ← Trước
          </button>
          <span className="text-sm text-slate-600">
            Trang {page + 1}/{data.totalPages}
          </span>
          <button type="button" className="btn-outline" disabled={page + 1 >= data.totalPages} onClick={() => update({ page: page + 1 })}>
            Sau →
          </button>
        </div>
      )}

      {toast && (
        <div className="fixed bottom-24 left-1/2 z-50 -translate-x-1/2 rounded-lg bg-slate-900 px-4 py-2 text-sm text-white shadow-lg">{toast}</div>
      )}
    </div>
  );
}
