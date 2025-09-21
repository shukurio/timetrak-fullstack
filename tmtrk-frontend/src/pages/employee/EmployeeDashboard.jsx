import { useState, useEffect } from 'react';
import { Clock, CreditCard, TrendingUp, Calendar, AlertCircle } from 'lucide-react';
import employeeService from '../../api/employeeService';
import useAuthStore from '../../store/authStore';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const EmployeeDashboard = () => {
  const { user, isAuthenticated, getAccessToken } = useAuthStore();

  // Debug logs - remove these after fixing
  console.log('Auth Debug:', {
    isAuthenticated,
    user: user?.firstName,
    hasToken: !!getAccessToken(),
    token: getAccessToken()?.substring(0, 20) + '...' // Show first 20 chars only
  });
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const summary = await employeeService.getDashboardSummary();
      setDashboardData(summary);
    } catch (err) {
      setError('Failed to load dashboard data');
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };


  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount || 0);
  };

  const formatHours = (hours) => {
    return hours ? `${hours.toFixed(1)} hrs` : '0 hrs';
  };


  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <LoadingSpinner />
      </div>
    );
  }

  if (!dashboardData) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <div className="text-center">
          <AlertCircle className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Data Available</h3>
          <p className="text-gray-600">Unable to load dashboard data</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Welcome back, {user?.firstName || 'Employee'}!</h1>
        </div>
      </div>

      {/* Period Header */}
      {dashboardData.formattedPeriod && (
        <div className="bg-gradient-to-r from-blue-600 to-blue-700 rounded-xl p-6 text-white">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold mb-1">Current Pay Period</h2>
              <p className="text-3xl font-extrabold text-blue-100">
                {dashboardData.formattedPeriod}
              </p>
              {dashboardData.periodNumber && (
                <p className="text-blue-200 mt-2">Period #{dashboardData.periodNumber}</p>
              )}
            </div>
            <div className="text-right">
              <Calendar className="h-12 w-12 text-blue-200 mb-2" />
              <p className="text-blue-200 text-sm">Active Period</p>
            </div>
          </div>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <AlertCircle className="h-5 w-5 text-red-400 mr-2" />
            <p className="text-red-800">{error}</p>
          </div>
        </div>
      )}

      {/* Today's Metrics */}
      <div className="card">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900 flex items-center">
            <Clock className="h-6 w-6 text-orange-500 mr-2" />
            Today's Work
          </h2>
          <span className="text-sm text-gray-500">{new Date().toLocaleDateString()}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-orange-50 rounded-lg p-4 border border-orange-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-orange-600">Hours Today</p>
                <p className="text-2xl font-bold text-orange-900">
                  {dashboardData.todayHours ? formatHours(dashboardData.todayHours) : '0 hrs'}
                </p>
              </div>
              <Clock className="h-8 w-8 text-orange-400" />
            </div>
          </div>

          <div className="bg-green-50 rounded-lg p-4 border border-green-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-600">Earnings Today</p>
                <p className="text-2xl font-bold text-green-900">
                  {formatCurrency(dashboardData.todayEarnings || 0)}
                </p>
              </div>
              <CreditCard className="h-8 w-8 text-green-400" />
            </div>
          </div>

          <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-600">Shifts Today</p>
                <p className="text-2xl font-bold text-blue-900">
                  {dashboardData.todayShifts || 0}
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-blue-400" />
            </div>
          </div>
        </div>

        {dashboardData.todayHours && dashboardData.todayHours > 0 && (
          <div className="mt-4 p-3 bg-gradient-to-r from-green-500 to-green-600 rounded-lg text-white">
            <div className="flex items-center justify-between">
              <span className="font-medium">🎉 Great work today!</span>
              <span className="text-green-100">Keep it up!</span>
            </div>
          </div>
        )}
      </div>

      {/* Period Summary */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-900 flex items-center">
            <TrendingUp className="h-6 w-6 text-blue-500 mr-2" />
            Period Summary
          </h2>
          <span className="text-sm text-gray-500">Current pay period performance</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-6 border border-blue-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-blue-600 mb-1">Total Hours</p>
                <p className="text-3xl font-bold text-blue-900">
                  {formatHours(dashboardData.currentPeriodHours)}
                </p>
                <p className="text-sm text-blue-600 mt-1">
                  {dashboardData.currentPeriodShifts} shifts completed
                </p>
              </div>
              <div className="bg-blue-200 p-3 rounded-lg">
                <Clock className="h-8 w-8 text-blue-700" />
              </div>
            </div>
          </div>

          <div className="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-6 border border-green-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-green-600 mb-1">Total Earnings</p>
                <p className="text-3xl font-bold text-green-900">
                  {formatCurrency(dashboardData.currentPeriodEarnings)}
                </p>
                <p className="text-sm text-green-600 mt-1">This period</p>
              </div>
              <div className="bg-green-200 p-3 rounded-lg">
                <CreditCard className="h-8 w-8 text-green-700" />
              </div>
            </div>
          </div>

          <div className="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-6 border border-purple-200">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-purple-600 mb-1">Average Rate</p>
                <p className="text-3xl font-bold text-purple-900">
                  {formatCurrency(dashboardData.averageHourlyRate)}
                </p>
                <p className="text-sm text-purple-600 mt-1">Per hour</p>
              </div>
              <div className="bg-purple-200 p-3 rounded-lg">
                <TrendingUp className="h-8 w-8 text-purple-700" />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Performance Metrics */}
      {(dashboardData.averageHoursPerDay || dashboardData.averageEarningsPerDay) && (
        <div className="card">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Performance Summary</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {dashboardData.averageHoursPerDay && (
              <div className="p-4 bg-blue-50 rounded-lg">
                <p className="text-sm font-medium text-blue-600">Average Hours/Day</p>
                <p className="text-xl font-semibold text-blue-900">
                  {formatHours(dashboardData.averageHoursPerDay)}
                </p>
              </div>
            )}
            {dashboardData.averageEarningsPerDay && (
              <div className="p-4 bg-green-50 rounded-lg">
                <p className="text-sm font-medium text-green-600">Average Earnings/Day</p>
                <p className="text-xl font-semibold text-green-900">
                  {formatCurrency(dashboardData.averageEarningsPerDay)}
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default EmployeeDashboard;