import { Clock, PlayCircle, CheckCircle } from 'lucide-react';

// Add CSS for blinking animation
const blinkingStyle = `
  @keyframes blink {
    0%, 50% { opacity: 1; }
    51%, 100% { opacity: 0.3; }
  }
`;

// Inject the CSS
if (typeof document !== 'undefined' && !document.getElementById('blink-animation')) {
  const style = document.createElement('style');
  style.id = 'blink-animation';
  style.textContent = blinkingStyle;
  document.head.appendChild(style);
}

const ShiftsStatusTabs = ({ selectedTab, onTabChange, hasActiveShifts }) => {
  const statusTabs = [
    { id: 'ALL', label: 'All Shifts', icon: Clock, color: 'text-gray-600' },
    { id: 'ACTIVE', label: 'Active', icon: PlayCircle, color: 'text-green-600' },
    { id: 'COMPLETED', label: 'Completed', icon: CheckCircle, color: 'text-blue-600' }
  ];

  return (
    <div className="border-b border-gray-200">
      <nav className="-mb-px flex w-full">
        {statusTabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = selectedTab === tab.id;

          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex-1 flex items-center justify-center py-4 px-2 border-b-2 font-medium text-xs sm:text-sm whitespace-nowrap transition-colors ${
                isActive
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              <Icon className="h-4 w-4 sm:h-5 sm:w-5 mr-1 sm:mr-2" />
              <span className="hidden sm:inline">{tab.label}</span>
              <span className="sm:hidden">
                {tab.id === 'ALL' ? 'All' :
                 tab.id === 'ACTIVE' ? 'Active' :
                 'Completed'}
              </span>
              {tab.id === 'ACTIVE' && hasActiveShifts && (
                <div className="ml-2 flex items-center">
                  <div
                    className="w-3 h-3 bg-green-500 rounded-full"
                    style={{
                      animation: 'blink 1s infinite',
                    }}
                  />
                </div>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
};

export default ShiftsStatusTabs;