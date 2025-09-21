import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '../../../test/test-utils'
import userEvent from '@testing-library/user-event'
import ShiftModal from '../ShiftModal'
import * as adminService from '../../../api/adminService'

// Mock adminService
vi.mock('../../../api/adminService', () => ({
  default: {
    getDepartments: vi.fn(),
    getEmployeesByDepartment: vi.fn(),
    getEmployeeJobsByEmployee: vi.fn(),
  }
}))

describe('ShiftModal', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onSubmit: vi.fn(),
    isLoading: false,
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
    adminService.default.getDepartments.mockResolvedValue(mockDepartments)
    adminService.default.getEmployeesByDepartment.mockResolvedValue(mockEmployees)
    adminService.default.getEmployeeJobsByEmployee.mockResolvedValue(mockEmployeeJobs)
  })

  describe('Create Mode', () => {
    it('should render create shift form', async () => {
      render(<ShiftModal {...defaultProps} />)

      expect(screen.getByText('Create New Shift')).toBeInTheDocument()
      expect(screen.getByLabelText(/Department/)).toBeInTheDocument()
      expect(screen.getByLabelText(/Employee/)).toBeInTheDocument()
      expect(screen.getByLabelText(/Job Assignment/)).toBeInTheDocument()
      expect(screen.getByLabelText(/Clock In Time/)).toBeInTheDocument()
      expect(screen.getByLabelText(/Status/)).toBeInTheDocument()
    })

    it('should show department options', async () => {
      render(<ShiftModal {...defaultProps} />)

      await waitFor(() => {
        expect(screen.getByDisplayValue('')).toBeInTheDocument()
      })

      const departmentSelect = screen.getByLabelText(/Department/)
      expect(departmentSelect).toBeInTheDocument()
    })

    it('should enable employee dropdown after department selection', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      const departmentSelect = screen.getByLabelText(/Department/)
      const employeeSelect = screen.getByLabelText(/Employee/)

      // Initially employee should be disabled
      expect(employeeSelect).toBeDisabled()

      // Select a department
      await user.selectOptions(departmentSelect, '1')

      await waitFor(() => {
        expect(employeeSelect).not.toBeDisabled()
      })
    })

    it('should enable job assignment dropdown after employee selection', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      const departmentSelect = screen.getByLabelText(/Department/)
      const employeeSelect = screen.getByLabelText(/Employee/)
      const jobSelect = screen.getByLabelText(/Job Assignment/)

      // Initially job select should be disabled
      expect(jobSelect).toBeDisabled()

      // Select department and employee
      await user.selectOptions(departmentSelect, '1')
      await waitFor(() => expect(employeeSelect).not.toBeDisabled())

      await user.selectOptions(employeeSelect, '1')
      await waitFor(() => {
        expect(jobSelect).not.toBeDisabled()
      })
    })

    it('should submit form with correct data', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      // Fill in form
      await user.selectOptions(screen.getByLabelText(/Department/), '1')
      await waitFor(() => expect(screen.getByLabelText(/Employee/)).not.toBeDisabled())

      await user.selectOptions(screen.getByLabelText(/Employee/), '1')
      await waitFor(() => expect(screen.getByLabelText(/Job Assignment/)).not.toBeDisabled())

      await user.selectOptions(screen.getByLabelText(/Job Assignment/), '1')
      await user.type(screen.getByLabelText(/Clock In Time/), '2023-01-01T09:00')
      await user.selectOptions(screen.getByLabelText(/Status/), 'ACTIVE')

      // Submit form
      await user.click(screen.getByRole('button', { name: /create/i }))

      await waitFor(() => {
        expect(defaultProps.onSubmit).toHaveBeenCalledWith({
          employeeJobId: 1,
          clockIn: '2023-01-01T09:00',
          clockOut: null,
          status: 'ACTIVE'
        })
      })
    })
  })

  describe('Edit Mode', () => {
    const editingShift = {
      id: 1,
      employeeJobId: 1,
      employeeId: 1,
      fullName: 'John Doe',
      jobTitle: 'Software Engineer',
      clockIn: '2023-01-01T09:00:00',
      clockOut: '2023-01-01T17:00:00',
      status: 'COMPLETED',
      notes: 'Test shift'
    }

    it('should render edit shift form', () => {
      render(<ShiftModal {...defaultProps} editingShift={editingShift} />)

      expect(screen.getByText('Edit Shift')).toBeInTheDocument()
      // Should not show department/employee selectors in edit mode
      expect(screen.queryByLabelText(/Department/)).not.toBeInTheDocument()
      expect(screen.queryByLabelText(/Employee/)).not.toBeInTheDocument()
    })

    it('should populate form with existing shift data', async () => {
      render(<ShiftModal {...defaultProps} editingShift={editingShift} />)

      await waitFor(() => {
        expect(screen.getByDisplayValue('2023-01-01T09:00')).toBeInTheDocument()
        expect(screen.getByDisplayValue('2023-01-01T17:00')).toBeInTheDocument()
        expect(screen.getByDisplayValue('COMPLETED')).toBeInTheDocument()
        expect(screen.getByDisplayValue('Test shift')).toBeInTheDocument()
      })
    })

    it('should show current job title by default', async () => {
      render(<ShiftModal {...defaultProps} editingShift={editingShift} />)

      await waitFor(() => {
        const jobSelect = screen.getByLabelText(/Job Assignment/)
        expect(jobSelect).toHaveDisplayValue('Software Engineer')
      })
    })

    it('should load additional job options when dropdown is clicked', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} editingShift={editingShift} />)

      const jobSelect = screen.getByLabelText(/Job Assignment/)

      // Click on the select to trigger loading
      await user.click(jobSelect)

      await waitFor(() => {
        expect(adminService.default.getEmployeeJobsByEmployee).toHaveBeenCalledWith(1)
      })
    })
  })

  describe('Validation', () => {
    it('should show validation errors for required fields', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      // Try to submit without filling required fields
      await user.click(screen.getByRole('button', { name: /create/i }))

      await waitFor(() => {
        expect(screen.getByText(/Employee job assignment is required/)).toBeInTheDocument()
        expect(screen.getByText(/Clock in time is required/)).toBeInTheDocument()
        expect(screen.getByText(/Status is required/)).toBeInTheDocument()
      })
    })

    it('should validate clock out is after clock in', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      // Fill in valid data first
      await user.selectOptions(screen.getByLabelText(/Department/), '1')
      await waitFor(() => expect(screen.getByLabelText(/Employee/)).not.toBeDisabled())

      await user.selectOptions(screen.getByLabelText(/Employee/), '1')
      await waitFor(() => expect(screen.getByLabelText(/Job Assignment/)).not.toBeDisabled())

      await user.selectOptions(screen.getByLabelText(/Job Assignment/), '1')
      await user.type(screen.getByLabelText(/Clock In Time/), '2023-01-01T17:00')
      await user.type(screen.getByLabelText(/Clock Out Time/), '2023-01-01T09:00')
      await user.selectOptions(screen.getByLabelText(/Status/), 'COMPLETED')

      await user.click(screen.getByRole('button', { name: /create/i }))

      await waitFor(() => {
        expect(screen.getByText(/Clock out must be after clock in/)).toBeInTheDocument()
      })
    })
  })

  describe('Modal Controls', () => {
    it('should close modal when close button is clicked', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      await user.click(screen.getByRole('button', { name: /×/ }))
      expect(defaultProps.onClose).toHaveBeenCalled()
    })

    it('should close modal when cancel button is clicked', async () => {
      const user = userEvent.setup()
      render(<ShiftModal {...defaultProps} />)

      await user.click(screen.getByRole('button', { name: /cancel/i }))
      expect(defaultProps.onClose).toHaveBeenCalled()
    })

    it('should not render when isOpen is false', () => {
      render(<ShiftModal {...defaultProps} isOpen={false} />)

      expect(screen.queryByText('Create New Shift')).not.toBeInTheDocument()
    })

    it('should show loading state when isLoading is true', () => {
      render(<ShiftModal {...defaultProps} isLoading={true} />)

      const submitButton = screen.getByRole('button', { name: /create/i })
      expect(submitButton).toBeDisabled()
    })
  })
})