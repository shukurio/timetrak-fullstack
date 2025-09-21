import { describe, it, expect, beforeEach, vi } from 'vitest'
import kioskService from '../kioskService'
import axios from 'axios'

// Mock axios
vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  }
}))

describe('KioskService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getEmployeeByUsername', () => {
    it('should get employee by username successfully', async () => {
      const mockEmployee = {
        data: {
          id: 1,
          firstName: 'John',
          lastName: 'Doe',
          username: 'johndoe',
          status: 'ACTIVE'
        }
      }
      axios.get.mockResolvedValue(mockEmployee)

      const result = await kioskService.getEmployeeByUsername('johndoe')

      expect(axios.get).toHaveBeenCalledWith('http://localhost:8093/api/kiosk/johndoe')
      expect(result).toEqual(mockEmployee.data)
    })

    it('should handle employee not found', async () => {
      const mockError = new Error('Employee not found')
      mockError.response = { status: 404, data: { message: 'Employee not found' } }
      axios.get.mockRejectedValue(mockError)

      await expect(kioskService.getEmployeeByUsername('nonexistent'))
        .rejects.toThrow('Employee not found')
    })
  })

  describe('getEmployeeJobs', () => {
    it('should get employee jobs successfully', async () => {
      const mockJobs = {
        data: [
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
      }
      axios.get.mockResolvedValue(mockJobs)

      const result = await kioskService.getEmployeeJobs('johndoe')

      expect(axios.get).toHaveBeenCalledWith('http://localhost:8093/api/kiosk/jobs/johndoe')
      expect(result).toEqual(mockJobs.data)
    })

    it('should handle no jobs found', async () => {
      const mockResponse = { data: [] }
      axios.get.mockResolvedValue(mockResponse)

      const result = await kioskService.getEmployeeJobs('johndoe')

      expect(result).toEqual([])
    })
  })

  describe('determineAction', () => {
    it('should determine CLOCK_IN action', async () => {
      const mockResponse = { data: 'CLOCK_IN' }
      axios.get.mockResolvedValue(mockResponse)

      const result = await kioskService.determineAction(1)

      expect(axios.get).toHaveBeenCalledWith('http://localhost:8093/api/kiosk/determineAction/1')
      expect(result).toBe('CLOCK_IN')
    })

    it('should determine CLOCK_OUT action', async () => {
      const mockResponse = { data: 'CLOCK_OUT' }
      axios.get.mockResolvedValue(mockResponse)

      const result = await kioskService.determineAction(1)

      expect(result).toBe('CLOCK_OUT')
    })

    it('should handle UNAVAILABLE action', async () => {
      const mockResponse = { data: 'UNAVAILABLE' }
      axios.get.mockResolvedValue(mockResponse)

      const result = await kioskService.determineAction(1)

      expect(result).toBe('UNAVAILABLE')
    })
  })

  describe('clockIn', () => {
    it('should clock in successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          employeeJobId: 1,
          clockIn: '2023-01-01T09:00:00',
          status: 'ACTIVE',
          jobTitle: 'Software Engineer'
        }
      }
      const clockData = {
        id: 1,
        latitude: 40.7128,
        longitude: -74.0060
      }
      axios.post.mockResolvedValue(mockResponse)

      const result = await kioskService.clockIn(clockData)

      expect(axios.post).toHaveBeenCalledWith('http://localhost:8093/api/kiosk/clock-in', clockData)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle clock in validation error', async () => {
      const mockError = new Error('Validation failed')
      mockError.response = {
        status: 400,
        data: { message: 'Latitude and longitude are required' }
      }
      const clockData = { id: 1 } // Missing lat/lng
      axios.post.mockRejectedValue(mockError)

      await expect(kioskService.clockIn(clockData))
        .rejects.toThrow('Validation failed')
    })
  })

  describe('clockOut', () => {
    it('should clock out successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          employeeId: 1,
          clockIn: '2023-01-01T09:00:00',
          clockOut: '2023-01-01T17:00:00',
          status: 'COMPLETED',
          hours: 8.0,
          shiftEarnings: 240.0,
          hourlyWage: 30.0
        }
      }
      const clockData = {
        id: 1,
        latitude: 40.7128,
        longitude: -74.0060
      }
      axios.post.mockResolvedValue(mockResponse)

      const result = await kioskService.clockOut(clockData)

      expect(axios.post).toHaveBeenCalledWith('http://localhost:8093/api/kiosk/clock-out', clockData)
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle clock out when no active shift', async () => {
      const mockError = new Error('No active shift found')
      mockError.response = {
        status: 404,
        data: { message: 'No active shift found for employee' }
      }
      const clockData = {
        id: 1,
        latitude: 40.7128,
        longitude: -74.0060
      }
      axios.post.mockRejectedValue(mockError)

      await expect(kioskService.clockOut(clockData))
        .rejects.toThrow('No active shift found')
    })
  })
})