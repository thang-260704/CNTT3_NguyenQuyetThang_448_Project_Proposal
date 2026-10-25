import { useEffect, useState } from 'react';
import api, { errorMessage, fieldErrors } from '../api/client';
import { useAuthStore } from '../store/authStore';
import Alert from '../components/Alert';

export default function Profile() {
  const setUser = useAuthStore((s) => s.setUser);
  const [profile, setProfile] = useState({ username: '', email: '', fullName: '', role: '' });
  const [msg, setMsg] = useState({ type: '', text: '' });
  const [errors, setErrors] = useState({});
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '' });
  const [pwMsg, setPwMsg] = useState({ type: '', text: '' });

  useEffect(() => {
    api.get('/users/me').then((r) => setProfile(r.data));
  }, []);

  const save = async (e) => {
    e.preventDefault();
    setErrors({});
    try {
      const { data } = await api.put('/users/me', { email: profile.email, fullName: profile.fullName });
      setProfile(data);
      setUser(data);
      setMsg({ type: 'success', text: 'Đã cập nhật hồ sơ' });
    } catch (err) {
      setErrors(fieldErrors(err));
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const changePassword = async (e) => {
    e.preventDefault();
    if (pw.newPassword.length < 6) {
      setPwMsg({ type: 'error', text: 'Mật khẩu mới tối thiểu 6 ký tự' });
      return;
    }
    try {
      await api.put('/users/me/password', pw);
      setPw({ currentPassword: '', newPassword: '' });
      setPwMsg({ type: 'success', text: 'Đổi mật khẩu thành công' });
    } catch (err) {
      setPwMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  return (
    <div className="mx-auto grid max-w-4xl gap-6 md:grid-cols-2">
      <form onSubmit={save} className="card space-y-4 p-6">
        <h1 className="text-xl font-bold">Hồ sơ cá nhân</h1>
        <Alert type={msg.type}>{msg.text}</Alert>
        <div>
          <span className="label">Tên đăng nhập</span>
          <p className="text-sm text-slate-700">
            {profile.username} <span className="ml-2 rounded bg-slate-100 px-2 py-0.5 text-xs">{profile.role}</span>
          </p>
        </div>
        <div>
          <label className="label" htmlFor="email">
            Email
          </label>
          <input id="email" className="input" value={profile.email} onChange={(e) => setProfile({ ...profile, email: e.target.value })} />
          {errors.email && <p className="field-error">{errors.email}</p>}
        </div>
        <div>
          <label className="label" htmlFor="fullName">
            Họ và tên
          </label>
          <input id="fullName" className="input" value={profile.fullName || ''} onChange={(e) => setProfile({ ...profile, fullName: e.target.value })} />
        </div>
        <button type="submit" className="btn-primary">
          Lưu thay đổi
        </button>
      </form>

      <form onSubmit={changePassword} className="card h-fit space-y-4 p-6">
        <h2 className="text-xl font-bold">Đổi mật khẩu</h2>
        <Alert type={pwMsg.type}>{pwMsg.text}</Alert>
        <div>
          <label className="label" htmlFor="currentPassword">
            Mật khẩu hiện tại
          </label>
          <input id="currentPassword" type="password" className="input" value={pw.currentPassword} onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} />
        </div>
        <div>
          <label className="label" htmlFor="newPassword">
            Mật khẩu mới
          </label>
          <input id="newPassword" type="password" className="input" value={pw.newPassword} onChange={(e) => setPw({ ...pw, newPassword: e.target.value })} />
        </div>
        <button type="submit" className="btn-outline">
          Đổi mật khẩu
        </button>
      </form>
    </div>
  );
}
