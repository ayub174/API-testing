import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import type { Account, Permission, Role } from '../types';

export default function AdminPage() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  if (loading) return <p>Laddar…</p>;

  return (
    <div>
      <h2>Admin – behörigheter</h2>
      <p className="muted">
        Bocka i/ur behörigheter per konto. Ändringar slår igenom direkt (kontot
        kan behöva ladda om sin vy).
      </p>
      {error && (
        <div className="error" role="alert" data-testid="admin-error">
          {error}
        </div>
      )}

      <table className="data-table admin-table" data-testid="admin-table">
        <thead>
          <tr>
            <th>Konto</th>
            <th>Roll</th>
            {permissions.map((p) => (
              <th key={p} className="perm-col">
                {p}
              </th>
            ))}
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
                  data-testid={`role-${account.username}`}
                >
                  {roles.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
                </select>
              </td>
              {permissions.map((p) => (
                <td key={p} className="perm-col">
                  <input
                    type="checkbox"
                    checked={account.permissions.includes(p)}
                    onChange={(e) => togglePermission(account, p, e.target.checked)}
                    data-testid={`perm-${account.username}-${p}`}
                  />
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
