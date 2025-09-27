import { Edit3, Trash2, Clock } from 'lucide-react';
import { format, parseISO } from 'date-fns';
import LoadingSpinner from '../common/LoadingSpinner';

const ShiftsTable = ({ 
  shiftsData, 
  shiftsLoading, 
  shiftsError,
  isSelectionMode,
  selectedShifts,
  onShiftSelection,
  onSelectAllShifts,
  onEditShift,
  onDeleteShift,
  currentPage,
  setCurrentPage,
  pageSize
}) => {
  const getStatusBadge = (status) => {
    const statusConfig = {
      ACTIVE: { color: 'bg-green-100 text-green-800', label: 'ACTIVE' },
      COMPLETED: { color: 'bg-blue-100 text-blue-800', label: 'COMPLETED' }
    };

    const config = statusConfig[status] || { color: 'bg-gray-100 text-gray-800', label: status };
    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${config.color}`}>
        {config.label}
      </span>
    );
  };

  const formatDuration = (clockIn, clockOut) => {
    if (!clockIn) return '-';
    if (!clockOut) return 'Active';
    
    const start = parseISO(clockIn);
    const end = parseISO(clockOut);
    const diffMs = end - start;
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    return `${hours}h ${minutes}m`;
  };

  const renderShiftRow = (shift) => (
    <tr key={shift.id} className="hover:bg-gray-50">
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="flex items-center">
          {isSelectionMode && (
            <input
              type="checkbox"
              checked={selectedShifts.includes(shift.employeeJobId)}
              onChange={(e) => onShiftSelection(shift.employeeJobId, e.target.checked)}
              className="h-4 w-4 text-primary-600 rounded border-gray-300 mr-3"
            />
          )}
          <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
            <span className="text-primary-600 font-medium text-sm">
              {shift.fullName?.split(' ').map(n => n[0]).join('').toUpperCase()}
            </span>
          </div>
          <div className="ml-3">
            <div className="text-sm font-medium text-gray-900">{shift.fullName}</div>
            <div className="text-sm text-gray-500">{shift.username}</div>
          </div>
        </div>
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {shift.jobTitle}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {shift.clockIn ? format(parseISO(shift.clockIn), 'MMM d, HH:mm') : '-'}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {shift.clockOut ? format(parseISO(shift.clockOut), 'MMM d, HH:mm') : '-'}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        {formatDuration(shift.clockIn, shift.clockOut)}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap">
        {getStatusBadge(shift.status)}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
        ${shift.shiftEarnings?.toFixed(2) || '0.00'}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
        {shift.notes || '-'}
      </td>
      
      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
        <div className="flex items-center space-x-2">
          <button
            onClick={() => onEditShift(shift)}
            className="text-primary-600 hover:text-primary-900"
          >
            <Edit3 className="h-4 w-4" />
          </button>
          <button
            onClick={() => onDeleteShift(shift)}
            className="text-red-600 hover:text-red-900"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </td>
    </tr>
  );

  if (shiftsError) {
    return (
      <div className="card p-0">
        <div className="p-6 text-center">
          <div className="text-red-600 mb-2">⚠️ Error loading shifts</div>
          <p className="text-sm text-gray-600 mb-4">
            {shiftsError.response?.data?.message || shiftsError.message || 'Failed to load shifts'}
          </p>
          <button onClick={() => window.location.reload()} className="btn-primary">
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (shiftsLoading) {
    return (
      <div className="card p-0">
        <div className="p-6">
          <LoadingSpinner />
        </div>
      </div>
    );
  }

  if (!shiftsData?.content?.length) {
    return (
      <div className="card p-0">
        <div className="text-center py-12">
          <Clock className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No shifts found</h3>
          <p className="text-gray-600">Create your first shift to get started</p>
        </div>
      </div>
    );
  }

  return (
    <div className="card p-0">
      {/* Mobile Cards View */}
      <div className="block sm:hidden">
        {isSelectionMode && (
          <div className="px-4 py-3 bg-gray-50 border-b flex items-center justify-between">
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={selectedShifts.length === shiftsData.content.length && shiftsData.content.length > 0}
                onChange={(e) => onSelectAllShifts(e.target.checked)}
                className="h-4 w-4 text-primary-600 rounded border-gray-300"
              />
              <span className="ml-3 text-sm text-gray-700">
                {selectedShifts.length > 0
                  ? `${selectedShifts.length} selected`
                  : `Select all`
                }
              </span>
            </div>
          </div>
        )}

        <div className="p-4 space-y-4">
          {shiftsData.content.map((shift) => (
            <div key={shift.id} className="border rounded-lg p-4 bg-white shadow-sm">
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center">
                  {isSelectionMode && (
                    <input
                      type="checkbox"
                      checked={selectedShifts.includes(shift.employeeJobId)}
                      onChange={(e) => onShiftSelection(shift.employeeJobId, e.target.checked)}
                      className="h-4 w-4 text-primary-600 rounded border-gray-300 mr-3"
                    />
                  )}
                  <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                    <span className="text-primary-600 font-medium text-sm">
                      {shift.fullName?.split(' ').map(n => n[0]).join('').toUpperCase()}
                    </span>
                  </div>
                  <div className="ml-3">
                    <div className="text-sm font-medium text-gray-900">{shift.fullName}</div>
                    <div className="text-xs text-gray-500">{shift.jobTitle}</div>
                  </div>
                </div>
                {getStatusBadge(shift.status)}
              </div>

              <div className="grid grid-cols-2 gap-3 text-xs text-gray-600 mb-3">
                <div>
                  <span className="font-medium">Clock In:</span><br />
                  {shift.clockIn ? format(parseISO(shift.clockIn), 'MMM d, HH:mm') : '-'}
                </div>
                <div>
                  <span className="font-medium">Clock Out:</span><br />
                  {shift.clockOut ? format(parseISO(shift.clockOut), 'MMM d, HH:mm') : '-'}
                </div>
                <div>
                  <span className="font-medium">Duration:</span><br />
                  {formatDuration(shift.clockIn, shift.clockOut)}
                </div>
                <div>
                  <span className="font-medium">Earnings:</span><br />
                  <span className="text-green-600 font-semibold">${shift.shiftEarnings?.toFixed(2) || '0.00'}</span>
                </div>
              </div>

              {shift.notes && (
                <div className="mb-3">
                  <span className="text-xs font-medium text-gray-700">Notes:</span>
                  <p className="text-xs text-gray-600 mt-1">{shift.notes}</p>
                </div>
              )}

              <div className="flex justify-end space-x-2">
                <button
                  onClick={() => onEditShift(shift)}
                  className="p-2 text-primary-600 hover:text-primary-900 hover:bg-primary-50 rounded"
                >
                  <Edit3 className="h-4 w-4" />
                </button>
                <button
                  onClick={() => onDeleteShift(shift)}
                  className="p-2 text-red-600 hover:text-red-900 hover:bg-red-50 rounded"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Desktop Table View */}
      <div className="hidden sm:block overflow-x-auto">
        {isSelectionMode && (
          <div className="px-6 py-3 bg-gray-50 border-b flex items-center justify-between">
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={selectedShifts.length === shiftsData.content.length && shiftsData.content.length > 0}
                onChange={(e) => onSelectAllShifts(e.target.checked)}
                className="h-4 w-4 text-primary-600 rounded border-gray-300"
              />
              <span className="ml-3 text-sm text-gray-700">
                {selectedShifts.length > 0
                  ? `${selectedShifts.length} selected`
                  : `Select all ${shiftsData.content.length} shifts`
                }
              </span>
            </div>
          </div>
        )}

        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Employee</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Job</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Clock In</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Clock Out</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Duration</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Earnings</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Notes</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {shiftsData.content.map(renderShiftRow)}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {shiftsData.totalPages > 1 && (
        <div className="px-4 sm:px-6 py-4 flex items-center justify-between border-t">
          <div className="flex-1 flex justify-between sm:hidden">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="btn-outline disabled:opacity-50"
            >
              Previous
            </button>
            <button
              onClick={() => setCurrentPage(Math.min(shiftsData.totalPages - 1, currentPage + 1))}
              disabled={currentPage >= shiftsData.totalPages - 1}
              className="btn-outline disabled:opacity-50"
            >
              Next
            </button>
          </div>
          <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-700">
                Showing <span className="font-medium">{currentPage * pageSize + 1}</span> to{' '}
                <span className="font-medium">
                  {Math.min((currentPage + 1) * pageSize, shiftsData.totalElements)}
                </span>{' '}
                of <span className="font-medium">{shiftsData.totalElements}</span> results
              </p>
            </div>
            <div>
              <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                {Array.from({ length: Math.min(5, shiftsData.totalPages) }, (_, i) => {
                  const page = currentPage < 3 ? i : currentPage - 2 + i;
                  if (page >= shiftsData.totalPages) return null;

                  return (
                    <button
                      key={page}
                      onClick={() => setCurrentPage(page)}
                      className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                        page === currentPage
                          ? 'z-10 bg-primary-50 border-primary-500 text-primary-600'
                          : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                      } ${
                        i === 0 ? 'rounded-l-md' : ''
                      } ${
                        i === Math.min(4, shiftsData.totalPages - 1) ? 'rounded-r-md' : ''
                      }`}
                    >
                      {page + 1}
                    </button>
                  );
                })}
              </nav>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ShiftsTable;