import apiClient from './client';

class AdminService {
  // Company Management
  async getCompany() {
    const response = await apiClient.get('/admin/organization/company');
    return response.data;
  }

  async updateCompany(data) {
    const response = await apiClient.patch('/admin/organization/company', data);
    return response.data;
  }

  // Department Management
  async getDepartments(params = {}) {
    const response = await apiClient.get('/admin/organization/departments', { params });
    return response.data;
  }

  async getDepartmentById(id) {
    const response = await apiClient.get(`/admin/organization/departments/${id}`);
    return response.data;
  }

  async createDepartment(data) {
    const response = await apiClient.post('/admin/organization/departments', data);
    return response.data;
  }

  async updateDepartment(id, data) {
    const response = await apiClient.put(`/admin/organization/departments/${id}`, data);
    return response.data;
  }

  async deleteDepartment(id) {
    const response = await apiClient.delete(`/admin/organization/departments/${id}`);
    return response.data;
  }

  // Employee Management
  async getEmployees(params = {}) {
    const response = await apiClient.get('/admin/employees/all', { params });
    return response.data;
  }

  async getActiveEmployees(params = {}) {
    const response = await apiClient.get('/admin/employees/active', { params });
    return response.data;
  }

  async getEmployeeById(id) {
    const response = await apiClient.get(`/admin/employees/${id}`);
    return response.data;
  }

  async getEmployeesByStatus(status, params = {}) {
    const response = await apiClient.get(`/admin/employees/status/${status}`, { params });
    return response.data;
  }

  async getEmployeesByDepartment(departmentId, params = {}) {
    const response = await apiClient.get(`/admin/employees/department/${departmentId}`, { params });
    return response.data;
  }

  async searchEmployees(query, params = {}) {
    const response = await apiClient.get('/admin/employees/search', { 
      params: { query, ...params } 
    });
    return response.data;
  }

  async registerEmployee(data) {
    const response = await apiClient.post('/admin/employees/register', data);
    return response.data;
  }

  async updateEmployee(id, data) {
    const response = await apiClient.put(`/admin/employees/${id}`, data);
    return response.data;
  }

  async approveEmployee(id) {
    const response = await apiClient.put(`/admin/employees/${id}/approve`);
    return response.data;
  }

  async rejectEmployee(id) {
    const response = await apiClient.put(`/admin/employees/${id}/reject`);
    return response.data;
  }

  async activateEmployee(id) {
    const response = await apiClient.put(`/admin/employees/${id}/activate`);
    return response.data;
  }

  async deactivateEmployee(id) {
    const response = await apiClient.put(`/admin/employees/${id}/deactivate`);
    return response.data;
  }

  async deleteEmployee(id) {
    const response = await apiClient.delete(`/admin/employees/delete/${id}`);
    return response.data;
  }

  // Job Management
  async getJobs() {
    const response = await apiClient.get('/admin/jobs');
    return response.data;
  }

  async getJobsPaged(params = {}) {
    const response = await apiClient.get('/admin/jobs/paged', { params });
    return response.data;
  }

  async getJobById(id) {
    const response = await apiClient.get(`/admin/jobs/${id}`);
    return response.data;
  }

  async getJobsByDepartment(departmentId) {
    const response = await apiClient.get(`/admin/jobs/department/${departmentId}`);
    return response.data;
  }

  async searchJobs(query) {
    const response = await apiClient.get('/admin/jobs/search', { params: { query } });
    return response.data;
  }

  async createJob(data) {
    const response = await apiClient.post('/admin/jobs/create', data);
    return response.data;
  }

  async updateJob(id, data) {
    const response = await apiClient.put(`/admin/jobs/update/${id}`, data);
    return response.data;
  }

  async deleteJob(id) {
    const response = await apiClient.delete(`/admin/jobs/delete/${id}`);
    return response.data;
  }

  // Employee-Job Assignment
  async getEmployeeJobs() {
    const response = await apiClient.get('/admin/employee-jobs');
    return response.data;
  }

  async getEmployeeJobsByEmployee(employeeId) {
    const response = await apiClient.get(`/admin/employee-jobs/employee/${employeeId}`);
    return response.data;
  }

  async getEmployeeJobsByDepartment(departmentId) {
    const response = await apiClient.get(`/admin/employee-jobs/department/${departmentId}`);
    return response.data;
  }

  async assignJobToEmployees(data) {
    const response = await apiClient.post('/admin/employee-jobs/assign', data);
    return response.data;
  }

  async assignIndividualJob(employeeId, jobId, hourlyWage = null) {
    const params = { employeeId, jobId };
    if (hourlyWage) params.hourlyWage = hourlyWage;
    
    const response = await apiClient.post('/admin/employee-jobs/individual', null, { params });
    return response.data;
  }

  async updateEmployeeJob(employeeJobId, data) {
    const response = await apiClient.put(`/admin/employee-jobs/${employeeJobId}`, data);
    return response.data;
  }

  async removeJobFromEmployees(employeeIds, jobId) {
    const response = await apiClient.delete('/admin/employee-jobs/remove', {
      params: { employeeIds, jobId }
    });
    return response.data;
  }

  // Shift Management
  async getShifts(params = {}) {
    const response = await apiClient.get('/admin/shifts/status/COMPLETED', { params });
    return response.data;
  }

  async getShiftsByStatus(status, params = {}) {
    const response = await apiClient.get(`/admin/shifts/status/${status}`, { params });
    return response.data;
  }

  async getShiftsByPeriodNumber(periodNumber, params = {}) {
    const response = await apiClient.get(`/admin/shifts/periodNumber/${periodNumber}`, { params });
    return response.data;
  }

