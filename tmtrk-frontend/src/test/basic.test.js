import { describe, it, expect } from 'vitest'

describe('Basic Test Setup', () => {
  it('should run a basic test', () => {
    expect(1 + 1).toBe(2)
  })

  it('should have access to vitest globals', () => {
    expect(typeof describe).toBe('function')
    expect(typeof it).toBe('function')
    expect(typeof expect).toBe('function')
  })

  it('should have access to jest-dom matchers', () => {
    const div = document.createElement('div')
    div.textContent = 'Hello World'
    document.body.appendChild(div)

    expect(div).toBeInTheDocument()
    expect(div).toHaveTextContent('Hello World')

    document.body.removeChild(div)
  })
})