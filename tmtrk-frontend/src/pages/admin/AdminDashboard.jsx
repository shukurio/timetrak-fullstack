import { useState, useEffect } from 'react';
import { Users, Briefcase, Clock, CreditCard, TrendingUp, AlertCircle } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { format, startOfWeek, endOfWeek, startOfMonth, endOfMonth } from 'date-fns';

const AdminDashboard = () => {
  // Fetch dashboard stats from single endpoint (includes shifts and payments)
  const { data: dashboardData, isLoading: dashboardLoading } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: () => adminService.getDashboardStats(),
  });

  const formatPercentageChange = (change) => {
    if (!change || change === 0) return '0%';
    const sign = change > 0 ? '+' : '';
    return `${sign}${change.toFixed(1)}%`;
  };

  const getChangeDescription = (change, type) => {
    if (!change || change === 0) return 'No change from last period';
    const direction = change > 0 ? 'increase' : 'decrease';
    const absChange = Math.abs(change).toFixed(1);

    if (type === 'hours') {
      return `${absChange}% ${direction} from last period`;
    } else if (type === 'revenue') {
      return `${absChange}% ${direction} from last period`;
    }
    return `${absChange}% ${direction} from last period`;
  };

  const formatCurrency = (amount) => {
    if (!amount) return '$0';
    return `$${Number(amount).toLocaleString()}`;
  };

  const statsCards = [
    {
      title: 'Active Employees',
      value: dashboardData?.activeEmployeeCount || 0,
      icon: Users,
      color: 'bg-blue-500',
      change: dashboardData?.activeShiftsCount > 0
        ? `${dashboardData.activeShiftsCount} active shifts`
        : 'No active shifts',
      changeType: dashboardData?.activeShiftsCount > 0 ? 'positive' : 'neutral'
    },
    {
      title: 'Pending Approvals',
      value: dashboardData?.pendingEmployeeCount || 0,
      icon: AlertCircle,
      color: 'bg-yellow-500',
      change: dashboardData?.pendingEmployeeCount > 0 ? 'Action Required' : 'All Cleared',
      changeType: dashboardData?.pendingEmployeeCount > 0 ? 'warning' : 'positive'
    },
    {
      title: 'Period Hours',
      value: dashboardData?.thisPeriodHours?.toFixed(1) || '0',
      icon: Clock,
      color: 'bg-green-500',
      change: getChangeDescription(dashboardData?.popHoursChange, 'hours'),
      changeType: dashboardData?.popHoursChange > 0 ? 'positive' :
                   dashboardData?.popHoursChange < 0 ? 'negative' : 'neutral'
    },
    {
      title: 'Period Revenue',
      value: formatCurrency(dashboardData?.thisPeriodRevenue),
      icon: CreditCard,
      color: 'bg-purple-500',
      change: getChangeDescription(dashboardData?.popRevenueChange, 'revenue'),
      changeType: dashboardData?.popRevenueChange > 0 ? 'positive' :
                   dashboardData?.popRevenueChange < 0 ? 'negative' : 'neutral'
    }
  ];

  if (dashboardLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner text="Loading dashboard..." />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <div className="text-sm text-gray-600">
          {format(new Date(), 'EEEE, MMMM d, yyyy')}
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statsCards.map((card, index) => (
          <div key={index} className="card">
            <div className="flex items-center">
              <div className={`${card.color} p-3 rounded-lg`}>
                <card.icon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">{card.title}</p>
                <p className="text-2xl font-semibold text-gray-900">{card.value}</p>
              </div>
            </div>
            <div className="mt-4">
              <span className={`text-base font-semibold ${
                card.changeType === 'positive' ? 'text-green-600' :
                card.changeType === 'negative' ? 'text-red-600' :
                card.changeType === 'warning' ? 'text-yellow-600' :
                'text-gray-500'
              }`}>
                {card.change}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Shifts */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Recent Shifts</h2>
            <Clock className="h-5 w-5 text-gray-400" />
          </div>
          
          {dashboardLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="space-y-3">
              {dashboardData?.recentShifts?.map((shift) => (
                <div key={shift.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                  <div>
                    <p className="font-medium text-gray-900">{shift.fullName}</p>
                    <p className="text-sm text-gray-600">{shift.jobTitle}</p>
                  </div>
                  <div className="text-right">
                    {shift.status === 'ACTIVE' ? (
                      <div className="flex items-center">
                        <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
                        <p className="text-sm font-medium text-green-600">Active</p>
                      </div>
                    ) : (
                      <p className="text-sm font-medium text-gray-900">
                        {shift.hours?.toFixed(1)} hrs
                      </p>
                    )}
                    <p className="text-sm text-gray-600">
                      {shift.clockIn ? format(new Date(shift.clockIn), 'MMM d') : '-'}
                    </p>
                  </div>
                </div>
              )) || (
                <p className="text-gray-500 text-center py-4">No recent shifts</p>
              )}
            </div>
          )}
        </div>

        {/* Recent Payments */}
        <div className="card">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Recent Payments</h2>
            <CreditCard className="h-5 w-5 text-gray-400" />
          </div>
          
          {dashboardLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="space-y-3">
              {dashboardData?.recentPayments?.map((payment) => (
                <div key={payment.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                  <div>
                    <p className="font-medium text-gray-900">{payment.employeeName}</p>
                    <p className="text-sm text-gray-600">{payment.formattedPeriod}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">
                      ${payment.totalEarnings?.toFixed(2)}
                    </p>
                    <span className={`text-xs px-2 py-1 rounded-full uppercase font-medium ${
                      payment.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                      payment.status === 'ISSUED' ? 'bg-blue-100 text-blue-800' :
                      payment.status === 'CALCULATED' ? 'bg-yellow-100 text-yellow-800' :
                      payment.status === 'VOIDED' ? 'bg-red-100 text-red-800' :
                      'bg-gray-100 text-gray-800'
                    }`}>
                      {payment.status}
                    </span>
                  </div>
                </div>
              )) || (
                <p className="text-gray-500 text-center py-4">No recent payments</p>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-400 hover:bg-primary-50 transition-colors">
            <Users className="h-8 w-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Add Employee</p>
          </button>
          
          <button className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-400 hover:bg-primary-50 transition-colors">
            <Briefcase className="h-8 w-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Create Job</p>
          </button>
          
          <button className="p-4 border-2 border-dashed border-gray-300 rounded-lg hover:border-primary-400 hover:bg-primary-50 transition-colors">
            <CreditCard className="h-8 w-8 text-gray-400 mx-auto mb-2" />
            <p className="text-sm font-medium text-gray-700">Process Payments</p>
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;