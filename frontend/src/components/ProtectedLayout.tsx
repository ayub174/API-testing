import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Gemensam ram för inloggade vyer: navigering, roll-badge och utloggning. */
export default function ProtectedLayout() {
  const { user, logout, hasPermission } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <div className="app">
      <header className="topbar">
        <nav className="nav">
          <span className="brand">📚 Boksystem</span>
          <NavLink to="/books">Böcker</NavLink>
          {hasPermission('LOAN_VIEW_OWN') && <NavLink to="/loans">Mina lån</NavLink>}
          {hasPermission('LOAN_VIEW_ALL') && <NavLink to="/all-loans">Alla lån</NavLink>}
          {hasPermission('USER_READ') && <NavLink to="/users">Användare</NavLink>}
          {(hasPermission('PERMISSION_MANAGE') || hasPermission('ACCOUNT_MANAGE')) && (
            <NavLink to="/admin">Admin</NavLink>
          )}
        </nav>
        <div className="user-box">
          <span className="user-name" data-testid="current-user">
            {user?.username}
          </span>
          <span className="role-badge">{user?.role}</span>
          <button onClick={handleLogout} data-testid="logout">
            Logga ut
          </button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
