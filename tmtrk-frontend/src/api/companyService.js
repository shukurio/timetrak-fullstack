import axios from 'axios';
import apiClient from './client';


class CompanyService {
  async registerCompany(data) {
    const response = await apiClient.post(`auth/companies/register`, data);
    return response.data;
  }

  async getCompany() {
    const response = await apiClient.get('/admin/organization/company');
    return response.data;
  }

  async getDepartments(params = {}) {
    const { page = 0, size = 100 } = params;
    const response = await apiClient.get('/admin/organization/departments', {
      params: { page, size }
    });
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