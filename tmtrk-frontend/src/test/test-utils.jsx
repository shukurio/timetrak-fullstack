import { render } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'

// Create a custom render function that includes providers
const AllTheProviders = ({ children }) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        cacheTime: 0,
      },
    },
  })

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        {children}
        <Toaster />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

const customRender = (ui, options) =>
  render(ui, { wrapper: AllTheProviders, ...options })

// Re-export everything
export * from '@testing-library/react'
export { customRender as render }

// Mock data generators
export const createMockEmployee = (overrides = {}) => ({
  id: 1,
  firstName: 'John',
  lastName: 'Doe',
  username: 'johndoe',
  email: 'john.doe@example.com',
  status: 'ACTIVE',
  department: 'Engineering',
  ...overrides,
})

export const createMockShift = (overrides = {}) => ({
  id: 1,
  employeeJobId: 1,
  fullName: 'John Doe',
  jobTitle: 'Software Engineer',
  clockIn: '2023-01-01T09:00:00',
  clockOut: '2023-01-01T17:00:00',
  status: 'COMPLETED',
  hours: 8.0,
  shiftEarnings: 240.0,
  ...overrides,
})

export const createMockDepartment = (overrides = {}) => ({
  id: 1,
  name: 'Engineering',
  description: 'Software Development',
  ...overrides,
})

export const createMockJob = (overrides = {}) => ({
  id: 1,
  title: 'Software Engineer',
  description: 'Develop software applications',
  hourlyWage: 30.0,
  departmentId: 1,
  ...overrides,
})

export const createMockPayment = (overrides = {}) => ({
  id: 1,
  employeeName: 'John Doe',
  totalEarnings: 1200.0,
  status: 'COMPLETED',
  formattedPeriod: 'Jan 1-15, 2023',
  ...overrides,
})