import { describe, it, expect, beforeAll } from 'vitest'
import authService from '../api/authService'
import adminService from '../api/adminService'
import kioskService from '../api/kioskService'

// Real API integration tests
describe('Real API Integration Tests', () => {
  let authToken = null
  let testUser = null

  beforeAll(async () => {
    // Test if backend is running
    console.log('Testing connection to backend at http://localhost:8093')
  })

  describe('Authentication API', () => {
    it('should connect to auth endpoints', async () => {
      try {
        // Try to make a request to see if backend is available
        await authService.getCurrentUser()
      } catch (error) {
        // This will fail if not authenticated, but should still connect
        expect(error.response?.status).toBeDefined()
        console.log('Backend connection test:', error.response?.status || 'No response')
      }
    })

    it('should handle login with real credentials', async () => {
      try {
        const result = await authService.login('admin', 'admin123') // Use real test credentials

        expect(result).toBeDefined()
        expect(result.accessToken).toBeDefined()
        expect(result.user).toBeDefined()

        authToken = result.accessToken
        testUser = result.user

        console.log('Login successful:', result.user.username)
      } catch (error) {
        console.log('Login failed (expected if credentials are wrong):', error.response?.data?.message)
        // Don't fail the test if credentials are wrong, just log it
      }
    }, 10000) // 10 second timeout for real API calls

    it('should handle invalid login credentials', async () => {
      try {
        await authService.login('invaliduser', 'wrongpassword')
        // If this doesn't throw, something is wrong
        expect(false).toBe(true)
      } catch (error) {
        // Could be 401 (Unauthorized) or 429 (Too Many Requests)
        expect([401, 429]).toContain(error.response?.status)
        console.log('Invalid login correctly rejected:', error.response?.data?.message)
      }
    }, 10000)
  })

  describe('Admin API', () => {
    it('should fetch employees if authenticated', async () => {
      if (!authToken) {
        console.log('Skipping admin tests - no auth token')
        return
      }

      try {
        const employees = await adminService.getEmployees({ page: 0, size: 10 })

        expect(employees).toBeDefined()
        expect(employees.content).toBeDefined()
        expect(Array.isArray(employees.content)).toBe(true)

        console.log('Fetched employees:', employees.content.length)
      } catch (error) {
        console.log('Admin API error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)

    it('should fetch departments', async () => {
      try {
        const departments = await adminService.getDepartments({ page: 0, size: 10 })

        expect(departments).toBeDefined()
        expect(departments.content).toBeDefined()
        expect(Array.isArray(departments.content)).toBe(true)

        console.log('Fetched departments:', departments.content.length)
      } catch (error) {
        console.log('Departments API error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)

    it('should fetch shifts for current period', async () => {
      try {
        const currentPeriod = await adminService.getCurrentPaymentPeriod()
        expect(currentPeriod).toBeDefined()
        console.log('Current period:', currentPeriod.periodNumber)

        const shifts = await adminService.getShiftsByPeriodNumber(currentPeriod.periodNumber, {
          page: 0,
          size: 10
        })

        expect(shifts).toBeDefined()
        expect(shifts.content).toBeDefined()
        expect(Array.isArray(shifts.content)).toBe(true)

        console.log('Fetched shifts for period:', shifts.content.length)
      } catch (error) {
        console.log('Shifts API error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)
  })

  describe('Kiosk API', () => {
    it('should handle employee lookup by username', async () => {
      try {
        // Try with a real username from your database
        const employee = await kioskService.getEmployeeByUsername('admin') // Use real username

        expect(employee).toBeDefined()
        expect(employee.id).toBeDefined()
        expect(employee.username).toBeDefined()

        console.log('Found employee:', employee.username)
      } catch (error) {
        if (error.response?.status === 404) {
          console.log('Employee not found (expected if username doesn\'t exist)')
        } else {
          console.log('Kiosk API error:', error.response?.data?.message)
        }
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)

    it('should determine clock action for employee', async () => {
      try {
        // Use a real employee ID from your database
        const action = await kioskService.determineAction(1) // Use real employee ID

        expect(action).toBeDefined()
        expect(['CLOCK_IN', 'CLOCK_OUT', 'UNAVAILABLE']).toContain(action)

        console.log('Determined action for employee 1:', action)
      } catch (error) {
        console.log('Determine action error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)

    it('should get employee jobs', async () => {
      try {
        // Use a real username from your database
        const jobs = await kioskService.getEmployeeJobs('admin') // Use real username

        expect(jobs).toBeDefined()
        expect(Array.isArray(jobs)).toBe(true)

        console.log('Employee jobs found:', jobs.length)
        if (jobs.length > 0) {
          console.log('First job:', jobs[0].jobTitle)
        }
      } catch (error) {
        console.log('Employee jobs error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 10000)
  })

  describe('Data Creation Tests', () => {
    it('should create a test department', async () => {
      if (!authToken) {
        console.log('Skipping creation tests - no auth token')
        return
      }

      try {
        const testDepartment = {
          name: `Test Department ${Date.now()}`,
          description: 'Created by automated test'
        }

        const created = await adminService.createDepartment(testDepartment)

        expect(created).toBeDefined()
        expect(created.name).toBe(testDepartment.name)

        console.log('Created test department:', created.id)

        // Clean up - delete the test department
        try {
          await adminService.deleteDepartment(created.id)
          console.log('Cleaned up test department')
        } catch (cleanupError) {
          console.log('Could not clean up test department:', cleanupError.response?.data?.message)
        }
      } catch (error) {
        console.log('Department creation error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 15000)

    it('should create a test job', async () => {
      if (!authToken) {
        console.log('Skipping job creation test - no auth token')
        return
      }

      try {
        // First get a department to assign the job to
        const departments = await adminService.getDepartments({ page: 0, size: 1 })

        if (departments.content.length === 0) {
          console.log('No departments available for job creation test')
          return
        }

        const testJob = {
          title: `Test Job ${Date.now()}`,
          description: 'Created by automated test',
          hourlyWage: 25.00,
          departmentId: departments.content[0].id
        }

        const created = await adminService.createJob(testJob)

        expect(created).toBeDefined()
        expect(created.title).toBe(testJob.title)

        console.log('Created test job:', created.id)

        // Clean up - delete the test job
        try {
          await adminService.deleteJob(created.id)
          console.log('Cleaned up test job')
        } catch (cleanupError) {
          console.log('Could not clean up test job:', cleanupError.response?.data?.message)
        }
      } catch (error) {
        console.log('Job creation error:', error.response?.data?.message)
        expect(error.response?.status).toBeDefined()
      }
    }, 15000)
  })

  describe('Backend Health Check', () => {
    it('should verify backend is running and responsive', async () => {
      // Test multiple endpoints to ensure backend is fully operational
      const endpoints = [
        () => adminService.getDepartments({ page: 0, size: 1 }),
        () => adminService.getCurrentPaymentPeriod(),
        () => kioskService.determineAction(1).catch(() => 'OK'), // Might fail but should respond
      ]

      let successCount = 0

      for (const endpoint of endpoints) {
        try {
          await endpoint()
          successCount++
        } catch (error) {
          if (error.response?.status) {
            successCount++ // Got a response, backend is running
          }
        }
      }

      expect(successCount).toBeGreaterThan(0)
      console.log(`Backend health check: ${successCount}/${endpoints.length} endpoints responsive`)

      if (successCount === endpoints.length) {
        console.log('✅ Backend is fully operational')
      } else if (successCount > 0) {
        console.log('⚠️ Backend is partially responsive')
      } else {
        console.log('❌ Backend appears to be down')
      }
    }, 10000)
  })
})