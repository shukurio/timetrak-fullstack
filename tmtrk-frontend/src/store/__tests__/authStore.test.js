import { describe, it, expect, beforeEach, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import useAuthStore from '../authStore'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock

describe('AuthStore', () => {
  beforeEach(() => {
    // Reset the store state before each test
    useAuthStore.setState({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
    })
    vi.clearAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      const { result } = renderHook(() => useAuthStore())

      expect(result.current.user).toBeNull()
      expect(result.current.token).toBeNull()
      expect(result.current.isAuthenticated).toBe(false)
      expect(result.current.isLoading).toBe(false)
      expect(result.current.isAdmin).toBe(false)
      expect(result.current.isEmployee).toBe(false)
    })
  })

  describe('setUser', () => {
    it('should set user correctly', () => {
      const { result } = renderHook(() => useAuthStore())
      const mockUser = {
        id: 1,
        username: 'testuser',
        role: 'ADMIN',
        firstName: 'John',
        lastName: 'Doe'
      }

      act(() => {
        result.current.setUser(mockUser)
      })

      expect(result.current.user).toEqual(mockUser)
      expect(result.current.isAuthenticated).toBe(true)
      expect(result.current.isAdmin).toBe(true)
      expect(result.current.isEmployee).toBe(false)
    })

    it('should handle employee role correctly', () => {
      const { result } = renderHook(() => useAuthStore())
      const mockUser = {
        id: 1,
        username: 'employee',
        role: 'EMPLOYEE',
        firstName: 'Jane',
        lastName: 'Doe'
      }

      act(() => {
        result.current.setUser(mockUser)
      })

      expect(result.current.isAdmin).toBe(false)
      expect(result.current.isEmployee).toBe(true)
    })
  })

  describe('setToken', () => {
    it('should set token and store in localStorage', () => {
      const { result } = renderHook(() => useAuthStore())
      const mockToken = 'mock-jwt-token'

      act(() => {
        result.current.setToken(mockToken)
      })

      expect(result.current.token).toBe(mockToken)
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', mockToken)
    })

    it('should remove token from localStorage when set to null', () => {
      const { result } = renderHook(() => useAuthStore())

      act(() => {
        result.current.setToken(null)
      })

      expect(result.current.token).toBeNull()
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
    })
  })

  describe('login', () => {
    it('should login user with token', () => {
      const { result } = renderHook(() => useAuthStore())
      const mockUser = {
        id: 1,
        username: 'testuser',
        role: 'ADMIN'
      }
      const mockToken = 'mock-token'

      act(() => {
        result.current.login(mockUser, mockToken)
      })

      expect(result.current.user).toEqual(mockUser)
      expect(result.current.token).toBe(mockToken)
      expect(result.current.isAuthenticated).toBe(true)
      expect(localStorageMock.setItem).toHaveBeenCalledWith('token', mockToken)
    })
  })

  describe('logout', () => {
    it('should logout and clear all data', () => {
      const { result } = renderHook(() => useAuthStore())

      // First login
      act(() => {
        result.current.login(
          { id: 1, username: 'test', role: 'ADMIN' },
          'mock-token'
        )
      })

      // Then logout
      act(() => {
        result.current.logout()
      })

      expect(result.current.user).toBeNull()
      expect(result.current.token).toBeNull()
      expect(result.current.isAuthenticated).toBe(false)
      expect(result.current.isAdmin).toBe(false)
      expect(result.current.isEmployee).toBe(false)
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('token')
    })
  })

  describe('setLoading', () => {
    it('should set loading state', () => {
      const { result } = renderHook(() => useAuthStore())

      act(() => {
        result.current.setLoading(true)
      })

      expect(result.current.isLoading).toBe(true)

      act(() => {
        result.current.setLoading(false)
      })

      expect(result.current.isLoading).toBe(false)
    })
  })

  describe('initializeAuth', () => {
    it('should initialize with token from localStorage', () => {
      localStorageMock.getItem.mockReturnValue('stored-token')
      const { result } = renderHook(() => useAuthStore())

      act(() => {
        result.current.initializeAuth()
      })

      expect(result.current.token).toBe('stored-token')
      expect(localStorageMock.getItem).toHaveBeenCalledWith('token')
    })

    it('should handle no token in localStorage', () => {
      localStorageMock.getItem.mockReturnValue(null)
      const { result } = renderHook(() => useAuthStore())

      act(() => {
        result.current.initializeAuth()
      })

      expect(result.current.token).toBeNull()
      expect(result.current.isAuthenticated).toBe(false)
    })
  })

  describe('Role-based computed properties', () => {
    it('should compute isAdmin correctly for different roles', () => {
      const { result } = renderHook(() => useAuthStore())

      // Test ADMIN role
      act(() => {
        result.current.setUser({ id: 1, role: 'ADMIN' })
      })
      expect(result.current.isAdmin).toBe(true)

      // Test EMPLOYEE role
      act(() => {
        result.current.setUser({ id: 1, role: 'EMPLOYEE' })
      })
      expect(result.current.isAdmin).toBe(false)

      // Test unknown role
      act(() => {
        result.current.setUser({ id: 1, role: 'UNKNOWN' })
      })
      expect(result.current.isAdmin).toBe(false)
    })

    it('should compute isEmployee correctly for different roles', () => {
      const { result } = renderHook(() => useAuthStore())

      // Test EMPLOYEE role
      act(() => {
        result.current.setUser({ id: 1, role: 'EMPLOYEE' })
      })
      expect(result.current.isEmployee).toBe(true)

      // Test ADMIN role
      act(() => {
        result.current.setUser({ id: 1, role: 'ADMIN' })
      })
      expect(result.current.isEmployee).toBe(false)
    })
  })
})