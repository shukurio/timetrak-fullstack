import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { MapPin, Save, RotateCcw, Edit3, AlertCircle, CheckCircle, Navigation } from 'lucide-react';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../common/LoadingSpinner';

// Validation schema
const locationSchema = yup.object({
  latitude: yup
    .number()
    .required('Latitude is required')
    .min(-90, 'Latitude must be between -90 and 90')
    .max(90, 'Latitude must be between -90 and 90')
    .typeError('Latitude must be a valid number'),
  longitude: yup
    .number()
    .required('Longitude is required')
    .min(-180, 'Longitude must be between -180 and 180')
    .max(180, 'Longitude must be between -180 and 180')
    .typeError('Longitude must be a valid number'),
  allowedRadius: yup
    .number()
    .required('Allowed radius is required')
    .positive('Allowed radius must be a positive number')
    .min(50, 'Minimum allowed radius is 50 meters')
    .max(10000, 'Maximum allowed radius is 10,000 meters')
    .typeError('Allowed radius must be a valid number'),
});

const CompanyLocation = () => {
  const [isEditing, setIsEditing] = useState(false);
  const [isGettingLocation, setIsGettingLocation] = useState(false);
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
    setValue,
    watch
  } = useForm({
    resolver: yupResolver(locationSchema),
  });

  // Watch form values for display
  const watchedValues = watch();

  // Update company mutation
  const updateCompanyMutation = useMutation({
    mutationFn: (data) => adminService.updateCompany({
      latitude: data.latitude,
      longitude: data.longitude,
      allowedRadius: data.allowedRadius
    }),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['company']);
      toast.success('Company location updated successfully');
      setIsEditing(false);
      reset({
        latitude: data.latitude,
        longitude: data.longitude,
        allowedRadius: data.allowedRadius
      });
    },
    onError: (error) => {
      console.error('Update company location error:', error);
      toast.error(error.response?.data?.message || 'Failed to update company location');
    }
  });

  // Set form values when company data loads
  useEffect(() => {
    if (company) {
      reset({
        latitude: company.latitude || '',
        longitude: company.longitude || '',
        allowedRadius: company.allowedRadius || 100,
      });
    }
  }, [company, reset]);

  const handleSave = (data) => {
    updateCompanyMutation.mutate(data);
  };

  const handleCancel = () => {
    if (company) {
      reset({
        latitude: company.latitude || '',
        longitude: company.longitude || '',
        allowedRadius: company.allowedRadius || 100,
      });
    }
    setIsEditing(false);
  };

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      toast.error('Geolocation is not supported by your browser');
      return;
    }

    setIsGettingLocation(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setValue('latitude', position.coords.latitude, { shouldDirty: true, shouldValidate: true });
        setValue('longitude', position.coords.longitude, { shouldDirty: true, shouldValidate: true });
        toast.success('Current location retrieved successfully');
        setIsGettingLocation(false);
      },
      (error) => {
        console.error('Error getting location:', error);
        let errorMessage = 'Unable to get your current location';
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = 'Location permission denied';
            break;
          case error.POSITION_UNAVAILABLE:
            errorMessage = 'Location information unavailable';
            break;
          case error.TIMEOUT:
            errorMessage = 'Location request timed out';
            break;
        }
        toast.error(errorMessage);
        setIsGettingLocation(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
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
            Failed to Load Company Location
          </h3>
          <p className="text-gray-600 mb-4">
            {error.response?.data?.message || 'An error occurred while loading company location.'}
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
          <MapPin className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            No Company Location Data Found
          </h3>
          <p className="text-gray-600">
            Company location information could not be loaded.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border">
      {/* Header */}
      <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <MapPin className="h-5 w-5 sm:h-6 sm:w-6 text-blue-600 flex-shrink-0" />
            <div>
              <h2 className="text-base sm:text-lg font-semibold text-gray-900">Company Location</h2>
              <p className="text-xs sm:text-sm text-gray-600">Manage location settings for employee check-in validation</p>
            </div>
          </div>
          {!isEditing && (
            <button
              onClick={() => setIsEditing(true)}
              className="btn-outline btn-sm flex items-center"
            >
              <Edit3 className="h-4 w-4 mr-1" />
              Edit
            </button>
          )}
        </div>
      </div>

      {/* Content */}
      <form onSubmit={handleSubmit(handleSave)} className="p-4 sm:p-6">
        <div className="space-y-6">
          {/* Location Info Box */}
          {isEditing && (
            <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
              <div className="flex items-start gap-3">
                <MapPin className="text-blue-600 w-5 h-5 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm font-medium text-blue-900 mb-1">
                    Company Location Settings
                  </p>
                  <p className="text-xs text-blue-700 mb-3">
                    These coordinates define the central point of your company location.
                    Employees must be within the specified radius to successfully check in.
                  </p>
                  <button
                    type="button"
                    onClick={getCurrentLocation}
                    disabled={isGettingLocation}
                    className="text-xs bg-white text-blue-600 px-3 py-1.5 rounded border border-blue-300 hover:bg-blue-50 transition-colors disabled:opacity-50 flex items-center gap-2"
                  >
                    {isGettingLocation ? (
                      <>
                        <div className="h-3 w-3 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600"></div>
                        Getting Location...
                      </>
                    ) : (
                      <>
                        <Navigation className="h-3 w-3" />
                        Use Current Location
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Latitude and Longitude */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Latitude *
              </label>
              {isEditing ? (
                <>
                  <input
                    type="number"
                    step="any"
                    {...register('latitude', { valueAsNumber: true })}
                    className={`w-full py-2 px-3 bg-white border ${errors.latitude ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                    placeholder="e.g., 40.7128"
                  />
                  {errors.latitude && (
                    <p className="mt-1 text-sm text-red-600">{errors.latitude.message}</p>
                  )}
                </>
              ) : (
                <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {company.latitude || <span className="text-gray-500 italic">Not set</span>}
                </div>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Longitude *
              </label>
              {isEditing ? (
                <>
                  <input
                    type="number"
                    step="any"
                    {...register('longitude', { valueAsNumber: true })}
                    className={`w-full py-2 px-3 bg-white border ${errors.longitude ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                    placeholder="e.g., -74.0060"
                  />
                  {errors.longitude && (
                    <p className="mt-1 text-sm text-red-600">{errors.longitude.message}</p>
                  )}
                </>
              ) : (
                <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                  {company.longitude || <span className="text-gray-500 italic">Not set</span>}
                </div>
              )}
            </div>
          </div>

          {/* Allowed Radius */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Allowed Check-in Radius (meters) *
            </label>
            {isEditing ? (
              <>
                <input
                  type="number"
                  min="50"
                  max="10000"
                  step="50"
                  {...register('allowedRadius', { valueAsNumber: true })}
                  className={`w-full py-2 px-3 bg-white border ${errors.allowedRadius ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                  placeholder="e.g., 100"
                />
                {errors.allowedRadius && (
                  <p className="mt-1 text-sm text-red-600">{errors.allowedRadius.message}</p>
                )}
                <p className="mt-1 text-xs text-gray-500">
                  Employees must be within this distance from the company location to check in (50-10,000 meters)
                </p>
              </>
            ) : (
              <div className="py-2 px-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                {company.allowedRadius ? `${company.allowedRadius} meters` : <span className="text-gray-500 italic">Not set</span>}
              </div>
            )}
          </div>

          {/* Map Preview (Optional - shows coordinates) */}
          {(company.latitude && company.longitude) && !isEditing && (
            <div className="pt-4 border-t">
              <h3 className="text-sm font-medium text-gray-700 mb-3">Location Summary</h3>
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-gray-600">Coordinates:</span>
                    <p className="font-mono text-gray-900 mt-1">
                      {company.latitude}, {company.longitude}
                    </p>
                  </div>
                  <div>
                    <span className="text-gray-600">Check-in Zone:</span>
                    <p className="font-medium text-gray-900 mt-1">
                      {company.allowedRadius} meter radius
                    </p>
                  </div>
                </div>
                <div className="mt-3 pt-3 border-t border-gray-200">
                  <p className="text-xs text-gray-600">
                    Employees can check in when they are within {company.allowedRadius} meters of the configured location.
                  </p>
                </div>
              </div>
            </div>
          )}
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
                  Save Location
                </>
              )}
            </button>
          </div>
        )}
      </form>
    </div>
  );
};

export default CompanyLocation;