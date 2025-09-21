import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '../test-utils'
import userEvent from '@testing-library/user-event'
import ShiftsPage from '../../pages/admin/ShiftsPage'
import * as adminService from '../../api/adminService'
import useAuthStore from '../../store/authStore'

// Mock adminService
vi.mock('../../api/adminService', () => ({
  default: {
    getAvailablePaymentPeriods: vi.fn(),
    getCurrentPaymentPeriod: vi.fn(),
    getShiftsByPeriodNumber: vi.fn(),
    getDepartments: vi.fn(),
    getEmployeesByDepartment: vi.fn(),
    getEmployeeJobsByEmployee: vi.fn(),
    createShift: vi.fn(),
    updateShift: vi.fn(),
    deleteShift: vi.fn(),
    clockIn: vi.fn(),
    clockOut: vi.fn(),
  }
}))

describe('Shift Management Integration', () => {
  const mockAdmin = {
    id: 1,
    username: 'admin',
    role: 'ADMIN'
  }

  const mockPeriods = [
    { periodNumber: 1, formattedPeriod: 'Jan 1-15, 2023' },
    { periodNumber: 2, formattedPeriod: 'Jan 16-31, 2023' }
  ]

  const mockCurrentPeriod = mockPeriods[0]

  const mockShifts = {
    content: [
      {
        id: 1,
        employeeJobId: 1,
        fullName: 'John Doe',
        jobTitle: 'Software Engineer',
        clockIn: '2023-01-01T09:00:00',
        clockOut: '2023-01-01T17:00:00',
        status: 'COMPLETED',
        hours: 8.0,
        shiftEarnings: 240.0
      },
      {
        id: 2,
        employeeJobId: 2,
        fullName: 'Jane Smith',
        jobTitle: 'Senior Engineer',
        clockIn: '2023-01-01T10:00:00',
        clockOut: null,
        status: 'ACTIVE',
        hours: 0,
        shiftEarnings: 0
      }
    ],
    totalElements: 2,
    totalPages: 1
  }

  const mockDepartments = {
    content: [
      { id: 1, name: 'Engineering' },
      { id: 2, name: 'Marketing' }
    ]
  }

  const mockEmployees = {
    content: [
      { id: 1, firstName: 'John', lastName: 'Doe' },
      { id: 2, firstName: 'Jane', lastName: 'Smith' }
    ]
  }

  const mockEmployeeJobs = [
    { employeeJobId: 1, jobTitle: 'Software Engineer' },
    { employeeJobId: 2, jobTitle: 'Senior Engineer' }
  ]

  beforeEach(() => {
    vi.clearAllMocks()

    // Set up admin user
    useAuthStore.getState().login(mockAdmin, 'mock-token')

    // Mock API responses
    adminService.default.getAvailablePaymentPeriods.mockResolvedValue(mockPeriods)
    adminService.default.getCurrentPaymentPeriod.mockResolvedValue(mockCurrentPeriod)
    adminService.default.getShiftsByPeriodNumber.mockResolvedValue(mockShifts)
    adminService.default.getDepartments.mockResolvedValue(mockDepartments)
    adminService.default.getEmployeesByDepartment.mockResolvedValue(mockEmployees)
    adminService.default.getEmployeeJobsByEmployee.mockResolvedValue(mockEmployeeJobs)
  })

  describe('Shifts Display', () => {
    it('should load and display shifts for current period', async () => {
      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
        expect(screen.getByText('Jane Smith')).toBeInTheDocument()
        expect(screen.getByText('Software Engineer')).toBeInTheDocument()
        expect(screen.getByText('Senior Engineer')).toBeInTheDocument()
      })

      expect(adminService.default.getAvailablePaymentPeriods).toHaveBeenCalled()
      expect(adminService.default.getCurrentPaymentPeriod).toHaveBeenCalled()
      expect(adminService.default.getShiftsByPeriodNumber).toHaveBeenCalledWith(
        1,
        expect.objectContaining({ page: 0, size: 20 })
      )
    })

    it('should filter shifts by status tabs', async () => {
      const user = userEvent.setup()
      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      // Click on ACTIVE tab
      const activeTab = screen.getByRole('button', { name: /active/i })
      await user.click(activeTab)

      expect(adminService.default.getShiftsByPeriodAndStatus).toHaveBeenCalledWith(
        1,
        'ACTIVE',
        expect.objectContaining({ page: 0, size: 20 })
      )
    })
  })

  describe('Create Shift Flow', () => {
    it('should create a new shift successfully', async () => {
      const user = userEvent.setup()
      adminService.default.createShift.mockResolvedValue({
        id: 3,
        employeeJobId: 1,
        status: 'ACTIVE'
      })

      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /create shift/i })).toBeInTheDocument()
      })

      // Click create shift button
      await user.click(screen.getByRole('button', { name: /create shift/i }))

      // Should open modal
      await waitFor(() => {
        expect(screen.getByText('Create New Shift')).toBeInTheDocument()
      })

      // Fill in the form
      const departmentSelect = screen.getByLabelText(/department/i)
      await user.selectOptions(departmentSelect, '1')

      await waitFor(() => {
        const employeeSelect = screen.getByLabelText(/employee/i)
        expect(employeeSelect).not.toBeDisabled()
      })

      const employeeSelect = screen.getByLabelText(/employee/i)
      await user.selectOptions(employeeSelect, '1')

      await waitFor(() => {
        const jobSelect = screen.getByLabelText(/job assignment/i)
        expect(jobSelect).not.toBeDisabled()
      })

      const jobSelect = screen.getByLabelText(/job assignment/i)
      await user.selectOptions(jobSelect, '1')

      const clockInInput = screen.getByLabelText(/clock in time/i)
      await user.type(clockInInput, '2023-01-02T09:00')

      const statusSelect = screen.getByLabelText(/status/i)
      await user.selectOptions(statusSelect, 'ACTIVE')

      // Submit form
      const submitButton = screen.getByRole('button', { name: /create/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(adminService.default.createShift).toHaveBeenCalledWith({
          employeeJobId: 1,
          clockIn: '2023-01-02T09:00',
          clockOut: null,
          status: 'ACTIVE'
        })
      })

      // Modal should close
      await waitFor(() => {
        expect(screen.queryByText('Create New Shift')).not.toBeInTheDocument()
      })
    })

    it('should validate required fields', async () => {
      const user = userEvent.setup()
      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /create shift/i })).toBeInTheDocument()
      })

      await user.click(screen.getByRole('button', { name: /create shift/i }))

      await waitFor(() => {
        expect(screen.getByText('Create New Shift')).toBeInTheDocument()
      })

      // Try to submit without filling required fields
      const submitButton = screen.getByRole('button', { name: /create/i })
      await user.click(submitButton)

      await waitFor(() => {
        expect(screen.getByText(/employee job assignment is required/i)).toBeInTheDocument()
        expect(screen.getByText(/clock in time is required/i)).toBeInTheDocument()
        expect(screen.getByText(/status is required/i)).toBeInTheDocument()
      })

      // Should not call API
      expect(adminService.default.createShift).not.toHaveBeenCalled()
    })
  })

  describe('Edit Shift Flow', () => {
    it('should edit an existing shift successfully', async () => {
      const user = userEvent.setup()
      adminService.default.updateShift.mockResolvedValue({
        id: 1,
        status: 'COMPLETED'
      })

      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      // Find and click edit button for first shift
      const editButtons = screen.getAllByTestId('edit-shift-button')
      await user.click(editButtons[0])

      await waitFor(() => {
        expect(screen.getByText('Edit Shift')).toBeInTheDocument()
      })

      // Form should be pre-filled
      expect(screen.getByDisplayValue('2023-01-01T09:00')).toBeInTheDocument()
      expect(screen.getByDisplayValue('2023-01-01T17:00')).toBeInTheDocument()
      expect(screen.getByDisplayValue('COMPLETED')).toBeInTheDocument()

      // Modify clock out time
      const clockOutInput = screen.getByLabelText(/clock out time/i)
      await user.clear(clockOutInput)
      await user.type(clockOutInput, '2023-01-01T18:00')

      // Submit changes
      const updateButton = screen.getByRole('button', { name: /update/i })
      await user.click(updateButton)

      await waitFor(() => {
        expect(adminService.default.updateShift).toHaveBeenCalledWith(1, {
          employeeJobId: 1,
          clockIn: '2023-01-01T09:00',
          clockOut: '2023-01-01T18:00',
          status: 'COMPLETED'
        })
      })
    })
  })

  describe('Bulk Operations', () => {
    it('should enable selection mode and bulk clock in', async () => {
      const user = userEvent.setup()
      adminService.default.clockIn.mockResolvedValue({
        completelySuccessful: true,
        successCount: 1,
        totalProcessed: 1
      })

      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      // Enable selection mode
      const selectButton = screen.getByRole('button', { name: /select/i })
      await user.click(selectButton)

      // Should show checkboxes
      await waitFor(() => {
        const checkboxes = screen.getAllByRole('checkbox')
        expect(checkboxes.length).toBeGreaterThan(0)
      })

      // Select first shift
      const firstCheckbox = screen.getAllByRole('checkbox')[1] // Skip select all
      await user.click(firstCheckbox)

      // Should show bulk action buttons
      await waitFor(() => {
        expect(screen.getByRole('button', { name: /clock in \(1\)/i })).toBeInTheDocument()
      })

      // Click bulk clock in
      await user.click(screen.getByRole('button', { name: /clock in \(1\)/i }))

      // Should open bulk clock modal (if implemented)
      // Or directly call API
      await waitFor(() => {
        expect(adminService.default.clockIn).toHaveBeenCalled()
      })
    })
  })

  describe('Delete Shift', () => {
    it('should delete a shift with confirmation', async () => {
      const user = userEvent.setup()
      adminService.default.deleteShift.mockResolvedValue({})

      // Mock window.confirm
      global.confirm = vi.fn(() => true)

      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      // Find and click delete button
      const deleteButtons = screen.getAllByTestId('delete-shift-button')
      await user.click(deleteButtons[0])

      expect(global.confirm).toHaveBeenCalledWith(
        'Are you sure you want to delete this shift for John Doe?'
      )

      await waitFor(() => {
        expect(adminService.default.deleteShift).toHaveBeenCalledWith(1)
      })
    })

    it('should not delete if user cancels confirmation', async () => {
      const user = userEvent.setup()

      // Mock window.confirm to return false
      global.confirm = vi.fn(() => false)

      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('John Doe')).toBeInTheDocument()
      })

      const deleteButtons = screen.getAllByTestId('delete-shift-button')
      await user.click(deleteButtons[0])

      expect(global.confirm).toHaveBeenCalled()
      expect(adminService.default.deleteShift).not.toHaveBeenCalled()
    })
  })

  describe('Period Navigation', () => {
    it('should change shifts when different period is selected', async () => {
      const user = userEvent.setup()
      render(<ShiftsPage />)

      await waitFor(() => {
        expect(screen.getByText('Jan 1-15, 2023')).toBeInTheDocument()
      })

      // Select different period
      const periodSelector = screen.getByDisplayValue('Jan 1-15, 2023')
      await user.selectOptions(periodSelector, '2')

      await waitFor(() => {
        expect(adminService.default.getShiftsByPeriodNumber).toHaveBeenLastCalledWith(
          2,
          expect.objectContaining({ page: 0, size: 20 })
        )
      })
    })
  })
})