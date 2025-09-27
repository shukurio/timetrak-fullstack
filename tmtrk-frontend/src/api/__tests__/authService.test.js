import { describe, it, expect, beforeEach, vi } from 'vitest'
import authService from '../authService'
import apiClient from '../client'

// Mock the API client
vi.mock('../client', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  }
}))

describe('AuthService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('login', () => {
    it('should login successfully and store token', async () => {
      const mockResponse = {
        data: {
          accessToken: 'mock-token',
          user: { id: 1, username: 'testuser', role: 'ADMIN' }
        }
      }
      apiClient.post.mockResolvedValue(mockResponse)

      const result = await authService.login('testuser', 'password')

      expect(apiClient.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password'
      })
      expect(result).toEqual(mockResponse.data)
      expect(localStorage.getItem('token')).toBe('mock-token')
    })

    it('should handle login failure', async () => {
      const mockError = new Error('Invalid credentials')
      apiClient.post.mockRejectedValue(mockError)

      await expect(authService.login('testuser', 'wrong-password'))
        .rejects.toThrow('Invalid credentials')

      expect(localStorage.getItem('token')).toBeNull()
    })
  })


  describe('logout', () => {
    it('should logout and clear token', async () => {
      localStorage.setItem('token', 'mock-token')
      apiClient.post.mockResolvedValue({ data: {} })

      await authService.logout()

      expect(apiClient.post).toHaveBeenCalledWith('/auth/logout')
      expect(localStorage.getItem('token')).toBeNull()
    })

    it('should clear token even if API call fails', async () => {
      localStorage.setItem('token', 'mock-token')
      apiClient.post.mockRejectedValue(new Error('Network error'))

      await authService.logout()

      expect(localStorage.getItem('token')).toBeNull()
    })
  })

  describe('getCurrentUser', () => {
    it('should get current user successfully', async () => {
      const mockUser = { id: 1, username: 'testuser', role: 'ADMIN' }
      apiClient.get.mockResolvedValue({ data: mockUser })

      const result = await authService.getCurrentUser()

      expect(apiClient.get).toHaveBeenCalledWith('/auth/me')
      expect(result).toEqual(mockUser)
    })
  })

  describe('refreshToken', () => {
    it('should refresh token successfully', async () => {
      const mockResponse = {
        data: { accessToken: 'new-token' }
      }
      apiClient.post.mockResolvedValue(mockResponse)

      const result = await authService.refreshToken()

      expect(apiClient.post).toHaveBeenCalledWith('/auth/refresh')
      expect(result).toEqual(mockResponse.data)
      expect(localStorage.getItem('token')).toBe('new-token')
    })
  })

  describe('requestPasswordReset', () => {
    it('should request password reset successfully', async () => {
      const mockResponse = { data: { message: 'Reset email sent' } }
      apiClient.post.mockResolvedValue(mockResponse)

      const result = await authService.requestPasswordReset('test@example.com')

      expect(apiClient.post).toHaveBeenCalledWith('/auth/reset-password-request', {
        email: 'test@example.com'
      })
      expect(result).toEqual(mockResponse.data)
    })
  })

  describe('resetPassword', () => {
    it('should reset password successfully', async () => {
      const mockResponse = { data: { message: 'Password reset successful' } }
      apiClient.post.mockResolvedValue(mockResponse)

      const result = await authService.resetPassword('reset-token', 'newpassword')

      expect(apiClient.post).toHaveBeenCalledWith('/auth/reset-password', {
        token: 'reset-token',
        newPassword: 'newpassword'
      })
      expect(result).toEqual(mockResponse.data)
    })
  })
})