import { Link } from 'react-router-dom';
import { Building2, ArrowLeft } from 'lucide-react';

const NoDepartmentsPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full text-center">
        <div className="mx-auto h-24 w-24 flex items-center justify-center rounded-full bg-yellow-100 mb-8">
          <Building2 className="h-12 w-12 text-yellow-600" />
        </div>
        
        <h1 className="text-3xl font-bold text-gray-900 mb-4">
          No Departments Assigned
        </h1>
        
        <p className="text-gray-600 mb-8">
          You don't have any departments assigned to manage. Please contact your administrator to assign departments to your account.
        </p>
        
        <div className="space-y-3">
          <Link
            to="/login"
            className="btn-primary w-full flex items-center justify-center"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Login
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NoDepartmentsPage;