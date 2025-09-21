import { Clock, PlayCircle, CheckCircle } from 'lucide-react';

const ShiftsStatusTabs = ({ selectedTab, onTabChange, shiftsData }) => {
  const statusTabs = [
    { id: 'ALL', label: 'All Shifts', icon: Clock, color: 'text-gray-600' },
    { id: 'ACTIVE', label: 'Active', icon: PlayCircle, color: 'text-green-600' },
    { id: 'COMPLETED', label: 'Completed', icon: CheckCircle, color: 'text-blue-600' }
  ];

  return (
    <div className="border-b border-gray-200">
      <nav className="-mb-px flex space-x-8">
        {statusTabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = selectedTab === tab.id;
          
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-center py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                isActive
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <Icon className="h-5 w-5 mr-2" />
              {tab.label}
              {shiftsData && (
                <span className="ml-2 bg-gray-100 text-gray-600 py-0.5 px-2.5 rounded-full text-xs">
                  {shiftsData.totalElements || 0}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
};

export default ShiftsStatusTabs;