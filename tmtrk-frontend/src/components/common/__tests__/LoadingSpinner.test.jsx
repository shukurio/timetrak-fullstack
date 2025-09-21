import { describe, it, expect } from 'vitest'
import { render, screen } from '../../../test/test-utils'
import LoadingSpinner from '../LoadingSpinner'

describe('LoadingSpinner', () => {
  it('should render with default props', () => {
    render(<LoadingSpinner />)

    const container = screen.getByRole('generic')
    expect(container).toBeInTheDocument()

    const text = screen.getByText('Loading...')
    expect(text).toBeInTheDocument()
  })

  it('should render with custom text', () => {
    render(<LoadingSpinner text="Please wait..." />)

    const text = screen.getByText('Please wait...')
    expect(text).toBeInTheDocument()
  })

  it('should render without text when text is empty', () => {
    render(<LoadingSpinner text="" />)

    expect(screen.queryByText('Loading...')).not.toBeInTheDocument()

    // Should still render the spinner div
    const container = screen.getByRole('generic')
    expect(container).toBeInTheDocument()
  })

  it('should apply small size class when size is sm', () => {
    render(<LoadingSpinner size="sm" />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('h-4', 'w-4')
  })

  it('should apply medium size class when size is md (default)', () => {
    render(<LoadingSpinner size="md" />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('h-8', 'w-8')
  })

  it('should apply large size class when size is lg', () => {
    render(<LoadingSpinner size="lg" />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('h-12', 'w-12')
  })

  it('should apply extra large size class when size is xl', () => {
    render(<LoadingSpinner size="xl" />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('h-16', 'w-16')
  })

  it('should have spinning animation class', () => {
    render(<LoadingSpinner />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('animate-spin')
  })

  it('should have proper border styling for spinner', () => {
    render(<LoadingSpinner />)

    const spinnerDiv = screen.getByRole('generic').querySelector('div')
    expect(spinnerDiv).toHaveClass('rounded-full', 'border-4', 'border-gray-300', 'border-t-primary-600')
  })

  it('should have correct container classes', () => {
    render(<LoadingSpinner />)

    const container = screen.getByRole('generic')
    expect(container).toHaveClass('flex', 'flex-col', 'items-center', 'justify-center', 'min-h-64')
  })
})