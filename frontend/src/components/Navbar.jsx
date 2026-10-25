import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useCartStore } from '../store/cartStore';

const linkClass = ({ isActive }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-brand-50 text-brand-600' : 'text-slate-600 hover:text-brand-600'}`;

export default function Navbar() {
  const { user, logout } = useAuthStore();
  const count = useCartStore((s) => s.items.reduce((sum, i) => sum + i.quantity, 0));
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setOpen(false);
    navigate('/');
  };

  const links = (
    <>
      <NavLink to="/" end className={linkClass} onClick={() => setOpen(false)}>
        Sản phẩm
      </NavLink>
      {user && (
        <NavLink to="/orders" className={linkClass} onClick={() => setOpen(false)}>
          Đơn hàng của tôi
        </NavLink>
      )}
      {user?.role === 'ADMIN' && (
        <NavLink to="/admin" className={linkClass} onClick={() => setOpen(false)}>
          Quản trị
        </NavLink>
      )}
    </>
  );

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-6xl items-center gap-4 px-4">
        <Link to="/" className="flex items-center gap-2 text-lg font-extrabold text-slate-900">
          <img src="/favicon.svg" alt="" className="h-8 w-8" />
          <span>
            Quick<span className="text-brand-500">Shop</span>
          </span>
        </Link>

        <nav className="hidden items-center gap-1 md:flex">{links}</nav>

        <div className="ml-auto flex items-center gap-2">
          <Link to="/cart" className="relative rounded-md p-2 text-slate-600 hover:text-brand-600" aria-label="Giỏ hàng">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M3 3h2l.4 2M7 13h10l3-8H5.4M7 13L5.4 5M7 13l-2 5h13M9 21a1 1 0 100-2 1 1 0 000 2zm8 0a1 1 0 100-2 1 1 0 000 2z" />
            </svg>
            {count > 0 && (
              <span data-testid="cart-count" className="absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-brand-500 px-1 text-xs font-bold text-white">
                {count}
              </span>
            )}
          </Link>

          <div className="hidden items-center gap-2 md:flex">
            {user ? (
              <>
                <Link to="/profile" className="text-sm font-medium text-slate-700 hover:text-brand-600">
                  {user.fullName || user.username}
                </Link>
                <button type="button" className="btn-outline" onClick={handleLogout}>
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-outline">
                  Đăng nhập
                </Link>
                <Link to="/register" className="btn-primary">
                  Đăng ký
                </Link>
              </>
            )}
          </div>

          <button type="button" className="rounded-md p-2 text-slate-600 md:hidden" onClick={() => setOpen(!open)} aria-label="Menu">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
      </div>

      {open && (
        <div className="border-t border-slate-200 bg-white px-4 py-3 md:hidden">
          <nav className="flex flex-col gap-1">{links}</nav>
          <div className="mt-3 flex gap-2 border-t border-slate-100 pt-3">
            {user ? (
              <>
                <Link to="/profile" className="btn-outline flex-1" onClick={() => setOpen(false)}>
                  Hồ sơ
                </Link>
                <button type="button" className="btn-outline flex-1" onClick={handleLogout}>
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-outline flex-1" onClick={() => setOpen(false)}>
                  Đăng nhập
                </Link>
                <Link to="/register" className="btn-primary flex-1" onClick={() => setOpen(false)}>
                  Đăng ký
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
