import apiClient from './client';
import axios from 'axios';

class AuthService {
  async login(credentials) {
    const response = await apiClient.post(`/auth/login`, credentials, {
      withCredentials: true
    });
    return response.data;
  }

  async register(userData) {
    const response = await apiClient.post(`/auth/register`, userData);
    return response.data;
  }

  async logout() {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    }
  }

  async getCurrentUser() {
    const response = await apiClient.get('/auth/me');
    return response.data;
  }

  async getUserRole() {
    const response = await apiClient.get('/auth/me/role');
    return response.data;
  }

  async changePassword(passwordData) {
    const response = await apiClient.post('/auth/change-password', passwordData);
    return response.data;
  }

  async resetPassword(email) {
    const response = await apiClient.post(`/auth/reset-password`, { email });
    return response.data;
  }

  async refreshToken() {
    const response = await axios.post(`/auth/refresh`, {}, {
      withCredentials: true
    });
    return response.data;
  }
}

export default new AuthService();