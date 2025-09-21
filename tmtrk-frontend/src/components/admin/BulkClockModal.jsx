import { X, LogIn, LogOut } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { format } from 'date-fns';
import LoadingSpinner from '../common/LoadingSpinner';

const bulkClockSchema = yup.object({
  ids: yup.array().min(1, 'Select at least one employee').required(),
  time: yup.string().required('Time is required'),
  notes: yup.string().max(500, 'Notes must be less than 500 characters'),
  reason: yup.string().max(500, 'Reason must be less than 500 characters')
});

const BulkClockModal = ({ 
  isOpen, 
  onClose, 
  bulkClockAction,
  selectedShifts,
  onSubmit,
  isLoading
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset
  } = useForm({
    resolver: yupResolver(bulkClockSchema)
  });

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleFormSubmit = (data) => {
    const clockData = {
      ids: selectedShifts,
      time: data.time,
      notes: data.notes || undefined,
      reason: data.reason || undefined
    };
    onSubmit({ action: bulkClockAction, data: clockData });
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 animate-scale-up">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900">
            Bulk Clock {bulkClockAction === 'in' ? 'In' : 'Out'}
          </h2>
          <button
            onClick={handleClose}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="bg-blue-50 p-3 rounded-lg">
            <p className="text-sm text-blue-800">
              You are about to clock {bulkClockAction} <strong>{selectedShifts.length} employees</strong>.
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Time *
            </label>
            <input
              {...register('time')}
              type="datetime-local"
              defaultValue={format(new Date(), "yyyy-MM-dd'T'HH:mm")}
              className="input-field"
            />
            {errors.time && (
              <p className="text-sm text-red-600 mt-1">{errors.time.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              {...register('notes')}
              rows="2"
              className="input-field resize-none"
              placeholder="Optional notes..."
            />
            {errors.notes && (
              <p className="text-sm text-red-600 mt-1">{errors.notes.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Reason
            </label>
            <input
              {...register('reason')}
              type="text"
              className="input-field"
              placeholder="Reason for bulk action..."
            />
            {errors.reason && (
              <p className="text-sm text-red-600 mt-1">{errors.reason.message}</p>
            )}
          </div>

          <div className="flex space-x-3 pt-4">
            <button
              type="button"
              onClick={handleClose}
              className="flex-1 btn-outline"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 btn-primary flex items-center justify-center"
            >
              {isLoading ? (
                <LoadingSpinner size="sm" text="" />
              ) : (
                <>
                  {bulkClockAction === 'in' ? (
                    <LogIn className="h-4 w-4 mr-2" />
                  ) : (
                    <LogOut className="h-4 w-4 mr-2" />
                  )}
                  Clock {bulkClockAction === 'in' ? 'In' : 'Out'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BulkClockModal;