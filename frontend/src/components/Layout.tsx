import { Link, NavLink, useNavigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from '../auth/AuthContext';
import type { Role } from '../types/api';

interface NavItem {
  to: string;
  label: string;
  roles?: Role[];
}

const NAV: NavItem[] = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/patients', label: 'Patients', roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/doctors', label: 'Doctors', roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/appointments', label: 'Appointments', roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/billing', label: 'Billing', roles: ['ADMIN', 'PATIENT'] },
  { to: '/lab', label: 'Lab Tests', roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
];

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate('/login');
  };

  const visible = NAV.filter((n) => !n.roles || (user && n.roles.includes(user.role)));

  return (
    <div className="flex h-full flex-col">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-3">
          <Link to="/dashboard" className="text-lg font-semibold text-brand-700">
            HMS
          </Link>
          <nav className="hidden gap-4 md:flex">
            {visible.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `text-sm ${isActive ? 'font-semibold text-brand-700' : 'text-slate-600 hover:text-slate-900'}`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-3 text-sm">
            {user && (
              <>
                <span className="text-slate-500">
                  {user.email} <span className="ml-1 rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-700">{user.role}</span>
                </span>
                <button
                  type="button"
                  onClick={onLogout}
                  className="rounded border border-slate-300 px-3 py-1 text-slate-700 hover:bg-slate-100"
                >
                  Sign out
                </button>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="flex-1 overflow-auto">
        <div className="mx-auto max-w-7xl px-6 py-6">{children}</div>
      </main>
    </div>
  );
}
