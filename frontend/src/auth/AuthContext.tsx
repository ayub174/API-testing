import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api, getToken, setToken, setUnauthorizedHandler } from '../api/client';
import type { LoginResponse, Permission, Role } from '../types';

interface AuthUser {
  username: string;
  role: Role;
  permissions: Permission[];
}

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasPermission: (permission: Permission) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface MeResponse {
  username: string;
  role: Role;
  permissions: Permission[];
  authenticated: boolean;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);

  // Logga ut globalt när klienten möter ett 401.
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

  // Vid uppstart: om en token finns, hämta aktuell identitet/behörigheter.
  useEffect(() => {
    if (!getToken()) return;
    api
      .get<MeResponse>('/auth/me')
      .then((me) =>
        setUser({ username: me.username, role: me.role, permissions: me.permissions }),
      )
      .catch(() => {
        setToken(null);
        setUser(null);
      });
  }, []);

  async function login(username: string, password: string): Promise<void> {
    const res = await api.post<LoginResponse>('/auth/login', { username, password });
    setToken(res.token);
    setUser({ username: res.username, role: res.role, permissions: res.permissions });
  }

  function logout(): void {
    api.post('/auth/logout').catch(() => undefined);
    setToken(null);
    setUser(null);
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      login,
      logout,
      hasPermission: (permission: Permission) => user?.permissions.includes(permission) ?? false,
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth måste användas inom AuthProvider');
  return ctx;
}
