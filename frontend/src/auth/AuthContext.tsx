import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi } from '../api/auth';
import type { AuthResponse, LoginRequest, RegisterRequest, Role } from '../types/api';

interface AuthState {
  email: string;
  role: Role;
}

interface AuthContextValue {
  user: AuthState | null;
  loading: boolean;
  login: (req: LoginRequest) => Promise<void>;
  register: (req: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthState | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = localStorage.getItem('hms.user');
    if (stored) {
      try {
        setUser(JSON.parse(stored));
      } catch {
        localStorage.removeItem('hms.user');
      }
    }
    setLoading(false);
  }, []);

  const persist = (response: AuthResponse) => {
    const next: AuthState = { email: response.email, role: response.role };
    localStorage.setItem('hms.token', response.token);
    localStorage.setItem('hms.user', JSON.stringify(next));
    setUser(next);
  };

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      login: async (req) => {
        const response = await authApi.login(req);
        persist(response);
      },
      register: async (req) => {
        const response = await authApi.register(req);
        persist(response);
      },
      logout: () => {
        localStorage.removeItem('hms.token');
        localStorage.removeItem('hms.user');
        setUser(null);
      },
    }),
    [user, loading],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
