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
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="page-title">Patients</h1>
          <p className="page-sub">Add, update, and review patient records.</p>
        </div>
        {canList && (
          <span className="badge-slate">
            {list.length} {list.length === 1 ? 'record' : 'records'}
          </span>
        )}
      </div>

      {canCreate && (
        <div className="card card-pad mb-6">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-500">
            {editingId ? 'Update patient' : 'Add a new patient'}
          </h2>
          <form onSubmit={onSubmit} className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <label className="block">
              <span className="label">Name *</span>
              <input className="input" required value={draft.name} onChange={(e) => setDraft({ ...draft, name: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Date of birth</span>
              <input className="input" type="date" value={draft.dateOfBirth ?? ''} onChange={(e) => setDraft({ ...draft, dateOfBirth: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Gender</span>
              <input className="input" placeholder="e.g. Male / Female" value={draft.gender ?? ''} onChange={(e) => setDraft({ ...draft, gender: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Phone</span>
              <input className="input" value={draft.phone ?? ''} onChange={(e) => setDraft({ ...draft, phone: e.target.value })} />
            </label>
            <label className="block sm:col-span-2">
              <span className="label">Address</span>
              <input className="input" value={draft.address ?? ''} onChange={(e) => setDraft({ ...draft, address: e.target.value })} />
            </label>
            <div className="flex gap-2 sm:col-span-3">
              <button type="submit" className="btn-primary">
                {editingId ? 'Update patient' : 'Create patient'}
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

      {canList ? (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="table-modern">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>DOB</th>
                  <th>Gender</th>
                  <th>Phone</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {list.map((p) => (
                  <tr key={p.id}>
                    <td className="text-slate-400">#{p.id}</td>
                    <td className="font-medium text-slate-900">{p.name}</td>
                    <td>{p.dateOfBirth ?? '—'}</td>
                    <td>{p.gender ?? '—'}</td>
                    <td>{p.phone ?? '—'}</td>
                    <td className="text-right">
                      {canCreate && (
                        <button onClick={() => onEdit(p)} className="mr-3 text-sm font-medium text-brand-700 hover:underline">Edit</button>
                      )}
                      {canDelete && (
                        <button onClick={() => onDelete(p.id)} className="text-sm font-medium text-red-600 hover:underline">Delete</button>
                      )}
                    </td>
                  </tr>
                ))}
                {list.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-4 py-10 text-center text-sm text-slate-500">
                      No patients yet. Use the form above to add one.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="card card-pad text-sm text-slate-500">
          Listing patients requires the <span className="badge-brand">ADMIN</span> or <span className="badge-brand">DOCTOR</span> role.
        </div>
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
