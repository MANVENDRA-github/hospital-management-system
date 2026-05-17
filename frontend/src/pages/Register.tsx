import { type FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import type { Role } from '../types/api';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<Role>('PATIENT');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await register({ email, password, role });
      navigate('/dashboard');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Registration failed';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="grid min-h-full lg:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden bg-gradient-to-br from-slate-800 via-slate-900 to-slate-950 p-12 text-white lg:flex">
        <div className="absolute -right-20 -top-20 h-72 w-72 rounded-full bg-white/10 blur-3xl" aria-hidden="true" />
        <div className="absolute -bottom-24 -left-12 h-72 w-72 rounded-full bg-white/10 blur-3xl" aria-hidden="true" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 ring-1 ring-inset ring-white/30">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
              <path d="M12 3v18M3 12h18" />
            </svg>
          </div>
          <div className="leading-tight">
            <div className="text-base font-semibold">HMS</div>
            <div className="text-[11px] font-medium uppercase tracking-wider text-slate-300/80">Hospital Management</div>
          </div>
        </div>
        <div className="relative">
          <h1 className="text-4xl font-semibold leading-tight tracking-tight">
            Join the team.<br />Get organized.
          </h1>
          <p className="mt-4 max-w-md text-base text-slate-300">
            Create your account to start managing patients, appointments, and more.
          </p>
        </div>
        <div className="relative text-xs text-slate-400">
          &copy; {new Date().getFullYear()} Hospital Management System
        </div>
      </div>

      <div className="flex items-center justify-center bg-slate-50 px-6 py-12">
        <form onSubmit={onSubmit} className="w-full max-w-md card card-pad">
          <div className="mb-6 flex items-center gap-2.5 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white shadow-sm">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
                <path d="M12 3v18M3 12h18" />
              </svg>
            </div>
            <span className="text-base font-semibold tracking-tight text-slate-900">HMS</span>
          </div>

          <h1 className="page-title">Create account</h1>
          <p className="page-sub mb-6">Set up your credentials and pick a role.</p>

          <label className="mb-4 block">
            <span className="label">Email</span>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="input"
              autoComplete="email"
            />
          </label>
          <label className="mb-4 block">
            <span className="label">Password</span>
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="input"
              autoComplete="new-password"
            />
          </label>
          <label className="mb-5 block">
            <span className="label">Role</span>
            <select value={role} onChange={(e) => setRole(e.target.value as Role)} className="input">
              <option value="PATIENT">Patient</option>
              <option value="DOCTOR">Doctor</option>
              <option value="ADMIN">Admin</option>
            </select>
          </label>

          {error && <div className="alert-error mb-4">{error}</div>}

          <button type="submit" disabled={submitting} className="btn-primary w-full">
            {submitting ? 'Registering…' : 'Register'}
          </button>

          <p className="mt-6 text-center text-sm text-slate-600">
            Already registered?{' '}
            <Link to="/login" className="font-medium text-brand-700 hover:underline">
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
