import apiClient from './client';

class EmployeeService {

  // Silent version for background checks (dashboard, etc.)
  async getActiveShiftSilent() {
    try {
      // Try to get active shifts from the regular shifts endpoint
      const response = await apiClient.get('/employee/shifts', {
        params: {
          status: 'ACTIVE',
          size: 1,
          page: 0
        }
      });

      // If we have active shifts, return the first one
      if (response.data && response.data.content && response.data.content.length > 0) {
        return response.data.content[0];
      }

      return null;
    } catch (error) {
      // Completely silent - return null if no active shift or any error
      return null;
    }
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

  // Payment History
  async getAllPayments(params = {}) {
    try {
      const response = await apiClient.get('/employee/payments', { params });
      return response.data;
    } catch (error) {
      console.error('Error fetching payments:', error);
      // Return empty data if endpoint doesn't exist
      return { content: [], totalElements: 0, totalPages: 0 };
    }
  }

  async getPaymentDetails(paymentId) {
    try {
      const response = await apiClient.get(`/employee/payments/${paymentId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching payment details:', error);
      return null;
    }
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