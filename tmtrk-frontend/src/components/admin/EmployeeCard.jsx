import { useState, useEffect, useRef } from 'react';
import { 
  User, 
  X, 
  Mail, 
  Phone, 
  Calendar, 
  Building2, 
  Clock, 
  DollarSign,
  Briefcase,
  ChevronLeft,
  ChevronRight,
  MoreVertical,
  Plus,
  Edit,
  CheckCircle,
  XCircle,
  UserCheck,
  UserX,
  Trash2
} from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../common/LoadingSpinner';

const EmployeeCard = ({ employee, isOpen, onClose, onActionClick, mutations }) => {
  const [activeTab, setActiveTab] = useState('info');
  const [showDropdown, setShowDropdown] = useState(false);
  const [showCreateShiftModal, setShowCreateShiftModal] = useState(false);
  const [showOverrideWageModal, setShowOverrideWageModal] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };

    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [showDropdown]);

  // Fetch employee shifts
  const { data: shiftsData, isLoading: shiftsLoading } = useQuery({
    queryKey: ['employee-shifts', employee.id],
    queryFn: () => adminService.getShiftsByEmployee(employee.id, { page: 0, size: 20, sortBy: 'clockIn', sortDir: 'desc' }),
    enabled: isOpen && activeTab === 'shifts'
  });

  // Fetch employee payments
  const { data: paymentsData, isLoading: paymentsLoading } = useQuery({
    queryKey: ['employee-payments', employee.id],
    queryFn: () => adminService.getPaymentsByEmployee(employee.id, { page: 0, size: 20 }),
    enabled: isOpen && activeTab === 'payments'
  });

  // Fetch employee jobs (always fetch when modal is open for shift creation)
  const { data: jobsData, isLoading: jobsLoading } = useQuery({
    queryKey: ['employee-jobs', employee.id],
    queryFn: () => adminService.getEmployeeJobsByEmployee(employee.id),
    enabled: isOpen
  });

  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { color: 'bg-yellow-100 text-yellow-800', label: 'Pending' },
      ACTIVE: { color: 'bg-green-100 text-green-800', label: 'Active' },
      DEACTIVATED: { color: 'bg-red-100 text-red-800', label: 'Deactivated' },
      REJECTED: { color: 'bg-gray-100 text-gray-600', label: 'Rejected' },
      DELETED: { color: 'bg-gray-100 text-gray-400', label: 'Deleted' },
    };

    const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-800', label: status };
    
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
        {config.label}
      </span>
    );
  };

  const formatDuration = (clockIn, clockOut) => {
    if (!clockIn) return '-';
    if (!clockOut) return 'Active';
    
    const start = new Date(clockIn);
    const end = new Date(clockOut);
    const diffMs = end - start;
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    return `${hours}h ${minutes}m`;
  };

  // Create Shift Modal
  const CreateShiftModal = () => {
    const queryClient = useQueryClient();
    const { register, handleSubmit, reset, watch, formState: { errors } } = useForm({
      defaultValues: {
        employeeJobId: '',
        clockIn: '',
        clockOut: '',
        notes: ''
      }
    });

    // Watch clockIn and clockOut values to auto-determine status
    const watchClockIn = watch('clockIn');
    const watchClockOut = watch('clockOut');

    // Auto-determine status based on clock times
    const getAutoStatus = () => {
      if (watchClockIn && watchClockOut) {
        return 'COMPLETED';
      } else if (watchClockIn && !watchClockOut) {
        return 'ACTIVE';
      }
      return '';
    };

    const autoStatus = getAutoStatus();

    const createShiftMutation = useMutation({
      mutationFn: (shiftData) => adminService.createShift(shiftData),
      onSuccess: () => {
        toast.success('Shift created successfully!');
        queryClient.invalidateQueries(['employee-shifts', employee.id]);
        setShowCreateShiftModal(false);
        reset();
      },
      onError: (error) => {
        console.error('Create shift error:', error);
        toast.error(error.response?.data?.message || 'Failed to create shift');
      }
    });

    const onSubmit = (data) => {
      // Auto-determine status based on clock times
      const status = data.clockOut ? 'COMPLETED' : 'ACTIVE';

      // Convert datetime-local values to ISO format
      const shiftData = {
        employeeJobId: parseInt(data.employeeJobId),
        clockIn: new Date(data.clockIn).toISOString(),
        clockOut: status === 'ACTIVE' ? null : (data.clockOut ? new Date(data.clockOut).toISOString() : null),
        status: status,
        ...(data.notes && data.notes.trim() && { notes: data.notes.trim() })
      };
      createShiftMutation.mutate(shiftData);
    };

    const jobs = jobsData || [];

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl">
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b">
            <h3 className="text-xl font-bold text-gray-900">Create New Shift</h3>
            <button
              onClick={() => {
                setShowCreateShiftModal(false);
                reset();
              }}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-6">
            {/* Job Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Assignment *
              </label>
              <select
                {...register('employeeJobId', { required: 'Job selection is required' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Select a job...</option>
                {jobs.map((job) => (
                  <option key={job.employeeJobId} value={job.employeeJobId}>
                    {job.jobTitle} - ${job.hourlyWage}/hr ({job.departmentName})
                  </option>
                ))}
              </select>
              {errors.employeeJobId && (
                <p className="mt-1 text-sm text-red-600">{errors.employeeJobId.message}</p>
              )}
            </div>

            {/* Clock In */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Clock In Time *
              </label>
              <input
                type="datetime-local"
                {...register('clockIn', { required: 'Clock in time is required' })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
              {errors.clockIn && (
                <p className="mt-1 text-sm text-red-600">{errors.clockIn.message}</p>
              )}
            </div>

            {/* Clock Out */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Clock Out Time
              </label>
              <input
                type="datetime-local"
                {...register('clockOut')}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
              <p className="mt-1 text-sm text-gray-500">
                Leave empty for active shift
              </p>
            </div>

            {/* Status */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>
              <div className="p-3 border rounded-lg bg-gray-50">
                <div className="flex items-center space-x-2">
                  <span className="text-sm font-medium text-gray-600">Auto Status:</span>
                  {autoStatus === 'COMPLETED' ? (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      <span className="w-2 h-2 mr-1 bg-blue-400 rounded-full"></span>
                      COMPLETED
                    </span>
                  ) : autoStatus === 'ACTIVE' ? (
                    <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      <span className="w-2 h-2 mr-1 bg-green-400 rounded-full"></span>
                      ACTIVE
                    </span>
                  ) : (
                    <span className="text-sm text-gray-500">Enter clock times</span>
                  )}
                </div>
                <p className="text-xs text-gray-500 mt-2">
                  {autoStatus === 'ACTIVE' && 'Shift is active (no clock out time)'}
                  {autoStatus === 'COMPLETED' && 'Shift is completed (has clock out time)'}
                  {!autoStatus && 'Status will be determined by clock times'}
                </p>
              </div>
            </div>

            {/* Notes */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Notes
              </label>
              <textarea
                {...register('notes')}
                rows={3}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                placeholder="Optional shift notes..."
              />
            </div>

            {/* Buttons */}
            <div className="flex justify-end space-x-3 pt-4 border-t">
              <button
                type="button"
                onClick={() => {
                  setShowCreateShiftModal(false);
                  reset();
                }}
                className="btn-outline"
                disabled={createShiftMutation.isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={createShiftMutation.isLoading}
              >
                {createShiftMutation.isLoading ? 'Creating...' : 'Create Shift'}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };

  // Override Wage Modal
  const OverrideWageModal = () => {
    const queryClient = useQueryClient();
    const { register, handleSubmit, reset, formState: { errors } } = useForm({
      defaultValues: {
        employeeJobId: "",
        hourlyWage: ""
      }
    });

    const overrideWageMutation = useMutation({
      mutationFn: ({ employeeJobId, hourlyWage }) => 
        adminService.updateEmployeeJob(employeeJobId, { hourlyWage }),
      onSuccess: () => {
        toast.success("Wage override applied successfully!");
        queryClient.invalidateQueries(["employee-jobs", employee.id]);
        setShowOverrideWageModal(false);
        reset();
      },
      onError: (error) => {
        console.error("Override wage error:", error);
        toast.error(error.response?.data?.message || "Failed to override wage");
      }
    });

    const onSubmit = (data) => {
      const wageData = {
        employeeJobId: parseInt(data.employeeJobId),
        hourlyWage: parseFloat(data.hourlyWage)
      };
      overrideWageMutation.mutate(wageData);
    };

    const jobs = jobsData || [];

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg">
          <div className="flex items-center justify-between p-6 border-b">
            <h3 className="text-xl font-bold text-gray-900">Override Employee Wage</h3>
            <button
              onClick={() => {
                setShowOverrideWageModal(false);
                reset();
              }}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="p-6 space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Assignment *
              </label>
              <select
                {...register("employeeJobId", { required: "Job selection is required" })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">Select a job to override wage...</option>
                {jobs.map((job) => (
                  <option key={job.employeeJobId} value={job.employeeJobId}>
                    {job.jobTitle} - Current: ${job.hourlyWage}/hr (Default: ${job.jobDefaultWage}/hr)
                  </option>
                ))}
              </select>
              {errors.employeeJobId && (
                <p className="mt-1 text-sm text-red-600">{errors.employeeJobId.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                New Hourly Wage *
              </label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500">$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  {...register("hourlyWage", { 
                    required: "New wage is required",
                    min: { value: 0, message: "Wage must be positive" }
                  })}
                  className="w-full pl-8 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
                  placeholder="15.00"
                />
              </div>
              {errors.hourlyWage && (
                <p className="mt-1 text-sm text-red-600">{errors.hourlyWage.message}</p>
              )}
              <p className="mt-1 text-sm text-gray-500">
                This will override the employee's wage for this specific job assignment
              </p>
            </div>

            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
              <div className="flex">
                <div className="flex-shrink-0">
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                </div>
                <div className="ml-3">
                  <h3 className="text-sm font-medium text-yellow-800">Wage Override</h3>
                  <div className="mt-2 text-sm text-yellow-700">
                    <p>This will change the hourly wage for this employee's specific job assignment. It will not affect the default job wage or other employees.</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4 border-t">
              <button
                type="button"
                onClick={() => {
                  setShowOverrideWageModal(false);
                  reset();
                }}
                className="btn-outline"
                disabled={overrideWageMutation.isLoading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={overrideWageMutation.isLoading}
              >
                {overrideWageMutation.isLoading ? "Updating..." : "Override Wage"}
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  };
  const tabs = [
    { id: 'info', label: 'Info', icon: User },
    { id: 'shifts', label: 'Shifts', icon: Clock },
    { id: 'payments', label: 'Payments', icon: DollarSign },
    { id: 'jobs', label: 'Jobs', icon: Briefcase }
  ];

  const renderEmployeeInfo = () => (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center">
          <span className="text-primary-600 font-bold text-xl">
            {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
          </span>
        </div>
        <div>
          <h3 className="text-xl font-semibold text-gray-900">
            {employee.firstName} {employee.lastName}
          </h3>
          <p className="text-gray-600">@{employee.username}</p>
          <div className="mt-1">{getStatusBadge(employee.status)}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-4">
          <div className="flex items-center space-x-3">
            <Mail className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-sm font-medium text-gray-900">Email</p>
              <p className="text-sm text-gray-600">{employee.email}</p>
            </div>
          </div>
          
          {employee.phoneNumber && (
            <div className="flex items-center space-x-3">
              <Phone className="h-5 w-5 text-gray-400" />
              <div>
                <p className="text-sm font-medium text-gray-900">Phone</p>
                <p className="text-sm text-gray-600">{employee.phoneNumber}</p>
              </div>
            </div>
          )}

          <div className="flex items-center space-x-3">
            <Building2 className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-sm font-medium text-gray-900">Department</p>
              <p className="text-sm text-gray-600">{employee.departmentName || 'Not Assigned'}</p>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex items-center space-x-3">
            <User className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-sm font-medium text-gray-900">Role</p>
              <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {employee.role}
              </span>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <Calendar className="h-5 w-5 text-gray-400" />
            <div>
              <p className="text-sm font-medium text-gray-900">Employee ID</p>
              <p className="text-sm text-gray-600">#{employee.id}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const renderShifts = () => {
    if (shiftsLoading) return <LoadingSpinner />;
    
    const shifts = shiftsData?.content || [];

    if (!shifts.length) {
      return (
        <div className="text-center py-8">
          <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No shifts found</h3>
          <p className="text-gray-600">This employee hasn't clocked any shifts yet.</p>
        </div>
      );
    }

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Recent Shifts</h3>
          <span className="text-sm text-gray-500">{shifts.length} shifts</span>
        </div>
        
        <div className="space-y-3">
          {shifts.map((shift) => (
            <div key={shift.id} className="border rounded-lg p-4 hover:bg-gray-50">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center space-x-3">
                  <span className="font-medium">{shift.jobTitle}</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    shift.status === 'ACTIVE' 
                      ? 'bg-green-100 text-green-800'
                      : shift.status === 'COMPLETED'
                      ? 'bg-blue-100 text-blue-800' 
                      : 'bg-gray-100 text-gray-800'
                  }`}>
                    {shift.status}
                  </span>
                </div>
                <span className="font-semibold text-green-600">
                  ${shift.shiftEarnings?.toFixed(2) || '0.00'}
                </span>
              </div>
              
              <div className="grid grid-cols-3 gap-4 text-sm text-gray-600">
                <div>
                  <span className="font-medium">Clock In:</span><br />
                  {format(new Date(shift.clockIn), 'MMM d, HH:mm')}
                </div>
                <div>
                  <span className="font-medium">Clock Out:</span><br />
                  {shift.clockOut ? format(new Date(shift.clockOut), 'MMM d, HH:mm') : 'Active'}
                </div>
                <div>
                  <span className="font-medium">Duration:</span><br />
                  {formatDuration(shift.clockIn, shift.clockOut)}
                </div>
              </div>
              
              {shift.notes && (
                <div className="mt-2 text-sm text-gray-600">
                  <span className="font-medium">Notes:</span> {shift.notes}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderPayments = () => {
    if (paymentsLoading) return <LoadingSpinner />;
    
    const payments = paymentsData?.content || [];

    if (!payments.length) {
      return (
        <div className="text-center py-8">
          <DollarSign className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No payments found</h3>
          <p className="text-gray-600">This employee hasn't received any payments yet.</p>
        </div>
      );
    }

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Payment History</h3>
          <span className="text-sm text-gray-500">{payments.length} payments</span>
        </div>
        
        <div className="space-y-3">
          {payments.map((payment) => (
            <div key={payment.id} className="border rounded-lg p-4 hover:bg-gray-50">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center space-x-3">
                  <span className="font-medium">{payment.formattedPeriod}</span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    payment.status === 'COMPLETED' 
                      ? 'bg-green-100 text-green-800'
                      : payment.status === 'ISSUED'
                      ? 'bg-blue-100 text-blue-800'
                      : payment.status === 'CALCULATED'
                      ? 'bg-yellow-100 text-yellow-800'
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {payment.status}
                  </span>
                </div>
                <span className="text-lg font-semibold text-green-600">
                  ${payment.totalEarnings?.toFixed(2) || '0.00'}
                </span>
              </div>
              
              <div className="grid grid-cols-3 gap-4 text-sm text-gray-600">
                <div>
                  <span className="font-medium">Hours:</span><br />
                  {payment.totalHours?.toFixed(1) || '0'} hrs
                </div>
                <div>
                  <span className="font-medium">Shifts:</span><br />
                  {payment.shiftsCount} shifts
                </div>
                <div>
                  <span className="font-medium">Jobs:</span><br />
                  {payment.jobsCount} positions
                </div>
              </div>
              
              {payment.calculatedAt && (
                <div className="mt-2 text-xs text-gray-500">
                  Calculated: {format(new Date(payment.calculatedAt), 'MMM d, yyyy HH:mm')}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderJobs = () => {
    if (jobsLoading) return <LoadingSpinner />;
    
    const jobs = jobsData || [];

    if (!jobs.length) {
      return (
        <div className="text-center py-8">
          <Briefcase className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No job assignments</h3>
          <p className="text-gray-600">This employee hasn't been assigned to any jobs yet.</p>
        </div>
      );
    }

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">Job Assignments</h3>
          <span className="text-sm text-gray-500">{jobs.length} assignments</span>
        </div>
        
        <div className="grid gap-4">
          {jobs.map((job) => (
            <div key={job.employeeJobId} className="border rounded-lg p-4 hover:bg-gray-50">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                    <Briefcase className="h-5 w-5 text-primary-600" />
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900">{job.jobTitle}</h4>
                    <p className="text-sm text-gray-600">{job.departmentName}</p>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-lg font-semibold text-green-600">
                    ${job.hourlyWage}/hr
                  </div>
                  {job.jobDefaultWage !== job.hourlyWage && (
                    <div className="text-xs text-gray-500">
                      (Default: ${job.jobDefaultWage}/hr)
                    </div>
                  )}
                </div>
              </div>
              
              <div className="flex items-center justify-between text-sm">
                <div className="flex items-center space-x-4">
                  <span className="text-gray-600">
                    Assigned: {format(new Date(job.assignedAt), 'MMM d, yyyy')}
                  </span>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                    job.isActive 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-gray-100 text-gray-600'
                  }`}>
                    {job.isActive ? 'Active' : 'Inactive'}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderModalContent = () => {
    switch (activeTab) {
      case 'info':
        return renderEmployeeInfo();
      case 'shifts':
        return renderShifts();
      case 'payments':
        return renderPayments();
      case 'jobs':
        return renderJobs();
      default:
        return renderEmployeeInfo();
    }
  };

  return (
    <>
      {/* Modal */}
      {isOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between p-6 border-b">
              <div className="flex items-center space-x-4">
                <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
                  <span className="text-primary-600 font-bold text-lg">
                    {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
                  </span>
                </div>
                <div>
                  <h2 className="text-xl font-bold text-gray-900">
                    {employee.firstName} {employee.lastName}
                  </h2>
                  <p className="text-gray-600">@{employee.username}</p>
                </div>
              </div>
              <div className="flex items-center space-x-2">
                {/* Dropdown Menu */}
                <div className="relative" ref={dropdownRef}>
                  <button
                    onClick={() => setShowDropdown(!showDropdown)}
                    className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
                  >
                    <MoreVertical className="h-5 w-5" />
                  </button>
                  
                  {showDropdown && (
                    <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-xl border border-gray-200 z-50">
                      <div className="py-2">
                        {/* Shift and Wage Actions */}
                        <button
                          onClick={() => {
                            setShowCreateShiftModal(true);
                            setShowDropdown(false);
                          }}
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 w-full text-left transition-colors"
                        >
                          <Plus className="h-4 w-4 mr-3" />
                          Create New Shift
                        </button>
                        <button
                          onClick={() => {
                            setShowOverrideWageModal(true);
                            setShowDropdown(false);
                          }}
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 w-full text-left transition-colors"
                        >
                          <Edit className="h-4 w-4 mr-3" />
                          Override Wage
                        </button>
                        
                        <div className="border-t border-gray-100 my-2"></div>
                        
                        {/* Employee Status Actions */}
                        {employee.status === 'PENDING' && (
                          <>
                            <button
                              onClick={(e) => {
                                onActionClick && onActionClick(e, 'approve', employee);
                                setShowDropdown(false);
                              }}
                              disabled={mutations?.approve?.isLoading}
                              className="flex items-center px-4 py-2 text-sm text-green-700 hover:bg-green-50 w-full text-left transition-colors"
                            >
                              <CheckCircle className="h-4 w-4 mr-3" />
                              Approve Employee
                            </button>
                            <button
                              onClick={(e) => {
                                onActionClick && onActionClick(e, 'reject', employee);
                                setShowDropdown(false);
                              }}
                              disabled={mutations?.reject?.isLoading}
                              className="flex items-center px-4 py-2 text-sm text-red-700 hover:bg-red-50 w-full text-left transition-colors"
                            >
                              <XCircle className="h-4 w-4 mr-3" />
                              Reject Employee
                            </button>
                          </>
                        )}
                        
                        {employee.status === 'ACTIVE' && (
                          <button
                            onClick={(e) => {
                              onActionClick && onActionClick(e, 'deactivate', employee);
                              setShowDropdown(false);
                            }}
                            disabled={mutations?.deactivate?.isLoading}
                            className="flex items-center px-4 py-2 text-sm text-orange-700 hover:bg-orange-50 w-full text-left transition-colors"
                          >
                            <UserX className="h-4 w-4 mr-3" />
                            Deactivate Employee
                          </button>
                        )}
                        
                        {(employee.status === 'DEACTIVATED' || employee.status === 'REJECTED') && (
                          <button
                            onClick={(e) => {
                              onActionClick && onActionClick(e, 'activate', employee);
                              setShowDropdown(false);
                            }}
                            disabled={mutations?.activate?.isLoading}
                            className="flex items-center px-4 py-2 text-sm text-green-700 hover:bg-green-50 w-full text-left transition-colors"
                          >
                            <UserCheck className="h-4 w-4 mr-3" />
                            Activate Employee
                          </button>
                        )}
                        
                        {onActionClick && (
                          <>
                            <div className="border-t border-gray-100 my-2"></div>
                            <button
                              onClick={(e) => {
                                onActionClick(e, 'delete', employee);
                                setShowDropdown(false);
                              }}
                              disabled={mutations?.delete?.isLoading}
                              className="flex items-center px-4 py-2 text-sm text-red-700 hover:bg-red-50 w-full text-left transition-colors"
                            >
                              <Trash2 className="h-4 w-4 mr-3" />
                              Delete Employee
                            </button>
                          </>
                        )}
                      </div>
                    </div>
                  )}
                </div>
                
                <button
                  onClick={onClose}
                  className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
                >
                  <X className="h-6 w-6" />
                </button>
              </div>
            </div>

            {/* Tabs */}
            <div className="border-b overflow-x-auto">
              <nav className="flex space-x-4 sm:space-x-8 px-6 min-w-max">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  const isActive = activeTab === tab.id;

                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`flex items-center py-4 px-2 border-b-2 font-medium text-sm whitespace-nowrap transition-colors ${
                        isActive
                          ? 'border-primary-500 text-primary-600'
                          : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                      }`}
                    >
                      <Icon className="h-4 w-4 sm:h-5 sm:w-5 mr-1 sm:mr-2" />
                      {tab.label}
                    </button>
                  );
                })}
              </nav>
            </div>

            {/* Content */}
            <div className="p-6 overflow-y-auto max-h-[60vh]">
              {renderModalContent()}
            </div>
          </div>
        </div>
      )}
      
      {/* Create Shift Modal */}
      {showCreateShiftModal && <CreateShiftModal />}\n      \n      {/* Override Wage Modal */}\n      {showOverrideWageModal && <OverrideWageModal />}
    </>
  );
};

export default EmployeeCard;