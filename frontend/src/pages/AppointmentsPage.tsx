import { type FormEvent, useEffect, useState } from 'react';
import { appointmentsApi } from '../api/appointments';
import { useAuth } from '../auth/AuthContext';
import type { Appointment, AppointmentInput } from '../types/api';

const EMPTY: AppointmentInput = {
  patientId: 0,
  doctorId: 0,
  appointmentDate: '',
  reason: '',
};

export default function AppointmentsPage() {
  const { user } = useAuth();
  const [list, setList] = useState<Appointment[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState<AppointmentInput>(EMPTY);
  const [filterPatient, setFilterPatient] = useState('');
  const [filterDoctor, setFilterDoctor] = useState('');

  const canBook = user?.role === 'ADMIN' || user?.role === 'PATIENT';
  const canCancel = user?.role === 'ADMIN' || user?.role === 'PATIENT';
  const canComplete = user?.role === 'ADMIN' || user?.role === 'DOCTOR';
  const canListAll = user?.role === 'ADMIN';

  const reload = () => {
    if (!canListAll) {
      setList([]);
      return;
    }
    appointmentsApi.list().then(setList).catch((e) => setError(extractError(e)));
  };
  useEffect(reload, [user?.role]);

  const onBook = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const payload: AppointmentInput = {
        ...draft,
        appointmentDate: draft.appointmentDate.includes('T') ? draft.appointmentDate : `${draft.appointmentDate}:00`,
      };
      await appointmentsApi.book(payload);
      setDraft(EMPTY);
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onCancel = async (id: number) => {
    setError(null);
    try { await appointmentsApi.cancel(id); reload(); } catch (e) { setError(extractError(e)); }
  };
  const onComplete = async (id: number) => {
    setError(null);
    try { await appointmentsApi.complete(id); reload(); } catch (e) { setError(extractError(e)); }
  };

  const onFilterPatient = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!filterPatient) return reload();
    try {
      const data = await appointmentsApi.byPatient(Number(filterPatient));
      setList(data);
    } catch (err) {
      setError(extractError(err));
    }
  };
  const onFilterDoctor = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!filterDoctor) return reload();
    try {
      const data = await appointmentsApi.byDoctor(Number(filterDoctor));
      setList(data);
    } catch (err) {
      setError(extractError(err));
    }
  };

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="page-title">Appointments</h1>
          <p className="page-sub">Book, filter, complete, or cancel appointments.</p>
        </div>
        <span className="badge-slate">{list.length} {list.length === 1 ? 'appointment' : 'appointments'}</span>
      </div>

      {canBook && (
        <div className="card card-pad mb-6">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-500">Book an appointment</h2>
          <form onSubmit={onBook} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="label">Patient ID *</span>
              <input className="input" type="number" required value={draft.patientId || ''} onChange={(e) => setDraft({ ...draft, patientId: Number(e.target.value) })} />
            </label>
            <label className="block">
              <span className="label">Doctor ID *</span>
              <input className="input" type="number" required value={draft.doctorId || ''} onChange={(e) => setDraft({ ...draft, doctorId: Number(e.target.value) })} />
            </label>
            <label className="block">
              <span className="label">Date &amp; time *</span>
              <input className="input" type="datetime-local" required value={draft.appointmentDate} onChange={(e) => setDraft({ ...draft, appointmentDate: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Reason</span>
              <input className="input" placeholder="Optional" value={draft.reason ?? ''} onChange={(e) => setDraft({ ...draft, reason: e.target.value })} />
            </label>
            <div className="sm:col-span-2">
              <button type="submit" className="btn-primary">Book appointment</button>
            </div>
          </form>
        </div>
      )}

      <div className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <form onSubmit={onFilterPatient} className="card card-pad flex gap-2 !p-4">
          <input className="input" type="number" placeholder="Filter by patient ID" value={filterPatient} onChange={(e) => setFilterPatient(e.target.value)} />
          <button className="btn-secondary shrink-0">Filter</button>
        </form>
        <form onSubmit={onFilterDoctor} className="card card-pad flex gap-2 !p-4">
          <input className="input" type="number" placeholder="Filter by doctor ID" value={filterDoctor} onChange={(e) => setFilterDoctor(e.target.value)} />
          <button className="btn-secondary shrink-0">Filter</button>
        </form>
      </div>

      {error && <div className="alert-error mb-4">{error}</div>}

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th>ID</th>
                <th>Patient</th>
                <th>Doctor</th>
                <th>Date</th>
                <th>Status</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map((a) => (
                <tr key={a.id}>
                  <td className="text-slate-400">#{a.id}</td>
                  <td>#{a.patientId}</td>
                  <td>#{a.doctorId}</td>
                  <td>{a.appointmentDate.replace('T', ' ').slice(0, 16)}</td>
                  <td>
                    <span className={statusBadge(a.status)}>{a.status}</span>
                  </td>
                  <td className="text-right">
                    {canCancel && a.status === 'SCHEDULED' && (
                      <button onClick={() => onCancel(a.id)} className="mr-3 text-sm font-medium text-red-600 hover:underline">Cancel</button>
                    )}
                    {canComplete && a.status === 'SCHEDULED' && (
                      <button onClick={() => onComplete(a.id)} className="text-sm font-medium text-emerald-700 hover:underline">Complete</button>
                    )}
                  </td>
                </tr>
              ))}
              {list.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-10 text-center text-sm text-slate-500">
                    No appointments to show. Use filters to load by patient or doctor ID.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function statusBadge(s: string) {
  if (s === 'SCHEDULED') return 'badge-blue';
  if (s === 'COMPLETED') return 'badge-green';
  if (s === 'CANCELLED') return 'badge-red';
  return 'badge-slate';
}

function extractError(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const r = (err as { response?: { data?: { message?: string } } }).response;
    if (r?.data?.message) return r.data.message;
  }
  return err instanceof Error ? err.message : 'Request failed';
}
