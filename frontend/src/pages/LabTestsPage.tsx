import { type FormEvent, useEffect, useState } from 'react';
import { labApi } from '../api/lab';
import { useAuth } from '../auth/AuthContext';
import type { LabTest, LabTestInput } from '../types/api';

const EMPTY: LabTestInput = { patientId: 0, doctorId: 0, testName: '', testDate: '' };

export default function LabTestsPage() {
  const { user } = useAuth();
  const [list, setList] = useState<LabTest[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [draft, setDraft] = useState<LabTestInput>(EMPTY);
  const [resultDrafts, setResultDrafts] = useState<Record<number, string>>({});
  const [patientId, setPatientId] = useState('');

  const canBook = user?.role === 'ADMIN' || user?.role === 'DOCTOR';
  const canResult = user?.role === 'ADMIN' || user?.role === 'DOCTOR';
  const canListAll = user?.role === 'ADMIN';

  const reload = () => {
    if (!canListAll) {
      setList([]);
      return;
    }
    labApi.list().then(setList).catch((e) => setError(extractError(e)));
  };
  useEffect(reload, [user?.role]);

  const onBook = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      const payload: LabTestInput = {
        ...draft,
        testDate: draft.testDate.includes('T') ? draft.testDate : `${draft.testDate}:00`,
      };
      await labApi.bookTest(payload);
      setDraft(EMPTY);
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onUploadResult = async (id: number) => {
    setError(null);
    const value = resultDrafts[id];
    if (!value) return;
    try {
      await labApi.uploadResult(id, value);
      setResultDrafts((d) => ({ ...d, [id]: '' }));
      reload();
    } catch (err) {
      setError(extractError(err));
    }
  };

  const onLoadByPatient = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!patientId) return reload();
    try {
      const data = await labApi.byPatient(Number(patientId));
      setList(data);
    } catch (err) {
      setError(extractError(err));
    }
  };

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="page-title">Lab Tests</h1>
          <p className="page-sub">Order tests, track status, and upload results.</p>
        </div>
        <span className="badge-slate">{list.length} {list.length === 1 ? 'test' : 'tests'}</span>
      </div>

      <form onSubmit={onLoadByPatient} className="card card-pad mb-6 flex flex-wrap gap-3 !p-4">
        <input className="input flex-1 min-w-[200px]" type="number" placeholder="Filter by patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} />
        <button className="btn-secondary shrink-0">Load</button>
      </form>

      {canBook && (
        <div className="card card-pad mb-6">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wider text-slate-500">Order a new test</h2>
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
              <span className="label">Test name *</span>
              <input className="input" required value={draft.testName} onChange={(e) => setDraft({ ...draft, testName: e.target.value })} />
            </label>
            <label className="block">
              <span className="label">Test date *</span>
              <input className="input" type="datetime-local" required value={draft.testDate} onChange={(e) => setDraft({ ...draft, testDate: e.target.value })} />
            </label>
            <div className="sm:col-span-2">
              <button type="submit" className="btn-primary">Book test</button>
            </div>
          </form>
        </div>
      )}

      {error && <div className="alert-error mb-4">{error}</div>}

      <div className="space-y-3">
        {list.map((t) => (
          <div key={t.id} className="card card-pad">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <div className="text-base font-semibold text-slate-900">{t.testName}</div>
                <div className="mt-1 text-sm text-slate-500">
                  Patient #{t.patientId} &middot; Doctor #{t.doctorId} &middot; {t.testDate.replace('T', ' ').slice(0, 16)}
                </div>
              </div>
              <span className={t.status === 'COMPLETED' ? 'badge-green' : 'badge-amber'}>{t.status}</span>
            </div>
            {t.result && (
              <div className="mt-3 rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-700 ring-1 ring-inset ring-slate-200">
                <span className="font-medium text-slate-900">Result:</span> {t.result}
              </div>
            )}
            {canResult && t.status === 'PENDING' && (
              <div className="mt-3 flex gap-2">
                <input
                  className="input"
                  placeholder="Enter result"
                  value={resultDrafts[t.id] ?? ''}
                  onChange={(e) => setResultDrafts((d) => ({ ...d, [t.id]: e.target.value }))}
                />
                <button onClick={() => onUploadResult(t.id)} className="btn-primary shrink-0">Upload result</button>
              </div>
            )}
          </div>
        ))}
        {list.length === 0 && (
          <div className="card card-pad text-center text-sm text-slate-500">No tests to show.</div>
        )}
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
