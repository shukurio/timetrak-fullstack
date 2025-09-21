import { useState, useEffect } from 'react';
import { Users, Search, Filter, Clock, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmployeeCard from '../../components/admin/EmployeeCard';
import EmployeesTable from '../../components/admin/EmployeesTable';

const EmployeesPage = () => {
  const [activeTab, setActiveTab] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [dropdownOpen, setDropdownOpen] = useState(null);
  const pageSize = 50;
  const queryClient = useQueryClient();

  // Define tabs with their configurations
  const tabs = [
    { 
      id: 'ALL', 
      label: 'All Employees', 
      color: 'text-gray-600', 
      activeColor: 'text-primary-600',
      icon: Users,
      badgeColor: 'bg-gray-100 text-gray-800'
    },
    { 
      id: 'PENDING', 
      label: 'Pending Approval', 
      color: 'text-yellow-600', 
      activeColor: 'text-yellow-600',
      icon: Clock,
      badgeColor: 'bg-yellow-100 text-yellow-800'
    },
    { 
      id: 'ACTIVE', 
      label: 'Active', 
      color: 'text-green-600', 
      activeColor: 'text-green-600',
      icon: CheckCircle,
      badgeColor: 'bg-green-100 text-green-800'
    },
    { 
      id: 'DEACTIVATED', 
      label: 'Deactivated', 
      color: 'text-red-600', 
      activeColor: 'text-red-600',
      icon: XCircle,
      badgeColor: 'bg-red-100 text-red-800'
    },
    { 
      id: 'REJECTED', 
      label: 'Rejected', 
      color: 'text-gray-500', 
      activeColor: 'text-gray-600',
      icon: AlertCircle,
      badgeColor: 'bg-gray-100 text-gray-600'
    }
  ];

  // Get employees based on active tab
  const getEmployeesQuery = () => {
    // Create proper params for Spring Boot Pageable - use flat params, not nested
    const params = { 
      page: currentPage, 
      size: pageSize,
      sort: 'firstName,asc' // String format for single sort
    };

    if (searchQuery.trim()) {
      // Search endpoint with query param and pageable
      return adminService.searchEmployees(searchQuery, params);
    }

    if (activeTab === 'ALL') {
      // All employees endpoint  
      return adminService.getEmployees(params);
    } else {
      // Status endpoints
      return adminService.getEmployeesByStatus(activeTab, params);
    }
  };

  const { data: employeesData, isLoading, error, refetch } = useQuery({
    queryKey: ['employees', activeTab, searchQuery, currentPage],
    queryFn: getEmployeesQuery,
    keepPreviousData: true,
    onError: (err) => {
      console.error('Employee API Error:', err);
    },
    retry: 1,
  });

  // Employee action mutations
  const approveEmployeeMutation = useMutation({
    mutationFn: (id) => adminService.approveEmployee(id),
    onSuccess: () => {
      toast.success('Employee approved successfully');
      queryClient.invalidateQueries(['employees']);
      queryClient.invalidateQueries(['employee-counts']);
      setDropdownOpen(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to approve employee');
    },
  });

  const rejectEmployeeMutation = useMutation({
    mutationFn: (id) => adminService.rejectEmployee(id),
    onSuccess: () => {
      toast.success('Employee rejected');
      queryClient.invalidateQueries(['employees']);
      queryClient.invalidateQueries(['employee-counts']);
      setDropdownOpen(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to reject employee');
    },
  });

  const activateEmployeeMutation = useMutation({
    mutationFn: (id) => adminService.activateEmployee(id),
    onSuccess: () => {
      toast.success('Employee activated successfully');
      queryClient.invalidateQueries(['employees']);
      queryClient.invalidateQueries(['employee-counts']);
      setDropdownOpen(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to activate employee');
    },
  });

  const deactivateEmployeeMutation = useMutation({
    mutationFn: (id) => adminService.deactivateEmployee(id),
    onSuccess: () => {
      toast.success('Employee deactivated');
      queryClient.invalidateQueries(['employees']);
      queryClient.invalidateQueries(['employee-counts']);
      setDropdownOpen(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to deactivate employee');
    },
  });

  const deleteEmployeeMutation = useMutation({
    mutationFn: (id) => adminService.deleteEmployee(id),
    onSuccess: () => {
      toast.success('Employee deleted');
      queryClient.invalidateQueries(['employees']);
      queryClient.invalidateQueries(['employee-counts']);
      setDropdownOpen(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to delete employee');
    },
  });

  // Fetch counts for all tabs
  const { data: allCounts, isLoading: countsLoading, error: countsError } = useQuery({
    queryKey: ['employee-counts'],
    queryFn: async () => {
      try {
        console.log('Fetching employee counts...');
        const params = { page: 0, size: 1 }; // Just get count, minimal data
        
        const [all, pending, active, deactivated, rejected] = await Promise.all([
          adminService.getEmployees(params),
          adminService.getEmployeesByStatus('PENDING', params),
          adminService.getEmployeesByStatus('ACTIVE', params),
          adminService.getEmployeesByStatus('DEACTIVATED', params),
          adminService.getEmployeesByStatus('REJECTED', params)
        ]);
        
        console.log('Raw API responses:', { all, pending, active, deactivated, rejected });
        
        const counts = {
          ALL: all?.page?.totalElements ?? all?.totalElements ?? 0,
          PENDING: pending?.page?.totalElements ?? pending?.totalElements ?? 0,
          ACTIVE: active?.page?.totalElements ?? active?.totalElements ?? 0,
          DEACTIVATED: deactivated?.page?.totalElements ?? deactivated?.totalElements ?? 0,
          REJECTED: rejected?.page?.totalElements ?? rejected?.totalElements ?? 0
        };
        console.log('Employee counts processed:', counts);
        return counts;
      } catch (error) {
        console.error('Error fetching employee counts:', error);
        throw error;
      }
    },
    staleTime: 30000, // Cache for 30 seconds
    refetchOnWindowFocus: false, // Don't refetch on window focus
    retry: 1,
  });

  const handleTabChange = (tabId) => {
    setActiveTab(tabId);
    setCurrentPage(0);
    setSearchQuery('');
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    refetch();
  };

  const handleActionClick = (e, action, employee) => {
    e?.stopPropagation(); // Prevent opening the modal
    setDropdownOpen(null);
    
    if (action === 'delete') {
      if (window.confirm(`Are you sure you want to delete ${employee.firstName} ${employee.lastName}? This action cannot be undone.`)) {
        deleteEmployeeMutation.mutate(employee.id);
      }
      return;
    }

    switch (action) {
      case 'approve':
        approveEmployeeMutation.mutate(employee.id);
        break;
      case 'reject':
        if (window.confirm(`Are you sure you want to reject ${employee.firstName} ${employee.lastName}?`)) {
          rejectEmployeeMutation.mutate(employee.id);
        }
        break;
      case 'activate':
        activateEmployeeMutation.mutate(employee.id);
        break;
      case 'deactivate':
        if (window.confirm(`Are you sure you want to deactivate ${employee.firstName} ${employee.lastName}?`)) {
          deactivateEmployeeMutation.mutate(employee.id);
        }
        break;
      default:
        break;
    }
  };

  const toggleDropdown = (e, employeeId) => {
    e.stopPropagation(); // Prevent opening the modal
    setDropdownOpen(dropdownOpen === employeeId ? null : employeeId);
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = () => {
      setDropdownOpen(null);
    };

    if (dropdownOpen) {
      document.addEventListener('click', handleClickOutside);
      return () => document.removeEventListener('click', handleClickOutside);
    }
  }, [dropdownOpen]);


  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Employee Management</h1>
        <div className="flex space-x-3">
          <button className="btn-outline">
            <Filter className="h-4 w-4 mr-2" />
            Filters
          </button>
        </div>
      </div>

      {/* Search Bar */}
      <div className="card">
        <form onSubmit={handleSearch} className="flex items-center space-x-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search employees by name, username, or email..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input-field pl-10"
            />
          </div>
          <button type="submit" className="btn-primary">
            Search
          </button>
          {searchQuery && (
            <button
              type="button"
              onClick={() => {
                setSearchQuery('');
                setCurrentPage(0);
              }}
              className="btn-outline"
            >
              Clear
            </button>
          )}
        </form>
      </div>

      {/* Status Tabs */}
      <div className="bg-white rounded-lg shadow-sm border">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex w-full px-6" aria-label="Tabs">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              
              return (
                <button
                  key={tab.id}
                  onClick={() => handleTabChange(tab.id)}
                  className={`flex-1 flex items-center justify-center py-4 px-2 border-b-2 font-medium text-sm transition-colors ${
                    isActive
                      ? `border-primary-500 ${tab.activeColor}`
                      : `border-transparent ${tab.color} hover:text-gray-700 hover:border-gray-300`
                  }`}
                >
                  <Icon className="h-5 w-5 mr-2" />
                  {tab.label}
                  <span className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium min-w-[24px] justify-center ${tab.badgeColor}`}>
                    {countsLoading ? '•' : countsError ? '!' : (allCounts?.[tab.id] ?? '–')}
                  </span>
                </button>
              );
            })}
          </nav>
        </div>

        {/* Employees Table */}
        <div className="min-h-[600px]">
          {error ? (
            <div className="p-6 text-center">
              <div className="text-red-600 mb-2">⚠️ Error loading employees</div>
              <p className="text-sm text-gray-600 mb-4">
                {error.response?.data?.message || error.message || 'Failed to load employees'}
              </p>
              <button onClick={() => refetch()} className="btn-primary">
                Retry
              </button>
            </div>
          ) : isLoading ? (
            <div className="p-6">
              <LoadingSpinner />
            </div>
          ) : !employeesData?.content?.length ? (
            <div className="text-center py-12">
              <Users className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {searchQuery ? 'No employees found' : `No ${activeTab.toLowerCase()} employees`}
              </h3>
              <p className="text-gray-600">
                {searchQuery 
                  ? `Try adjusting your search terms` 
                  : `There are no employees with ${activeTab.toLowerCase()} status.`
                }
              </p>
            </div>
          ) : (
            <>
              {/* Table View */}
              <EmployeesTable 
                employees={employeesData.content}
                onEmployeeClick={setSelectedEmployee}
                onActionClick={handleActionClick}
                dropdownOpen={dropdownOpen}
                toggleDropdown={toggleDropdown}
                mutations={{
                  approve: approveEmployeeMutation,
                  reject: rejectEmployeeMutation,
                  activate: activateEmployeeMutation,
                  deactivate: deactivateEmployeeMutation,
                  delete: deleteEmployeeMutation
                }}
              />

              {/* Pagination */}
              {employeesData.totalPages > 1 && (
                <div className="px-6 py-4 flex items-center justify-between border-t border-gray-200">
                  <div className="flex-1 flex justify-between sm:hidden">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="btn-outline disabled:opacity-50"
                    >
                      Previous
                    </button>
                    <button
                      onClick={() => setCurrentPage(Math.min(employeesData.totalPages - 1, currentPage + 1))}
                      disabled={currentPage >= employeesData.totalPages - 1}
                      className="btn-outline disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                  <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                    <div>
                      <p className="text-sm text-gray-700">
                        Showing <span className="font-medium">{currentPage * pageSize + 1}</span> to{' '}
                        <span className="font-medium">
                          {Math.min((currentPage + 1) * pageSize, employeesData.totalElements)}
                        </span>{' '}
                        of <span className="font-medium">{employeesData.totalElements}</span> results
                      </p>
                    </div>
                    <div>
                      <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                        {Array.from({ length: employeesData.totalPages }, (_, i) => (
                          <button
                            key={i}
                            onClick={() => setCurrentPage(i)}
                            className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                              i === currentPage
                                ? 'z-10 bg-primary-50 border-primary-500 text-primary-600'
                                : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                            } ${
                              i === 0 ? 'rounded-l-md' : ''
                            } ${
                              i === employeesData.totalPages - 1 ? 'rounded-r-md' : ''
                            }`}
                          >
                            {i + 1}
                          </button>
                        ))}
                      </nav>
                    </div>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Employee Detail Modal */}
      {selectedEmployee && (
        <EmployeeCard
          employee={selectedEmployee}
          isOpen={true}
          onClose={() => setSelectedEmployee(null)}
          onActionClick={handleActionClick}
          mutations={{
            approve: approveEmployeeMutation,
            reject: rejectEmployeeMutation,
            activate: activateEmployeeMutation,
            deactivate: deactivateEmployeeMutation,
            delete: deleteEmployeeMutation
          }}
        />
      )}
    </div>
  );
};

export default EmployeesPage;