import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export default function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await api.post('/auth/register', { username, password });
      // Logga in direkt efter lyckad registrering.
      await login(username, password);
      navigate('/books', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Registreringen misslyckades');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>📚 Skapa konto</h1>
        <p className="subtitle">Registrera dig som låntagare</p>

        {error && (
          <div className="error" role="alert" data-testid="register-error">
            {error}
          </div>
        )}

        <label>
          Användarnamn
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            data-testid="reg-username"
          />
        </label>
        <label>
          Lösenord (minst 6 tecken)
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            data-testid="reg-password"
          />
        </label>

        <button type="submit" disabled={busy} data-testid="register-submit">
          {busy ? 'Skapar konto…' : 'Skapa konto'}
        </button>

        <p className="switch-auth">
          Har du redan ett konto? <Link to="/login">Logga in</Link>
        </p>
      </form>
    </div>
  );
}
