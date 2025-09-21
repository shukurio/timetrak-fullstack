import { useState, useEffect } from 'react';
import { Clock, LogIn, LogOut, AlertCircle, Loader2 } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import employeeService from '../../api/employeeService';
import kioskService from '../../api/kioskService';
import useAuthStore from '../../store/authStore';

const ClockMenu = () => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [activeShift, setActiveShift] = useState(null);
  const [clockDirection, setClockDirection] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true);
  const [showJobSelection, setShowJobSelection] = useState(false);
  const { user } = useAuthStore();

  // Update clock every second
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // Check clock direction and active shift on mount
  useEffect(() => {
    checkClockStatus();
  }, [user]);

  const checkClockStatus = async () => {
    if (!user) {
      setIsInitializing(false);
      return;
    }

    setIsInitializing(true);
    try {
      // Determine clock direction using employee ID
      const direction = await kioskService.determineAction(user.id);
      setClockDirection(direction);

      // Get active shift if clocking out
      if (direction === 'CLOCK_OUT') {
        const shift = await employeeService.getActiveShiftSilent();
        setActiveShift(shift);
      } else if (direction === 'CLOCK_IN') {
        // Fetch available jobs
        const employeeJobs = await kioskService.getEmployeeJobs(user.username);
        setJobs(employeeJobs);

        // Auto-select if only one job
        if (employeeJobs.length === 1) {
          setSelectedJob(employeeJobs[0]);
        }
      }
    } catch (error) {
      console.error('Error checking clock status:', error);
      setClockDirection('UNAVAILABLE');
    } finally {
      setIsInitializing(false);
    }
  };

  const handleClockAction = async () => {
    if (clockDirection === 'UNAVAILABLE') {
      toast.error('Clock action unavailable. Please contact your supervisor.');
      return;
    }

    if (clockDirection === 'CLOCK_IN' && !selectedJob) {
      toast.error('Please select a job position');
      return;
    }

    setIsLoading(true);
    try {
      let response;

      if (clockDirection === 'CLOCK_IN') {
        const clockData = { id: selectedJob.employeeJobId };
        response = await kioskService.clockIn(clockData);
        toast.success('Clocked in successfully!');
      } else if (clockDirection === 'CLOCK_OUT') {
        const clockData = { id: user.id };
        response = await kioskService.clockOut(clockData);
        toast.success('Clocked out successfully!');
      }

      // Refresh clock status
      await checkClockStatus();
      setShowJobSelection(false);

    } catch (error) {
      console.error('Clock action error:', error);
      toast.error(error.response?.data?.message || 'Clock action failed');
    } finally {
      setIsLoading(false);
    }
  };

  const formatDuration = (startTime) => {
    if (!startTime) return '00:00:00';

    const start = new Date(startTime);
    const now = new Date();
    const diff = Math.floor((now - start) / 1000);

    const hours = Math.floor(diff / 3600);
    const minutes = Math.floor((diff % 3600) / 60);
    const seconds = diff % 60;

    return `${hours.toString().padStart(2, '0')}:${minutes
      .toString()
      .padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  if (isInitializing) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-center justify-center">
          <Loader2 className="h-6 w-6 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Loading clock status...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
      {/* Clock Header */}
      <div className="bg-gradient-to-r from-primary-500 to-primary-600 p-4">
        <div className="flex items-center justify-between text-white">
          <div className="flex items-center">
            <Clock className="h-5 w-5 mr-2" />
            <span className="font-semibold">Time Clock</span>
          </div>
          <div className="text-lg font-mono">
            {format(currentTime, 'HH:mm:ss')}
          </div>
        </div>
      </div>

      <div className="p-4">
        {/* Active Shift Display (for Clock Out) */}
        {clockDirection === 'CLOCK_OUT' && activeShift && (
          <div className="mb-4 bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <h4 className="text-sm font-medium text-green-900 mb-1">
                  Active Shift
                </h4>
                <p className="text-xs text-green-700 mb-2">
                  {activeShift.jobTitle || 'Current Position'}
                </p>
                <div className="space-y-1">
                  <div className="flex items-center text-xs text-green-600">
                    <LogIn className="h-3 w-3 mr-1" />
                    <span>Started: {format(new Date(activeShift.clockIn), 'HH:mm')}</span>
                  </div>
                  <div className="flex items-center text-xs text-green-600">
                    <Clock className="h-3 w-3 mr-1" />
                    <span>Duration: {formatDuration(activeShift.clockIn)}</span>
                  </div>
                </div>
              </div>
              <div className="text-right">
                <p className="text-xs text-green-600 mb-1">Hourly Rate</p>
                <p className="text-lg font-bold text-green-900">
                  ${activeShift.hourlyWage ? parseFloat(activeShift.hourlyWage).toFixed(2) : '0.00'}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Job Selection (for Clock In) */}
        {clockDirection === 'CLOCK_IN' && showJobSelection && jobs.length > 0 && (
          <div className="mb-4 space-y-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Select Job Position
            </label>
            {jobs.map((job) => (
              <button
                key={job.employeeJobId}
                onClick={() => setSelectedJob(job)}
                className={`w-full p-3 text-left border rounded-lg transition-all ${
                  selectedJob?.employeeJobId === job.employeeJobId
                    ? 'border-primary-500 bg-primary-50 ring-2 ring-primary-200'
                    : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                }`}
              >
                <div className="font-medium text-sm text-gray-900">
                  {job.jobTitle}
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  ${job.hourlyWage}/hr • {job.departmentName}
                </div>
              </button>
            ))}
          </div>
        )}

        {/* No Jobs Warning */}
        {clockDirection === 'CLOCK_IN' && jobs.length === 0 && (
          <div className="mb-4 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-start">
              <AlertCircle className="h-5 w-5 text-yellow-600 mt-0.5 mr-2" />
              <div>
                <h4 className="text-sm font-medium text-yellow-900">
                  No Jobs Assigned
                </h4>
                <p className="text-xs text-yellow-700 mt-1">
                  Please contact your supervisor to get assigned to a job position.
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Clock Action Button */}
        {clockDirection && clockDirection !== 'UNAVAILABLE' && (
          <>
            {clockDirection === 'CLOCK_IN' && !showJobSelection && jobs.length > 0 && (
              <button
                onClick={() => setShowJobSelection(true)}
                className="w-full bg-green-600 hover:bg-green-700 text-white py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center"
              >
                <LogIn className="h-5 w-5 mr-2" />
                Clock In
              </button>
            )}

            {clockDirection === 'CLOCK_IN' && showJobSelection && (
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    setShowJobSelection(false);
                    setSelectedJob(null);
                  }}
                  className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-700 py-3 px-4 rounded-lg font-medium transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleClockAction}
                  disabled={!selectedJob || isLoading}
                  className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center"
                >
                  {isLoading ? (
                    <Loader2 className="h-5 w-5 animate-spin" />
                  ) : (
                    <>
                      <LogIn className="h-5 w-5 mr-2" />
                      Confirm Clock In
                    </>
                  )}
                </button>
              </div>
            )}

            {clockDirection === 'CLOCK_IN' && jobs.length === 0 && (
              <button
                disabled
                className="w-full bg-gray-300 text-gray-500 py-3 px-4 rounded-lg font-medium cursor-not-allowed"
              >
                No Jobs Available
              </button>
            )}

            {clockDirection === 'CLOCK_OUT' && (
              <button
                onClick={handleClockAction}
                disabled={isLoading}
                className="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white py-3 px-4 rounded-lg font-medium transition-colors flex items-center justify-center"
              >
                {isLoading ? (
                  <Loader2 className="h-5 w-5 animate-spin" />
                ) : (
                  <>
                    <LogOut className="h-5 w-5 mr-2" />
                    Clock Out
                  </>
                )}
              </button>
            )}
          </>
        )}

        {/* Unavailable Status */}
        {clockDirection === 'UNAVAILABLE' && (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <div className="flex items-center text-gray-600">
              <AlertCircle className="h-5 w-5 mr-2" />
              <span className="text-sm">Clock action unavailable</span>
            </div>
            <p className="text-xs text-gray-500 mt-1 ml-7">
              Please contact your supervisor for assistance.
            </p>
          </div>
        )}

        {/* Date Display */}
        <div className="mt-4 pt-4 border-t border-gray-200">
          <div className="text-center text-xs text-gray-500">
            {format(currentTime, 'EEEE, MMMM d, yyyy')}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ClockMenu;