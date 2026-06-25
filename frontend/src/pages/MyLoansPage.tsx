import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { Loan } from '../types';

export default function MyLoansPage() {
  const { hasPermission } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const canReturn = hasPermission('LOAN_RETURN');

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setLoans(await api.get<Loan[]>('/loans/me'));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte hämta dina lån');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleReturn(id: number) {
    setError(null);
    try {
      await api.post(`/loans/${id}/return`);
      await load();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Kunde inte återlämna boken');
    }
  }

  const activeCount = loans.filter((l) => !l.returned).length;

  return (
    <div>
      <h2>Mina lån</h2>
      {!loading && loans.length > 0 && (
        <p className="muted" data-testid="active-count">
          Aktiva lån: {activeCount} av högst 3.
        </p>
      )}
      {error && (
        <div className="error" role="alert" data-testid="loans-error">
          {error}
        </div>
      )}

      {loading ? (
        <p>Laddar…</p>
      ) : loans.length === 0 ? (
        <p className="muted">
          Du har inga lån än. Gå till <Link to="/books">Böcker</Link> för att låna en bok.
        </p>
      ) : (
        <table className="data-table" data-testid="my-loans-table">
          <thead>
            <tr>
              <th>Bok</th>
              <th>Lånad</th>
              <th>Förfaller</th>
              <th>Status</th>
              <th>Åtgärd</th>
            </tr>
          </thead>
          <tbody>
            {loans.map((loan) => (
              <tr key={loan.id} data-testid={`my-loan-${loan.id}`}>
                <td>{loan.bookTitle}</td>
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
                <td className="actions">
                  {!loan.returned && canReturn && (
                    <button onClick={() => handleReturn(loan.id)} data-testid={`return-${loan.id}`}>
                      Återlämna
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
