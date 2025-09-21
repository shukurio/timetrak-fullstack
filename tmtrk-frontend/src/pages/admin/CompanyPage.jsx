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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Company Settings</h1>
          <p className="text-gray-600 mt-1">Manage your company information and configuration</p>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8" aria-label="Tabs">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                  isActive
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className="h-5 w-5 mr-2" />
                {tab.label}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="mt-6">
        {ActiveComponent ? (
          <ActiveComponent />
        ) : (
          <div className="card">
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <Settings className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">Settings Not Found</h3>
                <p className="text-gray-600">The requested settings page could not be found.</p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CompanyPage;