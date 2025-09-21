import apiClient from './client';

const inviteService = {
  // Admin/HR Endpoints (Protected)
  createInvite: async (inviteData) => {
    // Ensure departmentId is always provided
    if (!inviteData.departmentId) {
      throw new Error('Department is required');
    }
    const response = await apiClient.post('/invites', inviteData);
    return response.data;
  },

  getInvites: async (params = {}) => {
    const { page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = params;
    const response = await apiClient.get('/invites', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  getActiveInvites: async () => {
    const response = await apiClient.get('/invites/active');
    return response.data;
  },

  deactivateInvite: async (inviteCode) => {
    const response = await apiClient.put(`/invites/${inviteCode}/deactivate`);
    return response.data;
  },

  getInviteDetails: async (inviteCode) => {
    const response = await apiClient.get(`/invites/${inviteCode}`);
    return response.data;
  },

  // Public Endpoints
  validateInvite: async (inviteCode) => {
    const response = await apiClient.get(`/invites/validate/${inviteCode}`);
    return response.data;
  },

  registerWithInvite: async (registrationData) => {
    const response = await apiClient.post('/invites/register', registrationData);
    return response.data;
  },

  getInviteUrl: async (inviteCode) => {
    const response = await apiClient.get(`/invites/url/${inviteCode}`);
    return response.data;
  }
};

export default inviteService;