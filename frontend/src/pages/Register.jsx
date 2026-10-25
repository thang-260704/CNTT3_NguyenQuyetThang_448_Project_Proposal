import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api, { errorMessage, fieldErrors } from '../api/client';
import { useAuthStore } from '../store/authStore';
import Alert from '../components/Alert';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', fullName: '', password: '', confirm: '' });
  const [errors, setErrors] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const login = useAuthStore((s) => s.login);
  const navigate = useNavigate();

  const validate = () => {
    const e = {};
    if (!/^[a-zA-Z0-9_.]{3,50}$/.test(form.username)) e.username = 'Tên đăng nhập 3-50 ký tự, chỉ gồm chữ, số, _ và .';
    if (!EMAIL_REGEX.test(form.email)) e.email = 'Email không hợp lệ';
    if (form.password.length < 6) e.password = 'Mật khẩu tối thiểu 6 ký tự';
    if (form.confirm !== form.password) e.confirm = 'Mật khẩu nhập lại không khớp';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    setError('');
    try {
      const { confirm: _confirm, ...payload } = form;
      const { data } = await api.post('/auth/register', payload);
      login(data);
      navigate('/', { replace: true });
    } catch (err) {
      setErrors(fieldErrors(err));
      setError(errorMessage(err, 'Đăng ký thất bại'));
    } finally {
      setLoading(false);
    }
  };

  const field = (name, label, type = 'text', autoComplete) => (
    <div>
      <label className="label" htmlFor={name}>
        {label}
      </label>
      <input id={name} type={type} autoComplete={autoComplete} className="input" value={form[name]} onChange={(e) => setForm({ ...form, [name]: e.target.value })} />
      {errors[name] && <p className="field-error">{errors[name]}</p>}
    </div>
  );

  return (
    <div className="mx-auto max-w-md">
      <form onSubmit={submit} className="card space-y-4 p-6" noValidate>
        <h1 className="text-2xl font-bold">Tạo tài khoản</h1>
        <Alert>{error}</Alert>
        {field('username', 'Tên đăng nhập', 'text', 'username')}
        {field('email', 'Email', 'email', 'email')}
        {field('fullName', 'Họ và tên', 'text', 'name')}
        {field('password', 'Mật khẩu', 'password', 'new-password')}
        {field('confirm', 'Nhập lại mật khẩu', 'password', 'new-password')}
        <button type="submit" className="btn-primary w-full" disabled={loading}>
          {loading ? 'Đang tạo tài khoản...' : 'Đăng ký'}
        </button>
        <p className="text-center text-sm text-slate-600">
          Đã có tài khoản?{' '}
          <Link to="/login" className="font-semibold text-brand-600">
            Đăng nhập
          </Link>
        </p>
      </form>
    </div>
  );
}
