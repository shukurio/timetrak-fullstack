import { Link } from 'react-router-dom';
import { Search, ArrowLeft } from 'lucide-react';

const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full text-center">
        <div className="mx-auto h-24 w-24 flex items-center justify-center rounded-full bg-gray-100 mb-8">
          <Search className="h-12 w-12 text-gray-400" />
        </div>
        
        <h1 className="text-6xl font-bold text-gray-900 mb-4">404</h1>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Page Not Found</h2>
        
        <p className="text-gray-600 mb-8">
          The page you're looking for doesn't exist or has been moved.
        </p>
        
        <Link
          to="/"
          className="btn-primary inline-flex items-center"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Go Back Home
        </Link>
      </div>
    </div>
  );
};

export default NotFoundPage;