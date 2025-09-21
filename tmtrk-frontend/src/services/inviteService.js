import apiClient from '../api/client';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8093/api';

class InviteService {
  // Public Endpoints (No Auth Required)
  
  // Validate invite code
  async validateInvite(inviteCode) {
    try {
      const response = await axios.get(`${API_BASE_URL}/user/invites/validate/${inviteCode}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Register with invite code
  async registerWithInvite(data) {
    try {
      const response = await axios.post(`${API_BASE_URL}/user/invites/register`, data);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Get invite URL (public endpoint)
  async getInviteUrl(inviteCode) {
    try {
      const response = await axios.get(`${API_BASE_URL}/user/invites/url/${inviteCode}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Admin Endpoints (Requires Auth)
  
  // Create new multi-user invite
  async createInvite(inviteData) {
    try {
      const response = await apiClient.post('/admin/invites', inviteData);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Get all invites (with pagination)
  async getInvites(params = {}) {
    try {
      const response = await apiClient.get('/admin/invites', { params });
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Get active invites only
  async getActiveInvites() {
    try {
      const response = await apiClient.get('/admin/invites/active');
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Get specific invite details
  async getInviteDetails(inviteCode) {
    try {
      const response = await apiClient.get(`/admin/invites/${inviteCode}`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Deactivate invite
  async deactivateInvite(inviteCode) {
    try {
      const response = await apiClient.put(`/admin/invites/${inviteCode}/deactivate`);
      return response.data;
    } catch (error) {
      throw error;
    }
  }

  // Utility functions for sharing and QR codes
  
  // Generate shareable invite URL
  generateInviteUrl(inviteCode) {
    return `${window.location.origin}/register?invite=${inviteCode}`;
  }

  // Copy text to clipboard
  async copyToClipboard(text) {
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
  }

  // Generate WhatsApp sharing URL
  generateWhatsAppUrl(inviteCode, companyName = 'our company') {
    const inviteUrl = this.generateInviteUrl(inviteCode);
    const message = `🎉 You're invited to join ${companyName}!\n\nClick this link to complete your registration: ${inviteUrl}\n\nWelcome aboard!`;
    return `https://wa.me/?text=${encodeURIComponent(message)}`;
  }

  // Generate Email sharing data
  generateEmailShare(inviteCode, companyName = 'our company') {
    const inviteUrl = this.generateInviteUrl(inviteCode);
    return {
      subject: `Welcome to ${companyName} - Complete Your Registration`,
      body: `Hello!\n\nYou've been invited to join ${companyName}. Please click the link below to complete your registration:\n\n${inviteUrl}\n\nWe look forward to having you on our team!\n\nBest regards,\nThe ${companyName} Team`
    };
  }

  // Generate QR code data URL using a QR code library
  async generateQRCode(text, size = 200) {
    try {
      // This would require a QR code library like 'qrcode'
      // For now, return a placeholder that can be implemented with the library
      return `https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodeURIComponent(text)}`;
    } catch (error) {
      throw error;
    }
  }

  // Get invite statistics
  getInviteStats(invites) {
    const stats = {
      total: invites.length,
      active: 0,
      expired: 0,
      full: 0,
      totalRegistrations: 0,
      expiringSoon: 0
    };

    const now = new Date();
    const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);

    invites.forEach(invite => {
      stats.totalRegistrations += invite.currentUses || 0;
      
      if (!invite.isActive) {
        stats.expired++;
      } else if (invite.currentUses >= invite.maxUses) {
        stats.full++;
      } else if (new Date(invite.expiresAt) < tomorrow) {
        stats.expiringSoon++;
        stats.active++;
      } else {
        stats.active++;
      }
    });

    return stats;
  }

  // Get invite status
  getInviteStatus(invite) {
    if (!invite.isActive) return 'deactivated';
    if (new Date(invite.expiresAt) < new Date()) return 'expired';
    if (invite.currentUses >= invite.maxUses) return 'full';
    return 'active';
  }

  // Get status color for UI
  getStatusColor(status) {
    switch (status) {
      case 'active': return 'green';
      case 'full': return 'blue';
      case 'expired': return 'yellow';
      case 'deactivated': return 'gray';
      default: return 'gray';
    }
  }

  // Calculate usage percentage
  getUsagePercentage(invite) {
    return Math.min(100, Math.round((invite.currentUses / invite.maxUses) * 100));
  }
}

export default new InviteService();