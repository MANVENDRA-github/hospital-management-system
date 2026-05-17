import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import type { Role } from '../types/api';

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  roles?: Role[];
}

const iconClass = 'h-5 w-5 shrink-0';

const DashboardIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <rect x="3" y="3" width="7" height="9" rx="1.5" />
    <rect x="14" y="3" width="7" height="5" rx="1.5" />
    <rect x="14" y="12" width="7" height="9" rx="1.5" />
    <rect x="3" y="16" width="7" height="5" rx="1.5" />
  </svg>
);
const PatientsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
);
const DoctorsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <path d="M9 12h6" />
    <path d="M12 9v6" />
    <circle cx="12" cy="12" r="9" />
  </svg>
);
const AppointmentsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <rect x="3" y="4" width="18" height="18" rx="2" />
    <line x1="16" y1="2" x2="16" y2="6" />
    <line x1="8" y1="2" x2="8" y2="6" />
    <line x1="3" y1="10" x2="21" y2="10" />
  </svg>
);
const BillingIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <rect x="2" y="5" width="20" height="14" rx="2" />
    <line x1="2" y1="10" x2="22" y2="10" />
  </svg>
);
const LabIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <path d="M9 2v6.5L4 18a2 2 0 0 0 1.8 3h12.4A2 2 0 0 0 20 18l-5-9.5V2" />
    <line x1="9" y1="2" x2="15" y2="2" />
    <line x1="7" y1="13" x2="17" y2="13" />
  </svg>
);
const LogoutIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={iconClass} aria-hidden="true">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <polyline points="16 17 21 12 16 7" />
    <line x1="21" y1="12" x2="9" y2="12" />
  </svg>
);
const MenuIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
    <line x1="3" y1="6" x2="21" y2="6" />
    <line x1="3" y1="12" x2="21" y2="12" />
    <line x1="3" y1="18" x2="21" y2="18" />
  </svg>
);

const NAV: NavItem[] = [
  { to: '/dashboard', label: 'Dashboard', icon: DashboardIcon },
  { to: '/patients', label: 'Patients', icon: PatientsIcon, roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/doctors', label: 'Doctors', icon: DoctorsIcon, roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/appointments', label: 'Appointments', icon: AppointmentsIcon, roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
  { to: '/billing', label: 'Billing', icon: BillingIcon, roles: ['ADMIN', 'PATIENT'] },
  { to: '/lab', label: 'Lab Tests', icon: LabIcon, roles: ['ADMIN', 'DOCTOR', 'PATIENT'] },
];

function pageTitle(pathname: string): string {
  const match = NAV.find((n) => pathname.startsWith(n.to));
  return match?.label ?? 'HMS';
}

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const onLogout = () => {
    logout();
    navigate('/login');
  };

  const visible = NAV.filter((n) => !n.roles || (user && n.roles.includes(user.role)));
  const initial = user?.email?.[0]?.toUpperCase() ?? '?';
  const title = pageTitle(location.pathname);

  const SidebarBody = (
    <div className="flex h-full flex-col">
      <Link to="/dashboard" className="flex items-center gap-2.5 px-5 py-5">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white shadow-sm">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
            <path d="M12 3v18M3 12h18" />
          </svg>
        </div>
        <div className="flex flex-col leading-tight">
          <span className="text-base font-semibold tracking-tight text-slate-900">HMS</span>
          <span className="text-[11px] font-medium uppercase tracking-wider text-slate-400">Hospital</span>
        </div>
      </Link>

      <nav className="flex-1 space-y-0.5 px-3">
        <div className="px-2 pb-2 pt-3 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
          Workspace
        </div>
        {visible.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={() => setMobileOpen(false)}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
                isActive
                  ? 'bg-brand-50 text-brand-700'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
              }`
            }
          >
            {item.icon}
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {user && (
        <div className="border-t border-slate-200 p-3">
          <div className="flex items-center gap-3 rounded-lg px-2 py-2">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700 ring-1 ring-inset ring-brand-200">
              {initial}
            </div>
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-medium text-slate-900">{user.email}</div>
              <div className="text-[11px] font-medium uppercase tracking-wider text-slate-500">{user.role}</div>
            </div>
          </div>
          <button
            type="button"
            onClick={onLogout}
            className="mt-2 flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-slate-900"
          >
            {LogoutIcon}
            <span>Sign out</span>
          </button>
        </div>
      )}
    </div>
  );

  return (
    <div className="flex h-full">
      {/* Desktop sidebar */}
      <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white lg:block">
        {SidebarBody}
      </aside>

      {/* Mobile drawer */}
      {mobileOpen && (
        <>
          <div
            className="fixed inset-0 z-40 bg-slate-900/40 lg:hidden"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
          <aside className="fixed inset-y-0 left-0 z-50 w-64 border-r border-slate-200 bg-white shadow-xl lg:hidden">
            {SidebarBody}
          </aside>
        </>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-slate-200 bg-white/85 px-4 backdrop-blur supports-[backdrop-filter]:bg-white/70 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setMobileOpen(true)}
              className="rounded-md p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 lg:hidden"
              aria-label="Open menu"
            >
              {MenuIcon}
            </button>
            <h2 className="text-lg font-semibold tracking-tight text-slate-900">{title}</h2>
          </div>
          {user && (
            <div className="hidden items-center gap-2 sm:flex">
              <span className="text-sm text-slate-500">{user.email}</span>
              <span className="badge-brand">{user.role}</span>
            </div>
          )}
        </header>

        <main className="flex-1 overflow-auto">
          <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">{children}</div>
        </main>
      </div>
    </div>
  );
}
