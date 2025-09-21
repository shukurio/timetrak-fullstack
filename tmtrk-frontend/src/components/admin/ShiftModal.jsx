import { X, Save } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { format, parseISO } from 'date-fns';
import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import adminService from '../../api/adminService';
import LoadingSpinner from '../common/LoadingSpinner';

const shiftSchema = yup.object({
  employeeJobId: yup.number()
    .required('Employee job assignment is required')
    .positive('Please select a valid assignment'),
  clockIn: yup.string()
    .required('Clock in time is required'),
  clockOut: yup.string()
    .nullable()
    .test('clockOut-after-clockIn', 'Clock out must be after clock in', function(value) {
      const { clockIn } = this.parent;
      if (!value || !clockIn) return true;
      return new Date(value) > new Date(clockIn);
    }),
  notes: yup.string().max(1000, 'Notes must be less than 1000 characters'),
  status: yup.string().required('Status is required')
});

const ShiftModal = ({
  isOpen,
  onClose,
  editingShift,
  onSubmit,
  isLoading
}) => {
  const [dropdownClicked, setDropdownClicked] = useState(false);
  const [employeeJobs, setEmployeeJobs] = useState([]);
  const [selectedDepartment, setSelectedDepartment] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch
  } = useForm({
    resolver: yupResolver(shiftSchema)
  });

  // Fetch departments for create mode
  const { data: departmentsData } = useQuery({
    queryKey: ['departments-for-shift-modal'],
    queryFn: () => adminService.getDepartments({ page: 0, size: 100, sort: 'name,asc' }),
    enabled: !editingShift,
  });

  // Fetch employees for selected department
  const { data: employeesData } = useQuery({
    queryKey: ['employees-for-shift-modal', selectedDepartment],
    queryFn: () => adminService.getEmployeesByDepartment(selectedDepartment, { page: 0, size: 100, sort: 'firstName,asc' }),
    enabled: !!selectedDepartment && !editingShift,
  });

  // Fetch employee jobs for selected employee (create mode) or edit mode
  const { data: allEmployeeJobs, isLoading: jobsLoading } = useQuery({
    queryKey: ['employee-jobs-for-shift-modal', selectedEmployee || editingShift?.employeeId],
    queryFn: () => adminService.getEmployeeJobsByEmployee(selectedEmployee || editingShift?.employeeId),
    enabled: (!!selectedEmployee && !editingShift) || (dropdownClicked && !!editingShift?.employeeId),
  });

  // Set form values when editing
  useEffect(() => {
    if (editingShift && isOpen) {
      // Set initial employee jobs with current shift's job first
      const currentJob = {
        employeeJobId: editingShift.employeeJobId,
        employeeName: editingShift.fullName,
        jobTitle: editingShift.jobTitle
      };
      setEmployeeJobs([currentJob]);

      // Set form values with a small delay to ensure options are rendered
      setTimeout(() => {
        setValue('employeeJobId', editingShift.employeeJobId);
        setValue('clockIn', format(parseISO(editingShift.clockIn), "yyyy-MM-dd'T'HH:mm"));
        if (editingShift.clockOut) {
          setValue('clockOut', format(parseISO(editingShift.clockOut), "yyyy-MM-dd'T'HH:mm"));
        }
        setValue('notes', editingShift.notes || '');
        setValue('status', editingShift.status);
      }, 10);
    }
  }, [editingShift, isOpen, setValue]);

  // Update employee jobs when all jobs are loaded
  useEffect(() => {
    if (allEmployeeJobs) {
      if (editingShift && dropdownClicked) {
        setEmployeeJobs(allEmployeeJobs);
      } else if (!editingShift && selectedEmployee) {
        setEmployeeJobs(allEmployeeJobs);
      }
    }
  }, [allEmployeeJobs, dropdownClicked, editingShift, selectedEmployee]);

  const handleClose = () => {
    reset();
    setDropdownClicked(false);
    setEmployeeJobs([]);
    setSelectedDepartment('');
    setSelectedEmployee('');
    onClose();
  };

  // Reset state when modal closes
  useEffect(() => {
    if (!isOpen) {
      setDropdownClicked(false);
      setEmployeeJobs([]);
      setSelectedDepartment('');
      setSelectedEmployee('');
    }
  }, [isOpen]);

  const handleFormSubmit = (data) => {
    const shiftData = {
      employeeJobId: Number(data.employeeJobId),
      clockIn: data.clockIn,
      clockOut: data.clockOut || null,
      status: data.status,
      ...(data.notes && data.notes.trim() && { notes: data.notes.trim() })
    };

    // Remove any undefined values
    Object.keys(shiftData).forEach(key => {
      if (shiftData[key] === undefined) {
        delete shiftData[key];
      }
    });

    onSubmit(shiftData);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 animate-scale-up">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-900">
            {editingShift ? 'Edit Shift' : 'Create New Shift'}
          </h2>
          <button
            onClick={handleClose}
            className="p-2 text-gray-400 hover:text-gray-600 rounded-lg"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          {!editingShift && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Department *
                </label>
                <select
                  value={selectedDepartment}
                  onChange={(e) => {
                    setSelectedDepartment(e.target.value);
                    setSelectedEmployee('');
                    setEmployeeJobs([]);
                    setValue('employeeJobId', '');
                  }}
                  className="input-field"
                >
                  <option value="">Select Department</option>
                  {departmentsData?.content?.map((department) => (
                    <option key={department.id} value={department.id}>
                      {department.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Employee *
                </label>
                <select
                  value={selectedEmployee}
                  onChange={(e) => {
                    setSelectedEmployee(e.target.value);
                    setEmployeeJobs([]);
                    setValue('employeeJobId', '');
                  }}
                  className="input-field"
                  disabled={!selectedDepartment}
                >
                  <option value="">Select Employee</option>
                  {employeesData?.content?.map((employee) => (
                    <option key={employee.id} value={employee.id}>
                      {employee.firstName} {employee.lastName}
                    </option>
                  ))}
                </select>
              </div>
            </>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Job Assignment *
            </label>
            <select
              {...register('employeeJobId')}
              className="input-field"
              onClick={() => {
                if (editingShift && !dropdownClicked) {
                  setDropdownClicked(true);
                }
              }}
              disabled={!editingShift && !selectedEmployee}
            >
              <option value="">Select Job Assignment</option>
              {employeeJobs?.map((assignment) => (
                <option key={assignment.employeeJobId} value={assignment.employeeJobId}>
                  {assignment.jobTitle}
                </option>
              ))}
              {jobsLoading && (editingShift ? dropdownClicked : selectedEmployee) && (
                <option disabled>Loading jobs...</option>
              )}
            </select>
            {errors.employeeJobId && (
              <p className="text-sm text-red-600 mt-1">{errors.employeeJobId.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Clock In Time *
            </label>
            <input
              {...register('clockIn')}
              type="datetime-local"
              className="input-field"
            />
            {errors.clockIn && (
              <p className="text-sm text-red-600 mt-1">{errors.clockIn.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Clock Out Time
            </label>
            <input
              {...register('clockOut')}
              type="datetime-local"
              className="input-field"
            />
            {errors.clockOut && (
              <p className="text-sm text-red-600 mt-1">{errors.clockOut.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Status *
            </label>
            <select {...register('status')} className="input-field">
              <option value="">Select Status</option>
              <option value="ACTIVE">Active</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            {errors.status && (
              <p className="text-sm text-red-600 mt-1">{errors.status.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              {...register('notes')}
              rows="3"
              className="input-field resize-none"
              placeholder="Optional notes about this shift..."
            />
            {errors.notes && (
              <p className="text-sm text-red-600 mt-1">{errors.notes.message}</p>
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
                  <Save className="h-4 w-4 mr-2" />
                  {editingShift ? 'Update' : 'Create'}
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ShiftModal;