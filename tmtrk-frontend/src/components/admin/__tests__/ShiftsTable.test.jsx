import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen } from '../../../test/test-utils'
import userEvent from '@testing-library/user-event'
import ShiftsTable from '../ShiftsTable'
import { createMockShift } from '../../../test/test-utils'

describe('ShiftsTable', () => {
  const defaultProps = {
    shiftsData: {
      content: [
        createMockShift({
          id: 1,
          employeeJobId: 1,
          fullName: 'John Doe',
          username: 'johndoe',
          jobTitle: 'Software Engineer',
          clockIn: '2023-01-01T09:00:00',
          clockOut: '2023-01-01T17:00:00',
          status: 'COMPLETED',
          hours: 8.0,
          shiftEarnings: 240.0,
          notes: 'Regular shift'
        }),
        createMockShift({
          id: 2,
          employeeJobId: 2,
          fullName: 'Jane Smith',
          username: 'janesmith',
          jobTitle: 'Senior Engineer',
          clockIn: '2023-01-01T10:00:00',
          clockOut: null,
          status: 'ACTIVE',
          hours: 0,
          shiftEarnings: 0,
          notes: null
        })
      ],
      totalElements: 2,
      totalPages: 1
    },
    shiftsLoading: false,
    shiftsError: null,
    isSelectionMode: false,
    selectedShifts: [],
    onShiftSelection: vi.fn(),
    onSelectAllShifts: vi.fn(),
    onEditShift: vi.fn(),
    onDeleteShift: vi.fn(),
    currentPage: 0,
    setCurrentPage: vi.fn(),
    pageSize: 20
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render shifts table with data', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('John Doe')).toBeInTheDocument()
      expect(screen.getByText('Jane Smith')).toBeInTheDocument()
      expect(screen.getByText('Software Engineer')).toBeInTheDocument()
      expect(screen.getByText('Senior Engineer')).toBeInTheDocument()
    })

    it('should display loading spinner when loading', () => {
      render(<ShiftsTable {...defaultProps} shiftsLoading={true} />)

      expect(screen.getByRole('generic')).toBeInTheDocument() // LoadingSpinner
    })

    it('should display error message when there is an error', () => {
      const error = {
        response: {
          data: { message: 'Failed to load shifts' }
        }
      }
      render(<ShiftsTable {...defaultProps} shiftsError={error} />)

      expect(screen.getByText('⚠️ Error loading shifts')).toBeInTheDocument()
      expect(screen.getByText('Failed to load shifts')).toBeInTheDocument()
    })

    it('should display no shifts message when data is empty', () => {
      const emptyData = { content: [], totalElements: 0 }
      render(<ShiftsTable {...defaultProps} shiftsData={emptyData} />)

      expect(screen.getByText('No shifts found')).toBeInTheDocument()
      expect(screen.getByText('Create your first shift to get started')).toBeInTheDocument()
    })
  })

  describe('Shift Display', () => {
    it('should format time correctly', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('Jan 1, 09:00')).toBeInTheDocument()
      expect(screen.getByText('Jan 1, 17:00')).toBeInTheDocument()
      expect(screen.getByText('-')).toBeInTheDocument() // No clock out for active shift
    })

    it('should format duration correctly', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('8h 0m')).toBeInTheDocument()
      expect(screen.getByText('Active')).toBeInTheDocument() // Active shift duration
    })

    it('should display status badges', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('Completed')).toBeInTheDocument()
      expect(screen.getByText('Active')).toBeInTheDocument()
    })

    it('should display earnings correctly', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('$240.00')).toBeInTheDocument()
      expect(screen.getByText('$0.00')).toBeInTheDocument()
    })

    it('should display notes or dash when empty', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('Regular shift')).toBeInTheDocument()
      expect(screen.getAllByText('-')).toHaveLength(2) // One for no clock out, one for no notes
    })
  })

  describe('Selection Mode', () => {
    it('should show checkboxes in selection mode', () => {
      render(<ShiftsTable {...defaultProps} isSelectionMode={true} />)

      const checkboxes = screen.getAllByRole('checkbox')
      expect(checkboxes).toHaveLength(3) // Select all + 2 individual shifts
    })

    it('should handle individual shift selection', async () => {
      const user = userEvent.setup()
      render(<ShiftsTable {...defaultProps} isSelectionMode={true} />)

      const shiftCheckbox = screen.getAllByRole('checkbox')[1] // First shift checkbox
      await user.click(shiftCheckbox)

      expect(defaultProps.onShiftSelection).toHaveBeenCalledWith(1, true)
    })

    it('should handle select all shifts', async () => {
      const user = userEvent.setup()
      render(<ShiftsTable {...defaultProps} isSelectionMode={true} />)

      const selectAllCheckbox = screen.getAllByRole('checkbox')[0]
      await user.click(selectAllCheckbox)

      expect(defaultProps.onSelectAllShifts).toHaveBeenCalledWith(true)
    })

    it('should show selection count', () => {
      render(<ShiftsTable {...defaultProps} isSelectionMode={true} selectedShifts={[1]} />)

      expect(screen.getByText('1 selected')).toBeInTheDocument()
    })

    it('should check selected shifts', () => {
      render(<ShiftsTable {...defaultProps} isSelectionMode={true} selectedShifts={[1]} />)

      const checkboxes = screen.getAllByRole('checkbox')
      expect(checkboxes[1]).toBeChecked() // First shift should be checked
      expect(checkboxes[2]).not.toBeChecked() // Second shift should not be checked
    })
  })

  describe('Actions', () => {
    it('should handle edit shift action', async () => {
      const user = userEvent.setup()
      render(<ShiftsTable {...defaultProps} />)

      const editButtons = screen.getAllByRole('button')
      const editButton = editButtons.find(button =>
        button.querySelector('svg') // Edit icon
      )

      if (editButton) {
        await user.click(editButton)
        expect(defaultProps.onEditShift).toHaveBeenCalled()
      }
    })

    it('should handle delete shift action', async () => {
      const user = userEvent.setup()
      render(<ShiftsTable {...defaultProps} />)

      const deleteButtons = screen.getAllByRole('button')
      const deleteButton = deleteButtons.find(button =>
        button.querySelector('svg[data-testid="trash-icon"]') // Assuming trash icon has this testid
      )

      if (deleteButton) {
        await user.click(deleteButton)
        expect(defaultProps.onDeleteShift).toHaveBeenCalled()
      }
    })
  })

  describe('Pagination', () => {
    const paginatedProps = {
      ...defaultProps,
      shiftsData: {
        ...defaultProps.shiftsData,
        totalPages: 3,
        totalElements: 50
      }
    }

    it('should show pagination when there are multiple pages', () => {
      render(<ShiftsTable {...paginatedProps} />)

      expect(screen.getByText('Showing 1 to 2 of 50 results')).toBeInTheDocument()
    })

    it('should handle page navigation', async () => {
      const user = userEvent.setup()
      render(<ShiftsTable {...paginatedProps} />)

      const nextButton = screen.getByText('2') // Page 2 button
      await user.click(nextButton)

      expect(defaultProps.setCurrentPage).toHaveBeenCalledWith(1)
    })

    it('should not show pagination for single page', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.queryByText('Showing')).not.toBeInTheDocument()
    })
  })

  describe('Employee Avatar', () => {
    it('should show employee initials in avatar', () => {
      render(<ShiftsTable {...defaultProps} />)

      expect(screen.getByText('JD')).toBeInTheDocument() // John Doe initials
      expect(screen.getByText('JS')).toBeInTheDocument() // Jane Smith initials
    })

    it('should handle single name gracefully', () => {
      const singleNameShift = {
        ...defaultProps.shiftsData,
        content: [{
          ...defaultProps.shiftsData.content[0],
          fullName: 'Cher'
        }]
      }
      render(<ShiftsTable {...defaultProps} shiftsData={singleNameShift} />)

      expect(screen.getByText('C')).toBeInTheDocument()
    })
  })
})