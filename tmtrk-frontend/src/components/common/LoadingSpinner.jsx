const LoadingSpinner = ({ size = 'md', text = 'Loading...' }) => {
  const sizeClasses = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16',
  };

  // Only apply minimum height for medium and larger sizes
  const containerClasses = size === 'sm'
    ? 'flex flex-col items-center justify-center transition-opacity duration-300'
    : 'flex flex-col items-center justify-center min-h-64 transition-opacity duration-300';

  return (
    <div className={containerClasses}>
      <div className={`animate-spin rounded-full border-4 border-gray-300 border-t-primary-600 ${sizeClasses[size]}`}></div>
      {text && <p className="mt-4 text-gray-600 text-sm">{text}</p>}
    </div>
  );
};

export default LoadingSpinner;