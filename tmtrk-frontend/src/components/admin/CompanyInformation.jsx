import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Building2, Save, RotateCcw, Edit3, AlertCircle, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../common/LoadingSpinner';

// Validation schema
const companySchema = yup.object({
  name: yup
    .string()
    .min(2, 'Company name must be at least 2 characters')
    .max(100, 'Company name cannot exceed 100 characters')
    .required('Company name is required'),
  description: yup
    .string()
    .max(500, 'Description cannot exceed 500 characters'),
});

const CompanyInformation = () => {
  const [isEditing, setIsEditing] = useState(false);
  const queryClient = useQueryClient();

  // Get company information
  const { 
    data: company, 
    isLoading, 
    error,
    refetch 
  } = useQuery({
    queryKey: ['company'],
    queryFn: () => adminService.getCompany(),
    retry: 1,
  });

  // Form setup
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    reset,
  } = useForm({
    resolver: yupResolver(companySchema),
  });

  // Update company mutation
  const updateCompanyMutation = useMutation({
    mutationFn: (data) => adminService.updateCompany(data),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['company']);
      toast.success('Company information updated successfully');
      setIsEditing(false);
      reset(data);
    },
    onError: (error) => {
      console.error('Update company error:', error);
      toast.error(error.response?.data?.message || 'Failed to update company information');
    }
  });

  // Set form values when company data loads
  useEffect(() => {
    if (company) {
      reset({
        name: company.name || '',
        description: company.description || '',
      });
    }
  }, [company, reset]);

  const handleSave = (data) => {
    updateCompanyMutation.mutate(data);
  };

  const handleCancel = () => {
    if (company) {
      reset({
        name: company.name || '',
        description: company.description || '',
      });
    }
    setIsEditing(false);
  };

  // Loading state
  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="flex items-center justify-center py-12">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="text-center py-8">
          <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Failed to Load Company Information
          </h3>
          <p className="text-gray-600 mb-4">
            {error.response?.data?.message || 'An error occurred while loading company information.'}
          </p>
          <button
            onClick={() => refetch()}
            className="btn-outline flex items-center mx-auto"
          >
            <RotateCcw className="h-4 w-4 mr-2" />
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!company) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="text-center py-8">
          <Building2 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            No Company Information Found
          </h3>
          <p className="text-gray-600">
            Company information could not be loaded.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Building2 className="h-6 w-6 text-blue-600" />
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Company Information</h2>
              <p className="text-sm text-gray-600">Basic company details and settings</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {company && (
              <div className="flex items-center gap-2 text-sm text-green-600">
                <CheckCircle className="h-4 w-4" />
                <span>Active</span>
              </div>
            )}
            {!isEditing && (
              <button
                onClick={() => {
                  if (company) {
                    reset({
                      name: company.name || '',
                      description: company.description || '',
                    });
                  }
                  setIsEditing(true);
                }}
                className="btn-outline btn-sm flex items-center"
              >
                <Edit3 className="h-4 w-4 mr-1" />
                Edit
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <form onSubmit={handleSubmit(handleSave)} className="p-6">
        <div className="space-y-6">
          {/* Company Name */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Company Name *
            </label>
            {isEditing ? (
              <div>
                <input
                  type="text"
                  {...register('name')}
                  className={`w-full py-2 px-3 bg-white border ${errors.name ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                  placeholder="Enter company name"
                />
                {errors.name ? (
                  <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
                ) : null}
              </div>
            ) : (
              <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                {company.name}
              </div>
            )}
          </div>


          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            {isEditing ? (
              <div>
                <textarea
                  {...register('description')}
                  className={`w-full py-2 px-3 bg-white border ${errors.description ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors resize-none min-h-[100px]`}
                  placeholder="Enter company description (optional)"
                  style={{ minHeight: '100px' }}
                />
                {errors.description ? (
                  <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
                ) : null}
              </div>
            ) : (
              <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900 min-h-[100px]">
                {company.description || (
                  <span className="text-gray-500 italic">No description provided</span>
                )}
              </div>
            )}
          </div>

          {/* Company Status */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Status
            </label>
            <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg">
              <div className="flex items-center gap-2">
                {company.isActive ? (
                  <>
                    <CheckCircle className="h-4 w-4 text-green-500" />
                    <span className="text-green-700 font-medium">Active</span>
                  </>
                ) : (
                  <>
                    <AlertCircle className="h-4 w-4 text-red-500" />
                    <span className="text-red-700 font-medium">Inactive</span>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* Company ID (Read-only) */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Company ID
            </label>
            <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-700 font-mono">
              #{company.id}
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        {isEditing && (
          <div className="mt-8 pt-6 border-t border-gray-200 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={handleCancel}
              className="btn-outline"
              disabled={updateCompanyMutation.isLoading}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!isDirty || updateCompanyMutation.isLoading}
              className="btn-primary flex items-center disabled:opacity-50"
            >
              {updateCompanyMutation.isLoading ? (
                <LoadingSpinner size="sm" text="" />
              ) : (
                <>
                  <Save className="h-4 w-4 mr-2" />
                  Save Changes
                </>
              )}
            </button>
          </div>
        )}
      </form>
    </div>
  );
};

export default CompanyInformation;