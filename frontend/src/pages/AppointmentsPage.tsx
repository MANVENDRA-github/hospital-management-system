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
      <h1 className="mb-4 text-2xl font-semibold text-slate-900">Appointments</h1>

      {canBook && (
        <form onSubmit={onBook} className="mb-6 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2">
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Patient ID *" required value={draft.patientId || ''} onChange={(e) => setDraft({ ...draft, patientId: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Doctor ID *" required value={draft.doctorId || ''} onChange={(e) => setDraft({ ...draft, doctorId: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="datetime-local" required value={draft.appointmentDate} onChange={(e) => setDraft({ ...draft, appointmentDate: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Reason" value={draft.reason ?? ''} onChange={(e) => setDraft({ ...draft, reason: e.target.value })} />
          <div className="sm:col-span-2">
            <button type="submit" className="rounded bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Book appointment</button>
          </div>
        </form>
      )}

      <div className="mb-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <form onSubmit={onFilterPatient} className="flex gap-2">
          <input className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Filter by patient ID" value={filterPatient} onChange={(e) => setFilterPatient(e.target.value)} />
          <button className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Filter</button>
        </form>
        <form onSubmit={onFilterDoctor} className="flex gap-2">
          <input className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Filter by doctor ID" value={filterDoctor} onChange={(e) => setFilterDoctor(e.target.value)} />
          <button className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Filter</button>
        </form>
      </div>

      {error && <div className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">ID</th>
              <th className="px-4 py-2">Patient</th>
              <th className="px-4 py-2">Doctor</th>
              <th className="px-4 py-2">Date</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.map((a) => (
              <tr key={a.id}>
                <td className="px-4 py-2 text-slate-500">{a.id}</td>
                <td className="px-4 py-2">{a.patientId}</td>
                <td className="px-4 py-2">{a.doctorId}</td>
                <td className="px-4 py-2">{a.appointmentDate.replace('T', ' ').slice(0, 16)}</td>
                <td className="px-4 py-2">
                  <span className={statusClass(a.status)}>{a.status}</span>
                </td>
                <td className="px-4 py-2 text-right">
                  {canCancel && a.status === 'SCHEDULED' && (
                    <button onClick={() => onCancel(a.id)} className="mr-2 text-red-600 hover:underline">Cancel</button>
                  )}
                  {canComplete && a.status === 'SCHEDULED' && (
                    <button onClick={() => onComplete(a.id)} className="text-emerald-700 hover:underline">Complete</button>
                  )}
                </td>
              </tr>
            ))}
            {list.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">No appointments to show. Use filters to load by patient or doctor ID.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function statusClass(s: string) {
  const base = 'rounded px-2 py-0.5 text-xs font-medium';
  if (s === 'SCHEDULED') return `${base} bg-blue-100 text-blue-800`;
  if (s === 'COMPLETED') return `${base} bg-emerald-100 text-emerald-800`;
  return `${base} bg-slate-200 text-slate-700`;
}

function extractError(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const r = (err as { response?: { data?: { message?: string } } }).response;
    if (r?.data?.message) return r.data.message;
  }
  return err instanceof Error ? err.message : 'Request failed';
}
