import apiClient from './client';

const inviteService = {
  // Admin/HR Endpoints (Protected)
  createInvite: async (inviteData) => {
    // Ensure departmentId is always provided
    if (!inviteData.departmentId) {
      throw new Error('Department is required');
    }
    const response = await apiClient.post('/admin/invites', inviteData);
    return response.data;
  },

  getInvites: async (params = {}) => {
    const { page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = params;
    const response = await apiClient.get('/admin/invites', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  getActiveInvites: async () => {
    const response = await apiClient.get('/admin/invites/active');
    return response.data;
  },

  deactivateInvite: async (inviteCode) => {
    const response = await apiClient.put(`/admin/invites/${inviteCode}/deactivate`);
    return response.data;
  },

  getInviteDetails: async (inviteCode) => {
    const response = await apiClient.get(`/admin/invites/${inviteCode}`);
    return response.data;
  },

  // Public Endpoints
  validateInvite: async (inviteCode) => {
    const response = await apiClient.get(`/user/invites/validate/${inviteCode}`);
    return response.data;
  },

  registerWithInvite: async (registrationData) => {
    const response = await apiClient.post('/user/invites/register', registrationData);
    return response.data;
  },

  getInviteUrl: async (inviteCode) => {
    const response = await apiClient.get(`/user/invites/url/${inviteCode}`);
    return response.data;
  },

  // Utility functions for sharing and QR codes
  generateInviteUrl: (inviteCode) => {
    return `${window.location.origin}/register?invite=${inviteCode}`;
  },

  copyToClipboard: async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch (error) {
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();

      try {
        document.execCommand('copy');
        document.body.removeChild(textArea);
        return true;
      } catch (error) {
        document.body.removeChild(textArea);
        throw error;
      }
    }
  },

  generateWhatsAppUrl: (inviteCode, companyName = 'our company') => {
    const inviteUrl = inviteService.generateInviteUrl(inviteCode);
    const message = `🎉 You're invited to join ${companyName}!\n\nClick this link to complete your registration: ${inviteUrl}\n\nWelcome aboard!`;
    return `https://wa.me/?text=${encodeURIComponent(message)}`;
  },

  generateEmailShare: (inviteCode, companyName = 'our company') => {
    const inviteUrl = inviteService.generateInviteUrl(inviteCode);
    return {
      subject: `Welcome to ${companyName} - Complete Your Registration`,
      body: `Hello!\n\nYou've been invited to join ${companyName}. Please click the link below to complete your registration:\n\n${inviteUrl}\n\nWe look forward to having you on our team!\n\nBest regards,\nThe ${companyName} Team`
    };
  },

  generateQRCode: async (text, size = 200) => {
    try {
      return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}`;
    } catch (error) {
      throw error;
    }
  },

  // Utility functions for invite statistics and status
  getInviteStats: (invites = []) => {
    const total = invites.length;
    const active = invites.filter(invite => invite.isActive).length;
    const used = invites.filter(invite => invite.currentUses > 0).length;
    const expired = invites.filter(invite =>
      invite.expiryDate && new Date(invite.expiryDate) < new Date()
    ).length;

    return {
      total,
      active,
      used,
      expired,
      available: active - used
    };
  },

  getUsagePercentage: (invite) => {
    if (!invite.maxUses || invite.maxUses === 0) {
      return 0;
    }
    return Math.round((invite.currentUses / invite.maxUses) * 100);
  },

  getInviteStatus: (invite) => {
    if (!invite.isActive) {
      return 'inactive';
    }
    if (invite.expiryDate && new Date(invite.expiryDate) < new Date()) {
      return 'expired';
    }
    if (invite.maxUses && invite.currentUses >= invite.maxUses) {
      return 'exhausted';
    }
    return 'active';
  }
};

export default inviteService;