  async getShiftsByPeriodAndStatus(periodNumber, status, params = {}) {
    const response = await apiClient.get(`/admin/shifts/period/${periodNumber}/status/${status}`, { params });
    return response.data;
  }

  async getShiftsByEmployee(employeeId, params = {}) {
    const response = await apiClient.get(`/admin/shifts/employee/${employeeId}`, { params });
    return response.data;
  }

  async getShiftsByDepartment(departmentId, params = {}) {
    const response = await apiClient.get(`/admin/shifts/department/${departmentId}`, { params });
    return response.data;
  }

  async getThisWeekShifts(params = {}) {
    const response = await apiClient.get('/admin/shifts/this-week', { params });
    return response.data;
  }

  async getThisMonthShifts(params = {}) {
    const response = await apiClient.get('/admin/shifts/this-month', { params });
    return response.data;
  }

  async createShift(data) {
    const response = await apiClient.post('/admin/shifts/', data);
    return response.data;
  }

  async updateShift(id, data) {
    const response = await apiClient.put(`/admin/shifts/${id}`, data);
    return response.data;
  }

  async deleteShift(id) {
    const response = await apiClient.delete(`/admin/shifts/${id}`);
    return response.data;
  }

  async clockIn(data) {
    const response = await apiClient.post('/admin/shifts/clock-in', data);
    return response.data;
  }

  async clockOut(data) {
    const response = await apiClient.post('/admin/shifts/clock-out', data);
    return response.data;
  }

  // Payment Management
  async getAllPayments(params = {}) {
    const response = await apiClient.get('/admin/payments/all', { params });
    return response.data;
  }

  async getPaymentById(paymentId) {
    const response = await apiClient.get(`/admin/payments/${paymentId}`);
    return response.data;
  }

  async getPaymentsByStatus(status, params = {}) {
    const response = await apiClient.get(`/admin/payments/status/${status}`, { params });
    return response.data;
  }

  async getPaymentsByEmployee(employeeId, params = {}) {
    const response = await apiClient.get(`/admin/payments/employee/${employeeId}`, { params });
    return response.data;
  }

  async calculatePaymentsForPeriod(data) {
    const response = await apiClient.post('/admin/payments/calculate-period', data);
    return response.data;
  }

  async updatePaymentStatus(data) {
    const response = await apiClient.post('/admin/payments/updateStatus', data);
    return response.data;
  }

  async triggerAutomaticPayments() {
    const response = await apiClient.post('/admin/payments/trigger-automatic');
    return response.data;
  }

  async exportPayments(periodNumber) {
    const response = await apiClient.get('/admin/payments/download', {
      params: { periodNumber },
      responseType: 'blob'
    });
    return response.data;
  }

  // Payment Period Management - Updated to new API structure
  async getCurrentPaymentPeriod() {
    const response = await apiClient.get('/periods/current');
    return response.data;
  }

  async getAvailablePaymentPeriods(numberOfPeriods = 12) {
    const response = await apiClient.get('/periods/available', {
      params: { numberOfPeriods }
    });
    return response.data;
  }

  async getMostRecentCompletedPeriod() {
    const response = await apiClient.get('/periods/most-recent-completed');
    return response.data;
  }


  async getPaymentsByPeriodNumber(periodNumber, params = { page: 0, size: 20 }) {
    const response = await apiClient.get(`/admin/payments/period/${periodNumber}`, {
      params
    });
    return response.data;
  }

  async getPaymentById(paymentId) {
    const response = await apiClient.get(`/admin/payments/${paymentId}`);
    return response.data;
  }

  async getPaymentsByEmployee(employeeId, params = { page: 0, size: 20 }) {
    const response = await apiClient.get(`/admin/payments/employee/${employeeId}`, {
      params
    });
    return response.data;
  }

  async getEmployeePaymentDetails(employeeId, paymentId) {
    const response = await apiClient.get(`/admin/payments/employee/${employeeId}/payments/${paymentId}`);
    return response.data;
  }

  async exportPaymentsByPeriod(periodNumber) {
    const response = await apiClient.get(`/admin/payments/export/period/${periodNumber}`, {
      responseType: 'blob'
    });
    return response.data;
  }

  // Company Payment Settings Management
  async getCompanyPaymentSettings() {
    const response = await apiClient.get('/admin/company/payment-settings');
    return response.data;
  }

  async createCompanyPaymentSettings(data) {
    // Note: companyId will be automatically set by backend from authentication context
    const response = await apiClient.post('/admin/company/payment-settings', data);
    return response.data;
  }

  async updateCompanyPaymentSettings(data) {
    // Note: companyId will be automatically set by backend from authentication context
    const response = await apiClient.put('/admin/company/payment-settings', data);
    return response.data;
  }

  async checkPaymentSettingsExists() {
    const response = await apiClient.get('/admin/company/payment-settings/exists');
    return response.data;
  }

  async initializePaymentSettings() {
    const response = await apiClient.post('/admin/company/payment-settings/initialize');
    return response.data;
  }

  // Reports
  async exportCompanyShifts(periodNumber = null) {
    const params = {};
    if (periodNumber) params.periodNumber = periodNumber;
    
    const response = await apiClient.get('/admin/reports/shifts/company', {
      params,
      responseType: 'blob'
    });
    return response.data;
  }

  async exportDepartmentShifts(departmentIds, periodNumber = null) {
    const params = { departmentIds };
    if (periodNumber) params.periodNumber = periodNumber;
    
    const response = await apiClient.get('/admin/reports/shifts/departments', {
      params,
      responseType: 'blob'
    });
    return response.data;
  }
}

export default new AdminService();