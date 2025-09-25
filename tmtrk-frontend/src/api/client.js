import axios from 'axios';
import useAuthStore from '../store/authStore';
import toast from 'react-hot-toast';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Create axios instance
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true, // For cookies (refresh tokens)
});

// Request interceptor
apiClient.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().getAccessToken();

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor with automatic token refresh
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const isAuthRefresh = error.config?.url?.includes('/auth/refresh');
        const isLoginRequest = error.config?.url?.includes('/auth/login');

        // If 401 and not already retried
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            // Don't refresh if this is a login or refresh request failing
            if (isAuthRefresh || isLoginRequest) {
                // Login page handle its own 401 errors
                return Promise.reject(error);
            }

            try {
                const response = await axios.post(
                    `${API_BASE_URL}/auth/refresh`,
                    {},
                    { withCredentials: true }
                );

                const { token, expiresIn } = response.data;
                useAuthStore.getState().setTokens(token, expiresIn);

                // Retry original request
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return apiClient(originalRequest);
            } catch (refreshError) {
                // Refresh failed, logout user
                useAuthStore.getState().logout();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        // Handle other errors - suppress only 401 for auth refresh
        const isAuthRefresh401 = isAuthRefresh && error.response?.status === 401;

        if (!isAuthRefresh401) {
            // Show errors for everything except 401 on auth/refresh
            if (error.response?.status === 403) {
                toast.error('Access denied. Insufficient permissions.');
            } else if (error.response?.status === 404) {
                toast.error('Resource not found.');
            } else if (error.response?.status === 500) {
                toast.error('Server error. Please try again later.');
            }
        }

        return Promise.reject(error);
    }
);

export default apiClient;