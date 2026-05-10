import { type FormEvent, useEffect, useState } from 'react';
import { patientsApi } from '../api/patients';
import { useAuth } from '../auth/AuthContext';
import type { Patient, PatientInput } from '../types/api';

const EMPTY: PatientInput = { name: '', dateOfBirth: '', gender: '', phone: '', address: '' };

export default function PatientsPage() {
  const { user } = useAuth();
  const [list, setList] = useState<Patient[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState<PatientInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);

  const canCreate = user?.role === 'ADMIN' || user?.role === 'PATIENT';
  const canDelete = user?.role === 'ADMIN';
  const canList = user?.role === 'ADMIN' || user?.role === 'DOCTOR';

  const reload = () => {
    if (!canList) {
      setList([]);
      return;
    }
    patientsApi.list().then(setList).catch((e) => setError(extractError(e)));
  };

  useEffect(() => {
    reload();
  }, [user?.role]);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      if (editingId) {
        await patientsApi.update(editingId, draft);
      } else {
        await patientsApi.create(draft);
      }
      setDraft(EMPTY);
      setEditingId(null);
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onEdit = (p: Patient) => {
    setEditingId(p.id);
    setDraft({
      userId: p.userId ?? null,
      name: p.name,
      dateOfBirth: p.dateOfBirth ?? '',
      gender: p.gender ?? '',
      phone: p.phone ?? '',
      address: p.address ?? '',
    });
  };

  const onDelete = async (id: number) => {
    setError(null);
    try {
      await patientsApi.remove(id);
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900">Patients</h1>

      {canCreate && (
        <form onSubmit={onSubmit} className="mb-6 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-3">
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Name *" required value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="date" placeholder="DOB" value={draft.dateOfBirth ?? ''} onChange={(e) => setDraft({ ...draft, dateOfBirth: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Gender" value={draft.gender ?? ''} onChange={(e) => setDraft({ ...draft, gender: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" value={draft.phone ?? ''} onChange={(e) => setDraft({ ...draft, phone: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm sm:col-span-2" placeholder="Address" value={draft.address ?? ''} onChange={(e) => setDraft({ ...draft, address: e.target.value })} />
          <div className="sm:col-span-3 flex gap-2">
            <button type="submit" className="rounded bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">
              {editingId ? 'Update patient' : 'Create patient'}
            </button>
            {editingId && (
              <button type="button" onClick={() => { setEditingId(null); setDraft(EMPTY); }} className="rounded border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-slate-100">
                Cancel
              </button>
            )}
          </div>
        </form>
      )}

      {error && <div className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      {canList ? (
        <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-2">ID</th>
                <th className="px-4 py-2">Name</th>
                <th className="px-4 py-2">DOB</th>
                <th className="px-4 py-2">Gender</th>
                <th className="px-4 py-2">Phone</th>
                <th className="px-4 py-2 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {list.map((p) => (
                <tr key={p.id}>
                  <td className="px-4 py-2 text-slate-500">{p.id}</td>
                  <td className="px-4 py-2 font-medium text-slate-900">{p.name}</td>
                  <td className="px-4 py-2">{p.dateOfBirth ?? '—'}</td>
                  <td className="px-4 py-2">{p.gender ?? '—'}</td>
                  <td className="px-4 py-2">{p.phone ?? '—'}</td>
                  <td className="px-4 py-2 text-right">
                    {canCreate && (
                      <button onClick={() => onEdit(p)} className="mr-2 text-brand-700 hover:underline">Edit</button>
                    )}
                    {canDelete && (
                      <button onClick={() => onDelete(p.id)} className="text-red-600 hover:underline">Delete</button>
                    )}
                  </td>
                </tr>
              ))}
              {list.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">No patients yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="text-sm text-slate-500">Listing patients requires ADMIN or DOCTOR role.</p>
      )}
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
