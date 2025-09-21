import { MoreVertical, CheckCircle, XCircle, UserCheck, UserX, Trash2 } from 'lucide-react';

const EmployeesTable = ({ 
  employees, 
  onEmployeeClick,
  onActionClick,
  dropdownOpen,
  toggleDropdown,
  mutations
}) => {
  const getStatusBadge = (status) => {
    const statusConfig = {
      PENDING: { color: 'bg-yellow-100 text-yellow-800', label: 'Pending' },
      ACTIVE: { color: 'bg-green-100 text-green-800', label: 'Active' },
      DEACTIVATED: { color: 'bg-red-100 text-red-800', label: 'Deactivated' },
      REJECTED: { color: 'bg-gray-100 text-gray-600', label: 'Rejected' },
      DELETED: { color: 'bg-gray-100 text-gray-400', label: 'Deleted' },
    };
    
    const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-800', label: status };
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
        {config.label}
      </span>
    );
  };

  // Define fixed column widths for consistency - totals to 100%
  const columnStyles = {
    employee: "w-[30%]",
    contact: "w-[28%]",
    department: "w-[20%]",
    role: "w-[14%]",
    actions: "w-[8%]"
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full table-fixed divide-y divide-gray-200" style={{ minWidth: '800px' }}>
        <thead className="bg-gray-50">
          <tr>
            <th className={`${columnStyles.employee} px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider`}>
              Employee
            </th>
            <th className={`${columnStyles.contact} px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider`}>
              Contact
            </th>
            <th className={`${columnStyles.department} px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider`}>
              Department
            </th>
            <th className={`${columnStyles.role} px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider`}>
              Role
            </th>
            <th className={`${columnStyles.actions} px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider`}>
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {employees.map((employee, index) => (
            <tr 
              key={employee.id} 
              onClick={() => onEmployeeClick(employee)}
              className="hover:bg-gray-50 cursor-pointer"
            >
              <td className={`${columnStyles.employee} px-6 py-4`}>
                <div className="flex items-center min-w-0">
                  <div className="flex-shrink-0 h-10 w-10">
                    <div className="h-10 w-10 rounded-full bg-primary-100 flex items-center justify-center">
                      <span className="text-primary-600 font-medium text-sm">
                        {employee.firstName?.charAt(0)}{employee.lastName?.charAt(0)}
                      </span>
                    </div>
                  </div>
                  <div className="ml-4 min-w-0">
                    <div className="text-sm font-medium text-gray-900 truncate">
                      {employee.firstName} {employee.lastName}
                    </div>
                    <div className="text-sm text-gray-500 truncate">
                      @{employee.username}
                    </div>
                  </div>
                </div>
              </td>
              <td className={`${columnStyles.contact} px-6 py-4`}>
                <div className="min-w-0">
                  <div className="text-sm text-gray-900 truncate">{employee.email}</div>
                  {employee.phoneNumber && (
                    <div className="text-sm text-gray-500 truncate">{employee.phoneNumber}</div>
                  )}
                </div>
              </td>
              <td className={`${columnStyles.department} px-6 py-4`}>
                <div className="text-sm text-gray-900 truncate">
                  {employee.departmentName || 'Not Assigned'}
                </div>
              </td>
              <td className={`${columnStyles.role} px-6 py-4`}>
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {employee.role}
                </span>
              </td>
              <td className={`${columnStyles.actions} px-6 py-4 text-center`}>
                <div className="relative">
                  <button
                    onClick={(e) => toggleDropdown(e, employee.id)}
                    className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-2 rounded-full transition-colors"
                    title="More actions"
                  >
                    <MoreVertical className="h-4 w-4" />
                  </button>

                  {dropdownOpen === employee.id && (
                    <div className={`absolute right-0 w-48 bg-white rounded-lg shadow-xl border border-gray-200 z-[100] ${
                      index > employees.length - 3 ? 'bottom-full mb-2' : 'top-full mt-2'
                    }`}>
                      <div className="py-2">
                        {employee.status === 'PENDING' && (
                          <>
                            <button
                              onClick={(e) => onActionClick(e, 'approve', employee)}
                              disabled={mutations?.approve?.isLoading}
                              className="flex items-center w-full px-4 py-2 text-sm text-green-700 hover:bg-green-50 transition-colors disabled:opacity-50"
                            >
                              <CheckCircle className="h-4 w-4 mr-3" />
                              Approve Employee
                            </button>
                            <button
                              onClick={(e) => onActionClick(e, 'reject', employee)}
                              disabled={mutations?.reject?.isLoading}
                              className="flex items-center w-full px-4 py-2 text-sm text-red-700 hover:bg-red-50 transition-colors disabled:opacity-50"
                            >
                              <XCircle className="h-4 w-4 mr-3" />
                              Reject Employee
                            </button>
                          </>
                        )}

                        {employee.status === 'ACTIVE' && (
                          <button
                            onClick={(e) => onActionClick(e, 'deactivate', employee)}
                            disabled={mutations?.deactivate?.isLoading}
                            className="flex items-center w-full px-4 py-2 text-sm text-orange-700 hover:bg-orange-50 transition-colors disabled:opacity-50"
                          >
                            <UserX className="h-4 w-4 mr-3" />
                            Deactivate Employee
                          </button>
                        )}

                        {(employee.status === 'DEACTIVATED' || employee.status === 'REJECTED') && (
                          <button
                            onClick={(e) => onActionClick(e, 'activate', employee)}
                            disabled={mutations?.activate?.isLoading}
                            className="flex items-center w-full px-4 py-2 text-sm text-green-700 hover:bg-green-50 transition-colors disabled:opacity-50"
                          >
                            <UserCheck className="h-4 w-4 mr-3" />
                            Activate Employee
                          </button>
                        )}

                        <div className="border-t border-gray-100 my-2"></div>
                        <button
                          onClick={(e) => onActionClick(e, 'delete', employee)}
                          disabled={mutations?.delete?.isLoading}
                          className="flex items-center w-full px-4 py-2 text-sm text-red-700 hover:bg-red-50 transition-colors disabled:opacity-50"
                        >
                          <Trash2 className="h-4 w-4 mr-3" />
                          Delete Employee
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default EmployeesTable;