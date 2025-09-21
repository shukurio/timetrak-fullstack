import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '../../../test/test-utils'
import userEvent from '@testing-library/user-event'
import ClockMenu from '../ClockMenu'
import * as employeeService from '../../../api/employeeService'

// Mock employeeService
vi.mock('../../../api/employeeService', () => ({
  default: {
    determineClockAction: vi.fn(),
    clockIn: vi.fn(),
    clockOut: vi.fn(),
    getMyJobs: vi.fn(),
  }
}))

// Mock geolocation
const mockGeolocation = {
  getCurrentPosition: vi.fn(),
}
global.navigator.geolocation = mockGeolocation

describe('ClockMenu', () => {
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

  describe('Clock In State', () => {
    beforeEach(() => {
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue(mockJobs)
    })

    it('should show clock in interface when action is CLOCK_IN', async () => {
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByText(/ready to clock in/i)).toBeInTheDocument()
        expect(screen.getByText('Software Engineer')).toBeInTheDocument()
        expect(screen.getByText('Senior Engineer')).toBeInTheDocument()
      })
    })

    it('should allow job selection for clock in', async () => {
      const user = userEvent.setup()
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByText('Software Engineer')).toBeInTheDocument()
      })

      // Select first job
      const jobButton = screen.getByText('Software Engineer').closest('button')
      await user.click(jobButton)

      expect(jobButton).toHaveClass('border-primary-500') // Or whatever selected class
    })

    it('should clock in successfully', async () => {
      const user = userEvent.setup()
      const mockShiftData = {
        id: 1,
        employeeJobId: 1,
        clockIn: '2023-01-01T09:00:00',
        jobTitle: 'Software Engineer'
      }

      employeeService.default.clockIn.mockResolvedValue(mockShiftData)

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByText('Software Engineer')).toBeInTheDocument()
      })

      // Select job
      const jobButton = screen.getByText('Software Engineer').closest('button')
      await user.click(jobButton)

      // Click clock in
      const clockInButton = screen.getByRole('button', { name: /clock in/i })
      await user.click(clockInButton)

      await waitFor(() => {
        expect(employeeService.default.clockIn).toHaveBeenCalledWith({
          employeeJobId: 1,
          latitude: 40.7128,
          longitude: -74.0060
        })
      })
    })

    it('should auto-select job if only one available', async () => {
      employeeService.default.getMyJobs.mockResolvedValue([mockJobs[0]])

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        const clockInButton = screen.getByRole('button', { name: /clock in/i })
        expect(clockInButton).not.toBeDisabled()
      })
    })

    it('should require job selection for multiple jobs', async () => {
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        const clockInButton = screen.getByRole('button', { name: /clock in/i })
        expect(clockInButton).toBeDisabled()
      })
    })
  })

  describe('Clock Out State', () => {
    beforeEach(() => {
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_OUT')
    })

    it('should show clock out interface when action is CLOCK_OUT', async () => {
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByText(/ready to clock out/i)).toBeInTheDocument()
        expect(screen.getByRole('button', { name: /clock out/i })).toBeInTheDocument()
      })
    })

    it('should clock out successfully', async () => {
      const user = userEvent.setup()
      const mockShiftData = {
        id: 1,
        employeeId: 1,
        clockOut: '2023-01-01T17:00:00',
        hours: 8.0,
        shiftEarnings: 240.0
      }

      employeeService.default.clockOut.mockResolvedValue(mockShiftData)

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock out/i })).toBeInTheDocument()
      })

      const clockOutButton = screen.getByRole('button', { name: /clock out/i })
      await user.click(clockOutButton)

      await waitFor(() => {
        expect(employeeService.default.clockOut).toHaveBeenCalledWith({
          latitude: 40.7128,
          longitude: -74.0060
        })
      })
    })
  })

  describe('No Jobs Available', () => {
    beforeEach(() => {
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue([])
    })

    it('should show no jobs message', async () => {
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByText(/no jobs assigned/i)).toBeInTheDocument()
        expect(screen.getByText(/contact your supervisor/i)).toBeInTheDocument()
      })
    })

    it('should disable clock in button when no jobs', async () => {
      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        const clockInButton = screen.getByRole('button', { name: /clock in/i })
        expect(clockInButton).toBeDisabled()
      })
    })
  })

  describe('Error Handling', () => {
    it('should handle geolocation failure gracefully', async () => {
      const user = userEvent.setup()
      mockGeolocation.getCurrentPosition.mockImplementation((success, error) => {
        error({ code: 1, message: 'Permission denied' })
      })

      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue([mockJobs[0]])
      employeeService.default.clockIn.mockResolvedValue({})

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        const clockInButton = screen.getByRole('button', { name: /clock in/i })
        expect(clockInButton).not.toBeDisabled()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      await waitFor(() => {
        expect(employeeService.default.clockIn).toHaveBeenCalledWith({
          employeeJobId: 1,
          latitude: 0.0,
          longitude: 0.0 // Should use default coordinates
        })
      })
    })

    it('should handle clock in API failure', async () => {
      const user = userEvent.setup()
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue([mockJobs[0]])
      employeeService.default.clockIn.mockRejectedValue(new Error('Clock in failed'))

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in/i })).not.toBeDisabled()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      await waitFor(() => {
        expect(employeeService.default.clockIn).toHaveBeenCalled()
      })

      // Should handle error gracefully (could show error message)
    })

    it('should handle determine action API failure', async () => {
      employeeService.default.determineClockAction.mockRejectedValue(
        new Error('Failed to determine action')
      )

      render(<ClockMenu employee={mockEmployee} />)

      // Should handle error gracefully
      await waitFor(() => {
        // Component should still render something, maybe error state
        expect(screen.getByRole('generic')).toBeInTheDocument()
      })
    })
  })

  describe('Loading States', () => {
    it('should show loading state while determining action', () => {
      // Mock a pending promise
      employeeService.default.determineClockAction.mockReturnValue(new Promise(() => {}))

      render(<ClockMenu employee={mockEmployee} />)

      // Should show loading indicator
      expect(screen.getByRole('generic')).toBeInTheDocument() // LoadingSpinner
    })

    it('should show loading state while clocking in', async () => {
      const user = userEvent.setup()
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue([mockJobs[0]])

      // Mock pending clock in
      employeeService.default.clockIn.mockReturnValue(new Promise(() => {}))

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in/i })).not.toBeDisabled()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      // Button should be disabled during loading
      expect(screen.getByRole('button', { name: /clock in/i })).toBeDisabled()
    })
  })

  describe('Success Feedback', () => {
    it('should show success message after successful clock in', async () => {
      const user = userEvent.setup()
      employeeService.default.determineClockAction.mockResolvedValue('CLOCK_IN')
      employeeService.default.getMyJobs.mockResolvedValue([mockJobs[0]])
      employeeService.default.clockIn.mockResolvedValue({
        id: 1,
        clockIn: '2023-01-01T09:00:00'
      })

      render(<ClockMenu employee={mockEmployee} />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in/i })).not.toBeDisabled()
      })

      await user.click(screen.getByRole('button', { name: /clock in/i }))

      await waitFor(() => {
        // Should show success state or message
        expect(screen.getByText(/clocked in successfully/i)).toBeInTheDocument()
      })
    })
  })
})