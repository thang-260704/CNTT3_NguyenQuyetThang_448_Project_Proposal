import { NavLink, Outlet } from 'react-router-dom';

const tab = ({ isActive }) =>
  `whitespace-nowrap rounded-lg px-4 py-2 text-sm font-semibold ${isActive ? 'bg-slate-900 text-white' : 'bg-white text-slate-600 ring-1 ring-slate-200 hover:text-brand-600'}`;

export default function AdminLayout() {
  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="text-2xl font-bold">Trang quản trị</h1>
        <nav className="flex gap-2 overflow-x-auto">
          <NavLink to="/admin" end className={tab}>
            Tổng quan
          </NavLink>
          <NavLink to="/admin/products" className={tab}>
            Sản phẩm
          </NavLink>
          <NavLink to="/admin/orders" className={tab}>
            Đơn hàng
          </NavLink>
        </nav>
      </div>
      <Outlet />
    </div>
  );
}
