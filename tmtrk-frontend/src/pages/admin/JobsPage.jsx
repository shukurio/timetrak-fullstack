import { useState } from 'react';
import {
  Briefcase,
  Plus,
  Search,
  Filter,
  Edit3,
  Trash2,
  Users,
  DollarSign,
  Building2,
  X,
  Save
} from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import PageContainer from '../../components/common/PageContainer';
import SearchBar from '../../components/common/SearchBar';
import AddButton from '../../components/common/AddButton';

// Validation schema for job form
const jobSchema = yup.object({
  jobTitle: yup.string()
    .required('Job title is required')
    .min(2, 'Job title must be at least 2 characters')
    .max(100, 'Job title must be less than 100 characters'),
  hourlyWage: yup.number()
    .required('Hourly wage is required')
    .min(0.01, 'Hourly wage must be greater than 0')
    .max(1000, 'Hourly wage seems too high'),
  departmentId: yup.number()
    .required('Department is required')
    .positive('Please select a valid department')
});

const JobsPage = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingJob, setEditingJob] = useState(null);
  const pageSize = 12;

  const queryClient = useQueryClient();

  // Fetch jobs with pagination
  const { data: jobsData, isLoading: jobsLoading, error: jobsError } = useQuery({
    queryKey: ['jobs', searchQuery, selectedDepartment, currentPage],
    queryFn: () => {
      const params = { page: currentPage, size: pageSize, sort: 'jobTitle,asc' };
      
      if (searchQuery.trim()) {
        return adminService.searchJobs(searchQuery);
      } else if (selectedDepartment) {
        return { content: adminService.getJobsByDepartment(selectedDepartment) };
      } else {
        return adminService.getJobsPaged(params);
      }
    },
    keepPreviousData: true,
    onError: (err) => console.error('Jobs API Error:', err),
  });

  // Fetch departments for dropdown
  const { data: departmentsData } = useQuery({
    queryKey: ['departments-for-jobs'],
    queryFn: () => adminService.getDepartments({ page: 0, size: 100, sort: 'name,asc' }),
    onError: (err) => console.error('Departments API Error:', err),
  });

  // Create job mutation
  const createJobMutation = useMutation({
    mutationFn: (jobData) => adminService.createJob(jobData),
    onSuccess: () => {
      queryClient.invalidateQueries(['jobs']);
      setShowCreateModal(false);
      toast.success('Job created successfully!');
    },
    onError: (error) => {
      console.error('Create job error:', error);
      toast.error(error.response?.data?.message || 'Failed to create job');
    }
  });

  // Update job mutation  
  const updateJobMutation = useMutation({
    mutationFn: ({ id, data }) => adminService.updateJob(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['jobs']);
      setEditingJob(null);
      setShowCreateModal(false);
      reset();
      toast.success('Job updated successfully!');
    },
    onError: (error) => {
      console.error('Update job error:', error);
      toast.error(error.response?.data?.message || 'Failed to update job');
    }
  });

  // Delete job mutation
  const deleteJobMutation = useMutation({
    mutationFn: (id) => adminService.deleteJob(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['jobs']);
      toast.success('Job deleted successfully!');
    },
    onError: (error) => {
      console.error('Delete job error:', error);
      toast.error(error.response?.data?.message || 'Failed to delete job');
    }
  });

  // Form handling
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue
  } = useForm({
    resolver: yupResolver(jobSchema)
  });

  // Event handlers
  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
  };

  const handleCreateJob = (data) => {
    createJobMutation.mutate(data);
  };

  const handleEditJob = (job) => {
    setEditingJob(job);
    setValue('jobTitle', job.jobTitle);
    setValue('hourlyWage', job.hourlyWage);
    setValue('departmentId', job.departmentId);
    setShowCreateModal(true);
  };

  const handleUpdateJob = (data) => {
    if (editingJob) {
      updateJobMutation.mutate({ id: editingJob.id, data });
    }
  };

  const handleDeleteJob = (job) => {
    if (window.confirm(`Are you sure you want to delete "${job.jobTitle}"? This action cannot be undone.`)) {
      deleteJobMutation.mutate(job.id);
    }
  };

  const handleModalClose = () => {
    setShowCreateModal(false);
    setEditingJob(null);
    reset();
  };

  const clearFilters = () => {
    setSearchQuery('');
    setSelectedDepartment('');
    setCurrentPage(0);
  };

  // Render functions
  const renderJobCard = (job) => (
    <div key={job.id} className="card hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center">
          <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
            <Briefcase className="h-6 w-6 text-primary-600" />
          </div>
          <div className="ml-3">
            <h3 className="text-lg font-semibold text-gray-900">{job.jobTitle}</h3>
            <p className="text-sm text-gray-500">{job.departmentName}</p>
          </div>
        </div>
        
      </div>

      <div className="space-y-3">
        <div className="flex items-center text-sm text-gray-600">
          <DollarSign className="h-4 w-4 mr-2" />
          <span className="font-medium">${job.hourlyWage}/hour</span>
        </div>
        
        <div className="flex items-center text-sm text-gray-600">
          <Building2 className="h-4 w-4 mr-2" />
          <span>{job.departmentName}</span>
        </div>
      </div>

      <div className="flex items-center justify-between mt-4 pt-4 border-t">
        <button
          onClick={() => handleEditJob(job)}
          className="flex items-center text-sm text-primary-600 hover:text-primary-700"
        >
          <Edit3 className="h-4 w-4 mr-1" />
          Edit
        </button>
        
        <button
          onClick={() => handleDeleteJob(job)}
          className="flex items-center text-sm text-red-600 hover:text-red-700"
        >
          <Trash2 className="h-4 w-4 mr-1" />
          Delete
        </button>
      </div>
    </div>
  );

  const renderCreateEditModal = () => (
    showCreateModal && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 animate-scale-up">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-gray-900">
              {editingJob ? 'Edit Job' : 'Create New Job'}
            </h2>
            <button
              onClick={handleModalClose}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit(editingJob ? handleUpdateJob : handleCreateJob)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Job Title *
              </label>
              <input
                {...register('jobTitle')}
                type="text"
                className="input-field"
                placeholder="e.g. Software Engineer"
              />
              {errors.jobTitle && (
                <p className="text-sm text-red-600 mt-1">{errors.jobTitle.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Hourly Wage ($) *
              </label>
              <input
                {...register('hourlyWage')}
                type="number"
                step="0.01"
                min="0"
                className="input-field"
                placeholder="25.00"
              />
              {errors.hourlyWage && (
                <p className="text-sm text-red-600 mt-1">{errors.hourlyWage.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Department *
              </label>
              <select {...register('departmentId')} className="input-field">
                <option value="">Select Department</option>
                {departmentsData?.content?.map((dept) => (
                  <option key={dept.id} value={dept.id}>
                    {dept.name} ({dept.code})
                  </option>
                ))}
              </select>
              {errors.departmentId && (
                <p className="text-sm text-red-600 mt-1">{errors.departmentId.message}</p>
              )}
            </div>

            <div className="flex space-x-3 pt-4">
              <button
                type="button"
                onClick={handleModalClose}
                className="flex-1 btn-outline"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={createJobMutation.isLoading || updateJobMutation.isLoading}
                className="flex-1 btn-primary flex items-center justify-center"
              >
                {(createJobMutation.isLoading || updateJobMutation.isLoading) ? (
                  <LoadingSpinner size="sm" text="" />
                ) : (
                  <>
                    <Save className="h-4 w-4 mr-2" />
                    {editingJob ? 'Update' : 'Create'}
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    )
  );

  return (
    <PageContainer title="Job Positions" icon={Briefcase}>
      {/* Search and Create Section */}
      <div className="p-6 border-b border-gray-200 bg-gray-50 -m-6 mb-6">
        <div className="flex flex-col sm:flex-row gap-2">
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Search jobs by title..."
          />
          <AddButton onClick={() => setShowCreateModal(true)}>
            Create Job
          </AddButton>
        </div>
      </div>

      {/* Jobs Count Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">
            Available Positions
            {jobsData && (
              <span className="ml-2 text-sm text-gray-500">
                ({Array.isArray(jobsData.content) ? jobsData.content.length : jobsData.length || 0} jobs)
              </span>
            )}
          </h2>
        </div>
      </div>

      {/* Jobs Content */}
      <div>
          {jobsError ? (
            <div className="text-center py-12">
              <div className="text-red-600 mb-2">⚠️ Error loading jobs</div>
              <p className="text-sm text-gray-600 mb-4">
                {jobsError.response?.data?.message || jobsError.message || 'Failed to load jobs'}
              </p>
              <button onClick={() => window.location.reload()} className="btn-primary">
                Retry
              </button>
            </div>
          ) : jobsLoading ? (
            <div className="text-center py-12">
              <LoadingSpinner />
            </div>
          ) : !jobsData?.content?.length && !jobsData?.length ? (
            <div className="text-center py-12">
              <Briefcase className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {searchQuery || selectedDepartment ? 'No jobs found' : 'No jobs created yet'}
              </h3>
              <p className="text-gray-600 mb-4">
                {searchQuery || selectedDepartment
                  ? 'Try adjusting your search criteria'
                  : 'Create your first job position to get started'
                }
              </p>
              {!searchQuery && !selectedDepartment && (
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="btn-primary"
                >
                  Create First Job
                </button>
              )}
            </div>
          ) : (
            <>
              {/* Jobs Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 min-h-[600px] transition-all duration-200 ease-in-out">
                {(jobsData.content || jobsData).map(renderJobCard)}
              </div>

              {/* Pagination */}
              {jobsData.totalPages > 1 && (
                <div className="mt-6 flex items-center justify-between">
                  <div className="text-sm text-gray-700">
                    Showing page {currentPage + 1} of {jobsData.totalPages}
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
                      onClick={() => setCurrentPage(Math.min(jobsData.totalPages - 1, currentPage + 1))}
                      disabled={currentPage >= jobsData.totalPages - 1}
                      className="btn-outline disabled:opacity-50"
                    >
                      Next
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
      </div>

      {/* Create/Edit Modal */}
      {renderCreateEditModal()}
    </PageContainer>
  );
};

export default JobsPage;