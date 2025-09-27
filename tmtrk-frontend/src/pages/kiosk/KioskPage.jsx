import { useState, useEffect } from 'react';
import { Clock, User, LogIn, LogOut, Search, CheckCircle, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import kioskService from '../../api/kioskService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import TimeTrakIcon from '../../components/common/TimeTrakIcon';

const KioskPage = () => {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [username, setUsername] = useState('');
  const [employee, setEmployee] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [action, setAction] = useState(null); // 'CLOCK_IN' or 'CLOCK_OUT'
  const [isLoading, setIsLoading] = useState(false);
  const [isSearching, setIsSearching] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [successData, setSuccessData] = useState(null);

  // Update clock every second
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  // Auto-hide success message after 7 seconds
  useEffect(() => {
    if (showSuccess) {
      const timer = setTimeout(() => {
        resetForm();
      }, 7000);
      return () => clearTimeout(timer);
    }
  }, [showSuccess]);

  const resetForm = () => {
    setUsername('');
    setEmployee(null);
    setJobs([]);
    setSelectedJob(null);
    setAction(null);
    setShowSuccess(false);
    setSuccessData(null);
  };

  const searchEmployee = async () => {
    if (!username.trim()) {
      toast.error('Please enter a username');
      return;
    }

    setIsSearching(true);
    try {
      const employeeData = await kioskService.getEmployeeByUsername(username.trim());
      const actionType = await kioskService.determineAction(employeeData.id);

      setEmployee(employeeData);
      setAction(actionType);

      // Show error message for UNAVAILABLE status  
      if (actionType === 'UNAVAILABLE') {
        toast.error('Cannot determine clock action. Please contact your supervisor.');
        setJobs([]);
        setSelectedJob(null);
        return;
      }

      // Only fetch jobs if action is CLOCK_IN
      if (actionType === 'CLOCK_IN') {
        const employeeJobs = await kioskService.getEmployeeJobs(username.trim());
        setJobs(employeeJobs);
        
        // Auto-select job if only one available
        if (employeeJobs.length === 1) {
          setSelectedJob(employeeJobs[0]);
        }
      } else if (actionType === 'CLOCK_OUT') {
        // For CLOCK_OUT, no job selection needed
        setJobs([]);
        setSelectedJob(null);
      }

      toast.success(`Found employee: ${employeeData.firstName} ${employeeData.lastName}`);
    } catch (error) {
      console.error('Search error:', error);
      toast.error(error.response?.data?.message || 'Employee not found');
      setEmployee(null);
      setJobs([]);
      setAction(null);
    } finally {
      setIsSearching(false);
    }
  };

  const getLocation = () => {
    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by this browser'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          });
        },
        (error) => {
          console.error('Geolocation error:', error);
          // Use default coordinates if location access is denied
          resolve({
            latitude: 0.0,
            longitude: 0.0
          });
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000 // 5 minutes
        }
      );
    });
  };

  const handleClockAction = async () => {
    // Prevent action if UNAVAILABLE
    if (action === 'UNAVAILABLE') {
      toast.error('Cannot perform clock action. Please contact your supervisor.');
      return;
    }

    if (action === 'CLOCK_IN' && (!employee || !selectedJob)) {
      toast.error('Please select a job');
      return;
    }

    if (action === 'CLOCK_OUT' && !employee) {
      toast.error('Employee data missing');
      return;
    }

    setIsLoading(true);
    try {
      // Get location first
      const location = await getLocation();
      let response;

      if (action === 'CLOCK_IN') {
        // For clock in: use employeeJobId
        const clockData = {
          id: selectedJob.employeeJobId,
          latitude: location.latitude,
          longitude: location.longitude
        };
        response = await kioskService.clockIn(clockData);
        toast.success('Clocked in successfully!');
      } else {
        // For clock out: use employeeId
        const clockData = {
          id: employee.id,
          latitude: location.latitude,
          longitude: location.longitude
        };
        response = await kioskService.clockOut(clockData);
        toast.success('Clocked out successfully!');
      }

      // Show success screen with details
      setSuccessData({
        employee,
        job: selectedJob, // Will be null for clock out, that's fine
        action,
        time: new Date(),
        shiftData: response
      });
      setShowSuccess(true);

    } catch (error) {
      console.error('Clock action error:', error);
      console.error('Error details:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      });

      // Better error messages
      let errorMessage = 'An error occurred. Please try again.';
      if (error.response?.status === 500) {
        errorMessage = 'Server error. Please contact administrator.';
      } else if (error.response?.status === 404) {
        errorMessage = 'Employee or job not found.';
      } else if (error.response?.status === 400) {
        errorMessage = error.response?.data?.message || 'Invalid request. Please try again.';
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      }

      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (showSuccess && successData) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center p-4">
        <div className="bg-slate-700 rounded-3xl shadow-2xl p-8 max-w-md w-full text-center animate-scale-up relative">
          {/* Status Badge */}
          <div className="absolute top-6 right-6">
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${
              successData.action === 'CLOCK_IN' 
                ? 'bg-green-500 text-white' 
                : 'bg-blue-500 text-white'
            }`}>
              {successData.action === 'CLOCK_IN' ? 'Clocked in' : 'Clocked out'}
            </span>
          </div>

          {/* App Icon */}
          <div className="mb-6">
            <div className="w-16 h-16 flex items-center justify-center mx-auto">
              <TimeTrakIcon width={64} height={64} />
            </div>
          </div>

          {/* Welcome Message */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-white mb-2">
              {successData.action === 'CLOCK_IN' ? 'Welcome Back!' : 'Great Work!'}
            </h1>
            <p className="text-slate-300">
              {successData.action === 'CLOCK_IN' 
                ? 'Ready to start your productive day' 
                : 'You\'ve completed your shift'
              }
            </p>
          </div>

          {/* Employee Info */}
          <div className="bg-slate-800 rounded-2xl p-6 mb-6">
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div>
                <p className="text-slate-400 text-sm uppercase tracking-wide">Employee</p>
                <p className="text-white font-semibold">
                  {successData.employee.firstName} {successData.employee.lastName}
                </p>
              </div>
              <div>
                <p className="text-slate-400 text-sm uppercase tracking-wide">Position</p>
                <p className="text-white font-semibold">
                  {successData.job?.jobTitle || successData.shiftData?.jobTitle || 'N/A'}
                </p>
              </div>
            </div>

            {/* Clock In Time */}
            {successData.action === 'CLOCK_IN' && (
              <div className="border-l-4 border-blue-500 bg-slate-900 rounded-lg p-4 mb-4">
                <p className="text-slate-400 text-sm uppercase tracking-wide mb-1">Clock In Time</p>
                <p className="text-blue-400 text-3xl font-mono font-bold">
                  {format(successData.time, 'HH:mm:ss')}
                </p>
              </div>
            )}

            {/* Clock Out Information */}
            {successData.action === 'CLOCK_OUT' && successData.shiftData && (
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-900 rounded-lg p-3">
                  <p className="text-slate-400 text-sm uppercase tracking-wide">Hourly Rate</p>
                  <p className="text-white font-bold">
                    ${successData.shiftData.hourlyWage ? parseFloat(successData.shiftData.hourlyWage).toFixed(2) : '0.00'}
                  </p>
                </div>
                <div className="bg-slate-900 rounded-lg p-3">
                  <p className="text-slate-400 text-sm uppercase tracking-wide">Session Earnings</p>
                  <p className="text-green-400 font-bold">
                    ${successData.shiftData.shiftEarnings ? parseFloat(successData.shiftData.shiftEarnings).toFixed(2) : '0.00'}
                  </p>
                </div>
                <div className="bg-slate-900 rounded-lg p-3">
                  <p className="text-slate-400 text-sm uppercase tracking-wide">Clock In Time</p>
                  <p className="text-white font-mono">
                    {successData.shiftData.clockIn ? format(new Date(successData.shiftData.clockIn), 'HH:mm:ss') : '00:00:00'}
                  </p>
                </div>
                <div className="bg-slate-900 rounded-lg p-3">
                  <p className="text-slate-400 text-sm uppercase tracking-wide">Clock Out Time</p>
                  <p className="text-white font-mono">
                    {successData.shiftData.clockOut ? format(new Date(successData.shiftData.clockOut), 'HH:mm:ss') : format(successData.time, 'HH:mm:ss')}
                  </p>
                </div>
                <div className="bg-slate-900 rounded-lg p-3 col-span-2">
                  <p className="text-slate-400 text-sm uppercase tracking-wide">Shift Duration</p>
                  <p className="text-white font-bold text-lg">
                    {successData.shiftData.hours ? `${successData.shiftData.hours.toFixed(1)} hours` : '0.0 hours'}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Action Button */}
          <button
            onClick={resetForm}
            className="w-full bg-blue-500 hover:bg-blue-600 text-white py-4 rounded-2xl font-semibold text-lg transition-colors mb-4"
          >
            Clock Another Employee
          </button>

          <div className="text-slate-400 text-sm">
            This message will disappear in 7 seconds...
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center p-4">
      <div className="bg-slate-700 rounded-3xl shadow-2xl p-8 max-w-4xl w-full">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="flex items-center justify-center mb-6">
            <div className="w-12 h-12 flex items-center justify-center mr-4">
              <TimeTrakIcon width={48} height={48} />
            </div>
            <h1 className="text-4xl font-bold text-white">TimeTrak Kiosk</h1>
          </div>
          <div className="text-6xl font-bold text-blue-400 mb-2 font-mono">
            {format(currentTime, 'HH:mm:ss')}
          </div>
          <div className="text-xl text-slate-300">
            {format(currentTime, 'EEEE, MMMM d, yyyy')}
          </div>
        </div>

        {!employee ? (
          /* Employee Search */
          <div className="max-w-md mx-auto">
            <div className="mb-6">
              <label className="block text-lg font-medium text-white mb-3">
                Enter Username
              </label>
              <div className="relative">
                <User className="absolute left-4 top-1/2 transform -translate-y-1/2 h-6 w-6 text-slate-400" />
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && searchEmployee()}
                  className="w-full pl-12 pr-4 py-4 text-xl bg-slate-800 border-2 border-slate-600 rounded-xl focus:outline-none focus:border-blue-500 transition-colors text-white placeholder-slate-400"
                  placeholder="Username"
                  disabled={isSearching}
                />
              </div>
            </div>
            
            <button
              onClick={searchEmployee}
              disabled={isSearching}
              className="w-full bg-blue-500 text-white py-4 px-6 text-xl font-semibold rounded-xl hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
            >
              {isSearching ? (
                <LoadingSpinner size="sm" text="" />
              ) : (
                <>
                  <Search className="mr-3 h-6 w-6" />
                  Find Employee
                </>
              )}
            </button>
          </div>
        ) : (
          /* Clock In/Out Form */
          <div className="max-w-2xl mx-auto">
            {/* Employee Info */}
            <div className="bg-slate-800 rounded-2xl p-6 mb-8 text-center">
              <h2 className="text-2xl font-bold text-white mb-2">
                {employee.firstName} {employee.lastName}
              </h2>
              <p className="text-slate-400 text-sm">
                @{employee.username}
              </p>
            </div>

            {/* Job Selection - Only for CLOCK_IN */}
            {action === 'CLOCK_IN' && jobs.length === 0 && (
              <div className="mb-6">
                <div className="p-6 bg-yellow-900/20 border border-yellow-500 rounded-xl text-center">
                  <div className="flex items-center justify-center text-yellow-400 mb-3">
                    <AlertCircle className="h-8 w-8 mr-2" />
                    <span className="text-xl font-medium">No Jobs Assigned</span>
                  </div>
                  <p className="text-yellow-300 text-lg">
                    You don't have any job assignments yet.
                  </p>
                  <p className="text-yellow-400 text-sm mt-2">
                    Please contact your supervisor to get assigned to a job position.
                  </p>
                </div>
              </div>
            )}

            {action === 'CLOCK_IN' && jobs.length > 1 && (
              <div className="mb-6">
                <label className="block text-lg font-medium text-white mb-3">
                  Select Job Position
                </label>
                <div className="grid gap-3">
                  {jobs.map((job) => (
                    <button
                      key={job.employeeJobId}
                      onClick={() => setSelectedJob(job)}
                      className={`p-4 text-left border-2 rounded-xl transition-colors ${
                        selectedJob?.employeeJobId === job.employeeJobId
                          ? 'border-blue-500 bg-blue-900/20'
                          : 'border-slate-600 bg-slate-800 hover:border-slate-500'
                      }`}
                    >
                      <div className="font-semibold text-white">{job.jobTitle}</div>
                      <div className="text-sm text-slate-400">
                        ${job.hourlyWage}/hour
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Clock Out Info */}
            {action === 'CLOCK_OUT' && (
              <div className="mb-6 p-4 bg-red-900/20 border border-red-500 rounded-xl">
                <div className="flex items-center text-red-400">
                  <LogOut className="h-5 w-5 mr-2" />
                  <span className="font-medium">Ready to clock out</span>
                </div>
                <p className="text-sm text-red-300 mt-1">
                  You will be clocked out from your current active shift.
                </p>
              </div>
            )}


            {/* Action Buttons */}
            <div className="flex gap-4">
              <button
                onClick={resetForm}
                className="flex-1 bg-slate-600 text-white py-4 px-6 text-xl font-semibold rounded-xl hover:bg-slate-500 transition-colors"
              >
                Cancel
              </button>
              
              {action === 'UNAVAILABLE' ? (
                <div className="flex-2 py-4 px-6 text-xl font-semibold rounded-xl bg-slate-600 text-slate-300 flex items-center justify-center">
                  <AlertCircle className="mr-3 h-6 w-6" />
                  Action Unavailable
                </div>
              ) : (
                <button
                  onClick={handleClockAction}
                  disabled={(action === 'CLOCK_IN' && (!selectedJob || jobs.length === 0)) || isLoading}
                  className={`flex-2 py-4 px-6 text-xl font-semibold rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center ${
                    action === 'CLOCK_IN'
                      ? 'bg-green-600 text-white hover:bg-green-500'
                      : 'bg-red-600 text-white hover:bg-red-500'
                  }`}
                >
                  {isLoading ? (
                    <LoadingSpinner size="sm" text="" />
                  ) : (
                    <>
                      {action === 'CLOCK_IN' ? (
                        <LogIn className="mr-3 h-6 w-6" />
                      ) : (
                        <LogOut className="mr-3 h-6 w-6" />
                      )}
                      {action === 'CLOCK_IN' ? 'Clock In' : 'Clock Out'}
                    </>
                  )}
                </button>
              )}
            </div>

            {/* Action Status */}
            {action && action !== 'UNAVAILABLE' && (
              <div className="mt-4 p-3 rounded-lg bg-blue-900/20 border border-blue-500">
                <div className="flex items-center text-blue-400">
                  <AlertCircle className="h-5 w-5 mr-2" />
                  <span className="font-medium">
                    Ready to {action === 'CLOCK_IN' ? 'clock in' : 'clock out'}
                  </span>
                </div>
              </div>
            )}
            
            {action === 'UNAVAILABLE' && (
              <div className="mt-4 p-3 rounded-lg bg-yellow-900/20 border border-yellow-500">
                <div className="flex items-center text-yellow-400">
                  <AlertCircle className="h-5 w-5 mr-2" />
                  <span className="font-medium">
                    Cannot determine clock action. Please contact supervisor.
                  </span>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default KioskPage;