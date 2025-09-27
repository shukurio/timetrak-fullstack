import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '../test-utils'
import userEvent from '@testing-library/user-event'
import App from '../../App'
import * as authService from '../../api/authService'
import useAuthStore from '../../store/authStore'

// Mock authService
vi.mock('../../api/authService', () => ({
  default: {
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    register: vi.fn(),
  }
}))

describe('Authentication Flow Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    // Reset auth store
    useAuthStore.getState().logout()
  })

  describe('Login Flow', () => {
    it('should allow user to login and redirect to dashboard', async () => {
      const user = userEvent.setup()
      const mockUser = {
        id: 1,
        username: 'admin',
        role: 'ADMIN',
        firstName: 'Admin',
        lastName: 'User'
      }
      const mockToken = 'mock-jwt-token'

      authService.default.login.mockResolvedValue({
        user: mockUser,
        accessToken: mockToken
      })

      render(<App />)

      // Should show login page initially
      expect(screen.getByText(/welcome to timetrack/i)).toBeInTheDocument()

      // Fill in login form
      const usernameInput = screen.getByLabelText(/username/i)
      const passwordInput = screen.getByLabelText(/password/i)
      const loginButton = screen.getByRole('button', { name: /sign in/i })

      await user.type(usernameInput, 'admin')
      await user.type(passwordInput, 'password123')
      await user.click(loginButton)

      // Should call login API
      await waitFor(() => {
        expect(authService.default.login).toHaveBeenCalledWith('admin', 'password123')
      })

      // Should redirect to admin dashboard
      await waitFor(() => {
        expect(screen.getByText(/dashboard/i)).toBeInTheDocument()
      })

      // Should show user info in the store
      expect(useAuthStore.getState().user).toEqual(mockUser)
      expect(useAuthStore.getState().token).toBe(mockToken)
      expect(useAuthStore.getState().isAuthenticated).toBe(true)
      expect(useAuthStore.getState().isAdmin).toBe(true)
    })

    it('should handle login failure', async () => {
      const user = userEvent.setup()
      authService.default.login.mockRejectedValue(
        new Error('Invalid credentials')
      )

      render(<App />)

      const usernameInput = screen.getByLabelText(/username/i)
      const passwordInput = screen.getByLabelText(/password/i)
      const loginButton = screen.getByRole('button', { name: /sign in/i })

      await user.type(usernameInput, 'admin')
      await user.type(passwordInput, 'wrongpassword')
      await user.click(loginButton)

      await waitFor(() => {
        expect(authService.default.login).toHaveBeenCalledWith('admin', 'wrongpassword')
      })

      // Should stay on login page
      expect(screen.getByText(/welcome to timetrack/i)).toBeInTheDocument()

      // Should not be authenticated
      expect(useAuthStore.getState().isAuthenticated).toBe(false)
    })
  })

  describe('Logout Flow', () => {
    it('should allow user to logout and redirect to login', async () => {
      const user = userEvent.setup()

      // First login
      const mockUser = {
        id: 1,
        username: 'admin',
        role: 'ADMIN'
      }
      useAuthStore.getState().login(mockUser, 'mock-token')

      authService.default.logout.mockResolvedValue({})

      render(<App />)

      // Should be on dashboard
      expect(screen.getByText(/dashboard/i)).toBeInTheDocument()

      // Find and click logout button (could be in a dropdown or direct button)
      const logoutButton = screen.getByRole('button', { name: /logout/i })
      await user.click(logoutButton)

      await waitFor(() => {
        expect(authService.default.logout).toHaveBeenCalled()
      })

      // Should redirect to login page
      await waitFor(() => {
        expect(screen.getByText(/welcome to timetrack/i)).toBeInTheDocument()
      })

      // Should clear auth state
      expect(useAuthStore.getState().isAuthenticated).toBe(false)
      expect(useAuthStore.getState().user).toBeNull()
      expect(useAuthStore.getState().token).toBeNull()
    })
  })

  describe('Protected Routes', () => {
    it('should redirect unauthenticated users to login', async () => {
      render(<App />)

      // Try to navigate to protected route
      window.history.pushState({}, '', '/admin/dashboard')

      await waitFor(() => {
        expect(screen.getByText(/welcome to timetrack/i)).toBeInTheDocument()
      })
    })

    it('should allow authenticated admin users to access admin routes', async () => {
      const mockUser = {
        id: 1,
        username: 'admin',
        role: 'ADMIN'
      }
      useAuthStore.getState().login(mockUser, 'mock-token')

      render(<App />)

      // Should be able to access admin dashboard
      window.history.pushState({}, '', '/admin/dashboard')

      await waitFor(() => {
        expect(screen.getByText(/dashboard/i)).toBeInTheDocument()
      })
    })

    it('should prevent employees from accessing admin routes', async () => {
      const mockUser = {
        id: 1,
        username: 'employee',
        role: 'EMPLOYEE'
      }
      useAuthStore.getState().login(mockUser, 'mock-token')

      render(<App />)

      // Try to access admin route
      window.history.pushState({}, '', '/admin/dashboard')

      // Should redirect to employee dashboard or show access denied
      await waitFor(() => {
        expect(screen.queryByText(/admin dashboard/i)).not.toBeInTheDocument()
      })
    })
  })

  describe('Token Persistence', () => {
    it('should restore authentication state from localStorage', () => {
      // Simulate token in localStorage
      localStorage.setItem('token', 'stored-token')

      // Initialize auth store
      useAuthStore.getState().initializeAuth()

      expect(useAuthStore.getState().token).toBe('stored-token')
    })

    it('should clear token from localStorage on logout', async () => {
      useAuthStore.getState().setToken('test-token')
      expect(localStorage.getItem('token')).toBe('test-token')

      useAuthStore.getState().logout()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })
})