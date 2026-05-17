import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

interface Tile {
  to: string;
  title: string;
  blurb: string;
  icon: ReactNode;
  accent: string;
}

const ChevronRight = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4" aria-hidden="true">
    <polyline points="9 18 15 12 9 6" />
  </svg>
);

const ICON_BASE = 'h-6 w-6';
const PatientsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={ICON_BASE} aria-hidden="true">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
  </svg>
);
const DoctorsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={ICON_BASE} aria-hidden="true">
    <path d="M9 12h6" />
    <path d="M12 9v6" />
    <circle cx="12" cy="12" r="9" />
  </svg>
);
const AppointmentsIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={ICON_BASE} aria-hidden="true">
    <rect x="3" y="4" width="18" height="18" rx="2" />
    <line x1="16" y1="2" x2="16" y2="6" />
    <line x1="8" y1="2" x2="8" y2="6" />
    <line x1="3" y1="10" x2="21" y2="10" />
  </svg>
);
const BillingIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={ICON_BASE} aria-hidden="true">
    <rect x="2" y="5" width="20" height="14" rx="2" />
    <line x1="2" y1="10" x2="22" y2="10" />
  </svg>
);
const LabIcon = (
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={ICON_BASE} aria-hidden="true">
    <path d="M9 2v6.5L4 18a2 2 0 0 0 1.8 3h12.4A2 2 0 0 0 20 18l-5-9.5V2" />
    <line x1="9" y1="2" x2="15" y2="2" />
    <line x1="7" y1="13" x2="17" y2="13" />
  </svg>
);

const TILES_BY_ROLE: Record<string, Tile[]> = {
  ADMIN: [
    { to: '/patients', title: 'Patients', blurb: 'Manage patient records', icon: PatientsIcon, accent: 'bg-sky-50 text-sky-600' },
    { to: '/doctors', title: 'Doctors', blurb: 'Manage doctor profiles', icon: DoctorsIcon, accent: 'bg-emerald-50 text-emerald-600' },
    { to: '/appointments', title: 'Appointments', blurb: 'All bookings, status, history', icon: AppointmentsIcon, accent: 'bg-violet-50 text-violet-600' },
    { to: '/billing', title: 'Billing', blurb: 'Invoices and payments', icon: BillingIcon, accent: 'bg-amber-50 text-amber-600' },
    { to: '/lab', title: 'Lab Tests', blurb: 'Tests and results', icon: LabIcon, accent: 'bg-rose-50 text-rose-600' },
  ],
  DOCTOR: [
    { to: '/patients', title: 'Patients', blurb: 'Look up patient records', icon: PatientsIcon, accent: 'bg-sky-50 text-sky-600' },
    { to: '/appointments', title: 'Appointments', blurb: 'Your appointments and lifecycle', icon: AppointmentsIcon, accent: 'bg-violet-50 text-violet-600' },
    { to: '/lab', title: 'Lab Tests', blurb: 'Order tests and upload results', icon: LabIcon, accent: 'bg-rose-50 text-rose-600' },
  ],
  PATIENT: [
    { to: '/appointments', title: 'My Appointments', blurb: 'Book and manage your appointments', icon: AppointmentsIcon, accent: 'bg-violet-50 text-violet-600' },
    { to: '/billing', title: 'My Bills', blurb: 'Outstanding and paid bills', icon: BillingIcon, accent: 'bg-amber-50 text-amber-600' },
    { to: '/lab', title: 'My Lab Tests', blurb: 'Your tests and results', icon: LabIcon, accent: 'bg-rose-50 text-rose-600' },
  ],
};

export default function Dashboard() {
  const { user } = useAuth();
  if (!user) return null;
  const tiles = TILES_BY_ROLE[user.role] ?? [];

  return (
    <div>
      <div className="mb-8 overflow-hidden rounded-2xl bg-gradient-to-br from-slate-800 via-slate-900 to-slate-950 p-8 text-white shadow-card">
        <div className="text-sm font-medium uppercase tracking-wider text-slate-300/80">Welcome back</div>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">{user.email}</h1>
        <p className="mt-2 max-w-xl text-sm text-slate-300">
          You are signed in as <span className="font-semibold text-white">{user.role}</span>. Pick a module below to get started.
        </p>
      </div>

      <div className="mb-3 flex items-center justify-between">
        <h2 className="section-title">Modules</h2>
        <span className="text-xs text-slate-400">{tiles.length} available</span>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {tiles.map((t) => (
          <Link
            key={t.to}
            to={t.to}
            className="group card card-pad transition hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-card-hover"
          >
            <div className="flex items-start gap-4">
              <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${t.accent}`}>
                {t.icon}
              </div>
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2">
                  <div className="text-base font-semibold text-slate-900 group-hover:text-brand-700">{t.title}</div>
                  <span className="text-slate-300 transition group-hover:translate-x-0.5 group-hover:text-brand-500">
                    {ChevronRight}
                  </span>
                </div>
                <div className="mt-1 text-sm text-slate-500">{t.blurb}</div>
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
