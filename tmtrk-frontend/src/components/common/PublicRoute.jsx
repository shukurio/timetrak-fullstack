import { Navigate } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import LoadingSpinner from './LoadingSpinner';

const PublicRoute = ({ children }) => {
  const { isAuthenticated, isLoading, role } = useAuthStore();

  // Show loading while checking auth
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // Redirect authenticated users to their dashboard
  if (isAuthenticated) {
    switch (role) {
      case 'ADMIN':
      case 'SYSADMIN':
        return <Navigate to="/admin" replace />;
      case 'MANAGER':
        return <Navigate to="/manager" replace />;
      case 'EMPLOYEE':
        return <Navigate to="/employee" replace />;
      default:
        return <Navigate to="/login" replace />;
    }
  }

  return children;
};

export default PublicRoute;