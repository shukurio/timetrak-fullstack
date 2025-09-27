import { useState, useEffect } from 'react';
import {
  DollarSign,
  Plus,
  Search,
  Filter,
  Download,
  CheckCircle,
  Clock,
  XCircle,
  AlertTriangle,
  Eye,
  Calendar,
  User,
  X,
  Save
} from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import PaymentPeriodSelector from '../../components/admin/PaymentPeriodSelector';

// Validation schema for calculate payments
const generatePaymentSchema = yup.object({
  selectedPeriod: yup.object()
    .required('Payment period is required')
    .nullable()
});

const PaymentsPage = () => {
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [showCalculateModal, setShowCalculateModal] = useState(false);
  const [selectedPayment, setSelectedPayment] = useState(null);
  const [selectedPayments, setSelectedPayments] = useState(new Set());
  const [selectedPeriod, setSelectedPeriod] = useState(null);
  const [availablePeriods, setAvailablePeriods] = useState([]);
  const [selectedGenerationPeriod, setSelectedGenerationPeriod] = useState(null);
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const pageSize = 20;

  const queryClient = useQueryClient();

  // Load initial data on mount
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        // Load available periods
        const periods = await adminService.getAvailablePaymentPeriods(12);
        setAvailablePeriods(periods);
        
        // Load most recent completed period as default
        const defaultPeriod = await adminService.getMostRecentCompletedPeriod();
        setSelectedPeriod(defaultPeriod);
      } catch (error) {
        console.error('Error loading initial payment data:', error);
        toast.error('Failed to load payment periods');
      }
    };
    
    loadInitialData();
  }, []);

  // Define tabs
  const tabs = [
    { 
      id: 'ALL', 
      label: 'All Payments', 
      color: 'text-gray-600', 
      activeColor: 'text-primary-600',
      icon: DollarSign,
      badgeColor: 'bg-gray-100 text-gray-800'
    },
    { 
      id: 'CALCULATED', 
      label: 'Calculated', 
      color: 'text-yellow-600', 
      activeColor: 'text-yellow-600',
      icon: Clock,
      badgeColor: 'bg-yellow-100 text-yellow-800'
    },
    { 
      id: 'ISSUED', 
      label: 'Issued', 
      color: 'text-blue-600', 
      activeColor: 'text-blue-600',
      icon: CheckCircle,
      badgeColor: 'bg-blue-100 text-blue-800'
    },
    { 
      id: 'COMPLETED', 
      label: 'Completed', 
      color: 'text-green-600', 
      activeColor: 'text-green-600',
      icon: CheckCircle,
      badgeColor: 'bg-green-100 text-green-800'
    },
    { 
      id: 'VOIDED', 
      label: 'Voided', 
      color: 'text-red-600', 
      activeColor: 'text-red-600',
      icon: XCircle,
      badgeColor: 'bg-red-100 text-red-800'
    }
  ];

  // Get payments query function - now uses period number with pagination
  const getPaymentsQuery = () => {
    if (!selectedPeriod || !selectedPeriod.periodNumber) {
      // Don't fetch if no period selected
      return Promise.resolve({ content: [], totalElements: 0, totalPages: 0 });
    }

    // Always filter by period number with pagination
    const params = { 
      page: currentPage, 
      size: pageSize 
    };
    return adminService.getPaymentsByPeriodNumber(selectedPeriod.periodNumber, params);
  };

  // Fetch payments - now depends on selected period number and pagination
  const { data: paymentsData, isLoading, error, refetch } = useQuery({
    queryKey: ['payments', selectedPeriod?.periodNumber, currentPage],
    queryFn: getPaymentsQuery,
    enabled: !!selectedPeriod?.periodNumber, // Only fetch when period is selected
    keepPreviousData: true,
    onError: (err) => console.error('Payments API Error:', err),
    retry: 1,
  });

  // Handle period change
  const handlePeriodChange = (period) => {
    setSelectedPeriod(period);
    setCurrentPage(0); // Reset pagination
  };


  // Calculate payments mutation
  const generatePaymentsMutation = useMutation({
    mutationFn: (data) => adminService.calculatePaymentsForPeriod({ periodNumber: data.selectedPeriod.periodNumber }),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['payments']);
      queryClient.invalidateQueries(['payment-counts']);
      setShowCalculateModal(false);
      setSelectedGenerationPeriod(null);
      reset();
      
      const { successCount, failureCount } = data;
      if (failureCount === 0) {
        toast.success(`Successfully calculated ${successCount} payments!`);
      } else {
        toast.success(`Calculated ${successCount} payments, ${failureCount} failed`);
      }
    },
    onError: (error) => {
      console.error('Calculate payments error:', error);
      toast.error(error.response?.data?.message || 'Failed to calculate payments');
    }
  });

  // Update payment status mutation
  const updateStatusMutation = useMutation({
    mutationFn: (data) => adminService.updatePaymentStatus(data),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['payments']);
      queryClient.invalidateQueries(['payment-counts']);
      setSelectedPayments(new Set());
      
      const { successCount, failureCount } = data;
      if (failureCount === 0) {
        toast.success(`Successfully updated ${successCount} payments!`);
      } else {
        toast.success(`Updated ${successCount} payments, ${failureCount} failed`);
      }
    },
    onError: (error) => {
      console.error('Update status error:', error);
      toast.error(error.response?.data?.message || 'Failed to update payment status');
    }
  });

  // Export payments by period mutation - updated to new API
  const exportPaymentsByPeriodMutation = useMutation({
    mutationFn: (periodNumber) => adminService.exportPaymentsByPeriod(periodNumber),
    onSuccess: (blob, periodNumber) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `payments-period-${periodNumber}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success('Payment report exported successfully!');
    },
    onError: (error) => {
      console.error('Export payments error:', error);
      toast.error(error.response?.data?.message || 'Failed to export payment report');
    }
  });

  // Form handling
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch
  } = useForm({
    resolver: yupResolver(generatePaymentSchema)
  });

  const watchedSelectedPeriod = watch('selectedPeriod');

  // Remove tab handling - not needed for period-based approach

  const handleCalculatePayments = (data) => {
    generatePaymentsMutation.mutate(data);
  };

  const handleStatusUpdate = (targetStatus) => {
    if (selectedPayments.size === 0) {
      toast.error('Please select payments to update');
      return;
    }

    const paymentIds = Array.from(selectedPayments);
    updateStatusMutation.mutate({
      paymentIds,
      targetStatus,
      reason: `Bulk update to ${targetStatus}`
    });
  };

  const handleSelectPayment = (paymentId) => {
    const newSelected = new Set(selectedPayments);
    if (newSelected.has(paymentId)) {
      newSelected.delete(paymentId);
    } else {
      newSelected.add(paymentId);
    }
    setSelectedPayments(newSelected);
  };

  const handleSelectAll = () => {
    const payments = paymentsData?.content || [];
    if (selectedPayments.size === payments.length) {
      setSelectedPayments(new Set());
    } else {
      const allIds = new Set(payments.map(p => p.id) || []);
      setSelectedPayments(allIds);
    }
  };

  const handleExportPayments = () => {
    if (!selectedPeriod) {
      toast.error('Please select a payment period first');
      return;
    }
    // Use new export endpoint
    exportPaymentsByPeriodMutation.mutate(selectedPeriod.periodNumber);
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      CALCULATED: { color: 'bg-yellow-100 text-yellow-800', label: 'CALCULATED' },
      ISSUED: { color: 'bg-blue-100 text-blue-800', label: 'ISSUED' },
      COMPLETED: { color: 'bg-green-100 text-green-800', label: 'COMPLETED' },
      VOIDED: { color: 'bg-red-100 text-red-800', label: 'VOIDED' },
    };

    const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-800', label: status?.toUpperCase() || 'UNKNOWN' };

    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium uppercase ${config.color}`}>
        {config.label}
      </span>
    );
  };

  const renderCalculateModal = () => (
    showCalculateModal && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 animate-scale-up">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-gray-900">Calculate Payments</h2>
            <button
              onClick={() => {
                setShowCalculateModal(false);
                setSelectedGenerationPeriod(null);
                reset();
              }}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(handleCalculatePayments)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Payment Period *
              </label>
              {!availablePeriods || availablePeriods.length === 0 ? (
                <div className="p-3 border border-gray-300 rounded-md bg-gray-50 text-gray-500">
                  {!availablePeriods ? 'Loading periods...' : 'No periods available'}
                </div>
              ) : (
                <select
                  className="input-field"
                  value={selectedGenerationPeriod?.periodNumber || ''}
                  onChange={(e) => {
                    const period = availablePeriods?.find(p => p.periodNumber === parseInt(e.target.value));
                    setSelectedGenerationPeriod(period);
                    setValue('selectedPeriod', period);
                  }}
                >
                  <option value="">Select a payment period</option>
                  {availablePeriods.map((period) => (
                    <option key={period.periodNumber} value={period.periodNumber}>
                      {period.displayLabel || period.formattedPeriod}
                    </option>
                  ))}
                </select>
              )}
              {errors.selectedPeriod && (
                <p className="text-sm text-red-600 mt-1">{errors.selectedPeriod.message}</p>
              )}
              <p className="text-xs text-gray-500 mt-1">
                Select the pay period for which to calculate payments
              </p>
            </div>

            <div className="flex space-x-3 pt-4">
              <button
                type="button"
                onClick={() => {
                  setShowCalculateModal(false);
                  setSelectedGenerationPeriod(null);
                  reset();
                }}
                className="flex-1 btn-outline"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={generatePaymentsMutation.isLoading || !selectedGenerationPeriod}
                className="flex-1 btn-primary flex items-center justify-center"
              >
                {generatePaymentsMutation.isLoading ? (
                  <LoadingSpinner size="sm" text="" />
                ) : (
                  <>Calculate</>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  );

  const renderPaymentDetails = () => (
    selectedPayment && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b">
            <div>
              <h2 className="text-xl font-bold text-gray-900">Payment Details</h2>
              <p className="text-gray-600">{selectedPayment.employeeName}</p>
            </div>
            <button
              onClick={() => setSelectedPayment(null)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <X className="h-6 w-6" />
            </button>
          </div>

          {/* Content */}
          <div className="p-6 overflow-y-auto max-h-[70vh]">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-500">Employee</label>
                  <p className="text-lg font-semibold">{selectedPayment.employeeName}</p>
                  <p className="text-sm text-gray-500">@{selectedPayment.employeeUsername}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-500">Period</label>
                  <p className="text-lg">{selectedPayment.formattedPeriod}</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-500">Status</label>
                  <div className="mt-1">{getStatusBadge(selectedPayment.status)}</div>
                </div>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-500">Total Earnings</label>
                  <p className="text-2xl font-bold text-green-600">
                    ${selectedPayment.totalEarnings?.toFixed(2) || '0.00'}
                  </p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-500">Total Hours</label>
                  <p className="text-lg">{selectedPayment.totalHours?.toFixed(1) || '0'} hours</p>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-500">Average Rate</label>
                  <p className="text-lg">${selectedPayment.averageHourlyRate?.toFixed(2) || '0.00'}/hour</p>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <p className="text-sm text-gray-500">Shifts</p>
                <p className="text-xl font-semibold">{selectedPayment.shiftsCount}</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <p className="text-sm text-gray-500">Calculated</p>
                <p className="text-sm">
                  {selectedPayment.calculatedAt ? format(new Date(selectedPayment.calculatedAt), 'MMM d') : '-'}
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4 text-center">
                <p className="text-sm text-gray-500">Completed</p>
                <p className="text-sm">
                  {selectedPayment.completedAt ? format(new Date(selectedPayment.completedAt), 'MMM d') : '-'}
                </p>
              </div>
            </div>

            {/* Job Details */}
            {selectedPayment.jobDetails && selectedPayment.jobDetails.length > 0 && (
              <div>
                <h3 className="text-lg font-semibold mb-4">Job Breakdown</h3>
                <div className="space-y-3">
                  {selectedPayment.jobDetails.map((job, index) => (
                    <div key={index} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-medium">{job.jobTitle}</h4>
                        <span className="font-semibold text-green-600">
                          ${job.totalEarnings?.toFixed(2)}
                        </span>
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm text-gray-600">
                        <div>
                          <span className="font-medium">Hours:</span><br />
                          {job.totalHours?.toFixed(1)} hrs
                        </div>
                        <div>
                          <span className="font-medium">Rate:</span><br />
                          ${job.hourlyRate}/hr
                        </div>
                        <div>
                          <span className="font-medium">Shifts:</span><br />
                          {job.shiftsCount}
                        </div>
                        <div>
                          <span className="font-medium">% of Total:</span><br />
                          {job.percentageOfTotalPay?.toFixed(1)}%
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    )
  );

  return (
    <div className="space-y-6">
      {/* Main container with header and content */}
      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <div className="flex items-center justify-center py-4 px-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <DollarSign className="h-5 w-5 text-blue-600" />
            <h1 className="text-lg font-semibold text-gray-900">Payment Management</h1>
          </div>
        </div>

        {/* Payment Period Selector */}
        <div className="p-6 border-b border-gray-200">
          <PaymentPeriodSelector
            onPeriodChange={handlePeriodChange}
            selectedPeriod={selectedPeriod}
            periods={availablePeriods}
          />
        </div>

        {/* Action Buttons */}
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <div className="flex gap-2 justify-end overflow-x-auto">
        {isSelectionMode ? (
          <>
            <button
              onClick={() => {
                setIsSelectionMode(false);
                setSelectedPayments(new Set());
              }}
              className="btn-secondary flex items-center"
            >
              <X className="h-4 w-4 mr-2" />
              <span className="hidden sm:inline">Cancel Selection</span>
              <span className="sm:hidden">Cancel</span>
            </button>
          </>
        ) : (
          <>
            <button
              onClick={() => setIsSelectionMode(true)}
              className="btn-secondary flex items-center whitespace-nowrap"
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Select
            </button>
            <button
              onClick={handleExportPayments}
              className="btn-outline flex items-center whitespace-nowrap"
              disabled={exportPaymentsByPeriodMutation.isLoading}
            >
              {exportPaymentsByPeriodMutation.isLoading ? (
                <LoadingSpinner size="sm" text="" />
              ) : (
                <>
                  <Download className="h-4 w-4 mr-2" />
                  Export
                </>
              )}
            </button>
            <button
              onClick={() => {
                setSelectedGenerationPeriod(selectedPeriod);
                setValue('selectedPeriod', selectedPeriod);
                setShowCalculateModal(true);
              }}
              className="btn-primary flex items-center whitespace-nowrap"
            >
              <span className="hidden sm:inline">Calculate Payments</span>
              <span className="sm:hidden">Calculate</span>
            </button>
          </>
        )}
          </div>
        </div>

        {/* Payments Table */}
        <div className="p-6">

        {/* Bulk Actions */}
        {selectedPayments.size > 0 && (
          <div className="px-4 sm:px-6 py-4 bg-blue-50 border-b border-blue-200">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
              <div className="flex items-center gap-3">
                <span className="text-sm font-medium text-blue-800">
                  {selectedPayments.size} payment{selectedPayments.size !== 1 ? 's' : ''} selected
                </span>
              </div>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => handleStatusUpdate('ISSUED')}
                  className="px-4 py-2 text-sm font-medium text-blue-700 bg-blue-100 border border-blue-300 rounded-md hover:bg-blue-200 hover:border-blue-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={updateStatusMutation.isLoading}
                >
                  Mark Issued
                </button>
                <button
                  onClick={() => handleStatusUpdate('COMPLETED')}
                  className="px-4 py-2 text-sm font-medium text-green-700 bg-green-100 border border-green-300 rounded-md hover:bg-green-200 hover:border-green-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={updateStatusMutation.isLoading}
                >
                  Mark Completed
                </button>
                <button
                  onClick={() => handleStatusUpdate('VOIDED')}
                  className="px-4 py-2 text-sm font-medium text-red-700 bg-red-100 border border-red-300 rounded-md hover:bg-red-200 hover:border-red-400 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  disabled={updateStatusMutation.isLoading}
                >
                  Mark Voided
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Payments Table */}
        <div className="overflow-hidden min-h-[600px] transition-all duration-200 ease-in-out">
          {error ? (
            <div className="p-6 text-center">
              {error.response?.data?.message === 'Period number can not be null or zero' ||
               error.message === 'Period number can not be null or zero' ||
               (selectedPeriod && (selectedPeriod.periodNumber <= 0 || selectedPeriod.periodNumber === null)) ? (
                <div className="bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 rounded-xl p-8 max-w-sm mx-auto shadow-sm">
                  <div className="text-center">
                    <div className="w-16 h-16 bg-blue-200 rounded-full flex items-center justify-center mx-auto mb-4">
                      <Calendar className="h-8 w-8 text-blue-600" />
                    </div>
                    <h3 className="text-blue-900 font-semibold mb-2">No Data Available</h3>
                    <p className="text-blue-700 text-sm">
                      No data available for this period
                    </p>
                  </div>
                </div>
              ) : (
                <>
                  <div className="text-red-600 mb-2">⚠️ Error loading payments</div>
                  <p className="text-sm text-gray-600 mb-4">
                    {error.response?.data?.message || error.message || 'Failed to load payments'}
                  </p>
                  <button onClick={() => refetch()} className="btn-primary">
                    Retry
                  </button>
                </>
              )}
            </div>
          ) : isLoading ? (
            <div className="p-6">
              <LoadingSpinner />
            </div>
          ) : !selectedPeriod ? (
            <div className="text-center py-12">
              <Calendar className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                Select a payment period
              </h3>
              <p className="text-gray-600">
                Choose a payment period above to view payments
              </p>
            </div>
          ) : !paymentsData?.content?.length ? (
            <div className="text-center py-12">
              <DollarSign className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                No payments found
              </h3>
              <p className="text-gray-600 mb-4">
                Calculate payments for a pay period to get started
              </p>
              <button
                onClick={() => setShowCalculateModal(true)}
                className="btn-primary"
              >
                Calculate Payments
              </button>
            </div>
          ) : (
            <>
              {/* Mobile Cards View */}
              <div className="block sm:hidden">
                {isSelectionMode && (
                  <div className="px-4 py-3 bg-gray-50 border-b flex items-center justify-between">
                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        checked={selectedPayments.size === paymentsData?.content?.length && paymentsData?.content?.length > 0}
                        onChange={handleSelectAll}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <span className="ml-3 text-sm text-gray-700">
                        {selectedPayments.size > 0
                          ? `${selectedPayments.size} selected`
                          : `Select all`
                        }
                      </span>
                    </div>
                  </div>
                )}

                <div className="p-4 space-y-4">
                  {paymentsData?.content?.map((payment) => (
                    <div key={payment.id} className="border rounded-lg p-4 bg-white shadow-sm">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center">
                          {isSelectionMode && (
                            <input
                              type="checkbox"
                              checked={selectedPayments.has(payment.id)}
                              onChange={() => handleSelectPayment(payment.id)}
                              className="rounded border-gray-300 text-primary-600 focus:ring-primary-500 mr-3"
                            />
                          )}
                          <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                            <span className="text-primary-600 font-medium text-sm">
                              {payment.employeeName?.charAt(0)}
                            </span>
                          </div>
                          <div className="ml-3">
                            <div className="text-sm font-medium text-gray-900">
                              {payment.employeeName}
                            </div>
                            <div className="text-xs text-gray-500">
                              @{payment.employeeUsername}
                            </div>
                          </div>
                        </div>
                        {getStatusBadge(payment.status)}
                      </div>

                      <div className="grid grid-cols-2 gap-3 text-xs text-gray-600 mb-3">
                        <div>
                          <span className="font-medium">Hours:</span><br />
                          {payment.totalHours?.toFixed(1) || '0'} hrs
                          <div className="text-gray-500">{payment.shiftsCount} shifts</div>
                        </div>
                        <div>
                          <span className="font-medium">Earnings:</span><br />
                          <span className="text-green-600 font-semibold">
                            ${payment.totalEarnings?.toFixed(2) || '0.00'}
                          </span>
                        </div>
                        <div>
                          <span className="font-medium">Avg Rate:</span><br />
                          ${payment.averageHourlyRate?.toFixed(2) || '0'}/hr
                        </div>
                      </div>

                      <div className="flex justify-end">
                        <button
                          onClick={() => setSelectedPayment(payment)}
                          className="p-2 text-primary-600 hover:text-primary-900 hover:bg-primary-50 rounded"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Desktop Table View */}
              <div className="hidden sm:block overflow-x-auto">
                {isSelectionMode && (
                  <div className="px-6 py-3 bg-gray-50 border-b flex items-center justify-between">
                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        checked={selectedPayments.size === paymentsData?.content?.length && paymentsData?.content?.length > 0}
                        onChange={handleSelectAll}
                        className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                      />
                      <span className="ml-3 text-sm text-gray-700">
                        {selectedPayments.size > 0
                          ? `${selectedPayments.size} selected`
                          : `Select all ${paymentsData?.content?.length} payments`
                        }
                      </span>
                    </div>
                  </div>
                )}
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Employee
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Total Earnings
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Hours
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Status
                      </th>
                      <th className="relative px-6 py-3">
                        <span className="sr-only">Actions</span>
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {paymentsData?.content?.map((payment) => (
                      <tr key={payment.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center">
                            {isSelectionMode && (
                              <input
                                type="checkbox"
                                checked={selectedPayments.has(payment.id)}
                                onChange={() => handleSelectPayment(payment.id)}
                                className="rounded border-gray-300 text-primary-600 focus:ring-primary-500 mr-3"
                              />
                            )}
                            <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                              <span className="text-primary-600 font-medium text-sm">
                                {payment.employeeName?.charAt(0)}
                              </span>
                            </div>
                            <div className="ml-3">
                              <div className="text-sm font-medium text-gray-900">
                                {payment.employeeName}
                              </div>
                              <div className="text-sm text-gray-500">
                                @{payment.employeeUsername}
                              </div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-semibold text-green-600">
                            ${payment.totalEarnings?.toFixed(2) || '0.00'}
                          </div>
                          <div className="text-sm text-gray-500">
                            ${payment.averageHourlyRate?.toFixed(2) || '0'}/hr avg
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          <div>{payment.totalHours?.toFixed(1) || '0'} hrs</div>
                          <div className="text-sm text-gray-500">
                            {payment.shiftsCount} shifts
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          {getStatusBadge(payment.status)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <button
                            onClick={() => setSelectedPayment(payment)}
                            className="text-primary-600 hover:text-primary-700"
                          >
                            <Eye className="h-4 w-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {/* Pagination */}
          {paymentsData?.totalPages > 1 && (
                <div className="px-6 py-4 flex items-center justify-between border-t border-gray-200">
                  <div className="text-sm text-gray-700">
                    Showing <span className="font-medium">{currentPage * pageSize + 1}</span> to{' '}
                    <span className="font-medium">
                      {Math.min((currentPage + 1) * pageSize, paymentsData.totalElements)}
                    </span>{' '}
                    of <span className="font-medium">{paymentsData.totalElements}</span> payments
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="btn-outline disabled:opacity-50"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() => setCurrentPage(Math.min(paymentsData.totalPages - 1, currentPage + 1))}
                      disabled={currentPage >= paymentsData.totalPages - 1}
                      className="btn-outline disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
          )}
        </div>
        </div>
      </div>

      {/* Modals */}
      {renderCalculateModal()}
      {renderPaymentDetails()}
    </div>
  );
};

export default PaymentsPage;