# Frontend Testing Guide

This document provides comprehensive information about the testing setup and how to run tests for the TimeTrack frontend application.

## Testing Framework

The project uses **Vitest** as the primary testing framework along with **React Testing Library** for component testing. The project supports both **mocked tests** and **real API integration tests**.

### Key Dependencies
- `vitest` - Fast unit test framework
- `@testing-library/react` - React component testing utilities
- `@testing-library/jest-dom` - Custom jest matchers for DOM elements
- `@testing-library/user-event` - User interaction simulation
- `jsdom` - DOM environment for testing

## Testing Modes

### 1. Mocked Tests (Default)
- Fast execution
- No external dependencies
- Isolated component testing
- Uses mocked API responses

### 2. Real API Tests
- Tests against actual backend
- End-to-end integration validation
- Requires running backend server
- Slower execution but higher confidence

## Test Structure

```
src/
├── test/
│   ├── setup.js                 # Global test setup
│   ├── test-utils.jsx           # Custom render utilities and mock data
│   └── integration/             # Integration tests
│       ├── auth-flow.test.jsx
│       └── shift-management.test.jsx
├── api/
│   └── __tests__/               # API service tests
│       ├── authService.test.js
│       ├── adminService.test.js
│       └── kioskService.test.js
├── store/
│   └── __tests__/               # State management tests
│       └── authStore.test.js
├── components/
│   ├── common/__tests__/        # Common component tests
│   ├── admin/__tests__/         # Admin component tests
│   └── employee/__tests__/      # Employee component tests
└── pages/
    └── kiosk/__tests__/         # Page component tests
        └── KioskPage.test.jsx
```

## Running Tests

### Available Scripts

#### Mocked Tests (Default)
```bash
# Run tests in watch mode (development)
npm run test

# Run tests once with coverage
npm run test:coverage

# Run tests without watch mode
npm run test:run

# Run tests with UI (if @vitest/ui is installed)
npm run test:ui
```

#### Real API Tests
```bash
# Run real API tests in watch mode
npm run test:real-api

# Run real API tests once
npm run test:real-api:run

# Run integration tests only
npm run test:integration

# Run API health check only
npm run test:health
```

### Prerequisites for Real API Tests

Before running real API tests, ensure:

1. **Backend Server Running**
   ```bash
   # Backend should be running on http://localhost:8093
   # Check with: curl http://localhost:8093/api/health
   ```

2. **Test Database Setup**
   - Use a dedicated test database
   - Ensure you have admin credentials
   - Have at least one test employee account

3. **Environment Configuration**
   ```bash
   # The tests will use these endpoints:
   # - http://localhost:8093/api/auth/*
   # - http://localhost:8093/api/admin/*
   # - http://localhost:8093/api/kiosk/*
   ```

### Test Commands Examples

#### Mocked Tests
```bash
# Run all mocked tests
npm test

# Run tests with coverage report
npm run test:coverage

# Run specific test file
npm test -- AuthService

# Run tests matching a pattern
npm test -- --grep "login"

# Run tests in a specific directory
npm test -- src/api

# Run tests with verbose output
npm test -- --reporter=verbose
```

#### Real API Tests
```bash
# Quick health check
npm run test:health

# Full integration test suite
npm run test:integration

# Run specific real API test
npm run test:real-api:run -- --grep "authentication"

# Run with verbose output
npm run test:real-api:run -- --reporter=verbose
```

### Real API Test Credentials

Update the test files with your actual credentials:

```javascript
// In src/test/real-api.test.js and integration tests
const result = await authService.login('your-admin-username', 'your-admin-password')
```

**Security Note**: Never commit real credentials to version control. Consider using environment variables:

```javascript
const adminUsername = process.env.TEST_ADMIN_USERNAME || 'admin'
const adminPassword = process.env.TEST_ADMIN_PASSWORD || 'admin123'
```

## Test Categories

### 1. Unit Tests

**API Services** (`src/api/__tests__/`)
- `authService.test.js` - Authentication API calls
- `adminService.test.js` - Admin operations API
- `kioskService.test.js` - Kiosk functionality API

**State Management** (`src/store/__tests__/`)
- `authStore.test.js` - Zustand auth store logic

**Components** (`src/components/**/__tests__/`)
- Individual component testing
- Props validation
- User interactions
- Rendering logic

### 2. Integration Tests

**Authentication Flow** (`src/test/integration/auth-flow.test.jsx`)
- Login/logout process
- Route protection
- Token persistence
- Registration flow

**Shift Management** (`src/test/integration/shift-management.test.jsx`)
- Creating/editing shifts
- Bulk operations
- Period navigation
- Data persistence

### 3. Page Tests

**KioskPage** (`src/pages/kiosk/__tests__/KioskPage.test.jsx`)
- Complete kiosk workflow
- Employee search
- Clock in/out process
- Geolocation handling

## Testing Utilities

### Custom Render Function

```jsx
import { render } from '../test/test-utils'

// Automatically wraps components with:
// - React Query Provider
// - React Router
// - Toast notifications
```

