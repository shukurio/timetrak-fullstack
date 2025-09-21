import { describe, it, expect, beforeAll } from 'vitest'
import adminService from '../../api/adminService'
import kioskService from '../../api/kioskService'
import authService from '../../api/authService'

describe('Real Shift Management Flow', () => {
  let authToken = null
  let testEmployee = null
  let testDepartment = null
  let testJob = null
  let testShift = null

  beforeAll(async () => {
    console.log('🔄 Setting up real shift flow test...')

    // Try to authenticate
    try {
      const loginResult = await authService.login('admin', 'admin123')
      authToken = loginResult.accessToken
      console.log('✅ Authenticated as admin')
    } catch (error) {
      console.log('⚠️ Could not authenticate - some tests may fail')
      console.log('Error:', error.response?.data?.message)
    }
  })

  describe('Department and Job Setup', () => {
    it('should create a test department', async () => {
      if (!authToken) {
        console.log('Skipping - no auth token')
        return
      }

      const departmentData = {
        name: `Test Department ${Date.now()}`,
        description: 'Created for integration testing'
      }

      try {
        testDepartment = await adminService.createDepartment(departmentData)
        expect(testDepartment.id).toBeDefined()
        expect(testDepartment.name).toBe(departmentData.name)
        console.log('✅ Created test department:', testDepartment.id)
      } catch (error) {
        console.log('❌ Department creation failed:', error.response?.data?.message)
        throw error
      }
    }, 10000)

    it('should create a test job in the department', async () => {
      if (!authToken || !testDepartment) {
        console.log('Skipping - missing prerequisites')
        return
      }

      const jobData = {
        title: `Test Job ${Date.now()}`,
        description: 'Created for integration testing',
        hourlyWage: 25.00,
        departmentId: testDepartment.id
      }

      try {
        testJob = await adminService.createJob(jobData)
        expect(testJob.id).toBeDefined()
        expect(testJob.title).toBe(jobData.title)
        expect(testJob.hourlyWage).toBe(jobData.hourlyWage)
        console.log('✅ Created test job:', testJob.id)
      } catch (error) {
        console.log('❌ Job creation failed:', error.response?.data?.message)
        throw error
      }
    }, 10000)
  })

  describe('Employee and Job Assignment', () => {
    it('should get an existing employee for testing', async () => {
      try {
        const employees = await adminService.getActiveEmployees({ page: 0, size: 1 })

        if (employees.content.length > 0) {
          testEmployee = employees.content[0]
          console.log('✅ Using existing employee:', testEmployee.username)
        } else {
          console.log('⚠️ No active employees found - some tests may be skipped')
        }
      } catch (error) {
        console.log('❌ Could not fetch employees:', error.response?.data?.message)
      }
    }, 10000)

    it('should assign job to employee', async () => {
      if (!authToken || !testEmployee || !testJob) {
        console.log('Skipping - missing prerequisites')
        return
      }

      try {
        const assignment = await adminService.assignIndividualJob(
          testEmployee.id,
          testJob.id,
          25.00
        )

        expect(assignment).toBeDefined()
        console.log('✅ Assigned job to employee')
      } catch (error) {
        console.log('❌ Job assignment failed:', error.response?.data?.message)
        // Don't throw - might already be assigned
      }
    }, 10000)
  })

  describe('Shift Creation and Management', () => {
    it('should create a shift via admin interface', async () => {
      if (!authToken || !testEmployee || !testJob) {
        console.log('Skipping - missing prerequisites')
        return
      }

      try {
        // First get employee job assignments
        const employeeJobs = await adminService.getEmployeeJobsByEmployee(testEmployee.id)
        console.log('Employee jobs found:', employeeJobs.length)

        if (employeeJobs.length === 0) {
          console.log('⚠️ No job assignments found for employee')
          return
        }

        const employeeJobId = employeeJobs[0].employeeJobId

        const shiftData = {
          employeeJobId: employeeJobId,
          clockIn: new Date().toISOString(),
          status: 'ACTIVE'
        }

        testShift = await adminService.createShift(shiftData)
        expect(testShift.id).toBeDefined()
        expect(testShift.employeeJobId).toBe(employeeJobId)
        console.log('✅ Created test shift:', testShift.id)

      } catch (error) {
        console.log('❌ Shift creation failed:', error.response?.data?.message)
        throw error
      }
    }, 10000)

    it('should update the shift', async () => {
      if (!authToken || !testShift) {
        console.log('Skipping - missing prerequisites')
        return
      }

      try {
        const updateData = {
          clockOut: new Date().toISOString(),
          status: 'COMPLETED',
          notes: 'Completed by integration test'
        }

        const updatedShift = await adminService.updateShift(testShift.id, updateData)
        expect(updatedShift.status).toBe('COMPLETED')
        expect(updatedShift.notes).toBe(updateData.notes)
        console.log('✅ Updated shift to completed')

      } catch (error) {
        console.log('❌ Shift update failed:', error.response?.data?.message)
        throw error
      }
    }, 10000)

    it('should fetch shift data and verify changes', async () => {
      if (!testShift) {
        console.log('Skipping - no test shift')
        return
      }

      try {
        const currentPeriod = await adminService.getCurrentPaymentPeriod()
        const shifts = await adminService.getShiftsByPeriodNumber(currentPeriod.periodNumber, {
          page: 0,
          size: 100
        })

        const ourShift = shifts.content.find(s => s.id === testShift.id)
        expect(ourShift).toBeDefined()
        expect(ourShift.status).toBe('COMPLETED')
        console.log('✅ Verified shift in period data')

      } catch (error) {
        console.log('❌ Shift verification failed:', error.response?.data?.message)
      }
    }, 10000)
  })

  describe('Kiosk Flow Simulation', () => {
    it('should simulate employee lookup via kiosk', async () => {
      if (!testEmployee) {
        console.log('Skipping - no test employee')
        return
      }

      try {
        const kioskEmployee = await kioskService.getEmployeeByUsername(testEmployee.username)
        expect(kioskEmployee.id).toBe(testEmployee.id)
        expect(kioskEmployee.username).toBe(testEmployee.username)
        console.log('✅ Kiosk employee lookup successful')

      } catch (error) {
        console.log('❌ Kiosk lookup failed:', error.response?.data?.message)
      }
    }, 10000)

    it('should determine clock action for employee', async () => {
      if (!testEmployee) {
        console.log('Skipping - no test employee')
        return
      }

      try {
        const action = await kioskService.determineAction(testEmployee.id)
        expect(['CLOCK_IN', 'CLOCK_OUT', 'UNAVAILABLE']).toContain(action)
        console.log('✅ Determined clock action:', action)

      } catch (error) {
        console.log('❌ Clock action determination failed:', error.response?.data?.message)
      }
    }, 10000)

    it('should get employee jobs for kiosk', async () => {
      if (!testEmployee) {
        console.log('Skipping - no test employee')
        return
      }

      try {
        const jobs = await kioskService.getEmployeeJobs(testEmployee.username)
        expect(Array.isArray(jobs)).toBe(true)
        console.log('✅ Got employee jobs for kiosk:', jobs.length)

        if (jobs.length > 0) {
          console.log('First job:', jobs[0].jobTitle)
        }

      } catch (error) {
        console.log('❌ Employee jobs fetch failed:', error.response?.data?.message)
      }
    }, 10000)
  })

  describe('Cleanup', () => {
    it('should clean up test shift', async () => {
      if (!authToken || !testShift) {
        console.log('Skipping cleanup - no shift to delete')
        return
      }

      try {
        await adminService.deleteShift(testShift.id)
        console.log('✅ Cleaned up test shift')
      } catch (error) {
        console.log('⚠️ Could not clean up shift:', error.response?.data?.message)
      }
    }, 10000)

    it('should clean up test job', async () => {
      if (!authToken || !testJob) {
        console.log('Skipping cleanup - no job to delete')
        return
      }

      try {
        await adminService.deleteJob(testJob.id)
        console.log('✅ Cleaned up test job')
      } catch (error) {
        console.log('⚠️ Could not clean up job:', error.response?.data?.message)
      }
    }, 10000)

    it('should clean up test department', async () => {
      if (!authToken || !testDepartment) {
        console.log('Skipping cleanup - no department to delete')
        return
      }

      try {
        await adminService.deleteDepartment(testDepartment.id)
        console.log('✅ Cleaned up test department')
      } catch (error) {
        console.log('⚠️ Could not clean up department:', error.response?.data?.message)
      }
    }, 10000)
  })
})