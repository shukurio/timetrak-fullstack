import { Search, X } from 'lucide-react';

const ShiftsFilters = ({
  searchQuery,
  setSearchQuery,
  selectedDepartment,
  setSelectedDepartment,
  selectedEmployee,
  setSelectedEmployee,
  dateRange,
  setDateRange,
  departmentsData,
  employeesData,
  onClearFilters
}) => {
  const dateRangeOptions = [
    { value: 'today', label: 'Today' },
    { value: 'week', label: 'This Week' },
    { value: 'month', label: 'This Month' },
    { value: 'custom', label: 'Custom Range' }
  ];

  const hasActiveFilters = searchQuery || selectedDepartment || selectedEmployee;

  return (
    <div className="card">
      <div className="flex flex-col lg:flex-row gap-4">
        {/* Search */}
        <div className="flex-1">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by employee name..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="input-field pl-10"
            />
          </div>
        </div>

        {/* Department Filter */}
        <div className="lg:w-48">
          <select
            value={selectedDepartment}
            onChange={(e) => setSelectedDepartment(e.target.value)}
            className="input-field"
          >
            <option value="">All Departments</option>
            {departmentsData?.content?.map((dept) => (
              <option key={dept.id} value={dept.id}>
                {dept.name}
              </option>
            ))}
          </select>
        </div>

        {/* Employee Filter */}
        <div className="lg:w-48">
          <select
            value={selectedEmployee}
            onChange={(e) => setSelectedEmployee(e.target.value)}
            className="input-field"
          >
            <option value="">All Employees</option>
            {employeesData?.content?.map((emp) => (
              <option key={emp.id} value={emp.id}>
                {emp.firstName} {emp.lastName}
              </option>
            ))}
          </select>
        </div>

        {/* Date Range */}
        <div className="lg:w-40">
          <select
            value={dateRange}
            onChange={(e) => setDateRange(e.target.value)}
            className="input-field"
          >
            {dateRangeOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Clear Filters */}
        {hasActiveFilters && (
          <button
            onClick={onClearFilters}
            className="btn-outline flex items-center"
          >
            <X className="h-4 w-4 mr-2" />
            Clear
          </button>
        )}
      </div>
    </div>
  );
};

export default ShiftsFilters;