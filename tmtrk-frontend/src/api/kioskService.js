import axios from 'axios';
import apiClient from './client';


class KioskService {
  async getEmployeeByUsername(username) {
    const response = await apiClient.get(`/kiosk/${username}`);
    return response.data;
  }

  async getEmployeeJobs(username) {
    const response = await apiClient.get(`/kiosk/jobs/${username}`);
    return response.data;
  }

  async determineAction(employeeId) {
    const response = await apiClient.get(`/kiosk/determineAction/${employeeId}`);
    return response.data;
  }

  async clockIn(data) {
    const response = await apiClient.post(`/kiosk/clock-in`, data);
    return response.data;
  }

  async clockOut(data) {
    const response = await apiClient.post(`/kiosk/clock-out`, data);
    return response.data;
  }
}

export default new KioskService();