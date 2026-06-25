import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../api/client';

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) {
    navigate('/books', { replace: true });
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login(username, password);
      navigate('/books', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Inloggningen misslyckades');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>📚 Boksystem</h1>
        <p className="subtitle">Logga in för att fortsätta</p>

        {error && (
          <div className="error" role="alert" data-testid="login-error">
            {error}
          </div>
        )}

        <label>
          Användarnamn
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            data-testid="username"
          />
        </label>
        <label>
          Lösenord
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            data-testid="password"
          />
        </label>

        <button type="submit" disabled={busy} data-testid="login-submit">
          {busy ? 'Loggar in…' : 'Logga in'}
        </button>

        <div className="hint">
          <strong>Testkonton:</strong>
          <br />
          admin / hemligt123 (full behörighet)
          <br />
          handlaggare / handlaggare123 (begränsad)
        </div>
      </form>
    </div>
  );
}
