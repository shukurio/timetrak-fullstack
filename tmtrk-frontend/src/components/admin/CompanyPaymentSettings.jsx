import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Settings, Calendar, Clock, Bell, Save, RotateCcw, AlertCircle, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import adminService from '../../api/adminService';
import LoadingSpinner from '../common/LoadingSpinner';

// Validation schema
const paymentSettingsSchema = yup.object({
  payFrequency: yup
    .string()
    .oneOf(['WEEKLY', 'BIWEEKLY', 'MONTHLY'], 'Please select a valid pay frequency')
    .required('Pay frequency is required'),
  firstDay: yup
    .date()
    .required('First pay period day is required'),
  calculationDay: yup
    .string()
    .oneOf(['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'], 'Please select a valid day')
    .required('Calculation day is required'),
  calculationTime: yup
    .string()
    .matches(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Please enter time in HH:MM format')
    .required('Calculation time is required'),
  gracePeriodHours: yup
    .number()
    .min(0, 'Grace period must be 0 or positive')
    .max(168, 'Grace period cannot exceed 168 hours (1 week)')
    .required('Grace period is required'),
  autoCalculate: yup.boolean(),
  notifyOnCalculation: yup.boolean(),
  notificationEmail: yup
    .string()
    .email('Please enter a valid email address')
    .when('notifyOnCalculation', {
      is: true,
      then: (schema) => schema.required('Notification email is required when notifications are enabled'),
      otherwise: (schema) => schema.notRequired(),
    }),
});

const CompanyPaymentSettings = () => {
  const [isInitializing, setIsInitializing] = useState(false);
  const queryClient = useQueryClient();

  // Check if settings exist
  const { data: settingsExist, isLoading: checkingExists } = useQuery({
    queryKey: ['payment-settings-exists'],
    queryFn: () => adminService.checkPaymentSettingsExists(),
    retry: 1,
  });

  // Get payment settings
  const { 
    data: paymentSettings, 
    isLoading: loadingSettings, 
    error: settingsError,
    refetch: refetchSettings 
  } = useQuery({
    queryKey: ['payment-settings'],
    queryFn: () => adminService.getCompanyPaymentSettings(),
    enabled: settingsExist === true, // Only fetch if settings exist
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
    resolver: yupResolver(paymentSettingsSchema),
    defaultValues: {
      payFrequency: 'BIWEEKLY',
      firstDay: new Date().toISOString().split('T')[0], // Today's date as default
      calculationDay: 'TUESDAY',
      calculationTime: '02:00',
      gracePeriodHours: 72,
      autoCalculate: true,
      notifyOnCalculation: true,
      notificationEmail: '',
    }
  });

  // Initialize settings mutation
  const initializeSettingsMutation = useMutation({
    mutationFn: () => adminService.initializePaymentSettings(),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['payment-settings-exists']);
      queryClient.invalidateQueries(['payment-settings']);
      toast.success('Payment settings initialized with defaults');
      setIsInitializing(false);
    },
    onError: (error) => {
      console.error('Initialize settings error:', error);
      toast.error(error.response?.data?.message || 'Failed to initialize payment settings');
      setIsInitializing(false);
    }
  });

  // Update settings mutation
  const updateSettingsMutation = useMutation({
    mutationFn: (data) => adminService.updateCompanyPaymentSettings(data),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['payment-settings']);
      toast.success('Payment settings updated successfully');
      reset(data); // Reset form with new data to clear isDirty
    },
    onError: (error) => {
      console.error('Update settings error:', error);
      toast.error(error.response?.data?.message || 'Failed to update payment settings');
    }
  });

  // Create settings mutation (in case they don't exist)
  const createSettingsMutation = useMutation({
    mutationFn: (data) => adminService.createCompanyPaymentSettings(data),
    onSuccess: (data) => {
      queryClient.invalidateQueries(['payment-settings-exists']);
      queryClient.invalidateQueries(['payment-settings']);
      toast.success('Payment settings created successfully');
      reset(data);
    },
    onError: (error) => {
      console.error('Create settings error:', error);
      toast.error(error.response?.data?.message || 'Failed to create payment settings');
    }
  });

  // Populate form when settings are loaded
  useEffect(() => {
    if (paymentSettings) {
      // Ensure calculationTime is in HH:MM format
      let formattedTime = paymentSettings.calculationTime || '02:00';
      if (formattedTime && formattedTime.length > 5) {
        formattedTime = formattedTime.substring(0, 5); // Keep only HH:MM part
      }
      
      reset({
        payFrequency: paymentSettings.payFrequency || 'BIWEEKLY',
        firstDay: paymentSettings.firstDay || new Date().toISOString().split('T')[0],
        calculationDay: paymentSettings.calculationDay || 'TUESDAY',
        calculationTime: formattedTime,
        gracePeriodHours: paymentSettings.gracePeriodHours || 72,
        autoCalculate: paymentSettings.autoCalculate !== undefined ? paymentSettings.autoCalculate : true,
        notifyOnCalculation: paymentSettings.notifyOnCalculation !== undefined ? paymentSettings.notifyOnCalculation : true,
        notificationEmail: paymentSettings.notificationEmail || '',
      });
    }
  }, [paymentSettings, reset]);

  const handleInitialize = () => {
    setIsInitializing(true);
    initializeSettingsMutation.mutate();
  };

  const handleSave = (data) => {
    // Ensure calculationTime is in 24-hour format (HH:MM)
    const processedData = {
      ...data,
      calculationTime: data.calculationTime ? data.calculationTime.substring(0, 5) : '02:00', // Ensure HH:MM format only
    };

    if (paymentSettings) {
      updateSettingsMutation.mutate(processedData);
    } else {
      createSettingsMutation.mutate(processedData);
    }
  };

  const handleReset = () => {
    if (paymentSettings) {
      reset(paymentSettings);
    } else {
      reset(); // Reset to default values
    }
  };

  // Loading states
  if (checkingExists || (settingsExist === true && loadingSettings)) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="flex items-center justify-center py-12">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  // Settings don't exist - show initialize option
  if (settingsExist === false) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="text-center py-8">
          <Settings className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Payment Settings Not Configured
          </h3>
          <p className="text-gray-600 mb-6">
            Initialize payment settings with recommended defaults to get started.
          </p>
          <button
            onClick={handleInitialize}
            disabled={isInitializing || initializeSettingsMutation.isLoading}
            className="btn-primary flex items-center mx-auto"
          >
            {isInitializing || initializeSettingsMutation.isLoading ? (
              <LoadingSpinner size="sm" text="" />
            ) : (
              <>
                <Settings className="h-4 w-4 mr-2" />
                Initialize Default Settings
              </>
            )}
          </button>
          <div className="mt-4 text-sm text-gray-500">
            <p>Default settings:</p>
            <ul className="mt-2 space-y-1">
              <li>• Pay Frequency: Bi-weekly</li>
              <li>• First Pay Period: Current/Previous Monday</li>
              <li>• Calculation Day: Tuesday</li>
              <li>• Calculation Time: 2:00 AM</li>
              <li>• Grace Period: 72 hours</li>
              <li>• Auto Calculate: Enabled</li>
              <li>• Notifications: Enabled</li>
            </ul>
          </div>
        </div>
      </div>
    );
  }

  // Error loading settings
  if (settingsError) {
    return (
      <div className="bg-white rounded-lg shadow-sm border p-6">
        <div className="text-center py-8">
          <AlertCircle className="h-16 w-16 text-red-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            Failed to Load Payment Settings
          </h3>
          <p className="text-gray-600 mb-4">
            {settingsError.response?.data?.message || 'An error occurred while loading settings.'}
          </p>
          <button
            onClick={() => refetchSettings()}
            className="btn-outline flex items-center mx-auto"
          >
            <RotateCcw className="h-4 w-4 mr-2" />
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(handleSave)} className="space-y-6">
      <div className="bg-white rounded-lg shadow-sm border">
        {/* Header */}
        <div className="px-4 sm:px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Settings className="h-5 w-5 sm:h-6 sm:w-6 text-blue-600 flex-shrink-0" />
              <div>
                <h2 className="text-base sm:text-lg font-semibold text-gray-900">Payment Settings</h2>
                <p className="text-xs sm:text-sm text-gray-600">Configure company payroll calculation settings</p>
              </div>
            </div>
            {paymentSettings && (
              <div className="flex items-center gap-2 text-xs sm:text-sm text-green-600">
                <CheckCircle className="h-4 w-4" />
                <span>Configured</span>
              </div>
            )}
          </div>
        </div>

        <div className="p-4 sm:p-6 space-y-6">
          {/* Pay Frequency & First Pay Period */}
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Calendar className="h-4 w-4 inline mr-1" />
                Pay Frequency
              </label>
              <select
                {...register('payFrequency')}
                className={`w-full py-2 px-3 bg-white border ${errors.payFrequency ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
              >
                <option value="WEEKLY">Weekly</option>
                <option value="BIWEEKLY">Bi-weekly (Every 2 weeks)</option>
                <option value="MONTHLY">Monthly</option>
              </select>
              {errors.payFrequency && (
                <p className="mt-1 text-sm text-red-600">{errors.payFrequency.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Calendar className="h-4 w-4 inline mr-1" />
                First Pay Period Start
              </label>
              <input
                type="date"
                {...register('firstDay')}
                className={`w-full py-2 px-3 bg-white border ${errors.firstDay ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
              />
              {errors.firstDay && (
                <p className="mt-1 text-sm text-red-600">{errors.firstDay.message}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                Start date of your first pay period
              </p>
            </div>
          </div>

          {/* Calculation Day & Time */}
          <div className="grid md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Calendar className="h-4 w-4 inline mr-1" />
                Calculation Day
              </label>
              <select
                {...register('calculationDay')}
                className={`w-full py-2 px-3 bg-white border ${errors.calculationDay ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
              >
                <option value="MONDAY">Monday</option>
                <option value="TUESDAY">Tuesday</option>
                <option value="WEDNESDAY">Wednesday</option>
                <option value="THURSDAY">Thursday</option>
                <option value="FRIDAY">Friday</option>
                <option value="SATURDAY">Saturday</option>
                <option value="SUNDAY">Sunday</option>
              </select>
              {errors.calculationDay && (
                <p className="mt-1 text-sm text-red-600">{errors.calculationDay.message}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                Day when payments are calculated. <span className="font-medium text-blue-600">24 hours after period end is recommended</span> for accurate time tracking.
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                <Clock className="h-4 w-4 inline mr-1" />
                Calculation Time
              </label>
              <input
                type="time"
                {...register('calculationTime')}
                className={`w-full py-2 px-3 bg-white border ${errors.calculationTime ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                step="60"
              />
              {errors.calculationTime && (
                <p className="mt-1 text-sm text-red-600">{errors.calculationTime.message}</p>
              )}
              <p className="mt-1 text-xs text-gray-500">
                Time when automatic calculation runs. <span className="font-medium text-blue-600">Use 24-hour format (e.g., 02:00 for 2:00 AM, 14:00 for 2:00 PM)</span>
              </p>
            </div>
          </div>

          {/* Grace Period */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Grace Period (Hours)
            </label>
            <input
              type="number"
              min="0"
              max="168"
              {...register('gracePeriodHours')}
              className={`w-full py-2 px-3 bg-white border ${errors.gracePeriodHours ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
            />
            {errors.gracePeriodHours && (
              <p className="mt-1 text-sm text-red-600">{errors.gracePeriodHours.message}</p>
            )}
            <p className="mt-1 text-xs text-gray-500">
              Hours after period end before calculations are finalized (0-168 hours)
            </p>
          </div>

          {/* Automation Settings */}
          <div className="space-y-4">
            <h3 className="text-sm font-medium text-gray-900">Automation Settings</h3>
            
            <div className="space-y-4">
              <label className="flex items-start gap-3">
                <input
                  type="checkbox"
                  {...register('autoCalculate')}
                  className="mt-1 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <div>
                  <span className="text-sm font-medium text-gray-700">
                    Automatic Payment Calculation
                  </span>
                  <p className="text-xs text-gray-500">
                    Automatically calculate payments on the scheduled day and time
                  </p>
                </div>
              </label>

              <label className="flex items-start gap-3">
                <input
                  type="checkbox"
                  {...register('notifyOnCalculation')}
                  className="mt-1 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <div>
                  <span className="text-sm font-medium text-gray-700">
                    <Bell className="h-3 w-3 inline mr-1" />
                    Calculation Notifications
                  </span>
                  <p className="text-xs text-gray-500">
                    Send notifications when automatic calculations are completed
                  </p>
                </div>
              </label>

              {/* Conditional notification email field */}
              {watch('notifyOnCalculation') && (
                <div className="ml-7">
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Notification Email *
                  </label>
                  <input
                    type="email"
                    {...register('notificationEmail')}
                    className={`w-full py-2 px-3 bg-white border ${errors.notificationEmail ? 'border-red-500' : 'border-gray-200'} rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-colors`}
                    placeholder="admin@company.com"
                  />
                  {errors.notificationEmail && (
                    <p className="mt-1 text-sm text-red-600">{errors.notificationEmail.message}</p>
                  )}
                  <p className="mt-1 text-xs text-gray-500">
                    Email address to receive payment calculation notifications
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
          <div className="text-xs text-gray-500">
            {isDirty && 'You have unsaved changes'}
          </div>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={handleReset}
              disabled={!isDirty}
              className="btn-outline btn-sm disabled:opacity-50"
            >
              <RotateCcw className="h-4 w-4 mr-1" />
              Reset
            </button>
            <button
              type="submit"
              disabled={!isDirty || updateSettingsMutation.isLoading || createSettingsMutation.isLoading}
              className="btn-primary btn-sm disabled:opacity-50 flex items-center"
            >
              {(updateSettingsMutation.isLoading || createSettingsMutation.isLoading) ? (
                <LoadingSpinner size="sm" text="" />
              ) : (
                <>
                  <Save className="h-4 w-4 mr-1" />
                  Save Changes
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </form>
  );
};

export default CompanyPaymentSettings;