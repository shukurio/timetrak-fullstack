import apiClient from './client';

class EmployeeService {
  // Test endpoint to verify auth is working
  async testAuth() {
    const response = await apiClient.get('/employee/shifts/test');
    return response.data;
  }

  // Shift Management
  async getMyShifts(params = {}) {
    // Fix sortBy to avoid field mapping issues
    const fixedParams = {
      ...params,
      sortBy: 'id' // Use 'id' instead of 'clockIn'
    };
    const response = await apiClient.get('/employee/shifts', { params: fixedParams });
    return response.data;
  }

  async getActiveShift() {
    const response = await apiClient.get('/employee/shifts/active');
    return response.data;
  }

  // Silent version for background checks (dashboard, etc.)
  async getActiveShiftSilent() {
    try {
      const response = await apiClient.get('/employee/shifts/active', {
        // Suppress axios error logging for this request
        validateStatus: function (status) {
          return status < 500; // Don't reject 404, only reject 5xx errors
        }
      });

      // If we got a 404, return null (no active shift)
      if (response.status === 404) {
        return null;
      }

      return response.data;
    } catch (error) {
      // Completely silent - return null if no active shift or any error
      return null;
    }
  }

  async getShiftsByStatus(status, params = {}) {
    const response = await apiClient.get(`/employee/shifts/status/${status}`, { params });
    return response.data;
  }

  async getShiftsByDateRange(startDate, endDate, params = {}) {
    const response = await apiClient.get('/employee/shifts/date-range', {
      params: { startDate, endDate, ...params }
    });
    return response.data;
  }

  async getShiftSummary(startDate, endDate) {
    const response = await apiClient.get('/employee/shifts/summary', {
      params: { startDate, endDate }
    });
    return response.data;
  }

  async getShiftsByPeriod(periodNumber, params = {}) {
    const response = await apiClient.get(`/employee/shifts/period/${periodNumber}`, { params });
    return response.data;
  }

  // Dashboard Summary
  async getDashboardSummary() {
    const response = await apiClient.get('/employee/dashboard/summary');
    return response.data;
  }

  // Payment History - Note: these endpoints might not exist yet
  async getAllPayments(params = {}) {
    // This endpoint might not exist - returning empty data for now
    return { content: [], totalElements: 0, totalPages: 0 };
  }

  async getPaymentDetails(paymentId) {
    // This endpoint might not exist - returning empty data for now
    return null;
  }

  // Period Management
  async getAvailablePaymentPeriods(numberOfPeriods = 12) {
    const response = await apiClient.get('/periods/available', {
      params: { numberOfPeriods }
    });
    return response.data;
  }

  async getCurrentPaymentPeriod() {
    const response = await apiClient.get('/periods/current');
    return response.data;
  }

  // Clock Management
  async determineClockAction(employeeId) {
    const response = await apiClient.get(`/employee/clock/determineAction/${employeeId}`);
    return response.data;
  }

  async getEmployeeClockJobs() {
    const response = await apiClient.get('/employee/clock/jobs');
    return response.data;
  }

  async clockIn(request) {
    const response = await apiClient.post('/employee/clock/clockIn', request);
    return response.data;
  }

  async clockOut(request) {
    const response = await apiClient.post('/employee/clock/clockOut', request);
    return response.data;
  }
}

export default new EmployeeService();