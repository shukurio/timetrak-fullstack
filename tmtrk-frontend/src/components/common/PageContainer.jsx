import React from 'react';

const PageContainer = ({
  title,
  icon: Icon,
  children,
  className = "",
  headerClassName = "",
  contentClassName = ""
}) => {
  return (
    <div className={`space-y-6 ${className}`}>
      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <div className={`flex items-center justify-center py-4 px-6 border-b border-gray-200 ${headerClassName}`}>
          <div className="flex items-center gap-2">
            {Icon && <Icon className="h-5 w-5 text-blue-600" />}
            <h1 className="text-lg font-semibold text-gray-900">{title}</h1>
          </div>
        </div>

        <div className={`p-6 ${contentClassName}`}>
          {children}
        </div>
      </div>
    </div>
  );
};

export default PageContainer;