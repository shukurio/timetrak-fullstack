import { useState, useEffect } from 'react';
import { Users, Briefcase, Clock, CreditCard, TrendingUp, AlertCircle } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { format, startOfWeek, endOfWeek, startOfMonth, endOfMonth } from 'date-fns';

const AdminDashboard = () => {
  const [activeEmployees, setActiveEmployees] = useState(0);
  const [pendingEmployees, setPendingEmployees] = useState(0);

  // Fetch active employees count
  const { data: activeEmployeesData } = useQuery({
    queryKey: ['activeEmployees'],
    queryFn: () => adminService.getActiveEmployees({ page: 0, size: 1 }),
  });

  // Fetch pending employees count
  const { data: pendingEmployeesData } = useQuery({
    queryKey: ['pendingEmployees'],
    queryFn: () => adminService.getEmployeesByStatus('PENDING', { page: 0, size: 1 }),
  });

  // Fetch this week's shifts
  const { data: weekShifts, isLoading: weekShiftsLoading } = useQuery({
    queryKey: ['weekShifts'],
    queryFn: () => adminService.getThisWeekShifts({ page: 0, size: 100 }),
  });

  // Fetch this month's shifts
  const { data: monthShifts, isLoading: monthShiftsLoading } = useQuery({
    queryKey: ['monthShifts'],
    queryFn: () => adminService.getThisMonthShifts({ page: 0, size: 100 }),
  });

  // Fetch recent payments
  const { data: recentPayments, isLoading: paymentsLoading } = useQuery({
    queryKey: ['recentPayments'],
    queryFn: () => adminService.getAllPayments({ page: 0, size: 10 }),
  });

  useEffect(() => {
    if (activeEmployeesData) {
      setActiveEmployees(activeEmployeesData.totalElements || 0);
    }
  }, [activeEmployeesData]);

  useEffect(() => {
    if (pendingEmployeesData) {
      setPendingEmployees(pendingEmployeesData.totalElements || 0);
    }
  }, [pendingEmployeesData]);

  const statsCards = [
    {
      title: 'Active Employees',
      value: activeEmployees,
      icon: Users,
      color: 'bg-blue-500',
      change: '+2.5%',
      changeType: 'positive'
    },
    {
      title: 'Pending Approvals',
      value: pendingEmployees,
      icon: AlertCircle,
      color: 'bg-yellow-500',
      change: pendingEmployees > 0 ? 'Action Required' : 'All Cleared',
      changeType: pendingEmployees > 0 ? 'warning' : 'positive'
    },
    {
      title: 'This Week Hours',
      value: weekShifts?.content?.reduce((sum, shift) => sum + (shift.hours || 0), 0).toFixed(1) || '0',
      icon: Clock,
      color: 'bg-green-500',
      change: `${weekShifts?.content?.length || 0} shifts`,
      changeType: 'neutral'
    },
    {
      title: 'This Month Revenue',
      value: `$${monthShifts?.content?.reduce((sum, shift) => sum + (shift.shiftEarnings || 0), 0).toFixed(0) || '0'}`,
      icon: CreditCard,
      color: 'bg-purple-500',
      change: `${monthShifts?.content?.length || 0} shifts`,
      changeType: 'neutral'
    }
  ];

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
              <span className={`text-sm ${
                card.changeType === 'positive' ? 'text-green-600' :
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
          
          {weekShiftsLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="space-y-3">
              {weekShifts?.content?.slice(0, 5).map((shift) => (
                <div key={shift.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                  <div>
                    <p className="font-medium text-gray-900">{shift.fullName}</p>
                    <p className="text-sm text-gray-600">{shift.jobTitle}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">
                      {shift.hours?.toFixed(1)} hrs
                    </p>
                    <p className="text-sm text-gray-600">
                      {shift.clockIn ? format(new Date(shift.clockIn), 'MMM d') : '-'}
                    </p>
                  </div>
                </div>
              )) || (
                <p className="text-gray-500 text-center py-4">No shifts this week</p>
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
          
          {paymentsLoading ? (
            <LoadingSpinner />
          ) : (
            <div className="space-y-3">
              {recentPayments?.content?.slice(0, 5).map((payment) => (
                <div key={payment.id} className="flex items-center justify-between py-2 border-b last:border-b-0">
                  <div>
                    <p className="font-medium text-gray-900">{payment.employeeName}</p>
                    <p className="text-sm text-gray-600">{payment.formattedPeriod}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900">
                      ${payment.totalEarnings?.toFixed(2)}
                    </p>
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      payment.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                      payment.status === 'ISSUED' ? 'bg-blue-100 text-blue-800' :
                      payment.status === 'CALCULATED' ? 'bg-yellow-100 text-yellow-800' :
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