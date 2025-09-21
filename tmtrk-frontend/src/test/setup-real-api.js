import '@testing-library/jest-dom'
import { beforeAll, afterEach, afterAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'

// Setup for real API tests
beforeAll(() => {
  console.log('🚀 Setting up real API tests...')
  console.log('Backend URL:', process.env.VITE_API_BASE_URL || 'http://localhost:8093/api')

  // Mock browser APIs that aren't available in test environment
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  })

  // Mock geolocation with default test coordinates
  Object.defineProperty(global.navigator, 'geolocation', {
    writable: true,
    value: {
      getCurrentPosition: vi.fn((success) => {
        success({
          coords: {
            latitude: 40.7128,
            longitude: -74.0060
          }
        })
      }),
      watchPosition: vi.fn(),
      clearWatch: vi.fn(),
    },
  })

  // Mock ResizeObserver
  global.ResizeObserver = vi.fn().mockImplementation(() => ({
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  }))

  // Mock IntersectionObserver
  global.IntersectionObserver = vi.fn().mockImplementation(() => ({
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  }))

  // Mock window methods
  global.confirm = vi.fn(() => true)
  global.alert = vi.fn()

  // Warn about real API usage
  console.log('⚠️  These tests will make REAL API calls to your backend!')
  console.log('🔧 Make sure your backend is running on http://localhost:8093')
})

// Cleanup after each test
afterEach(() => {
  cleanup()
  // Don't clear all mocks for real API tests - we want to keep API state
})

afterAll(() => {
  console.log('🏁 Real API tests completed')
})