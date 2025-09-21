import { Link } from 'react-router-dom';
import { AlertTriangle, ArrowLeft } from 'lucide-react';

const UnauthorizedPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full text-center">
        <div className="mx-auto h-24 w-24 flex items-center justify-center rounded-full bg-error-100 mb-8">
          <AlertTriangle className="h-12 w-12 text-error-600" />
        </div>
        
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          Access Denied
        </h1>
        
        <p className="text-gray-600 mb-8">
          You don't have permission to access this page. Please contact your administrator if you think this is an error.
        </p>
        
        <div className="space-y-3">
          <Link
            to="/"
            className="btn-primary w-full flex items-center justify-center"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Go Back Home
          </Link>
          
          <Link
            to="/login"
            className="btn-outline w-full"
          >
            Sign In Again
          </Link>
        </div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;