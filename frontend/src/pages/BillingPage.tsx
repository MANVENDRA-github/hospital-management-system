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
      <h1 className="mb-4 text-2xl font-semibold text-slate-900">Billing</h1>

      <form onSubmit={onLoad} className="mb-4 flex gap-2">
        <input className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} />
        <button className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Load bills</button>
      </form>

      {canCreate && (
        <form onSubmit={onCreate} className="mb-6 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2">
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Patient ID *" required value={draft.patientId || ''} onChange={(e) => setDraft({ ...draft, patientId: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Appointment ID (optional)" value={draft.appointmentId ?? ''} onChange={(e) => setDraft({ ...draft, appointmentId: e.target.value ? Number(e.target.value) : null })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" step="0.01" placeholder="Amount *" required value={draft.amount || ''} onChange={(e) => setDraft({ ...draft, amount: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Description" value={draft.description ?? ''} onChange={(e) => setDraft({ ...draft, description: e.target.value })} />
          <div className="sm:col-span-2">
            <button type="submit" className="rounded bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Create bill</button>
          </div>
        </form>
      )}

      {error && <div className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">ID</th>
              <th className="px-4 py-2">Patient</th>
              <th className="px-4 py-2">Appointment</th>
              <th className="px-4 py-2">Amount</th>
              <th className="px-4 py-2">Status</th>
              <th className="px-4 py-2">Created</th>
              <th className="px-4 py-2 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.map((b) => (
              <tr key={b.id}>
                <td className="px-4 py-2 text-slate-500">{b.id}</td>
                <td className="px-4 py-2">{b.patientId}</td>
                <td className="px-4 py-2">{b.appointmentId ?? '—'}</td>
                <td className="px-4 py-2 font-medium">{b.amount}</td>
                <td className="px-4 py-2">
                  <span className={b.paymentStatus === 'PAID' ? 'rounded bg-emerald-100 px-2 py-0.5 text-xs text-emerald-800' : 'rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-800'}>
                    {b.paymentStatus}
                  </span>
                </td>
                <td className="px-4 py-2 text-slate-500">{b.createdAt.replace('T', ' ').slice(0, 16)}</td>
                <td className="px-4 py-2 text-right">
                  {canPay && b.paymentStatus === 'PENDING' && (
                    <button onClick={() => onPay(b.id)} className="text-emerald-700 hover:underline">Mark paid</button>
                  )}
                </td>
              </tr>
            ))}
            {list.length === 0 && (
              <tr><td colSpan={7} className="px-4 py-6 text-center text-slate-500">Load bills by patient ID.</td></tr>
            )}
          </tbody>
        </table>
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
