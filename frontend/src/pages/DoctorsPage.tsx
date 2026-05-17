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
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="page-title">Doctors</h1>
          <p className="page-sub">Browse the directory and manage doctor profiles.</p>
        </div>
        <span className="badge-slate">{list.length} {list.length === 1 ? 'doctor' : 'doctors'}</span>
      </div>

      <form onSubmit={onSearch} className="card card-pad mb-6 flex flex-wrap gap-3">
        <input className="input flex-1 min-w-[200px]" placeholder="Filter by specialization (e.g. cardiology)" value={specQuery} onChange={(e) => setSpecQuery(e.target.value)} />
        <button className="btn-secondary">Search</button>
        <button type="button" onClick={() => { setSpecQuery(''); reload(); }} className="btn-ghost">Reset</button>
      </form>

      {canCreate && (
        <div className="card card-pad mb-6">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-500">
            {editingId ? 'Update doctor' : 'Add a new doctor'}
          </h2>
          <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="label">Name *</span>
              <input className="input" required value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Specialization *</span>
              <input className="input" required value={draft.specialization} onChange={(e) => setDraft({ ...draft, specialization: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Phone</span>
              <input className="input" value={draft.phone ?? ''} onChange={(e) => setDraft({ ...draft, phone: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Email</span>
              <input className="input" type="email" value={draft.email ?? ''} onChange={(e) => setDraft({ ...draft, email: e.target.value })} />
            </label>
            <div className="flex gap-2 sm:col-span-2">
              <button type="submit" className="btn-primary">
                {editingId ? 'Update doctor' : 'Create doctor'}
              </button>
              {editingId && (
                <button type="button" onClick={() => { setEditingId(null); setDraft(EMPTY); }} className="btn-secondary">
                  Cancel
                </button>
              )}
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
                <th>Name</th>
                <th>Specialization</th>
                <th>Phone</th>
                <th>Email</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {list.map((d) => (
                <tr key={d.id}>
                  <td className="text-slate-400">#{d.id}</td>
                  <td className="font-medium text-slate-900">{d.name}</td>
                  <td><span className="badge-brand">{d.specialization}</span></td>
                  <td>{d.phone ?? '—'}</td>
                  <td>{d.email ?? '—'}</td>
                  <td className="text-right">
                    {canEdit && <button onClick={() => onEdit(d)} className="mr-3 text-sm font-medium text-brand-700 hover:underline">Edit</button>}
                    {canDelete && (
                      <button
                        onClick={async () => { try { await doctorsApi.remove(d.id); reload(); } catch (e) { setError(extractError(e)); } }}
                        className="text-sm font-medium text-red-600 hover:underline"
                      >
                        Delete
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {list.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-10 text-center text-sm text-slate-500">No doctors yet.</td>
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
