import { useState } from 'react';
import { Building2, Settings, CreditCard, MapPin } from 'lucide-react';
import CompanyInformation from '../../components/admin/CompanyInformation';
import CompanyPaymentSettings from '../../components/admin/CompanyPaymentSettings';
import CompanyLocation from '../../components/admin/CompanyLocation';

const CompanyPage = () => {
  const [activeTab, setActiveTab] = useState('information');

  const tabs = [
    {
      id: 'information',
      label: 'Company Information',
      icon: Building2,
      component: CompanyInformation,
    },
    {
      id: 'location',
      label: 'Location',
      icon: MapPin,
      component: CompanyLocation,
    },
    {
      id: 'payment-settings',
      label: 'Payment Settings',
      icon: CreditCard,
      component: CompanyPaymentSettings,
    },
  ];

  const ActiveComponent = tabs.find(tab => tab.id === activeTab)?.component;

  return (
    <div className="space-y-4 sm:space-y-6">
      {/* Tab Navigation with integrated header */}
      <div className="bg-white rounded-lg shadow border border-gray-200">
        <div className="flex items-center justify-center py-4 px-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <Settings className="h-5 w-5 text-blue-600" />
            <h1 className="text-lg font-semibold text-gray-900">Company Settings</h1>
          </div>
        </div>
        <nav className="flex w-full overflow-x-auto px-6" aria-label="Tabs">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;

            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex-1 flex items-center justify-center py-3 sm:py-4 px-2 sm:px-4 border-b-2 font-medium text-xs sm:text-sm whitespace-nowrap transition-colors ${
                  isActive
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className="h-4 w-4 sm:h-5 sm:w-5 mr-1 sm:mr-2" />
                <span className="hidden sm:inline">{tab.label}</span>
                <span className="sm:hidden">
                  {tab.id === 'information' ? 'Info' :
                   tab.id === 'payment-settings' ? 'Payment' :
                   tab.label}
                </span>
              </button>
            );
          })}
        </nav>

        {/* Tab Content - now inside the same container */}
        <div className="p-6">
          {ActiveComponent ? (
            <ActiveComponent />
          ) : (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <Settings className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">Settings Not Found</h3>
                <p className="text-gray-600">The requested settings page could not be found.</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CompanyPage;