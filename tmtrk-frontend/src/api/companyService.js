import axios from 'axios';
import apiClient from './client';


class CompanyService {
  async registerCompany(data) {
    const response = await apiClient.post(`auth/register/company`, data);
    return response.data;
  }

  async getCompany() {
    const response = await apiClient.get('/admin/organization/company');
    return response.data;
  }

  async getAllDepartments() {
    const response = await apiClient.get('/admin/organization/departments', {
      params: { page: 0, size: 1000 }
    });
    return response.data;
  }
}

export default new CompanyService();