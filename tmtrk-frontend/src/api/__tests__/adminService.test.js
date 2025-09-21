import { describe, it, expect, beforeEach, vi } from 'vitest'
import adminService from '../adminService'
import apiClient from '../client'

// Mock the API client
vi.mock('../client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  }
}))

describe('AdminService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Employee Management', () => {
    it('should get all employees', async () => {
      const mockEmployees = {
        data: {
          content: [
            { id: 1, firstName: 'John', lastName: 'Doe', status: 'ACTIVE' },
            { id: 2, firstName: 'Jane', lastName: 'Smith', status: 'PENDING' }
          ],
          totalElements: 2
        }
      }
      apiClient.get.mockResolvedValue(mockEmployees)

      const result = await adminService.getEmployees({ page: 0, size: 10 })

      expect(apiClient.get).toHaveBeenCalledWith('/admin/employees/all', {
        params: { page: 0, size: 10 }
      })
      expect(result).toEqual(mockEmployees.data)
    })

    it('should get active employees', async () => {
      const mockEmployees = { data: { content: [], totalElements: 0 } }
      apiClient.get.mockResolvedValue(mockEmployees)

      const result = await adminService.getActiveEmployees()

      expect(apiClient.get).toHaveBeenCalledWith('/admin/employees/active', {
        params: {}
      })
      expect(result).toEqual(mockEmployees.data)
    })

    it('should get employees by department', async () => {
      const mockEmployees = { data: { content: [], totalElements: 0 } }
      apiClient.get.mockResolvedValue(mockEmployees)

      const result = await adminService.getEmployeesByDepartment(1, { page: 0 })

      expect(apiClient.get).toHaveBeenCalledWith('/admin/employees/department/1', {
        params: { page: 0 }
      })
      expect(result).toEqual(mockEmployees.data)
    })

    it('should register employee', async () => {
      const mockEmployee = { data: { id: 1, firstName: 'John' } }
      const employeeData = {
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com'
      }
      apiClient.post.mockResolvedValue(mockEmployee)

      const result = await adminService.registerEmployee(employeeData)

      expect(apiClient.post).toHaveBeenCalledWith('/admin/employees/register', employeeData)
      expect(result).toEqual(mockEmployee.data)
    })

    it('should approve employee', async () => {
      const mockResponse = { data: { message: 'Employee approved' } }
      apiClient.put.mockResolvedValue(mockResponse)

      const result = await adminService.approveEmployee(1)

      expect(apiClient.put).toHaveBeenCalledWith('/admin/employees/1/approve')
      expect(result).toEqual(mockResponse.data)
    })
  })

  describe('Department Management', () => {
    it('should get departments', async () => {
      const mockDepartments = {
        data: {
          content: [
            { id: 1, name: 'Engineering' },
            { id: 2, name: 'Marketing' }
          ]
        }
      }
      apiClient.get.mockResolvedValue(mockDepartments)

      const result = await adminService.getDepartments({ page: 0, size: 10 })

      expect(apiClient.get).toHaveBeenCalledWith('/admin/organization/departments', {
        params: { page: 0, size: 10 }
      })
      expect(result).toEqual(mockDepartments.data)
    })

    it('should create department', async () => {
      const mockDepartment = { data: { id: 1, name: 'Engineering' } }
      const departmentData = { name: 'Engineering', description: 'Software Dev' }
      apiClient.post.mockResolvedValue(mockDepartment)

      const result = await adminService.createDepartment(departmentData)

      expect(apiClient.post).toHaveBeenCalledWith('/admin/organization/departments', departmentData)
      expect(result).toEqual(mockDepartment.data)
    })
  })

  describe('Shift Management', () => {
    it('should get shifts', async () => {
      const mockShifts = {
        data: {
          content: [
            { id: 1, employeeName: 'John Doe', status: 'COMPLETED' }
          ]
        }
      }
      apiClient.get.mockResolvedValue(mockShifts)

      const result = await adminService.getShifts({ page: 0 })

      expect(apiClient.get).toHaveBeenCalledWith('/admin/shifts/status/COMPLETED', {
        params: { page: 0 }
      })
      expect(result).toEqual(mockShifts.data)
    })

    it('should create shift', async () => {
      const mockShift = { data: { id: 1, employeeJobId: 1 } }
      const shiftData = {
        employeeJobId: 1,
        clockIn: '2023-01-01T09:00:00',
        status: 'ACTIVE'
      }
      apiClient.post.mockResolvedValue(mockShift)

      const result = await adminService.createShift(shiftData)

      expect(apiClient.post).toHaveBeenCalledWith('/admin/shifts/', shiftData)
      expect(result).toEqual(mockShift.data)
    })

    it('should update shift', async () => {
      const mockShift = { data: { id: 1, status: 'COMPLETED' } }
      const shiftData = { status: 'COMPLETED', clockOut: '2023-01-01T17:00:00' }
      apiClient.put.mockResolvedValue(mockShift)

      const result = await adminService.updateShift(1, shiftData)

      expect(apiClient.put).toHaveBeenCalledWith('/admin/shifts/1', shiftData)
      expect(result).toEqual(mockShift.data)
    })

    it('should delete shift', async () => {
      const mockResponse = { data: { message: 'Shift deleted' } }
      apiClient.delete.mockResolvedValue(mockResponse)

      const result = await adminService.deleteShift(1)

      expect(apiClient.delete).toHaveBeenCalledWith('/admin/shifts/1')
      expect(result).toEqual(mockResponse.data)
    })
  })

  describe('Job Management', () => {
    it('should get jobs', async () => {
      const mockJobs = { data: [{ id: 1, title: 'Software Engineer' }] }
      apiClient.get.mockResolvedValue(mockJobs)

      const result = await adminService.getJobs()

      expect(apiClient.get).toHaveBeenCalledWith('/admin/jobs')
      expect(result).toEqual(mockJobs.data)
    })

    it('should create job', async () => {
      const mockJob = { data: { id: 1, title: 'Software Engineer' } }
      const jobData = {
        title: 'Software Engineer',
        hourlyWage: 30.0,
        departmentId: 1
      }
      apiClient.post.mockResolvedValue(mockJob)

      const result = await adminService.createJob(jobData)

      expect(apiClient.post).toHaveBeenCalledWith('/admin/jobs/create', jobData)
      expect(result).toEqual(mockJob.data)
    })
  })

  describe('Employee-Job Assignments', () => {
    it('should get employee jobs by employee', async () => {
      const mockJobs = { data: [{ employeeJobId: 1, jobTitle: 'Engineer' }] }
      apiClient.get.mockResolvedValue(mockJobs)

      const result = await adminService.getEmployeeJobsByEmployee(1)

      expect(apiClient.get).toHaveBeenCalledWith('/admin/employee-jobs/employee/1')
      expect(result).toEqual(mockJobs.data)
    })

    it('should assign job to employees', async () => {
      const mockResponse = { data: { message: 'Job assigned' } }
      const assignmentData = { employeeIds: [1, 2], jobId: 1 }
      apiClient.post.mockResolvedValue(mockResponse)

      const result = await adminService.assignJobToEmployees(assignmentData)

      expect(apiClient.post).toHaveBeenCalledWith('/admin/employee-jobs/assign', assignmentData)
      expect(result).toEqual(mockResponse.data)
    })
  })
})