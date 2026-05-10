import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

interface Tile {
  to: string;
  title: string;
  blurb: string;
}

const TILES_BY_ROLE: Record<string, Tile[]> = {
  ADMIN: [
    { to: '/patients', title: 'Patients', blurb: 'Manage patient records' },
    { to: '/doctors', title: 'Doctors', blurb: 'Manage doctor profiles' },
    { to: '/appointments', title: 'Appointments', blurb: 'All bookings, status, history' },
    { to: '/billing', title: 'Billing', blurb: 'Invoices and payments' },
    { to: '/lab', title: 'Lab Tests', blurb: 'Tests and results' },
  ],
  DOCTOR: [
    { to: '/patients', title: 'Patients', blurb: 'Look up patient records' },
    { to: '/appointments', title: 'Appointments', blurb: 'Your appointments and lifecycle' },
    { to: '/lab', title: 'Lab Tests', blurb: 'Order tests and upload results' },
  ],
  PATIENT: [
    { to: '/appointments', title: 'My Appointments', blurb: 'Book and manage your appointments' },
    { to: '/billing', title: 'My Bills', blurb: 'Outstanding and paid bills' },
    { to: '/lab', title: 'My Lab Tests', blurb: 'Your tests and results' },
  ],
};

export default function Dashboard() {
  const { user } = useAuth();
  if (!user) return null;
  const tiles = TILES_BY_ROLE[user.role] ?? [];

  return (
    <div>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900">Welcome, {user.email}</h1>
      <p className="mb-6 text-sm text-slate-500">
        Signed in as <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-medium">{user.role}</span>
      </p>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {tiles.map((t) => (
          <Link
            key={t.to}
            to={t.to}
            className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm transition hover:border-brand-500 hover:shadow"
          >
            <div className="text-base font-semibold text-slate-900">{t.title}</div>
            <div className="mt-1 text-sm text-slate-500">{t.blurb}</div>
          </Link>
        ))}
      </div>
    </div>
  );
}