### Mock Data Generators

```jsx
import {
  createMockEmployee,
  createMockShift,
  createMockDepartment
} from '../test/test-utils'

const mockEmployee = createMockEmployee({
  firstName: 'John',
  role: 'ADMIN'
})
```

## Mocking Strategy

### API Services
All API services are mocked using Vitest's `vi.mock()`:

```jsx
vi.mock('../../../api/adminService', () => ({
  default: {
    getEmployees: vi.fn(),
    createShift: vi.fn(),
    // ... other methods
  }
}))
```

### Browser APIs
- **Geolocation**: Mocked globally in setup
- **LocalStorage**: Mocked with custom implementation
- **Window.matchMedia**: Mocked for responsive components

### External Libraries
- **React Router**: Provided via test-utils wrapper
- **React Query**: Configured with test-friendly defaults

## Writing Tests

### Best Practices

1. **Use descriptive test names**
   ```jsx
   it('should display validation error when email is invalid', () => {
     // test implementation
   })
   ```

2. **Follow AAA pattern (Arrange, Act, Assert)**
   ```jsx
   it('should login user successfully', async () => {
     // Arrange
     const mockUser = createMockEmployee()
     authService.login.mockResolvedValue(mockUser)

     // Act
     render(<LoginForm />)
     await user.type(screen.getByLabelText(/username/), 'testuser')
     await user.click(screen.getByRole('button', { name: /login/ }))

     // Assert
     expect(authService.login).toHaveBeenCalledWith('testuser', expect.any(String))
   })
   ```

3. **Test user interactions, not implementation details**
   ```jsx
   // Good
   await user.click(screen.getByRole('button', { name: /submit/ }))

   // Avoid
   fireEvent.click(component.find('.submit-button'))
   ```

4. **Use waitFor for async operations**
   ```jsx
   await waitFor(() => {
     expect(screen.getByText('Success!')).toBeInTheDocument()
   })
   ```

### Component Testing Template

```jsx
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { render, screen } from '../../../test/test-utils'
import userEvent from '@testing-library/user-event'
import YourComponent from '../YourComponent'

describe('YourComponent', () => {
  const defaultProps = {
    // Define default props
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Rendering', () => {
    it('should render with default props', () => {
      render(<YourComponent {...defaultProps} />)

      expect(screen.getByText('Expected Text')).toBeInTheDocument()
    })
  })

  describe('User Interactions', () => {
    it('should handle button click', async () => {
      const user = userEvent.setup()
      const mockHandler = vi.fn()

      render(<YourComponent {...defaultProps} onClick={mockHandler} />)

      await user.click(screen.getByRole('button'))

      expect(mockHandler).toHaveBeenCalled()
    })
  })
})
```

## Coverage Reports

Coverage reports are generated in the `coverage/` directory and include:

- **HTML Report**: `coverage/index.html` - Visual coverage report
- **JSON Report**: `coverage/coverage-final.json` - Machine-readable data
- **Text Report**: Displayed in terminal

### Coverage Targets
- **Statements**: > 80%
- **Branches**: > 75%
- **Functions**: > 80%
- **Lines**: > 80%

## Debugging Tests

### Common Issues

1. **Tests timing out**
   ```jsx
   // Increase timeout for slow operations
   it('should handle slow operation', async () => {
     // test code
   }, 10000) // 10 second timeout
   ```

2. **Mock not being called**
   ```jsx
   // Ensure mock is properly set up
   beforeEach(() => {
     vi.clearAllMocks()
     mockFunction.mockResolvedValue(expectedValue)
   })
   ```

3. **Component not updating**
   ```jsx
   // Use waitFor for async updates
   await waitFor(() => {
     expect(screen.getByText('Updated Text')).toBeInTheDocument()
   })
   ```

### Debug Utilities

```jsx
// View current DOM
screen.debug()

// Find elements in test
screen.logTestingPlaygroundURL()

// Check what queries are available
screen.getByRole('') // Will show available roles
```

## Continuous Integration

Tests are run automatically on:
- Pull requests
- Commits to main branch
- Scheduled runs (if configured)

### CI Configuration Example (GitHub Actions)

```yaml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
        with:
          node-version: '18'
      - run: npm ci
      - run: npm run test:coverage
      - uses: codecov/codecov-action@v1
```

## Performance Testing

For performance-critical components, consider:

```jsx
import { performance } from 'perf_hooks'

it('should render large list efficiently', () => {
  const start = performance.now()

  render(<LargeList items={largeDataSet} />)

  const end = performance.now()
  expect(end - start).toBeLessThan(100) // 100ms threshold
})
```

## Contributing

When adding new features:

1. Write tests first (TDD approach)
2. Ensure all existing tests pass
3. Add integration tests for new user flows
4. Maintain coverage above target thresholds
5. Update this documentation for new testing patterns

## Useful Resources

- [Vitest Documentation](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- [Jest DOM Matchers](https://github.com/testing-library/jest-dom)
- [User Event API](https://testing-library.com/docs/user-event/intro/)