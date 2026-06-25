import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { User } from '../types';

export default function UsersPage() {
  const { hasPermission } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const canManage = hasPermission('USER_MANAGE');

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setUsers(await api.get<User[]>('/users'));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte hämta användare');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleDelete(id: number) {
    setError(null);
    try {
      await api.del(`/users/${id}`);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte ta bort användaren');
    }
  }

  return (
    <div>
      <h2>Användare</h2>
      {error && (
        <div className="error" role="alert">
          {error}
        </div>
      )}
      {loading ? (
        <p>Laddar…</p>
      ) : (
        <table className="data-table" data-testid="users-table">
          <thead>
            <tr>
              <th>Id</th>
              <th>Namn</th>
              <th>E-post</th>
              <th>Roll</th>
              {canManage && <th>Åtgärder</th>}
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.name}</td>
                <td>{user.email}</td>
                <td>{user.role ?? '–'}</td>
                {canManage && (
                  <td className="actions">
                    <button
                      className="danger"
                      onClick={() => handleDelete(user.id)}
                      data-testid={`delete-user-${user.id}`}
                    >
                      Ta bort
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
