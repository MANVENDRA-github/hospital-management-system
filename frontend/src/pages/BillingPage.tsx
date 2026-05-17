import { type FormEvent, useState } from 'react';
import { billingApi } from '../api/billing';
import { useAuth } from '../auth/AuthContext';
import type { Bill, BillInput } from '../types/api';

const EMPTY: BillInput = { patientId: 0, appointmentId: null, amount: 0, description: '' };

export default function BillingPage() {
  const { user } = useAuth();
  const [list, setList] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState<BillInput>(EMPTY);
  const [patientId, setPatientId] = useState('');

  const canCreate = user?.role === 'ADMIN';
  const canPay = user?.role === 'ADMIN';

  const onLoad = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!patientId) return setList([]);
    try {
      const data = await billingApi.byPatient(Number(patientId));
      setList(data);
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await billingApi.create(draft);
      setDraft(EMPTY);
      if (patientId) {
        const data = await billingApi.byPatient(Number(patientId));
        setList(data);
      }
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onPay = async (id: number) => {
    setError(null);
    try {
      await billingApi.markPaid(id);
      if (patientId) setList(await billingApi.byPatient(Number(patientId)));
    } catch (err) {
      setError(extractError(err));
    }
  };

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="page-title">Billing</h1>
          <p className="page-sub">Create bills, view a patient's invoices, and mark them paid.</p>
        </div>
        {list.length > 0 && <span className="badge-slate">{list.length} {list.length === 1 ? 'bill' : 'bills'}</span>}
      </div>

      <form onSubmit={onLoad} className="card card-pad mb-6 flex flex-wrap gap-3 !p-4">
        <input className="input flex-1 min-w-[200px]" type="number" placeholder="Enter patient ID to load bills" value={patientId} onChange={(e) => setPatientId(e.target.value)} />
        <button className="btn-secondary shrink-0">Load bills</button>
      </form>

      {canCreate && (
        <div className="card card-pad mb-6">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-500">Create a new bill</h2>
          <form onSubmit={onCreate} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="label">Patient ID *</span>
              <input className="input" type="number" required value={draft.patientId || ''} onChange={(e) => setDraft({ ...draft, patientId: Number(e.target.value) })} />
            </label>
            <label className="block">
              <span className="label">Appointment ID</span>
              <input className="input" type="number" placeholder="Optional" value={draft.appointmentId ?? ''} onChange={(e) => setDraft({ ...draft, appointmentId: e.target.value ? Number(e.target.value) : null })} />
            </label>
            <label className="block">
              <span className="label">Amount *</span>
              <input className="input" type="number" step="0.01" required value={draft.amount || ''} onChange={(e) => setDraft({ ...draft, amount: Number(e.target.value) })} />
            </label>
            <label className="block">
              <span className="label">Description</span>
              <input className="input" value={draft.description ?? ''} onChange={(e) => setDraft({ ...draft, description: e.target.value })} />
            </label>
            <div className="sm:col-span-2">
              <button type="submit" className="btn-primary">Create bill</button>
            </div>
          </form>
        </div>
      )}

      {error && <div className="alert-error mb-4">{error}</div>}

      <div className="card overflow-hidden">
        <div className="overflow-x-auto">
          <table className="table-modern">
            <thead>
              <tr>
                <th>ID</th>
                <th>Patient</th>
                <th>Appointment</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Created</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map((b) => (
                <tr key={b.id}>
                  <td className="text-slate-400">#{b.id}</td>
                  <td>#{b.patientId}</td>
                  <td>{b.appointmentId ? `#${b.appointmentId}` : '—'}</td>
                  <td className="font-medium text-slate-900">{b.amount}</td>
                  <td>
                    <span className={b.paymentStatus === 'PAID' ? 'badge-green' : 'badge-amber'}>
                      {b.paymentStatus}
                    </span>
                  </td>
                  <td className="text-slate-500">{b.createdAt.replace('T', ' ').slice(0, 16)}</td>
                  <td className="text-right">
                    {canPay && b.paymentStatus === 'PENDING' && (
                      <button onClick={() => onPay(b.id)} className="text-sm font-medium text-emerald-700 hover:underline">Mark paid</button>
                    )}
                  </td>
                </tr>
              ))}
              {list.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-4 py-10 text-center text-sm text-slate-500">
                    Enter a patient ID above to load their bills.
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

function extractError(err: unknown): string {
  if (typeof err === 'object' && err !== null && 'response' in err) {
    const r = (err as { response?: { data?: { message?: string } } }).response;
    if (r?.data?.message) return r.data.message;
  }
  return err instanceof Error ? err.message : 'Request failed';
}
