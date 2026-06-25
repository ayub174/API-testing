import { useEffect, useState } from 'react';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { Loan } from '../types';

export default function AllLoansPage() {
  const { hasPermission } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<'all' | 'active' | 'overdue'>('all');

  const canManage = hasPermission('LOAN_MANAGE');

  async function load() {
    setLoading(true);
    setError(null);
    const query =
      filter === 'active' ? '?active=true' : filter === 'overdue' ? '?overdue=true' : '';
    try {
      setLoans(await api.get<Loan[]>(`/loans${query}`));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte hämta lån');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter]);

  async function handleReturn(id: number) {
    setError(null);
    try {
      await api.post(`/loans/${id}/return`);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte registrera återlämning');
    }
  }

  return (
    <div>
      <h2>Alla lån</h2>
      <div className="filter-row">
        <label>
          Visa:{' '}
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value as typeof filter)}
            data-testid="loan-filter"
          >
            <option value="all">Alla</option>
            <option value="active">Aktiva</option>
            <option value="overdue">Försenade</option>
          </select>
        </label>
      </div>

      {error && (
        <div className="error" role="alert" data-testid="all-loans-error">
          {error}
        </div>
      )}

      {loading ? (
        <p>Laddar…</p>
      ) : (
        <table className="data-table" data-testid="all-loans-table">
          <thead>
            <tr>
              <th>Id</th>
              <th>Bok</th>
              <th>Låntagare</th>
              <th>Lånad</th>
              <th>Förfaller</th>
              <th>Status</th>
              {canManage && <th>Åtgärd</th>}
            </tr>
          </thead>
          <tbody>
            {loans.map((loan) => (
              <tr key={loan.id} className={loan.overdue ? 'row-overdue' : ''}>
                <td>{loan.id}</td>
                <td>{loan.bookTitle}</td>
                <td>{loan.username}</td>
                <td>{loan.borrowedAt}</td>
                <td>{loan.dueDate}</td>
                <td>
                  {loan.returned ? (
                    <span className="badge badge-ok">Återlämnad</span>
                  ) : loan.overdue ? (
                    <span className="badge badge-overdue">Försenad</span>
                  ) : (
                    <span className="badge">Aktiv</span>
                  )}
                </td>
                {canManage && (
                  <td className="actions">
                    {!loan.returned && (
                      <button onClick={() => handleReturn(loan.id)} data-testid={`force-return-${loan.id}`}>
                        Återlämna
                      </button>
                    )}
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
