import { BarChart3 } from 'lucide-react';

const ReportsPage = () => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-gray-900">Reports</h1>
        <button className="btn-primary">Generate Report</button>
      </div>

      <div className="card">
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <BarChart3 className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Reports & Analytics</h3>
            <p className="text-gray-600">Reporting and analytics features will be implemented here.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReportsPage;