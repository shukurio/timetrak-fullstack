import { useState, useEffect } from 'react';
import { Plus, X, CheckCircle, LogIn, LogOut, Download, Clock } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import useAuthStore from '../../store/authStore';
import PaymentPeriodSelector from '../../components/admin/PaymentPeriodSelector';
import ShiftsStatusTabs from '../../components/admin/ShiftsStatusTabs';
import ShiftsFilters from '../../components/admin/ShiftsFilters';
import ShiftsTable from '../../components/admin/ShiftsTable';
import ShiftModal from '../../components/admin/ShiftModal';
import BulkClockModal from '../../components/admin/BulkClockModal';

const ShiftsPage = () => {
  const [selectedTab, setSelectedTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState('');
  const [dateRange, setDateRange] = useState('week');
  const [currentPage, setCurrentPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showBulkClockModal, setShowBulkClockModal] = useState(false);
  const [bulkClockAction, setBulkClockAction] = useState('in');
  const [editingShift, setEditingShift] = useState(null);
  const [selectedShifts, setSelectedShifts] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState(null);
  const [availablePeriods, setAvailablePeriods] = useState([]);
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const pageSize = 20;

  const queryClient = useQueryClient();
  const { isAdmin } = useAuthStore();

  // Load initial data on mount
  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const periods = await adminService.getAvailablePaymentPeriods(12);
        setAvailablePeriods(periods);
        
        const currentPeriod = await adminService.getCurrentPaymentPeriod();
        setSelectedPeriod(currentPeriod);
      } catch (error) {
        console.error('Error loading initial shift data:', error);
        toast.error('Failed to load payment periods');
      }
    };
    
    loadInitialData();
  }, []);

  // Fetch shifts based on filters
  const { data: shiftsData, isLoading: shiftsLoading, error: shiftsError, refetch: refetchShifts } = useQuery({
    queryKey: ['shifts', selectedTab, searchQuery, selectedDepartment, selectedEmployee, dateRange, currentPage, selectedPeriod?.periodNumber],
    queryFn: () => {
      const params = { page: currentPage, size: pageSize, sortBy: 'clockIn', sortDir: 'desc' };
      
      if (selectedTab === 'ACTIVE' && selectedPeriod?.periodNumber) {
        return adminService.getShiftsByPeriodAndStatus(selectedPeriod.periodNumber, 'ACTIVE', params);
      } else if (selectedTab === 'COMPLETED' && selectedPeriod?.periodNumber) {
        return adminService.getShiftsByPeriodAndStatus(selectedPeriod.periodNumber, 'COMPLETED', params);
      } else if (selectedTab === 'ALL' && selectedPeriod?.periodNumber) {
        return adminService.getShiftsByPeriodNumber(selectedPeriod.periodNumber, params);
      } else {
        return dateRange === 'week' 
          ? adminService.getThisWeekShifts(params)
          : dateRange === 'month' 
          ? adminService.getThisMonthShifts(params)
          : adminService.getShifts(params);
      }
    },
    keepPreviousData: true,
    enabled: !!selectedPeriod,
    onError: (err) => console.error('Shifts API Error:', err),
  });

  // Fetch other data
  const { data: departmentsData } = useQuery({
    queryKey: ['departments-for-shifts'],
    queryFn: () => adminService.getDepartments({ page: 0, size: 100, sort: 'name,asc' }),
  });

  const { data: employeesData } = useQuery({
    queryKey: ['employees-for-shifts', selectedDepartment],
    queryFn: () => {
      const params = { page: 0, size: 100, sort: 'firstName,asc' };
      return selectedDepartment 
        ? adminService.getEmployeesByDepartment(selectedDepartment, params)
        : adminService.getActiveEmployees(params);
    },
  });


  // Mutations
  const createShiftMutation = useMutation({
    mutationFn: (shiftData) => adminService.createShift(shiftData),
    onSuccess: () => {
      queryClient.invalidateQueries(['shifts']);
      setShowCreateModal(false);
      toast.success('Shift created successfully!');
    },
    onError: (error) => {
      console.error('Create shift error:', error);
      toast.error(error.response?.data?.message || 'Failed to create shift');
    }
  });

  const updateShiftMutation = useMutation({
    mutationFn: ({ id, data }) => adminService.updateShift(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['shifts']);
      setEditingShift(null);
      setShowCreateModal(false);
      toast.success('Shift updated successfully!');
    },
    onError: (error) => {
      console.error('Update shift error:', error);
      toast.error(error.response?.data?.message || 'Failed to update shift');
    }
  });

  const deleteShiftMutation = useMutation({
    mutationFn: (id) => adminService.deleteShift(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['shifts']);
      toast.success('Shift deleted successfully!');
    },
    onError: (error) => {
      console.error('Delete shift error:', error);
      toast.error(error.response?.data?.message || 'Failed to delete shift');
    }
  });

  const bulkClockMutation = useMutation({
    mutationFn: ({ action, data }) => {
      return action === 'in' 
        ? adminService.clockIn(data)
        : adminService.clockOut(data);
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries(['shifts']);
      setShowBulkClockModal(false);
      setSelectedShifts([]);
      
      if (data.completelySuccessful) {
        toast.success(`Successfully clocked ${bulkClockAction} ${data.successCount} employees`);
      } else {
        toast.success(`Clocked ${bulkClockAction} ${data.successCount}/${data.totalProcessed} employees`);
        if (data.failureCount > 0) {
          toast.error(`${data.failureCount} employees failed to clock ${bulkClockAction}`);
        }
      }
    },
    onError: (error) => {
      console.error('Bulk clock error:', error);
      toast.error(error.response?.data?.message || 'Bulk clock operation failed');
    }
  });

  // Force refetch when selectedPeriod changes
  useEffect(() => {
    if (selectedPeriod?.periodNumber && refetchShifts) {
      refetchShifts();
    }
  }, [selectedPeriod?.periodNumber, refetchShifts]);

  // Event handlers
  const handleTabChange = (tabId) => {
    setSelectedTab(tabId);
    setCurrentPage(0);
  };

  const handleEditShift = (shift) => {
    setEditingShift(shift);
    setShowCreateModal(true);
  };

  const handleDeleteShift = (shift) => {
    if (window.confirm(`Are you sure you want to delete this shift for ${shift.fullName}?`)) {
      deleteShiftMutation.mutate(shift.id);
    }
  };

  const handleShiftSelection = (shiftId, isSelected) => {
    if (isSelected) {
      setSelectedShifts([...selectedShifts, shiftId]);
    } else {
      setSelectedShifts(selectedShifts.filter(id => id !== shiftId));
    }
  };

  const handleSelectAllShifts = (isSelected) => {
    if (isSelected) {
      setSelectedShifts(shiftsData?.content?.map(s => s.employeeJobId) || []);
    } else {
      setSelectedShifts([]);
    }
  };

  const handlePeriodChange = (period) => {
    setSelectedPeriod(period);
    setCurrentPage(0);
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedDepartment('');
    setSelectedEmployee('');
    setCurrentPage(0);
  };

  const handleCreateShift = (data) => {
    createShiftMutation.mutate(data);
  };

  const handleUpdateShift = (data) => {
    if (editingShift) {
      updateShiftMutation.mutate({ id: editingShift.id, data });
    }
  };

  const handleBulkClock = (data) => {
    bulkClockMutation.mutate(data);
  };

  const handleDownloadReport = async () => {
    if (!selectedPeriod) {
      toast.error('Please select a payment period first');
      return;
    }

    try {
      toast.loading('Generating report...');
      const blob = await adminService.exportCompanyShifts(selectedPeriod.periodNumber);

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `shifts_report_period_${selectedPeriod.periodNumber}.pdf`;
      document.body.appendChild(link);
      link.click();

      // Cleanup
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      toast.dismiss();
      toast.success('Report downloaded successfully');
    } catch (error) {
      toast.dismiss();
      toast.error('Failed to download report');
      console.error('Download error:', error);
    }
  };

  return (
    <div className="space-y-6">
      {/* Main container with header and content */}
      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <div className="flex items-center justify-center py-4 px-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <Clock className="h-5 w-5 text-blue-600" />
            <h1 className="text-lg font-semibold text-gray-900">Shifts Management</h1>
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

        {/* Status Tabs */}
        <ShiftsStatusTabs
          selectedTab={selectedTab}
          onTabChange={handleTabChange}
          hasActiveShifts={shiftsData?.content?.some(shift => shift.status === 'ACTIVE') || false}
        />

        {/* Filters */}
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <ShiftsFilters
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
        selectedDepartment={selectedDepartment}
        setSelectedDepartment={setSelectedDepartment}
        selectedEmployee={selectedEmployee}
        setSelectedEmployee={setSelectedEmployee}
        dateRange={dateRange}
        setDateRange={setDateRange}
        departmentsData={departmentsData}
        employeesData={employeesData}
        onClearFilters={clearFilters}
          />
        </div>

        {/* Action Buttons */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex flex-col sm:flex-row gap-2 sm:gap-3 items-stretch sm:items-center sm:justify-end">
            {isSelectionMode ? (
              <>
                <button
                  onClick={() => {
                    setIsSelectionMode(false);
                    setSelectedShifts([]);
                  }}
                  className="btn-secondary flex items-center justify-center w-full sm:w-auto"
                >
                  <X className="h-4 w-4 mr-2" />
                  <span className="hidden sm:inline">Cancel Selection</span>
                  <span className="sm:hidden">Cancel</span>
                </button>
                {selectedShifts.length > 0 && (
                  <>
                    <button
                      onClick={() => {
                        setBulkClockAction('in');
                        setShowBulkClockModal(true);
                      }}
                      className="btn-secondary flex items-center justify-center w-full sm:w-auto"
                    >
                      <LogIn className="h-4 w-4 mr-2" />
                      <span className="hidden sm:inline">Clock In ({selectedShifts.length})</span>
                      <span className="sm:hidden">In ({selectedShifts.length})</span>
                    </button>
                    <button
                      onClick={() => {
                        setBulkClockAction('out');
                        setShowBulkClockModal(true);
                      }}
                      className="btn-secondary flex items-center justify-center w-full sm:w-auto"
                    >
                      <LogOut className="h-4 w-4 mr-2" />
                      <span className="hidden sm:inline">Clock Out ({selectedShifts.length})</span>
                      <span className="sm:hidden">Out ({selectedShifts.length})</span>
                    </button>
                  </>
                )}
              </>
            ) : (
              <>
                <button
                  onClick={() => handleDownloadReport()}
                  className="btn-secondary flex items-center justify-center w-full sm:w-auto"
                  disabled={!selectedPeriod}
                >
                  <Download className="h-4 w-4 mr-2" />
                  <span className="hidden sm:inline">Download Report</span>
                  <span className="sm:hidden">Download</span>
                </button>
                <button
                  onClick={() => setIsSelectionMode(true)}
                  className="btn-secondary flex items-center justify-center w-full sm:w-auto"
                >
                  <CheckCircle className="h-4 w-4 mr-2" />
                  Select
                </button>
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="btn-primary flex items-center justify-center w-full sm:w-auto"
                >
                  <span className="hidden sm:inline">Create Shift</span>
                  <span className="sm:hidden">Create</span>
                </button>
              </>
            )}
          </div>
        </div>

        {/* Shifts Table */}
        <div className="p-6">
          <ShiftsTable
            shiftsData={shiftsData}
            shiftsLoading={shiftsLoading}
            shiftsError={shiftsError}
            isSelectionMode={isSelectionMode}
            selectedShifts={selectedShifts}
            onShiftSelection={handleShiftSelection}
            onSelectAllShifts={handleSelectAllShifts}
            onEditShift={handleEditShift}
            onDeleteShift={handleDeleteShift}
            currentPage={currentPage}
            setCurrentPage={setCurrentPage}
            pageSize={pageSize}
          />
        </div>
      </div>

      {/* Modals */}
      <ShiftModal
        isOpen={showCreateModal}
        onClose={() => {
          setShowCreateModal(false);
          setEditingShift(null);
        }}
        editingShift={editingShift}
        onSubmit={editingShift ? handleUpdateShift : handleCreateShift}
        isLoading={createShiftMutation.isLoading || updateShiftMutation.isLoading}
      />

      <BulkClockModal
        isOpen={showBulkClockModal}
        onClose={() => setShowBulkClockModal(false)}
        bulkClockAction={bulkClockAction}
        selectedShifts={selectedShifts}
        onSubmit={handleBulkClock}
        isLoading={bulkClockMutation.isLoading}
      />

    </div>
  );
};

export default ShiftsPage;