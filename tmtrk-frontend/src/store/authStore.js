import { create } from 'zustand';

// In-memory token storage (no localStorage!)
let accessToken = null;
let tokenExpiresAt = null;

const useAuthStore = create((set, get) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  role: null,
  departments: [], // For managers - their assigned departments

  // Token management in memory only
  setTokens: (access, expiresIn) => {
    accessToken = access;
    tokenExpiresAt = Date.now() + (expiresIn * 1000);
  },

  getAccessToken: () => {
    if (!accessToken || Date.now() >= tokenExpiresAt) {
      return null;
    }
    return accessToken;
  },

  clearTokens: () => {
    accessToken = null;
    tokenExpiresAt = null;
  },

  // Auth actions
  login: (userData, access, expiresIn) => {
    get().setTokens(access, expiresIn);
    
    set({
      user: userData.user,
      isAuthenticated: true,
      isLoading: false,
      role: userData.user.role,
      departments: userData.departments || []
    });
  },

  logout: () => {
    get().clearTokens();
    
    set({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      role: null,
      departments: []
    });
  },

  updateUser: (userData) => {
    set({ user: userData });
  },

  setLoading: (loading) => {
    set({ isLoading: loading });
  },

  // Role checking helpers
  isAdmin: () => get().role === 'ADMIN' || get().role === 'SYSADMIN',
  isManager: () => get().role === 'MANAGER',
  isEmployee: () => get().role === 'EMPLOYEE',
  
  // Check if manager has access to a department
  canAccessDepartment: (departmentId) => {
    const state = get();
    if (state.isAdmin()) return true;
    if (!state.isManager()) return false;
    return state.departments.some(d => d.id === departmentId);
  }
}));

export default useAuthStore;