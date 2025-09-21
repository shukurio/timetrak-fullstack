import { useState, useEffect } from 'react';
import { Clock, Calendar, Filter, Search, ChevronLeft, ChevronRight, Activity, AlertCircle } from 'lucide-react';
import employeeService from '../../api/employeeService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import PaymentPeriodSelector from '../../components/employee/PaymentPeriodSelector';

const MyShiftsPage = () => {
  const [shifts, setShifts] = useState([]);
  const [filteredShifts, setFilteredShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0,
    totalElements: 0
  });
  const [filters, setFilters] = useState({
    status: 'ALL',
    search: ''
  });
  const [activeShift, setActiveShift] = useState(null);
  const [selectedPeriod, setSelectedPeriod] = useState(null);
  const [availablePeriods, setAvailablePeriods] = useState([]);

  // Load initial data on mount
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const periods = await employeeService.getAvailablePaymentPeriods(12);
        setAvailablePeriods(periods);

        const currentPeriod = await employeeService.getCurrentPaymentPeriod();
        setSelectedPeriod(currentPeriod || (periods.length > 0 ? periods[0] : null));
      } catch (error) {
        console.error('Error loading initial data:', error);
        setError('Failed to load payment periods');
      }
    };

    loadInitialData();
    fetchActiveShift();
  }, []);

  // Fetch shifts when period or filters change
  useEffect(() => {
    if (selectedPeriod) {
      fetchShiftsByPeriod();
    }
  }, [selectedPeriod?.periodNumber, pagination.page, filters.status, filters.search]);

  const fetchActiveShift = async () => {
    const shift = await employeeService.getActiveShiftSilent();
    setActiveShift(shift);
  };

  const fetchShiftsByPeriod = async () => {
    if (!selectedPeriod) return;

    try {
      setLoading(true);
      setError(null);

      const params = {
        page: pagination.page,
        size: pagination.size,
        sortBy: 'clockIn',
        sortDir: 'desc'
      };

      const response = await employeeService.getShiftsByPeriod(selectedPeriod.periodNumber, params);
      let shiftsData = response.content || [];

      // Apply status filter
      if (filters.status !== 'ALL') {
        shiftsData = shiftsData.filter(shift => shift.status === filters.status);
      }

      // Apply search filter
      if (filters.search) {
        shiftsData = shiftsData.filter(shift =>
          shift.jobTitle?.toLowerCase().includes(filters.search.toLowerCase()) ||
          shift.notes?.toLowerCase().includes(filters.search.toLowerCase())
        );
      }

      setShifts(response.content || []);
      setFilteredShifts(shiftsData);
      setPagination(prev => ({
        ...prev,
        totalPages: response.totalPages || 0,
        totalElements: response.totalElements || 0
      }));
    } catch (err) {
      setError('Failed to load shifts for this period');
      console.error('Shifts error:', err);
      setShifts([]);
      setFilteredShifts([]);
    } finally {
      setLoading(false);
    }
  };

  const handlePeriodChange = (period) => {
    setSelectedPeriod(period);
    setPagination(prev => ({ ...prev, page: 0 })); // Reset to first page
  };

  const handleFilterChange = (filterType, value) => {
    setFilters(prev => ({ ...prev, [filterType]: value }));
    setPagination(prev => ({ ...prev, page: 0 }));
  };


  const formatDateTime = (dateTime) => {
    return new Date(dateTime).toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
  };

  const formatDuration = (hours) => {
    if (!hours) return '-';
    const h = Math.floor(hours);
    const m = Math.round((hours - h) * 60);
    return `${h}h ${m}m`;
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount || 0);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const handlePageChange = (newPage) => {
    setPagination(prev => ({ ...prev, page: newPage }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">My Shifts</h1>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <AlertCircle className="h-5 w-5 text-red-400 mr-2" />
            <p className="text-red-800">{error}</p>
          </div>
        </div>
      )}

      {activeShift && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Activity className="h-5 w-5 text-green-600 mr-2" />
              <div>
                <p className="text-green-800 font-medium">Active Shift in Progress</p>
                <p className="text-green-600 text-sm">
                  {activeShift.jobTitle} • Started at {formatDateTime(activeShift.clockIn)}
                </p>
              </div>
            </div>
            <div className="text-right">
              <p className="text-green-800 font-semibold">
                {formatDuration(activeShift.hours)} worked
              </p>
              <p className="text-green-600 text-sm">
                {formatCurrency(activeShift.hourlyWage)}/hr
              </p>
            </div>
          </div>
        </div>
      )}

      <PaymentPeriodSelector
        onPeriodChange={handlePeriodChange}
        selectedPeriod={selectedPeriod}
        periods={availablePeriods}
      />

      <div className="card">

        <div className="flex flex-col sm:flex-row gap-4 mb-6">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search by job title or notes..."
                className="pl-10 pr-4 py-2 w-full border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                value={filters.search}
                onChange={(e) => {
                  handleFilterChange('search', e.target.value);
                }}
              />
            </div>
          </div>

          <div className="flex gap-3">
            <select
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              value={filters.status}
              onChange={(e) => handleFilterChange('status', e.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-12">
            <LoadingSpinner />
            <span className="ml-3 text-gray-600">Loading shifts...</span>
          </div>
        ) : filteredShifts.length > 0 ? (
          <>
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Job</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Date & Time</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Duration</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Rate</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Earnings</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-900">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredShifts.map((shift) => (
                    <tr key={shift.id} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-4 px-4">
                        <div>
                          <p className="font-medium text-gray-900">{shift.jobTitle}</p>
                          {shift.notes && (
                            <p className="text-sm text-gray-500">{shift.notes}</p>
                          )}
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <div className="text-sm">
                          <p className="text-gray-900">{formatDateTime(shift.clockIn)}</p>
                          {shift.clockOut && (
                            <p className="text-gray-500">to {formatDateTime(shift.clockOut)}</p>
                          )}
                        </div>
                      </td>
                      <td className="py-4 px-4">
                        <span className="text-gray-900">{formatDuration(shift.hours)}</span>
                      </td>
                      <td className="py-4 px-4">
                        <span className="text-gray-900">{formatCurrency(shift.hourlyWage)}</span>
                      </td>
                      <td className="py-4 px-4">
                        <span className="font-medium text-gray-900">{formatCurrency(shift.shiftEarnings)}</span>
                      </td>
                      <td className="py-4 px-4">
                        <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(shift.status)}`}>
                          {shift.status.toLowerCase()}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {pagination.totalPages > 1 && (
              <div className="flex items-center justify-between mt-6 pt-4 border-t border-gray-200">
                <div className="text-sm text-gray-700">
                  Showing {pagination.page * pagination.size + 1} to {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of {pagination.totalElements} shifts
                </div>
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handlePageChange(pagination.page - 1)}
                    disabled={pagination.page === 0}
                    className="p-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <span className="text-sm text-gray-700">
                    Page {pagination.page + 1} of {pagination.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(pagination.page + 1)}
                    disabled={pagination.page >= pagination.totalPages - 1}
                    className="p-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <Clock className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Shifts Found</h3>
              <p className="text-gray-600">
                {filters.status !== 'ALL' || filters.dateRange !== 'ALL' || filters.search
                  ? 'No shifts match your current filters.'
                  : 'You haven\'t worked any shifts yet.'}
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyShiftsPage;