import { useState, useEffect } from 'react';
import { ChevronLeft, ChevronRight, Calendar } from 'lucide-react';
import { format, parseISO } from 'date-fns';

const PaymentPeriodSelector = ({ onPeriodChange, selectedPeriod, periods = [] }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isScrollable, setIsScrollable] = useState(false);

  useEffect(() => {
    if (periods.length > 0 && selectedPeriod) {
      const index = periods.findIndex(period =>
        period.periodStart === selectedPeriod.periodStart &&
        period.periodEnd === selectedPeriod.periodEnd
      );
      if (index !== -1) {
        setCurrentIndex(index);
      }
    }
  }, [periods, selectedPeriod]);

  useEffect(() => {
    setIsScrollable(periods.length > 3);
  }, [periods]);

  const formatPeriodLabel = (period) => {
    const startDate = parseISO(period.periodStart);
    const endDate = parseISO(period.periodEnd);
    return `${format(startDate, 'MMM d')} - ${format(endDate, 'MMM d, yyyy')}`;
  };

  const handlePeriodClick = (period, index) => {
    setCurrentIndex(index);
    onPeriodChange(period);
  };

  const scrollLeft = () => {
    if (currentIndex > 0) {
      const newIndex = Math.max(0, currentIndex - 1);
      setCurrentIndex(newIndex);
      onPeriodChange(periods[newIndex]);
    }
  };

  const scrollRight = () => {
    if (currentIndex < periods.length - 1) {
      const newIndex = Math.min(periods.length - 1, currentIndex + 1);
      setCurrentIndex(newIndex);
      onPeriodChange(periods[newIndex]);
    }
  };

  const getVisiblePeriods = () => {
    if (!isScrollable) return periods;

    // Show 3 periods at a time, centered around current selection
    const start = Math.max(0, Math.min(currentIndex - 1, periods.length - 3));
    return periods.slice(start, start + 3);
  };

  const getVisibleStartIndex = () => {
    if (!isScrollable) return 0;
    return Math.max(0, Math.min(currentIndex - 1, periods.length - 3));
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border p-4 mb-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Calendar className="h-5 w-5 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">Pay Period</h3>
        </div>
        {periods.length > 0 && (
          <div className="text-sm text-gray-500">
            {selectedPeriod?.frequency || 'N/A'} Period
          </div>
        )}
      </div>

      <div className="flex items-center gap-2">
        {/* Left Arrow */}
        {isScrollable && (
          <button
            onClick={scrollLeft}
            disabled={currentIndex === 0}
            className="p-2 rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
        )}

        {/* Period Buttons */}
        <div className="flex gap-2 flex-1 overflow-hidden">
          {getVisiblePeriods().map((period, visibleIndex) => {
            const actualIndex = getVisibleStartIndex() + visibleIndex;
            const isActive = actualIndex === currentIndex;

            return (
              <button
                key={`${period.periodStart}-${period.periodEnd}`}
                onClick={() => handlePeriodClick(period, actualIndex)}
                className={`flex-1 px-4 py-3 rounded-lg border text-center transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white border-blue-600 shadow-md'
                    : 'bg-white text-gray-700 border-gray-200 hover:bg-blue-50 hover:border-blue-300'
                }`}
              >
                <div className="text-sm font-medium">
                  {period.displayLabel || formatPeriodLabel(period)}
                </div>
              </button>
            );
          })}
        </div>

        {/* Right Arrow */}
        {isScrollable && (
          <button
            onClick={scrollRight}
            disabled={currentIndex === periods.length - 1}
            className="p-2 rounded-lg border border-gray-200 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        )}
      </div>

      {periods.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <Calendar className="h-8 w-8 mx-auto mb-2 opacity-50" />
          <div>Loading payment periods...</div>
        </div>
      )}
    </div>
  );
};

export default PaymentPeriodSelector;