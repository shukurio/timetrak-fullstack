import { Navigate, useLocation } from 'react-router-dom';
import useAuthStore from '../../store/authStore';
import LoadingSpinner from './LoadingSpinner';

const ProtectedRoute = ({ children, allowedRoles = [], requireDepartment = false }) => {
  const { isAuthenticated, isLoading, role, departments } = useAuthStore();
  const location = useLocation();

  // Show loading spinner while checking auth
  if (isLoading) {
    return <LoadingSpinner />;
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check if user role is allowed
  if (allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  // For managers, check if they have departments assigned (if required)
  if (requireDepartment && role === 'MANAGER' && (!departments || departments.length === 0)) {
    return <Navigate to="/no-departments" replace />;
  }

  return children;
};

export default ProtectedRoute;