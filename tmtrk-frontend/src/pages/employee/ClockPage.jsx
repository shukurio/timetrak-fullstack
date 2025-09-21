import { useState, useEffect } from 'react';
import { Clock, LogIn, LogOut, AlertCircle, Loader2, Calendar, DollarSign, Timer, MapPin } from 'lucide-react';
import { format, differenceInSeconds } from 'date-fns';
import toast from 'react-hot-toast';
import employeeService from '../../api/employeeService';
import useAuthStore from '../../store/authStore';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const ClockPage = () => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [activeShift, setActiveShift] = useState(null);
  const [clockDirection, setClockDirection] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true);
  const [shiftDuration, setShiftDuration] = useState('00:00:00');
  const [location, setLocation] = useState(null);
  const [locationError, setLocationError] = useState(null);
  const [isGettingLocation, setIsGettingLocation] = useState(false);
  const { user } = useAuthStore();

  // Update clock every second
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // Update shift duration if there's an active shift
  useEffect(() => {
    if (activeShift?.clockIn) {
      const updateDuration = () => {
        const start = new Date(activeShift.clockIn);
        const now = new Date();
        const totalSeconds = differenceInSeconds(now, start);

        const hours = Math.floor(totalSeconds / 3600);
        const minutes = Math.floor((totalSeconds % 3600) / 60);
        const seconds = totalSeconds % 60;

        setShiftDuration(
          `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
        );
      };

      updateDuration();
      const timer = setInterval(updateDuration, 1000);
      return () => clearInterval(timer);
    }
  }, [activeShift]);

  // Check clock direction and active shift on mount
  useEffect(() => {
    checkClockStatus();
  }, [user]);

  // Get user's current location
  const getCurrentLocation = () => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by this browser'));
        return;
      }

      setIsGettingLocation(true);
      setLocationError(null);

      navigator.geolocation.getCurrentPosition(
        (position) => {
          console.log('Geolocation position received:', position);
          const coords = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          };
          console.log('Coordinates created:', coords);
          setLocation(coords);
          setIsGettingLocation(false);
          resolve(coords);
        },
        (error) => {
          let errorMessage = 'Unable to get location';
          switch (error.code) {
            case error.PERMISSION_DENIED:
              errorMessage = 'Location access denied. Please enable location permissions.';
              break;
            case error.POSITION_UNAVAILABLE:
              errorMessage = 'Location information unavailable.';
              break;
            case error.TIMEOUT:
              errorMessage = 'Location request timed out.';
              break;
          }
          setLocationError(errorMessage);
          setIsGettingLocation(false);
          reject(new Error(errorMessage));
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000 // 1 minute
        }
      );
    });
  };

  const checkClockStatus = async () => {
    if (!user) {
      setIsInitializing(false);
      return;
    }

    setIsInitializing(true);
    try {
      // Determine clock direction using employee ID
      const direction = await employeeService.determineClockAction(user.id);
      setClockDirection(direction);

      // Get active shift if clocking out
      if (direction === 'CLOCK_OUT') {
        const shift = await employeeService.getActiveShiftSilent();
        setActiveShift(shift);
      } else if (direction === 'CLOCK_IN') {
        // Fetch available jobs
        const employeeJobs = await employeeService.getEmployeeClockJobs();
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

  const handleClockIn = async () => {
    if (!selectedJob) {
      toast.error('Please select a job position');
      return;
    }

    setIsLoading(true);
    try {
      // Get current location
      console.log('Getting location for clock in...');
      const coords = await getCurrentLocation();
      console.log('Location obtained:', coords);

      const clockData = {
        id: selectedJob.employeeJobId,
        latitude: coords.latitude,
        longitude: coords.longitude
      };

      console.log('Clock in data being sent:', clockData);
      console.log('Latitude type:', typeof coords.latitude, 'Value:', coords.latitude);
      console.log('Longitude type:', typeof coords.longitude, 'Value:', coords.longitude);

      const response = await employeeService.clockIn(clockData);

      toast.success('Clocked in successfully!');

      // Refresh the page state
      await checkClockStatus();
    } catch (error) {
      console.error('Clock in error:', error);
      if (error.message.includes('location') || error.message.includes('Location')) {
        toast.error(`Clock in failed: ${error.message}`);
      } else {
        toast.error(error.response?.data?.message || 'Failed to clock in');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleClockOut = async () => {
    setIsLoading(true);
    try {
      // Get current location
      const coords = await getCurrentLocation();

      const clockData = {
        id: user.id,
        latitude: coords.latitude,
        longitude: coords.longitude
      };

      console.log('Clock out data being sent:', clockData);
      const response = await employeeService.clockOut(clockData);

      // Show success message with earnings
      if (response?.shiftEarnings) {
        toast.success(`Clocked out successfully! Earned: $${parseFloat(response.shiftEarnings).toFixed(2)}`);
      } else {
        toast.success('Clocked out successfully!');
      }

      // Reset state
      setActiveShift(null);
      setShiftDuration('00:00:00');

      // Refresh the page state
      await checkClockStatus();
    } catch (error) {
      console.error('Clock out error:', error);
      if (error.message.includes('location') || error.message.includes('Location')) {
        toast.error(`Clock out failed: ${error.message}`);
      } else {
        toast.error(error.response?.data?.message || 'Failed to clock out');
      }
    } finally {
      setIsLoading(false);
    }
  };

  if (isInitializing) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="lg" text="Loading clock status..." />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Time Clock</h1>
        <p className="text-gray-600 mt-1">Clock in and out of your shifts</p>
      </div>

      {/* Current Time Display */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-xl shadow-lg p-8 mb-8 text-white">
        <div className="text-center">
          <div className="flex items-center justify-center mb-4">
            <Clock className="h-8 w-8 mr-3" />
            <span className="text-xl font-semibold">Current Time</span>
          </div>
          <div className="text-6xl font-bold font-mono mb-2">
            {format(currentTime, 'HH:mm:ss')}
          </div>
          <div className="text-lg opacity-90">
            {format(currentTime, 'EEEE, MMMM d, yyyy')}
          </div>

          {/* Location Status */}
          <div className="mt-4 pt-4 border-t border-primary-500/30">
            <div className="flex items-center justify-center text-sm opacity-90">
              {isGettingLocation ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Getting location...
                </>
              ) : location ? (
                <>
                  <MapPin className="h-4 w-4 mr-2" />
                  Location ready
                </>
              ) : locationError ? (
                <>
                  <AlertCircle className="h-4 w-4 mr-2" />
                  Location unavailable
                </>
              ) : (
                <>
                  <MapPin className="h-4 w-4 mr-2" />
                  Location required for clock actions
                </>
              )}
            </div>
            {locationError && (
              <div className="text-xs mt-1 text-red-200">
                {locationError}
                <button
                  onClick={getCurrentLocation}
                  className="ml-2 underline hover:no-underline"
                >
                  Retry
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Active Shift Card (for Clock Out) */}
      {clockDirection === 'CLOCK_OUT' && activeShift && (
        <div className="bg-white rounded-xl shadow-md border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900">Active Shift</h2>
            <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">
              Currently Working
            </span>
          </div>

          <div className="grid md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <label className="text-sm text-gray-500">Position</label>
                <p className="text-lg font-medium text-gray-900">
                  {activeShift.jobTitle || 'Current Position'}
                </p>
              </div>

              <div>
                <label className="text-sm text-gray-500">Department</label>
                <p className="text-lg font-medium text-gray-900">
                  {activeShift.departmentName || 'N/A'}
                </p>
              </div>

              <div>
                <label className="text-sm text-gray-500">Clock In Time</label>
                <div className="flex items-center">
                  <LogIn className="h-4 w-4 text-gray-400 mr-2" />
                  <p className="text-lg font-medium text-gray-900">
                    {format(new Date(activeShift.clockIn), 'HH:mm:ss')}
                  </p>
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="text-sm text-gray-500">Hourly Rate</label>
                <div className="flex items-center">
                  <DollarSign className="h-4 w-4 text-gray-400 mr-1" />
                  <p className="text-lg font-medium text-gray-900">
                    {activeShift.hourlyWage ? parseFloat(activeShift.hourlyWage).toFixed(2) : '0.00'} / hour
                  </p>
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-500">Current Duration</label>
                <div className="flex items-center">
                  <Timer className="h-4 w-4 text-gray-400 mr-2" />
                  <p className="text-2xl font-bold text-primary-600 font-mono">
                    {shiftDuration}
                  </p>
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-500">Estimated Earnings</label>
                <div className="flex items-center">
                  <DollarSign className="h-4 w-4 text-green-500 mr-1" />
                  <p className="text-lg font-bold text-green-600">
                    {activeShift.hourlyWage
                      ? (parseFloat(activeShift.hourlyWage) * parseFloat(shiftDuration.split(':')[0]) +
                         parseFloat(activeShift.hourlyWage) * parseFloat(shiftDuration.split(':')[1]) / 60).toFixed(2)
                      : '0.00'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Clock Out Button */}
          <div className="mt-6">
            <button
              onClick={handleClockOut}
              disabled={isLoading || isGettingLocation}
              className="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-300 text-white py-4 px-6 rounded-lg font-semibold text-lg transition-colors disabled:cursor-not-allowed flex items-center justify-center"
            >
              {isLoading || isGettingLocation ? (
                <>
                  <Loader2 className="h-6 w-6 animate-spin mr-2" />
                  {isGettingLocation ? 'Getting Location...' : 'Clocking Out...'}
                </>
              ) : (
                <>
                  <LogOut className="h-6 w-6 mr-2" />
                  Clock Out
                </>
              )}
            </button>
          </div>
        </div>
      )}

      {/* Job Selection (for Clock In) */}
      {clockDirection === 'CLOCK_IN' && (
        <div className="bg-white rounded-xl shadow-md border border-gray-200 p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Clock In</h2>

          {jobs.length === 0 ? (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 text-center">
              <AlertCircle className="h-12 w-12 text-yellow-500 mx-auto mb-3" />
              <h3 className="text-lg font-medium text-yellow-900 mb-1">No Jobs Assigned</h3>
              <p className="text-yellow-700">
                You don't have any job positions assigned yet.
                Please contact your supervisor for job assignment.
              </p>
            </div>
          ) : (
            <>
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  Select Job Position
                </label>
                <div className="space-y-3">
                  {jobs.map((job) => (
                    <button
                      key={job.employeeJobId}
                      onClick={() => setSelectedJob(job)}
                      className={`w-full p-4 text-left border-2 rounded-lg transition-all ${
                        selectedJob?.employeeJobId === job.employeeJobId
                          ? 'border-primary-500 bg-primary-50 ring-2 ring-primary-200'
                          : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <h4 className="font-semibold text-gray-900">{job.jobTitle}</h4>
                          <p className="text-sm text-gray-500 mt-1">
                            {job.departmentName}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-lg font-bold text-gray-900">
                            ${job.hourlyWage}/hr
                          </p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              </div>

              <button
                onClick={handleClockIn}
                disabled={!selectedJob || isLoading || isGettingLocation}
                className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white py-4 px-6 rounded-lg font-semibold text-lg transition-colors disabled:cursor-not-allowed flex items-center justify-center"
              >
                {isLoading || isGettingLocation ? (
                  <>
                    <Loader2 className="h-6 w-6 animate-spin mr-2" />
                    {isGettingLocation ? 'Getting Location...' : 'Clocking In...'}
                  </>
                ) : (
                  <>
                    <LogIn className="h-6 w-6 mr-2" />
                    Clock In
                  </>
                )}
              </button>
            </>
          )}
        </div>
      )}

      {/* Unavailable State */}
      {clockDirection === 'UNAVAILABLE' && (
        <div className="bg-white rounded-xl shadow-md border border-gray-200 p-6">
          <div className="text-center py-8">
            <AlertCircle className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-medium text-gray-900 mb-2">Clock Status Unavailable</h3>
            <p className="text-gray-600">
              Unable to determine your clock status. Please contact your supervisor for assistance.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default ClockPage;