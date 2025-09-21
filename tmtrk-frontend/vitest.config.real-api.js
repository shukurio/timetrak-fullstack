import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Configuration for real API testing
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup-real-api.js',
    css: true,
    testTimeout: 15000, // Longer timeout for real API calls
    hookTimeout: 10000,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
        '**/*.d.ts',
        '**/*.config.js',
        '**/coverage/**'
      ]
    },
    // Only run real API tests
    include: ['**/real-api.test.js', '**/integration/**/*.test.{js,jsx}'],
    // Environment variables for API testing
    env: {
      VITE_API_BASE_URL: 'http://localhost:8093/api',
      VITE_TEST_MODE: 'real-api'
    }
  }
})