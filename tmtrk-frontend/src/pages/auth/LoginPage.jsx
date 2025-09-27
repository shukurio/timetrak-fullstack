import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import authService from '../../api/authService';
import useAuthStore from '../../store/authStore';
import TimeTrakIcon from '../../components/common/TimeTrakIcon';
import { normalizeUsername } from '../../utils/stringUtils';

const schema = yup.object({
  username: yup.string().required('Username is required').min(3, 'Username must be at least 3 characters'),
  password: yup.string().required('Password is required'),
});

const LoginPage = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((state) => state.login);

  const from = location.state?.from?.pathname || '/';

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
  });

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      // Normalize username before sending to backend
      const loginData = {
        ...data,
        username: normalizeUsername(data.username)
      };

      const response = await authService.login(loginData);
      
      login(response, response.token, response.expiresIn);
      
      toast.success('Login successful!');
      
      // Redirect based on role
      const role = response.user.role;
      switch (role) {
        case 'ADMIN':
        case 'SYSADMIN':
          navigate('/admin', { replace: true });
          break;
        case 'MANAGER':
          navigate('/manager', { replace: true });
          break;
        case 'EMPLOYEE':
          navigate('/employee', { replace: true });
          break;
        default:
          navigate(from, { replace: true });
      }
    } catch (error) {
      console.error('Login error:', error);
      toast.error(error.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <div className="mx-auto flex items-center justify-center">
            <TimeTrakIcon width={80} height={80} />
          </div>
          <h2 className="mt-6 text-center text-3xl font-bold text-gray-900">
            TimeTrak
          </h2>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                {...register('username')}
                type="text"
                className="input-field mt-1"
                placeholder="Enter your username"
              />
              {errors.username && (
                <p className="mt-1 text-sm text-error-600">{errors.username.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1 relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  className="input-field pr-10"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5 text-gray-400" />
                  ) : (
                    <Eye className="h-5 w-5 text-gray-400" />
                  )}
                </button>
              </div>
              {errors.password && (
                <p className="mt-1 text-sm text-error-600">{errors.password.message}</p>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div className="text-sm">
              <Link to="/forgot-password" className="font-medium text-primary-600 hover:text-primary-500">
                Forgot your password?
              </Link>
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isLoading}
              className="btn-primary w-full flex justify-center"
            >
              {isLoading ? (
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                'Sign In'
              )}
            </button>
          </div>

          <div className="mt-6 text-center">
            <Link
              to="/company/register"
              className="text-sm text-blue-600 hover:text-blue-500 font-medium"
            >
              New to TimeTrak? Register your company →
            </Link>
          </div>
        </form>

        {/* Demo Credentials */}
        <div className="mt-8 p-4 bg-blue-50 rounded-lg border border-blue-200">
          <div className="text-center">
            <p className="text-sm font-semibold text-blue-900 mb-2">
              Demo Credentials
            </p>
            <div className="space-y-2 text-xs text-blue-700">
              <div className="flex justify-between items-center bg-white rounded px-3 py-2">
                <span className="font-medium">Admin:</span>
                <span className="font-mono">testAdmin / Password@123</span>
              </div>
              <div className="flex justify-between items-center bg-white rounded px-3 py-2">
                <span className="font-medium">User:</span>
                <span className="font-mono">testUser / Password@123</span>
              </div>
            </div>
            <p className="text-xs text-blue-600 mt-2 italic">
              Use these credentials to explore the application
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;