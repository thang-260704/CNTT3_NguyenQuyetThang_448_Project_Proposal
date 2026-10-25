import { useCallback, useEffect, useState } from 'react';
import api, { errorMessage, fieldErrors } from '../../api/client';
import { formatPrice, PLACEHOLDER_IMAGE } from '../../utils/format';
import Spinner from '../../components/Spinner';
import Alert from '../../components/Alert';

const EMPTY = { name: '', description: '', price: '', stock: '', imageUrl: '', categoryId: '' };

export default function AdminProducts() {
  const [products, setProducts] = useState(null);
  const [categories, setCategories] = useState([]);
  const [keyword, setKeyword] = useState('');
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [errors, setErrors] = useState({});
  const [msg, setMsg] = useState({ type: '', text: '' });
  const [aiLoading, setAiLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const load = useCallback(() => {
    api
      .get('/products', { params: { keyword: keyword || undefined, size: 100 } })
      .then((r) => setProducts(r.data.content))
      .catch((err) => setMsg({ type: 'error', text: errorMessage(err) }));
  }, [keyword]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    api.get('/categories').then((r) => setCategories(r.data));
  }, []);

  const openCreate = () => {
    setForm(EMPTY);
    setEditingId(null);
    setErrors({});
    setShowForm(true);
  };

  const openEdit = (p) => {
    setForm({
      name: p.name,
      description: p.description || '',
      price: String(p.price),
      stock: String(p.stock),
      imageUrl: p.imageUrl || '',
      categoryId: p.category ? String(p.category.id) : '',
    });
    setEditingId(p.id);
    setErrors({});
    setShowForm(true);
    window.scrollTo?.({ top: 0, behavior: 'smooth' });
  };

  const generateDescription = async () => {
    if (!form.name.trim()) {
      setErrors({ name: 'Nhập tên sản phẩm trước khi dùng AI' });
      return;
    }
    setAiLoading(true);
    try {
      const category = categories.find((c) => String(c.id) === form.categoryId)?.name;
      const { data } = await api.post('/admin/ai/description', { name: form.name.trim(), category });
      setForm((f) => ({ ...f, description: data.description }));
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err, 'AI chưa sinh được mô tả') });
    } finally {
      setAiLoading(false);
    }
  };

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setErrors({});
    const payload = {
      name: form.name,
      description: form.description,
      price: form.price === '' ? null : Number(form.price),
      stock: form.stock === '' ? null : Number(form.stock),
      imageUrl: form.imageUrl || null,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
    };
    try {
      if (editingId) await api.put(`/admin/products/${editingId}`, payload);
      else await api.post('/admin/products', payload);
      setMsg({ type: 'success', text: editingId ? 'Đã cập nhật sản phẩm' : 'Đã thêm sản phẩm mới' });
      setShowForm(false);
      load();
    } catch (err) {
      setErrors(fieldErrors(err));
      setMsg({ type: 'error', text: errorMessage(err) });
    } finally {
      setSaving(false);
    }
  };

  const remove = async (p) => {
    if (!window.confirm(`Xóa sản phẩm "${p.name}"?`)) return;
    try {
      await api.delete(`/admin/products/${p.id}`);
      setMsg({ type: 'success', text: 'Đã xóa sản phẩm' });
      load();
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const generateMock = async () => {
    try {
      const { data } = await api.post('/admin/ai/mock-products', null, { params: { count: 4 } });
      setMsg({ type: 'success', text: `AI đã tạo ${data.length} sản phẩm mẫu` });
      load();
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const input = (name, label, props = {}) => (
    <div>
      <label className="label" htmlFor={`p-${name}`}>
        {label}
      </label>
      <input id={`p-${name}`} className="input" value={form[name]} onChange={(e) => setForm({ ...form, [name]: e.target.value })} {...props} />
      {errors[name] && <p className="field-error">{errors[name]}</p>}
    </div>
  );

  return (
    <div className="space-y-4">
      <Alert type={msg.type}>{msg.text}</Alert>

      {showForm && (
        <form onSubmit={save} className="card grid gap-4 p-5 md:grid-cols-2" noValidate>
          <h2 className="text-lg font-bold md:col-span-2">{editingId ? `Sửa sản phẩm #${editingId}` : 'Thêm sản phẩm mới'}</h2>
          {input('name', 'Tên sản phẩm')}
          <div>
            <label className="label" htmlFor="p-category">
              Danh mục
            </label>
            <select id="p-category" className="input" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">-- Không chọn --</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          {input('price', 'Giá (VNĐ)', { type: 'number', min: 0 })}
          {input('stock', 'Tồn kho', { type: 'number', min: 0 })}
          <div className="md:col-span-2">{input('imageUrl', 'URL ảnh', { placeholder: 'https://...' })}</div>
          <div className="md:col-span-2">
            <div className="mb-1 flex items-center justify-between">
              <label className="label mb-0" htmlFor="p-description">
                Mô tả
              </label>
              <button type="button" className="btn bg-violet-600 px-3 py-1 text-xs text-white hover:bg-violet-700" onClick={generateDescription} disabled={aiLoading}>
                {aiLoading ? 'AI đang viết...' : '✨ AI viết mô tả'}
              </button>
            </div>
            <textarea id="p-description" rows={5} className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="flex gap-2 md:col-span-2">
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Đang lưu...' : 'Lưu'}
            </button>
            <button type="button" className="btn-outline" onClick={() => setShowForm(false)}>
              Hủy
            </button>
          </div>
        </form>
      )}

      <div className="flex flex-col gap-2 sm:flex-row">
        <input className="input sm:max-w-xs" placeholder="Tìm theo tên..." value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        <div className="flex gap-2 sm:ml-auto">
          <button type="button" className="btn-outline" onClick={generateMock}>
            ✨ AI tạo dữ liệu mẫu
          </button>
          <button type="button" className="btn-primary" onClick={openCreate}>
            + Thêm sản phẩm
          </button>
        </div>
      </div>

      {!products ? (
        <Spinner />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full min-w-[640px] text-sm">
            <thead className="bg-slate-50 text-left text-slate-500">
              <tr>
                <th className="p-3">Sản phẩm</th>
                <th className="p-3">Danh mục</th>
                <th className="p-3 text-right">Giá</th>
                <th className="p-3 text-right">Tồn kho</th>
                <th className="p-3" />
              </tr>
            </thead>
            <tbody className="divide-y">
              {products.map((p) => (
                <tr key={p.id}>
                  <td className="p-3">
                    <div className="flex items-center gap-3">
                      <img src={p.imageUrl || PLACEHOLDER_IMAGE} alt="" className="h-10 w-10 rounded object-cover" />
                      <span className="font-medium">{p.name}</span>
                    </div>
                  </td>
                  <td className="p-3 text-slate-500">{p.category?.name || '-'}</td>
                  <td className="p-3 text-right">{formatPrice(p.price)}</td>
                  <td className={`p-3 text-right ${p.stock === 0 ? 'font-semibold text-red-600' : ''}`}>{p.stock}</td>
                  <td className="p-3 text-right">
                    <div className="flex justify-end gap-2">
                      <button type="button" className="btn-outline px-3 py-1" onClick={() => openEdit(p)}>
                        Sửa
                      </button>
                      <button type="button" className="btn-danger px-3 py-1" onClick={() => remove(p)}>
                        Xóa
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {products.length === 0 && (
                <tr>
                  <td colSpan={5} className="p-6 text-center text-slate-500">
                    Không có sản phẩm
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
