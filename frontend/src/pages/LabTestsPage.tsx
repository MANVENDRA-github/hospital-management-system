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
      <h1 className="mb-4 text-2xl font-semibold text-slate-900">Lab Tests</h1>

      <form onSubmit={onLoadByPatient} className="mb-4 flex gap-2">
        <input className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Patient ID" value={patientId} onChange={(e) => setPatientId(e.target.value)} />
        <button className="rounded border border-slate-300 px-3 py-2 text-sm hover:bg-slate-100">Load</button>
      </form>

      {canBook && (
        <form onSubmit={onBook} className="mb-6 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-2">
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Patient ID *" required value={draft.patientId || ''} onChange={(e) => setDraft({ ...draft, patientId: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="number" placeholder="Doctor ID *" required value={draft.doctorId || ''} onChange={(e) => setDraft({ ...draft, doctorId: Number(e.target.value) })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" placeholder="Test name *" required value={draft.testName} onChange={(e) => setDraft({ ...draft, testName: e.target.value })} />
          <input className="rounded border border-slate-300 px-3 py-2 text-sm" type="datetime-local" required value={draft.testDate} onChange={(e) => setDraft({ ...draft, testDate: e.target.value })} />
          <div className="sm:col-span-2">
            <button type="submit" className="rounded bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700">Book test</button>
          </div>
        </form>
      )}

      {error && <div className="mb-4 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>}

      <div className="space-y-3">
        {list.map((t) => (
          <div key={t.id} className="rounded-lg border border-slate-200 bg-white p-4">
            <div className="flex items-start justify-between">
              <div>
                <div className="text-base font-semibold text-slate-900">{t.testName}</div>
                <div className="text-sm text-slate-500">
                  Patient #{t.patientId} • Doctor #{t.doctorId} • {t.testDate.replace('T', ' ').slice(0, 16)}
                </div>
              </div>
              <span className={t.status === 'COMPLETED' ? 'rounded bg-emerald-100 px-2 py-0.5 text-xs text-emerald-800' : 'rounded bg-amber-100 px-2 py-0.5 text-xs text-amber-800'}>
                {t.status}
              </span>
            </div>
            {t.result && <p className="mt-2 rounded bg-slate-50 p-2 text-sm text-slate-700">Result: {t.result}</p>}
            {canResult && t.status === 'PENDING' && (
              <div className="mt-3 flex gap-2">
                <input
                  className="flex-1 rounded border border-slate-300 px-3 py-2 text-sm"
                  placeholder="Enter result"
                  value={resultDrafts[t.id] ?? ''}
                  onChange={(e) => setResultDrafts((d) => ({ ...d, [t.id]: e.target.value }))}
                />
                <button onClick={() => onUploadResult(t.id)} className="rounded bg-brand-600 px-3 py-2 text-sm font-medium text-white hover:bg-brand-700">Upload result</button>
              </div>
            )}
          </div>
        ))}
        {list.length === 0 && (
          <div className="rounded-lg border border-slate-200 bg-white px-4 py-6 text-center text-slate-500">No tests to show.</div>
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
