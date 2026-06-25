import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';

/** Skickar oinloggade användare till /login. */
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}
