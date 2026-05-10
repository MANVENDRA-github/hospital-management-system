import { type FormEvent, useEffect, useState } from 'react';
import { doctorsApi } from '../api/doctors';
import { useAuth } from '../auth/AuthContext';
import type { Doctor, DoctorInput } from '../types/api';

const EMPTY: DoctorInput = { name: '', specialization: '', phone: '', email: '' };

export default function DoctorsPage() {
  const { user } = useAuth();
  const [list, setList] = useState<Doctor[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState<DoctorInput>(EMPTY);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [specQuery, setSpecQuery] = useState('');

  const canCreate = user?.role === 'ADMIN';
  const canEdit = user?.role === 'ADMIN' || user?.role === 'DOCTOR';
  const canDelete = user?.role === 'ADMIN';

  const reload = () => {
    doctorsApi.list().then(setList).catch((e) => setError(extractError(e)));
  };
  useEffect(reload, []);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      if (editingId) await doctorsApi.update(editingId, draft);
      else await doctorsApi.create(draft);
      setDraft(EMPTY);
      setEditingId(null);
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onEdit = (d: Doctor) => {
    setEditingId(d.id);
    setDraft({ userId: d.userId ?? null, name: d.name, specialization: d.specialization, phone: d.phone ?? '', email: d.email ?? '' });
  };

  const onSearch = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const data = specQuery ? await doctorsApi.bySpecialization(specQuery) : await doctorsApi.list();
      setList(data);
    } catch (err) {
      setError(extractError(err));
    }
  };

  return (
    <div>
      <h1 className="mb-4 text-2xl font-semibold text-slate-900">Doctors</h1>

      <form onSubmit={onSearch} className="mb-4 flex gap-2">
        <input className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Filter by specialization (e.g. cardiology)" value={specQuery} onChange={(e) => setSpecQuery(e.target.value)} />
        <button className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Search</button>
        <button type="button" onClick={() => { setSpecQuery(''); reload(); }} className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Reset</button>
      </form>

      {canCreate && (
        <form onSubmit={onSubmit} className="mb-6 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2">
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Name *" required value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Specialization *" required value={draft.specialization} onChange={(e) => setDraft({ ...draft, specialization: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Phone" value={draft.phone ?? ''} onChange={(e) => setDraft({ ...draft, phone: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="email" placeholder="Email" value={draft.email ?? ''} onChange={(e) => setDraft({ ...draft, email: e.target.value })} />
          <div className="sm:col-span-2 flex gap-2">
            <button type="submit" className="rounded bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">
              {editingId ? 'Update doctor' : 'Create doctor'}
            </button>
            {editingId && (
              <button type="button" onClick={() => { setEditingId(null); setDraft(EMPTY); }} className="rounded border border-slate-300 px-4 py-2 text-sm hover:bg-slate-100">
                Cancel
              </button>
            )}
          </div>
        </form>
      )}

      {error && <div className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">ID</th>
              <th className="px-4 py-2">Name</th>
              <th className="px-4 py-2">Specialization</th>
              <th className="px-4 py-2">Phone</th>
              <th className="px-4 py-2">Email</th>
              <th className="px-4 py-2 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.map((d) => (
              <tr key={d.id}>
                <td className="px-4 py-2 text-slate-500">{d.id}</td>
                <td className="px-4 py-2 font-medium">{d.name}</td>
                <td className="px-4 py-2">{d.specialization}</td>
                <td className="px-4 py-2">{d.phone ?? '—'}</td>
                <td className="px-4 py-2">{d.email ?? '—'}</td>
                <td className="px-4 py-2 text-right">
                  {canEdit && <button onClick={() => onEdit(d)} className="mr-2 text-brand-700 hover:underline">Edit</button>}
                  {canDelete && <button onClick={async () => { try { await doctorsApi.remove(d.id); reload(); } catch (e) { setError(extractError(e)); } }} className="text-red-600 hover:underline">Delete</button>}
                </td>
              </tr>
            ))}
            {list.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">No doctors yet.</td></tr>
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
