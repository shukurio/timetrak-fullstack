import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '../../../test/test-utils'
import userEvent from '@testing-library/user-event'
import KioskPage from '../KioskPage'
import * as kioskService from '../../../api/kioskService'

// Mock kioskService
vi.mock('../../../api/kioskService', () => ({
  default: {
    getEmployeeByUsername: vi.fn(),
    getEmployeeJobs: vi.fn(),
    determineAction: vi.fn(),
    clockIn: vi.fn(),
    clockOut: vi.fn(),
  }
}))

// Mock geolocation
const mockGeolocation = {
  getCurrentPosition: vi.fn(),
}
global.navigator.geolocation = mockGeolocation

describe('KioskPage', () => {
  const mockEmployee = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    username: 'johndoe'
  }

  const mockJobs = [
    {
      employeeJobId: 1,
      jobTitle: 'Software Engineer',
      hourlyWage: 30.0,
      departmentName: 'Engineering'
    },
    {
      employeeJobId: 2,
      jobTitle: 'Senior Engineer',
      hourlyWage: 35.0,
      departmentName: 'Engineering'
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2023-01-01T12:00:00'))

    // Mock successful geolocation
    mockGeolocation.getCurrentPosition.mockImplementation((success) => {
      success({
        coords: {
          latitude: 40.7128,
          longitude: -74.0060
        }
      })
    })
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('Initial State', () => {
    it('should render kiosk page with time display', () => {
      render(<KioskPage />)

      expect(screen.getByText('TimeTrak Kiosk')).toBeInTheDocument()
      expect(screen.getByText('12:00:00')).toBeInTheDocument()
      expect(screen.getByText('Sunday, January 1, 2023')).toBeInTheDocument()
      expect(screen.getByLabelText('Enter Username')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /find employee/i })).toBeInTheDocument()
    })

    it('should update time every second', () => {
      render(<KioskPage />)

      expect(screen.getByText('12:00:00')).toBeInTheDocument()

      // Advance time by 1 second
      vi.advanceTimersByTime(1000)

      expect(screen.getByText('12:00:01')).toBeInTheDocument()
    })
  })

  describe('Employee Search', () => {
    it('should search for employee successfully', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue(mockJobs)

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      const searchButton = screen.getByRole('button', { name: /find employee/i })

      await user.type(usernameInput, 'johndoe')
      await user.click(searchButton)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
        expect(screen.getByText('@johndoe')).toBeInTheDocument()
      })

      expect(kioskService.default.getEmployeeByUsername).toHaveBeenCalledWith('johndoe')
      expect(kioskService.default.determineAction).toHaveBeenCalledWith(1)
      expect(kioskService.default.getEmployeeJobs).toHaveBeenCalledWith('johndoe')
    })

    it('should handle employee not found', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockRejectedValue(
        new Error('Employee not found')
      )

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      const searchButton = screen.getByRole('button', { name: /find employee/i })

      await user.type(usernameInput, 'notfound')
      await user.click(searchButton)

      await waitFor(() => {
        expect(kioskService.default.getEmployeeByUsername).toHaveBeenCalledWith('notfound')
      })
    })

    it('should require username input', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      render(<KioskPage />)

      const searchButton = screen.getByRole('button', { name: /find employee/i })
      await user.click(searchButton)

      // Should not make API call without username
      expect(kioskService.default.getEmployeeByUsername).not.toHaveBeenCalled()
    })

    it('should search on Enter key press', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue(mockJobs)

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe{enter}')

      await waitFor(() => {
        expect(kioskService.default.getEmployeeByUsername).toHaveBeenCalledWith('johndoe')
      })
    })
  })

  describe('Clock In Flow', () => {
    beforeEach(async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue(mockJobs)

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })
    })

    it('should show job selection for multiple jobs', () => {
      expect(screen.getByText('Select Job Position')).toBeInTheDocument()
      expect(screen.getByText('Software Engineer')).toBeInTheDocument()
      expect(screen.getByText('Senior Engineer')).toBeInTheDocument()
      expect(screen.getByText('$30/hour • Engineering')).toBeInTheDocument()
    })

    it('should allow job selection', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })

      const jobButton = screen.getByText('Software Engineer').closest('button')
      await user.click(jobButton)

      expect(jobButton).toHaveClass('border-blue-500')
    })

    it('should clock in successfully', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      const mockShiftData = {
        id: 1,
        employeeJobId: 1,
        clockIn: '2023-01-01T12:00:00',
        jobTitle: 'Software Engineer'
      }
      kioskService.default.clockIn.mockResolvedValue(mockShiftData)

      // Select a job
      const jobButton = screen.getByText('Software Engineer').closest('button')
      await user.click(jobButton)

      // Click clock in
      const clockInButton = screen.getByRole('button', { name: /clock in/i })
      await user.click(clockInButton)

      await waitFor(() => {
        expect(kioskService.default.clockIn).toHaveBeenCalledWith({
          id: 1,
          latitude: 40.7128,
          longitude: -74.0060
        })
      })

      // Should show success screen
      await waitFor(() => {
        expect(screen.getByText('Welcome Back!')).toBeInTheDocument()
      })
    })

    it('should auto-select job if only one available', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      const singleJob = [mockJobs[0]]
      kioskService.default.getEmployeeJobs.mockResolvedValue(singleJob)

      // Reset and search again
      await user.click(screen.getByRole('button', { name: /cancel/i }))

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.clear(usernameInput)
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        // Should automatically enable clock in button
        expect(screen.getByRole('button', { name: /clock in/i })).not.toBeDisabled()
      })
    })
  })

  describe('Clock Out Flow', () => {
    beforeEach(async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_OUT')

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })
    })

    it('should show clock out interface', () => {
      expect(screen.getByText('Ready to clock out')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /clock out/i })).toBeInTheDocument()
    })

    it('should clock out successfully', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      const mockShiftData = {
        id: 1,
        employeeId: 1,
        clockIn: '2023-01-01T08:00:00',
        clockOut: '2023-01-01T12:00:00',
        hours: 4.0,
        shiftEarnings: 120.0,
        hourlyWage: 30.0
      }
      kioskService.default.clockOut.mockResolvedValue(mockShiftData)

      const clockOutButton = screen.getByRole('button', { name: /clock out/i })
      await user.click(clockOutButton)

      await waitFor(() => {
        expect(kioskService.default.clockOut).toHaveBeenCalledWith({
          id: 1,
          latitude: 40.7128,
          longitude: -74.0060
        })
      })

      // Should show success screen
      await waitFor(() => {
        expect(screen.getByText('Great Work!')).toBeInTheDocument()
        expect(screen.getByText('$120.00')).toBeInTheDocument()
        expect(screen.getByText('4.0 hours')).toBeInTheDocument()
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle UNAVAILABLE action', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('UNAVAILABLE')

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByText('Cannot determine clock action. Please contact supervisor.')).toBeInTheDocument()
        expect(screen.getByText('Action Unavailable')).toBeInTheDocument()
      })
    })

    it('should handle no jobs available', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue([])

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByText('No Jobs Assigned')).toBeInTheDocument()
        expect(screen.getByText('You don\'t have any job assignments yet.')).toBeInTheDocument()
      })
    })

    it('should handle geolocation failure gracefully', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })

      // Mock geolocation failure
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 1, message: 'Permission denied' })
      })

      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue(mockJobs)
      kioskService.default.clockIn.mockResolvedValue({})

      render(<KioskPage />)

      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      // Select job and clock in
      const jobButton = screen.getByText('Software Engineer').closest('button')
      await user.click(jobButton)

      const clockInButton = screen.getByRole('button', { name: /clock in/i })
      await user.click(clockInButton)

      await waitFor(() => {
        expect(kioskService.default.clockIn).toHaveBeenCalledWith({
          id: 1,
          latitude: 0.0,
          longitude: 0.0 // Should use default coordinates
        })
      })
    })
  })

  describe('Success Screen', () => {
    it('should auto-hide success screen after 7 seconds', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue([mockJobs[0]])
      kioskService.default.clockIn.mockResolvedValue({
        id: 1,
        jobTitle: 'Software Engineer'
      })

      render(<KioskPage />)

      // Complete clock in flow
      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in/i })).toBeInTheDocument()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      await waitFor(() => {
        expect(screen.getByText('Welcome Back!')).toBeInTheDocument()
      })

      // Fast forward 7 seconds
      vi.advanceTimersByTime(7000)

      await waitFor(() => {
        expect(screen.queryByText('Welcome Back!')).not.toBeInTheDocument()
        expect(screen.getByLabelText('Enter Username')).toBeInTheDocument()
      })
    })

    it('should allow manual return to main screen', async () => {
      const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
      kioskService.default.getEmployeeByUsername.mockResolvedValue(mockEmployee)
      kioskService.default.determineAction.mockResolvedValue('CLOCK_IN')
      kioskService.default.getEmployeeJobs.mockResolvedValue([mockJobs[0]])
      kioskService.default.clockIn.mockResolvedValue({})

      render(<KioskPage />)

      // Complete clock in flow
      const usernameInput = screen.getByPlaceholderText('Username')
      await user.type(usernameInput, 'johndoe')
      await user.click(screen.getByRole('button', { name: /find employee/i }))

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in/i })).toBeInTheDocument()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      await waitFor(() => {
        expect(screen.getByText('Welcome Back!')).toBeInTheDocument()
      })

      // Click "Clock Another Employee"
      await user.click(screen.getByRole('button', { name: /clock another employee/i }))

      expect(screen.getByLabelText('Enter Username')).toBeInTheDocument()
    })
  })
})