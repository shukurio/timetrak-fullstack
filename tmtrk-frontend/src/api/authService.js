import apiClient from './client';

class AuthService {
  async login(credentials) {
    const response = await apiClient.post(`/auth/login`, credentials, {
      withCredentials: true
    });
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

  async changePassword(passwordData) {
    const response = await apiClient.post('/user/change-password', passwordData);
    return response.data;
  }

  async updateProfile(profileData) {
    const response = await apiClient.put('/user/profile', profileData);
    return response.data;
  }

  async getProfile() {
    const response = await apiClient.get('/user/profile');
    return response.data;
  }

  async resetPassword(email) {
    const response = await apiClient.post(`/auth/reset-password`, { email });
    return response.data;
  }

  async refreshToken() {
    const response = await apiClient.post(`/auth/refresh`, {}, {
      withCredentials: true
    });
    return response.data;
  }
}

export default new AuthService();