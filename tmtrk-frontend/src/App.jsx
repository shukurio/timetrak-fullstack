import { useEffect, Suspense } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import useAuthStore from './store/authStore';
import authService from './api/authService';

// Layouts
import AdminLayout from './layouts/AdminLayout';
import EmployeeLayout from './layouts/EmployeeLayout';

// Route Guards
import ProtectedRoute from './components/common/ProtectedRoute';
import PublicRoute from './components/common/PublicRoute';
import LoadingSpinner from './components/common/LoadingSpinner';

// Auth Pages
import LoginPage from './pages/auth/LoginPage';
import EnhancedInviteRegistration from './pages/auth/EnhancedInviteRegistration';
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage';
import CompanyRegistration from './pages/auth/CompanyRegistration';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import CompanyPage from './pages/admin/CompanyPage';
import DepartmentsPage from './pages/admin/DepartmentsPage';
import EmployeesPage from './pages/admin/EmployeesPage';
import JobsPage from './pages/admin/JobsPage';
import ShiftsPage from './pages/admin/ShiftsPage';
import PaymentsPage from './pages/admin/PaymentsPage';
import ReportsPage from './pages/admin/ReportsPage';
import InvitesPage from './pages/admin/InvitesPage';
import InviteManagement from './pages/admin/InviteManagement';
import MultiUserInviteManagement from './pages/admin/MultiUserInviteManagement';

// Employee Pages
import EmployeeDashboard from './pages/employee/EmployeeDashboard';
import ClockPage from './pages/employee/ClockPage';
import MyShiftsPage from './pages/employee/MyShiftsPage';
import MyPaymentsPage from './pages/employee/MyPaymentsPage';
import ProfilePage from './pages/employee/ProfilePage';

// Kiosk Pages
import KioskPage from './pages/kiosk/KioskPage';

// Error Pages
import UnauthorizedPage from './pages/error/UnauthorizedPage';
import NotFoundPage from './pages/error/NotFoundPage';
import NoDepartmentsPage from './pages/error/NoDepartmentsPage';

// Create QueryClient
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      retry: 1,
    },
  },
});

function App() {
  const { setLoading, login, isAuthenticated, clearTokens } = useAuthStore();

  // Initialize auth state on app load (only once)
  useEffect(() => {
    let isMounted = true; // Prevent state updates if component unmounts

    const initializeAuth = async () => {
      if (!isMounted) return;
      
      try {
        // Step 1: Check if refresh token exists (implicit check by calling refresh)
        // Step 2: Call /auth/refresh to get new access token
        const refreshData = await authService.refreshToken();
        
        if (refreshData && refreshData.token && isMounted) {
          const { token, expiresIn, user } = refreshData;
          
          if (isMounted) {
            // Step 3: Login with user data from refresh response (no need for /auth/me)
            login({ user }, token, expiresIn);
          }
        }
      } catch (error) {
        // Only log refresh errors in development or if it's not a 401/500
        if (error.response?.status === 401 || error.response?.status === 403) {
          console.log('No valid session - user not logged in');
        } else if (error.response?.status === 500) {
          // Silently handle 500 errors (usually means no refresh token cookie)
          console.log('No refresh token available');
        } else {
          console.error('Refresh failed:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message
          });
        }
        
        // Step 4: Clear auth state but don't call backend logout during initialization
        if (isMounted) {
          // Just clear in-memory state, don't call authService.logout()
          clearTokens();
          // Don't need to manually set state, just let it stay logged out
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    // Only initialize if not already authenticated
    if (!isAuthenticated) {
      initializeAuth();
    } else {
      setLoading(false);
    }

    return () => {
      isMounted = false; // Cleanup flag
    };
  }, []); // Empty dependency array - run only once

  return (
    <QueryClientProvider client={queryClient}>
      <Router>
        <div className="App">
          <Suspense fallback={<LoadingSpinner size="xl" text="Loading application..." />}>
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={
                <PublicRoute>
                  <LoginPage />
                </PublicRoute>
              } />
              
              <Route path="/signup" element={
                <PublicRoute>
                  <EnhancedInviteRegistration />
                </PublicRoute>
              } />
              
              <Route path="/register" element={
                <PublicRoute>
                  <EnhancedInviteRegistration />
                </PublicRoute>
              } />
              
              <Route path="/forgot-password" element={
                <PublicRoute>
                  <ForgotPasswordPage />
                </PublicRoute>
              } />
              
              <Route path="/company/register" element={
                <PublicRoute>
                  <CompanyRegistration />
                </PublicRoute>
              } />

              {/* Kiosk Route (Public) */}
              <Route path="/kiosk" element={<KioskPage />} />

              {/* Admin Routes */}
              <Route path="/admin" element={
                <ProtectedRoute allowedRoles={['ADMIN', 'SYSADMIN']}>
                  <AdminLayout />
                </ProtectedRoute>
              }>
                <Route index element={<AdminDashboard />} />
                <Route path="company" element={<CompanyPage />} />
                <Route path="departments" element={<DepartmentsPage />} />
                <Route path="employees" element={<EmployeesPage />} />
                <Route path="jobs" element={<JobsPage />} />
                <Route path="shifts" element={<ShiftsPage />} />
                <Route path="payments" element={<PaymentsPage />} />
                <Route path="reports" element={<ReportsPage />} />
                <Route path="invites" element={<MultiUserInviteManagement />} />
              </Route>

              {/* Manager Routes (same as admin but with department filtering) */}
              <Route path="/manager" element={
                <ProtectedRoute allowedRoles={['MANAGER']} requireDepartment={true}>
                  <AdminLayout />
                </ProtectedRoute>
              }>
                <Route index element={<AdminDashboard />} />
                <Route path="shifts" element={<ShiftsPage />} />
                <Route path="reports" element={<ReportsPage />} />
              </Route>

              {/* Employee Routes */}
              <Route path="/employee" element={
                <ProtectedRoute allowedRoles={['EMPLOYEE']}>
                  <EmployeeLayout />
                </ProtectedRoute>
              }>
                <Route index element={<EmployeeDashboard />} />
                <Route path="clock" element={<ClockPage />} />
                <Route path="shifts" element={<MyShiftsPage />} />
                <Route path="payments" element={<MyPaymentsPage />} />
                <Route path="profile" element={<ProfilePage />} />
              </Route>

              {/* Error Routes */}
              <Route path="/unauthorized" element={<UnauthorizedPage />} />
              <Route path="/no-departments" element={<NoDepartmentsPage />} />

              {/* Default redirects */}
              <Route path="/" element={<Navigate to="/login" replace />} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </Suspense>

          {/* Global Toast Notifications */}
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 4000,
              style: {
                background: '#363636',
                color: '#fff',
              },
              success: {
                duration: 3000,
                style: {
                  background: '#10B981',
                },
              },
              error: {
                duration: 5000,
                style: {
                  background: '#EF4444',
                },
              },
            }}
          />
        </div>
      </Router>
    </QueryClientProvider>
  );
}

export default App;
