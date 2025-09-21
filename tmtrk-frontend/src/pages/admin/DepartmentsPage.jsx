import { useState, useEffect } from 'react';
import { 
  Building2, Users, Briefcase, BarChart3, Plus, Edit2, Trash2, 
  MoreVertical, Search, DollarSign, User,
  XCircle, AlertCircle, UserPlus, UserMinus, Loader2, ChevronDown
} from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const DepartmentsPage = () => {
  const [selectedDepartment, setSelectedDepartment] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [activeTab, setActiveTab] = useState('info');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [dropdownOpen, setDropdownOpen] = useState(null);
  const [showJobForm, setShowJobForm] = useState(false);
  const [editingJob, setEditingJob] = useState(null);
  const [showJobAssignModal, setShowJobAssignModal] = useState(false);
  const [assigningJob, setAssigningJob] = useState(null);
  
  const queryClient = useQueryClient();

  // Fetch departments
  const { data: departments, isLoading: departmentsLoading, error: departmentsError } = useQuery({
    queryKey: ['departments'],
    queryFn: () => adminService.getDepartments({ size: 100 }),
  });

  // Fetch selected department details
  const { data: departmentDetail, isLoading: detailLoading } = useQuery({
    queryKey: ['department', selectedDepartment?.id],
    queryFn: () => adminService.getDepartmentById(selectedDepartment.id),
    enabled: !!selectedDepartment?.id,
  });

  // Fetch employees for selected department
  const { data: departmentEmployees, isLoading: employeesLoading } = useQuery({
    queryKey: ['department-employees', selectedDepartment?.id],
    queryFn: () => adminService.getEmployeesByDepartment(selectedDepartment.id),
    enabled: !!selectedDepartment?.id && (activeTab === 'employees' || showDetailModal),
  });

  // Fetch jobs for selected department
  const { data: departmentJobs, isLoading: jobsLoading } = useQuery({
    queryKey: ['department-jobs', selectedDepartment?.id],
    queryFn: () => adminService.getJobsByDepartment(selectedDepartment.id),
    enabled: !!selectedDepartment?.id && (activeTab === 'jobs' || showDetailModal),
  });

  // Create department mutation
  const createDepartmentMutation = useMutation({
    mutationFn: (departmentData) => adminService.createDepartment(departmentData),
    onSuccess: () => {
      toast.success('Department created successfully');
      queryClient.invalidateQueries(['departments']);
      setShowCreateForm(false);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create department');
    },
  });

  // Update department mutation
  const updateDepartmentMutation = useMutation({
    mutationFn: ({ id, data }) => adminService.updateDepartment(id, data),
    onSuccess: () => {
      toast.success('Department updated successfully');
      queryClient.invalidateQueries(['departments']);
      setShowEditForm(false);
      setEditingDepartment(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to update department');
    },
  });

  // Delete department mutation
  const deleteDepartmentMutation = useMutation({
    mutationFn: (id) => adminService.deleteDepartment(id),
    onSuccess: () => {
      toast.success('Department deleted successfully');
      queryClient.invalidateQueries(['departments']);
      setDropdownOpen(null);
      if (selectedDepartment?.id === id) {
        setSelectedDepartment(null);
        setShowDetailModal(false);
      }
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to delete department');
    },
  });

  // Job mutations
  const createJobMutation = useMutation({
    mutationFn: (jobData) => adminService.createJob(jobData),
    onSuccess: () => {
      toast.success('Job created successfully');
      queryClient.invalidateQueries(['department-jobs']);
      setShowJobForm(false);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create job');
    },
  });

  const updateJobMutation = useMutation({
    mutationFn: ({ id, data }) => adminService.updateJob(id, data),
    onSuccess: () => {
      toast.success('Job updated successfully');
      queryClient.invalidateQueries(['department-jobs']);
      setEditingJob(null);
      setShowJobForm(false);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to update job');
    },
  });

  const deleteJobMutation = useMutation({
    mutationFn: (id) => adminService.deleteJob(id),
    onSuccess: () => {
      toast.success('Job deleted successfully');
      queryClient.invalidateQueries(['department-jobs']);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to delete job');
    },
  });

  // Job assignment mutations
  const assignJobMutation = useMutation({
    mutationFn: (assignmentData) => {
      if (assignmentData.employeeId) {
        return adminService.assignIndividualJob(
          assignmentData.employeeId, 
          assignmentData.jobId, 
          assignmentData.hourlyWage
        );
      }
      return adminService.assignJobToEmployees(assignmentData);
    },
    onSuccess: (_, variables) => {
      const isMultiple = variables.employeeIds?.length > 1;
      toast.success(isMultiple 
        ? `Job assigned to ${variables.employeeIds.length} employees successfully` 
        : 'Job assigned successfully'
      );
      queryClient.invalidateQueries(['department-jobs']);
      queryClient.invalidateQueries(['department-employees']);
      setShowJobAssignModal(false);
      setAssigningJob(null);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to assign job');
    },
  });

  const unassignJobMutation = useMutation({
    mutationFn: ({ employeeIds, jobId }) => 
      adminService.removeJobFromEmployees(employeeIds, jobId),
    onSuccess: () => {
      toast.success('Job unassigned successfully');
      queryClient.invalidateQueries(['department-jobs']);
      queryClient.invalidateQueries(['department-employees']);
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to unassign job');
    },
  });

  const handleDeleteDepartment = (department) => {
    if (window.confirm(`Are you sure you want to delete ${department.name}? This action cannot be undone.`)) {
      deleteDepartmentMutation.mutate(department.id);
    }
  };

  const handleEditDepartment = (department) => {
    setEditingDepartment(department);
    setShowEditForm(true);
    setDropdownOpen(null);
    setShowDetailModal(false); // Close detail modal when opening edit modal
  };

  const handleDepartmentClick = (department) => {
    setSelectedDepartment(department);
    setActiveTab('info');
    setShowDetailModal(true);
  };

  const toggleDropdown = (e, deptId) => {
    e.stopPropagation();
    setDropdownOpen(dropdownOpen === deptId ? null : deptId);
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

  // Extract departments from paginated response and filter based on search
  const departmentsList = departments?.content || departments || [];
  
  const filteredDepartments = departmentsList?.filter(dept =>
    dept.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    dept.description?.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

  if (departmentsLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">Departments</h1>
        </div>
        <LoadingSpinner size="lg" text="Loading departments..." />
      </div>
    );
  }

  if (departmentsError) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold text-gray-900">Departments</h1>
        </div>
        <div className="card p-6 text-center">
          <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">Error Loading Departments</h3>
          <p className="text-gray-600 mb-4">{departmentsError.message}</p>
          <button 
            onClick={() => queryClient.invalidateQueries(['departments'])}
            className="btn-primary"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Departments</h1>
        <div className="flex space-x-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search departments..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button 
            onClick={() => setShowCreateForm(true)}
            className="btn-primary"
          >
            <Plus className="h-4 w-4 mr-2" />
            Add Department
          </button>
        </div>
      </div>

      {/* Departments Table */}
      <div className="card p-0">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Department
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Description
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Employees
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Jobs
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredDepartments.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center">
                    <Building2 className="h-12 w-12 text-gray-400 mx-auto mb-3" />
                    <p className="text-gray-500">No departments found</p>
                  </td>
                </tr>
              ) : (
                filteredDepartments.map((department) => (
                  <tr 
                    key={department.id} 
                    className="hover:bg-gray-50 cursor-pointer"
                    onClick={() => handleDepartmentClick(department)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <Building2 className="h-5 w-5 text-blue-600" />
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {department.name}
                          </div>
                          <div className="text-sm text-gray-500">
                            ID: {department.id}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm text-gray-900 max-w-xs truncate">
                        {department.description || 'No description'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center text-sm text-gray-900">
                        <Users className="h-4 w-4 mr-1 text-gray-400" />
                        {department.employeeCount || 0}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center text-sm text-gray-900">
                        <Briefcase className="h-4 w-4 mr-1 text-gray-400" />
                        {department.jobCount || 0}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        department.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                      }`}>
                        {department.status || 'ACTIVE'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {department.createdAt ? new Date(department.createdAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="relative">
                        <button
                          onClick={(e) => toggleDropdown(e, department.id)}
                          className="text-gray-400 hover:text-gray-600 p-1 rounded"
                        >
                          <MoreVertical className="h-5 w-5" />
                        </button>
                        
                        {dropdownOpen === department.id && (
                          <div className="absolute right-0 top-full mt-1 w-36 bg-white rounded-md shadow-lg border border-gray-200 z-50 min-w-max">
                            <div className="py-1">
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleEditDepartment(department);
                                }}
                                className="flex items-center w-full px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                              >
                                <Edit2 className="h-3 w-3 mr-2" />
                                Edit
                              </button>
                              <button
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDeleteDepartment(department);
                                }}
                                className="flex items-center w-full px-3 py-2 text-sm text-red-700 hover:bg-red-50"
                              >
                                <Trash2 className="h-3 w-3 mr-2" />
                                Delete
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create Department Modal */}
      {showCreateForm && (
        <CreateDepartmentModal
          isOpen={showCreateForm}
          onClose={() => setShowCreateForm(false)}
          onSubmit={createDepartmentMutation.mutate}
          isLoading={createDepartmentMutation.isLoading}
        />
      )}

      {/* Edit Department Modal */}
      {showEditForm && editingDepartment && (
        <EditDepartmentModal
          isOpen={showEditForm}
          department={editingDepartment}
          onClose={() => {
            setShowEditForm(false);
            setEditingDepartment(null);
          }}
          onSubmit={(data) => updateDepartmentMutation.mutate({ id: editingDepartment.id, data })}
          isLoading={updateDepartmentMutation.isLoading}
        />
      )}

      {/* Department Detail Modal */}
      {showDetailModal && selectedDepartment && (
        <DepartmentDetailModal
          department={selectedDepartment}
          departmentDetail={departmentDetail}
          departmentEmployees={departmentEmployees}
          departmentJobs={departmentJobs}
          activeTab={activeTab}
          setActiveTab={setActiveTab}
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedDepartment(null);
          }}
          onEditDepartment={handleEditDepartment}
          detailLoading={detailLoading}
          employeesLoading={employeesLoading}
          jobsLoading={jobsLoading}
          showJobForm={showJobForm}
          setShowJobForm={setShowJobForm}
          editingJob={editingJob}
          setEditingJob={setEditingJob}
          showJobAssignModal={showJobAssignModal}
          setShowJobAssignModal={setShowJobAssignModal}
          assigningJob={assigningJob}
          setAssigningJob={setAssigningJob}
          createJobMutation={createJobMutation}
          updateJobMutation={updateJobMutation}
          deleteJobMutation={deleteJobMutation}
          assignJobMutation={assignJobMutation}
          unassignJobMutation={unassignJobMutation}
        />
      )}
    </div>
  );
};

// Department Detail Modal Component
const DepartmentDetailModal = ({
  department,
  departmentDetail,
  departmentEmployees,
  departmentJobs,
  activeTab,
  setActiveTab,
  isOpen,
  onClose,
  onEditDepartment,
  detailLoading,
  employeesLoading,
  jobsLoading,
  showJobForm,
  setShowJobForm,
  editingJob,
  setEditingJob,
  showJobAssignModal,
  setShowJobAssignModal,
  assigningJob,
  setAssigningJob,
  createJobMutation,
  updateJobMutation,
  deleteJobMutation,
  assignJobMutation,
  unassignJobMutation,
}) => {
  const tabs = [
    { id: 'info', label: 'Department Info', icon: Building2 },
    { id: 'employees', label: 'Employees', icon: Users },
    { id: 'jobs', label: 'Jobs', icon: Briefcase },
    { id: 'reports', label: 'Reports', icon: BarChart3 },
  ];

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[90] p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-6xl w-full max-h-[90vh] overflow-hidden">
        {/* Modal Header */}
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-2xl font-bold text-gray-900">
            {department.name}
          </h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XCircle size={24} />
          </button>
        </div>

        {/* Tab Navigation */}
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8 px-6" aria-label="Tabs">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                    isActive
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <Icon className="h-4 w-4 mr-2" />
                  {tab.label}
                </button>
              );
            })}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="p-6 overflow-y-auto max-h-[calc(90vh-200px)]">
          {activeTab === 'info' && (
            <DepartmentInfoTab 
              department={department} 
              detail={departmentDetail}
              isLoading={detailLoading}
              onEditDepartment={onEditDepartment}
            />
          )}
          {activeTab === 'employees' && (
            <DepartmentEmployeesTab 
              department={department}
              employees={departmentEmployees}
              isLoading={employeesLoading}
            />
          )}
          {activeTab === 'jobs' && (
            <DepartmentJobsTab 
              department={department}
              jobs={departmentJobs}
              employees={departmentEmployees}
              isLoading={jobsLoading}
              showJobForm={showJobForm}
              setShowJobForm={setShowJobForm}
              editingJob={editingJob}
              setEditingJob={setEditingJob}
              showJobAssignModal={showJobAssignModal}
              setShowJobAssignModal={setShowJobAssignModal}
              assigningJob={assigningJob}
              setAssigningJob={setAssigningJob}
              createJobMutation={createJobMutation}
              updateJobMutation={updateJobMutation}
              deleteJobMutation={deleteJobMutation}
              assignJobMutation={assignJobMutation}
              unassignJobMutation={unassignJobMutation}
            />
          )}
          {activeTab === 'reports' && (
            <DepartmentReportsTab 
              department={department}
            />
          )}
        </div>
      </div>
    </div>
  );
};

// Department Info Tab Component
const DepartmentInfoTab = ({ department, detail, isLoading, onEditDepartment }) => {
  if (isLoading) {
    return <LoadingSpinner text="Loading department details..." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">{department.name}</h2>
          {department.description && (
            <p className="text-gray-600 mt-1">{department.description}</p>
          )}
        </div>
        <button 
          onClick={() => onEditDepartment && onEditDepartment(department)}
          className="btn-outline"
        >
          <Edit2 className="h-4 w-4 mr-2" />
          Edit Department
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-blue-50 rounded-lg p-4">
          <div className="flex items-center">
            <Users className="h-8 w-8 text-blue-600" />
            <div className="ml-3">
              <p className="text-sm font-medium text-blue-600">Employees</p>
              <p className="text-2xl font-bold text-blue-900">{department.employeeCount || 0}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-green-50 rounded-lg p-4">
          <div className="flex items-center">
            <Briefcase className="h-8 w-8 text-green-600" />
            <div className="ml-3">
              <p className="text-sm font-medium text-green-600">Jobs</p>
              <p className="text-2xl font-bold text-green-900">{department.jobCount || 0}</p>
            </div>
          </div>
        </div>
        
        <div className="bg-purple-50 rounded-lg p-4">
          <div className="flex items-center">
            <BarChart3 className="h-8 w-8 text-purple-600" />
            <div className="ml-3">
              <p className="text-sm font-medium text-purple-600">Active</p>
              <p className="text-2xl font-bold text-purple-900">
                {department.status === 'ACTIVE' ? 'Yes' : 'No'}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Department Details */}
      <div className="bg-gray-50 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Department Details</h3>
        <div className="grid grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Department ID</label>
            <p className="text-sm text-gray-900">{department.id}</p>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
              department.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}>
              {department.status || 'ACTIVE'}
            </span>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Created</label>
            <p className="text-sm text-gray-900">
              {department.createdAt ? new Date(department.createdAt).toLocaleDateString() : 'N/A'}
            </p>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Last Updated</label>
            <p className="text-sm text-gray-900">
              {department.updatedAt ? new Date(department.updatedAt).toLocaleDateString() : 'N/A'}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

// Department Employees Tab Component
const DepartmentEmployeesTab = ({ department, employees, isLoading }) => {
  if (isLoading) {
    return <LoadingSpinner text="Loading department employees..." />;
  }

  // Extract employees from paginated response if needed
  const employeesList = employees?.content || employees || [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">Employees in {department.name}</h2>
        <button className="btn-primary">
          <UserPlus className="h-4 w-4 mr-2" />
          Assign Employee
        </button>
      </div>

      {!employeesList?.length ? (
        <div className="text-center py-12">
          <Users className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Employees</h3>
          <p className="text-gray-600 mb-4">This department doesn't have any employees assigned yet.</p>
          <button className="btn-primary">
            <UserPlus className="h-4 w-4 mr-2" />
            Assign First Employee
          </button>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          <ul className="divide-y divide-gray-200">
            {employeesList.map((employee) => (
              <li key={employee.id}>
                <div className="px-4 py-4 flex items-center justify-between hover:bg-gray-50">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                        <span className="text-blue-600 font-medium text-sm">
                          {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
                        </span>
                      </div>
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">
                        {employee.firstName} {employee.lastName}
                      </div>
                      <div className="text-sm text-gray-500">@{employee.username}</div>
                    </div>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      employee.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 
                      employee.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {employee.status}
                    </span>
                    <button className="text-red-600 hover:text-red-800">
                      <UserMinus className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

// Department Jobs Tab Component  
const DepartmentJobsTab = ({ 
  department, 
  jobs, 
  employees,
  isLoading, 
  showJobForm, 
  setShowJobForm, 
  editingJob, 
  setEditingJob,
  showJobAssignModal,
  setShowJobAssignModal,
  assigningJob,
  setAssigningJob,
  createJobMutation,
  updateJobMutation,
  deleteJobMutation,
  assignJobMutation,
  unassignJobMutation
}) => {
  if (isLoading) {
    return <LoadingSpinner text="Loading department jobs..." />;
  }

  // Extract jobs and employees from paginated response if needed
  const jobsList = jobs?.content || jobs || [];
  const employeesList = employees?.content || employees || [];

  const handleCreateJob = () => {
    setEditingJob(null);
    setShowJobForm(true);
  };

  const handleEditJob = (job) => {
    setEditingJob(job);
    setShowJobForm(true);
  };

  const handleAssignJob = (job) => {
    setAssigningJob(job);
    setShowJobAssignModal(true);
  };

  const handleDeleteJob = (job) => {
    if (window.confirm(`Are you sure you want to delete "${job.jobTitle || job.title}"? This will unassign it from all employees.`)) {
      deleteJobMutation.mutate(job.id);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">Jobs in {department.name}</h2>
        <button 
          onClick={handleCreateJob}
          className="btn-primary"
        >
          <Plus className="h-4 w-4 mr-2" />
          Create Job
        </button>
      </div>

      {!jobsList?.length ? (
        <div className="text-center py-12">
          <Briefcase className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Jobs</h3>
          <p className="text-gray-600 mb-4">This department doesn't have any jobs created yet.</p>
          <button 
            onClick={handleCreateJob}
            className="btn-primary"
          >
            <Plus className="h-4 w-4 mr-2" />
            Create First Job
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {jobsList.map((job) => (
            <JobCard 
              key={job.id}
              job={job}
              employees={employeesList}
              onEdit={() => handleEditJob(job)}
              onAssign={() => handleAssignJob(job)}
              onDelete={() => handleDeleteJob(job)}
              onUnassign={(employeeIds) => unassignJobMutation.mutate({ employeeIds, jobId: job.id })}
              isDeleting={deleteJobMutation.isLoading}
              isUnassigning={unassignJobMutation.isLoading}
            />
          ))}
        </div>
      )}

      {/* Job Form Modal */}
      {showJobForm && (
        <JobFormModal
          department={department}
          job={editingJob}
          isOpen={showJobForm}
          onClose={() => {
            setShowJobForm(false);
            setEditingJob(null);
          }}
          onSubmit={(jobData) => {
            if (editingJob) {
              updateJobMutation.mutate({ id: editingJob.id, data: jobData });
            } else {
              createJobMutation.mutate({ ...jobData, departmentId: department.id });
            }
          }}
          isLoading={createJobMutation.isLoading || updateJobMutation.isLoading}
        />
      )}

      {/* Job Assignment Modal */}
      {showJobAssignModal && (
        <JobAssignModal
          job={assigningJob}
          employees={employeesList}
          isOpen={showJobAssignModal}
          onClose={() => {
            setShowJobAssignModal(false);
            setAssigningJob(null);
          }}
          onAssign={(assignmentData) => assignJobMutation.mutate(assignmentData)}
          isLoading={assignJobMutation.isLoading}
        />
      )}
    </div>
  );
};

// Department Reports Tab Component
const DepartmentReportsTab = ({ department }) => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold text-gray-900">Reports for {department.name}</h2>
      </div>

      <div className="text-center py-12">
        <BarChart3 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">Reports Coming Soon</h3>
        <p className="text-gray-600">Department-specific reports and analytics will be available here.</p>
      </div>
    </div>
  );
};

// Job Form Modal Component
const JobFormModal = ({ department, job, isOpen, onClose, onSubmit, isLoading }) => {
  const [formData, setFormData] = useState({
    jobTitle: '',
    hourlyWage: '',
    departmentId: department?.id || ''
  });

  // Populate form when editing
  useEffect(() => {
    if (job) {
      setFormData({
        jobTitle: job.jobTitle || job.title || '',
        hourlyWage: job.hourlyWage || job.payRate || job.hourlyRate || job.salary || '',
        departmentId: department?.id || job.departmentId || ''
      });
    } else {
      setFormData({
        jobTitle: '',
        hourlyWage: '',
        departmentId: department?.id || ''
      });
    }
  }, [job, department]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100] p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b">
          <h3 className="text-lg font-semibold text-gray-900">
            {job ? 'Edit Job' : 'Create New Job'} - {department.name}
          </h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XCircle size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Job Title *
              </label>
              <input
                type="text"
                name="jobTitle"
                value={formData.jobTitle}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Enter job title..."
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Hourly Wage ($) *
              </label>
              <input
                type="number"
                name="hourlyWage"
                value={formData.hourlyWage}
                onChange={handleChange}
                required
                step="0.01"
                min="0.01"
                max="999.99"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="15.00"
              />
              <p className="text-xs text-gray-500 mt-1">
                Must be positive (0.01 - 999.99)
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Department
              </label>
              <input
                type="text"
                value={department?.name || 'Unknown Department'}
                disabled
                className="w-full px-3 py-2 bg-gray-100 border border-gray-300 rounded-lg text-gray-600"
              />
              <p className="text-xs text-gray-500 mt-1">
                Job will be created for this department
              </p>
            </div>
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading || !formData.jobTitle.trim() || !formData.hourlyWage || parseFloat(formData.hourlyWage) <= 0}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center"
            >
              {isLoading ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" />
                  {job ? 'Updating...' : 'Creating...'}
                </>
              ) : (
                <>
                  {job ? 'Update Job' : 'Create Job'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Job Card Component
const JobCard = ({ job, employees, onEdit, onAssign, onDelete, onUnassign, isDeleting, isUnassigning }) => {
  const [showAssignedEmployees, setShowAssignedEmployees] = useState(false);
  
  // Get employees assigned to this job (this would normally come from the API)
  const assignedEmployees = employees?.filter(emp => emp.jobs?.some(j => j.id === job.id)) || [];
  
  const handleUnassignEmployee = (employeeId) => {
    if (window.confirm('Are you sure you want to unassign this job from the employee?')) {
      onUnassign([employeeId]);
    }
  };

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-lg font-medium text-gray-900">{job.jobTitle || job.title}</h3>
            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
              job.isActive !== false ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
            }`}>
              {job.isActive !== false ? 'Active' : 'Inactive'}
            </span>
          </div>
          
          <div className="flex items-center text-sm text-gray-600 mb-3">
            <DollarSign className="h-4 w-4 mr-1" />
            ${job.hourlyWage || job.payRate || job.hourlyRate || job.salary || 'TBD'}/hr
          </div>

          {/* Assigned Employees Section */}
          <div className="mb-3">
            <button
              onClick={() => setShowAssignedEmployees(!showAssignedEmployees)}
              className="flex items-center text-sm text-gray-700 hover:text-gray-900"
            >
              <Users className="h-4 w-4 mr-1" />
              {assignedEmployees.length} assigned employee{assignedEmployees.length !== 1 ? 's' : ''}
              <ChevronDown className={`h-3 w-3 ml-1 transition-transform ${showAssignedEmployees ? 'rotate-180' : ''}`} />
            </button>
            
            {showAssignedEmployees && assignedEmployees.length > 0 && (
              <div className="mt-2 space-y-1">
                {assignedEmployees.map((employee) => (
                  <div key={employee.id} className="flex items-center justify-between bg-gray-50 rounded px-3 py-2">
                    <div className="flex items-center">
                      <div className="flex-shrink-0 h-6 w-6 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-blue-600 font-medium text-xs">
                          {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
                        </span>
                      </div>
                      <span className="ml-2 text-sm text-gray-900">
                        {employee.firstName} {employee.lastName}
                      </span>
                    </div>
                    <button
                      onClick={() => handleUnassignEmployee(employee.id)}
                      disabled={isUnassigning}
                      className="text-red-600 hover:text-red-800 text-xs"
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        
        <div className="flex items-center space-x-2 ml-4">
          <button 
            onClick={onEdit}
            className="btn-outline btn-sm"
          >
            <Edit2 className="h-3 w-3 mr-1" />
            Edit
          </button>
          <button 
            onClick={onAssign}
            className="btn-outline btn-sm text-blue-600 hover:bg-blue-50"
          >
            <UserPlus className="h-3 w-3 mr-1" />
            Assign
          </button>
        </div>
      </div>
    </div>
  );
};

// Job Assignment Modal Component
const JobAssignModal = ({ job, employees, isOpen, onClose, onAssign, isLoading }) => {
  const [assignmentMode, setAssignmentMode] = useState('single'); // 'single' or 'bulk'
  const [selectedEmployee, setSelectedEmployee] = useState('');
  const [selectedEmployees, setSelectedEmployees] = useState([]);
  const [customWage, setCustomWage] = useState('');
  const [useCustomWage, setUseCustomWage] = useState(false);

  // Get available employees (not already assigned to this job)
  const availableEmployees = employees?.filter(emp => 
    emp.status === 'ACTIVE' && !emp.jobs?.some(j => j.id === job?.id)
  ) || [];

  const handleEmployeeToggle = (employeeId) => {
    setSelectedEmployees(prev => 
      prev.includes(employeeId)
        ? prev.filter(id => id !== employeeId)
        : [...prev, employeeId]
    );
  };

  const handleSelectAll = () => {
    if (selectedEmployees.length === availableEmployees.length) {
      setSelectedEmployees([]);
    } else {
      setSelectedEmployees(availableEmployees.map(emp => emp.id));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (assignmentMode === 'single') {
      if (!selectedEmployee) return;
      
      const assignmentData = {
        employeeId: parseInt(selectedEmployee),
        jobId: job.id,
        hourlyWage: useCustomWage && customWage ? parseFloat(customWage) : null
      };
      
      onAssign(assignmentData);
    } else {
      if (selectedEmployees.length === 0) return;
      
      const assignmentData = {
        jobId: job.id,
        employeeIds: selectedEmployees,
        hourlyWage: useCustomWage && customWage ? parseFloat(customWage) : job.hourlyWage
      };
      
      onAssign(assignmentData);
    }
  };

  useEffect(() => {
    if (isOpen) {
      setAssignmentMode('single');
      setSelectedEmployee('');
      setSelectedEmployees([]);
      setCustomWage('');
      setUseCustomWage(false);
    }
  }, [isOpen, job]);

  if (!isOpen || !job) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100] p-4">
      <div className="bg-white rounded-xl shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b">
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Assign Job: {job.jobTitle}
            </h3>
            <p className="text-sm text-gray-500 mt-1">
              ${job.hourlyWage}/hr • {availableEmployees.length} available employees
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <XCircle size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Assignment Mode Toggle */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-3">
              Assignment Mode
            </label>
            <div className="flex rounded-lg bg-gray-100 p-1">
              <button
                type="button"
                onClick={() => setAssignmentMode('single')}
                className={`flex-1 py-2 px-4 text-sm font-medium rounded-md transition-colors ${
                  assignmentMode === 'single'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <User className="h-4 w-4 inline-block mr-2" />
                Single Employee
              </button>
              <button
                type="button"
                onClick={() => setAssignmentMode('bulk')}
                className={`flex-1 py-2 px-4 text-sm font-medium rounded-md transition-colors ${
                  assignmentMode === 'bulk'
                    ? 'bg-white text-blue-600 shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                <Users className="h-4 w-4 inline-block mr-2" />
                Multiple Employees
              </button>
            </div>
          </div>

          {/* Single Employee Mode */}
          {assignmentMode === 'single' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Select Employee *
              </label>
              <select
                value={selectedEmployee}
                onChange={(e) => setSelectedEmployee(e.target.value)}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">Choose an employee...</option>
                {availableEmployees.map((employee) => (
                  <option key={employee.id} value={employee.id}>
                    {employee.firstName} {employee.lastName} (@{employee.username})
                  </option>
                ))}
              </select>
            </div>
          )}

          {/* Bulk Employee Mode */}
          {assignmentMode === 'bulk' && (
            <div>
              <div className="flex items-center justify-between mb-3">
                <label className="block text-sm font-medium text-gray-700">
                  Select Employees * ({selectedEmployees.length} selected)
                </label>
                <button
                  type="button"
                  onClick={handleSelectAll}
                  className="text-sm text-blue-600 hover:text-blue-800"
                >
                  {selectedEmployees.length === availableEmployees.length ? 'Deselect All' : 'Select All'}
                </button>
              </div>
              
              <div className="max-h-48 overflow-y-auto border border-gray-300 rounded-lg">
                {availableEmployees.map((employee) => (
                  <label
                    key={employee.id}
                    className="flex items-center p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                  >
                    <input
                      type="checkbox"
                      checked={selectedEmployees.includes(employee.id)}
                      onChange={() => handleEmployeeToggle(employee.id)}
                      className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <div className="ml-3 flex items-center">
                      <div className="flex-shrink-0 h-8 w-8 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-blue-600 font-medium text-sm">
                          {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
                        </span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm font-medium text-gray-900">
                          {employee.firstName} {employee.lastName}
                        </p>
                        <p className="text-xs text-gray-500">@{employee.username}</p>
                      </div>
                    </div>
                  </label>
                ))}
              </div>
            </div>
          )}

          {/* Wage Configuration */}
          <div>
            <div className="bg-gray-50 rounded-lg p-3 mb-3">
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-600">Default Job Wage:</span>
                <span className="font-medium">${job.hourlyWage}/hr</span>
              </div>
            </div>

            <div className="flex items-center mb-2">
              <input
                type="checkbox"
                id="useCustomWage"
                checked={useCustomWage}
                onChange={(e) => setUseCustomWage(e.target.checked)}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="useCustomWage" className="ml-2 text-sm text-gray-700">
                {assignmentMode === 'bulk' 
                  ? 'Override with custom wage for all selected employees'
                  : 'Override with custom wage'
                }
              </label>
            </div>
            
            {useCustomWage && (
              <input
                type="number"
                value={customWage}
                onChange={(e) => setCustomWage(e.target.value)}
                step="0.01"
                min="0.01"
                placeholder="Enter custom hourly wage..."
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            )}
          </div>

          {availableEmployees.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              <Users className="h-12 w-12 mx-auto mb-3 text-gray-400" />
              <p className="text-sm">No available employees to assign to this job.</p>
              <p className="text-xs text-gray-400 mt-1">All active employees are already assigned to this job.</p>
            </div>
          )}

          <div className="flex items-center justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={
                isLoading || 
                availableEmployees.length === 0 ||
                (assignmentMode === 'single' && !selectedEmployee) ||
                (assignmentMode === 'bulk' && selectedEmployees.length === 0)
              }
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center"
            >
              {isLoading ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" />
                  Assigning...
                </>
              ) : (
                <>
                  {assignmentMode === 'bulk' ? (
                    <>
                      <Users size={16} className="mr-2" />
                      Assign to {selectedEmployees.length} Employee{selectedEmployees.length !== 1 ? 's' : ''}
                    </>
                  ) : (
                    <>
                      <UserPlus size={16} className="mr-2" />
                      Assign Job
                    </>
                  )}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Create Department Modal Component
const CreateDepartmentModal = ({ isOpen, onClose, onSubmit, isLoading }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: ''
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (formData.name.trim()) {
      onSubmit({
        name: formData.name.trim(),
        description: formData.description.trim() || null
      });
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: ''
    });
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100] p-4">
      <div className="bg-white rounded-lg max-w-md w-full">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-xl font-semibold text-gray-900">Create New Department</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircle className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Department Name *
            </label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter department name"
              required
              disabled={isLoading}
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter department description (optional)"
              disabled={isLoading}
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={handleClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading || !formData.name.trim()}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center"
            >
              {isLoading ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" />
                  Creating...
                </>
              ) : (
                <>
                  <Plus size={16} className="mr-2" />
                  Create Department
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// Edit Department Modal Component
const EditDepartmentModal = ({ isOpen, department, onClose, onSubmit, isLoading }) => {
  const [formData, setFormData] = useState({
    name: department?.name || '',
    description: department?.description || ''
  });

  // Update form when department changes
  useEffect(() => {
    if (department) {
      setFormData({
        name: department.name || '',
        description: department.description || ''
      });
    }
  }, [department]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (formData.name.trim()) {
      onSubmit({
        name: formData.name.trim(),
        description: formData.description.trim() || null
      });
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleClose = () => {
    onClose();
  };

  if (!isOpen || !department) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[100] p-4">
      <div className="bg-white rounded-lg max-w-md w-full">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-xl font-semibold text-gray-900">Edit Department</h2>
          <button
            onClick={handleClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <XCircle className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label htmlFor="edit-name" className="block text-sm font-medium text-gray-700 mb-2">
              Department Name *
            </label>
            <input
              type="text"
              id="edit-name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter department name"
              required
              disabled={isLoading}
            />
          </div>

          <div>
            <label htmlFor="edit-description" className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <textarea
              id="edit-description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Enter department description (optional)"
              disabled={isLoading}
            />
          </div>

          <div className="flex items-center justify-end space-x-3 pt-4 border-t">
            <button
              type="button"
              onClick={handleClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
              disabled={isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading || !formData.name.trim()}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors flex items-center"
            >
              {isLoading ? (
                <>
                  <Loader2 size={16} className="animate-spin mr-2" />
                  Updating...
                </>
              ) : (
                <>
                  <Edit2 size={16} className="mr-2" />
                  Update Department
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DepartmentsPage;