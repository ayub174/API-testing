import { useEffect, useState, type FormEvent } from 'react';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { Account, Permission, Role } from '../types';

export default function AdminPage() {
  const { hasPermission } = useAuth();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const canManageAccounts = hasPermission('ACCOUNT_MANAGE');
  const canManagePermissions = hasPermission('PERMISSION_MANAGE');

  // Personal utan PERMISSION_MANAGE får bara skapa/ta bort låntagare (ANVANDARE).
  const creatableRoles = canManagePermissions ? roles : roles.filter((r) => r === 'ANVANDARE');
  const canDeleteAccount = (role: Role) => canManagePermissions || role === 'ANVANDARE';

  const [form, setForm] = useState({ username: '', password: '', role: 'ANVANDARE' });

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [accs, perms, rs] = await Promise.all([
        api.get<Account[]>('/admin/accounts'),
        api.get<Permission[]>('/admin/permissions'),
        api.get<Role[]>('/admin/roles'),
      ]);
      setAccounts(accs);
      setPermissions(perms);
      setRoles(rs);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte hämta admin-data');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function togglePermission(account: Account, permission: Permission, enabled: boolean) {
    setError(null);
    try {
      const updated = enabled
        ? await api.post<Account>(`/admin/accounts/${account.username}/permissions/${permission}`)
        : await api.del<Account>(`/admin/accounts/${account.username}/permissions/${permission}`);
      setAccounts((prev) => prev.map((a) => (a.username === updated.username ? updated : a)));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte ändra behörighet');
    }
  }

  async function changeRole(account: Account, role: Role) {
    setError(null);
    try {
      const updated = await api.put<Account>(`/admin/accounts/${account.username}/role`, { role });
      setAccounts((prev) => prev.map((a) => (a.username === updated.username ? updated : a)));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte byta roll');
    }
  }

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.post<Account>('/admin/accounts', form);
      setForm({ username: '', password: '', role: 'ANVANDARE' });
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte skapa kontot');
    }
  }

  async function handleDelete(username: string) {
    setError(null);
    try {
      await api.del(`/admin/accounts/${username}`);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte ta bort kontot');
    }
  }

  if (loading) return <p>Laddar…</p>;

  return (
    <div>
      <h2>{canManagePermissions ? 'Admin – konton & behörigheter' : 'Låntagarkonton'}</h2>
      <p className="muted">
        {canManagePermissions
          ? 'Bocka i/ur behörigheter per konto, byt roll, eller skapa/ta bort konton. Ändringar slår igenom direkt (kontot kan behöva ladda om sin vy).'
          : 'Skapa och ta bort låntagarkonton. Roller och behörigheter hanteras av en administratör.'}
      </p>
      {error && (
        <div className="error" role="alert" data-testid="admin-error">
          {error}
        </div>
      )}

      {canManageAccounts && (
        <form className="book-form" onSubmit={handleCreate} data-testid="create-account-form">
          <h3>Skapa konto</h3>
          <div className="form-row">
            <input
              placeholder="Användarnamn"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              data-testid="new-account-username"
              required
            />
            <input
              placeholder="Lösenord"
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              data-testid="new-account-password"
              required
            />
            <select
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
              data-testid="new-account-role"
            >
              {creatableRoles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>
          <div className="form-actions">
            <button type="submit" data-testid="create-account-submit">
              Skapa konto
            </button>
          </div>
        </form>
      )}

      <table className="data-table admin-table" data-testid="admin-table">
        <thead>
          <tr>
            <th>Konto</th>
            <th>Roll</th>
            {canManagePermissions &&
              permissions.map((p) => (
                <th key={p} className="perm-col">
                  {p}
                </th>
              ))}
            {canManageAccounts && <th>Åtgärd</th>}
          </tr>
        </thead>
        <tbody>
          {accounts.map((account) => (
            <tr key={account.username} data-testid={`account-row-${account.username}`}>
              <td>{account.username}</td>
              <td>
                <select
                  value={account.role}
                  onChange={(e) => changeRole(account, e.target.value as Role)}
                  disabled={!canManagePermissions}
                  data-testid={`role-${account.username}`}
                >
                  {roles.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
                </select>
              </td>
              {canManagePermissions &&
                permissions.map((p) => (
                  <td key={p} className="perm-col">
                    <input
                      type="checkbox"
                      checked={account.permissions.includes(p)}
                      onChange={(e) => togglePermission(account, p, e.target.checked)}
                      data-testid={`perm-${account.username}-${p}`}
                    />
                  </td>
                ))}
              {canManageAccounts && (
                <td className="actions">
                  {canDeleteAccount(account.role) && (
                    <button
                      className="danger"
                      onClick={() => handleDelete(account.username)}
                      data-testid={`delete-account-${account.username}`}
                    >
                      Ta bort
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
