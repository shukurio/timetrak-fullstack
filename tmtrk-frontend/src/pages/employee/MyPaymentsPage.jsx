import { useState, useEffect } from 'react';
import { CreditCard, Calendar, DollarSign, Clock, AlertCircle, ChevronDown, ChevronUp, Briefcase } from 'lucide-react';
import employeeService from '../../api/employeeService';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const MyPaymentsPage = () => {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedPayments, setExpandedPayments] = useState(new Set());

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      setError(null);

      const paymentsData = await employeeService.getAllPayments();
      setPayments(paymentsData.content || []);
    } catch (err) {
      setError('Failed to load payment history');
      console.error('Payments error:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount || 0);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'ISSUED':
        return 'bg-blue-100 text-blue-800';
      case 'PENDING':
      case 'CALCULATED':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const togglePaymentExpansion = (paymentId) => {
    const newExpanded = new Set(expandedPayments);
    if (newExpanded.has(paymentId)) {
      newExpanded.delete(paymentId);
    } else {
      newExpanded.add(paymentId);
    }
    setExpandedPayments(newExpanded);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-64">
        <LoadingSpinner />
        <span className="ml-3 text-gray-600">Loading payments...</span>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Main container with header and content */}
      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <div className="flex items-center justify-center py-4 px-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <CreditCard className="h-5 w-5 text-blue-600" />
            <h1 className="text-lg font-semibold text-gray-900">Payment History</h1>
          </div>
        </div>

        {/* Content Area */}
        <div className="p-6">
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
              <div className="flex">
                <AlertCircle className="h-5 w-5 text-red-400 mr-2" />
                <p className="text-red-800">{error}</p>
              </div>
            </div>
          )}
        {payments.length > 0 ? (
          <div className="space-y-4">
            {payments.map((payment) => (
              <div key={payment.id} className="border border-gray-200 rounded-lg shadow-sm overflow-hidden">
                <div
                  className="p-4 hover:bg-gray-50 cursor-pointer"
                  onClick={() => togglePaymentExpansion(payment.id)}
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div className="flex items-start sm:items-center space-x-4">
                      <div className="bg-blue-100 p-2 rounded-lg flex-shrink-0">
                        <CreditCard className="h-6 w-6 text-blue-600" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">
                          {payment.formattedPeriod || `Payment #${payment.id}`}
                        </h3>
                        <div className="flex flex-wrap items-center gap-3 text-sm text-gray-600 mt-1">
                          <div className="flex items-center">
                            <Clock className="h-4 w-4 mr-1" />
                            <span>{payment.totalHours?.toFixed(1) || '0'} hours</span>
                          </div>
                          <div className="flex items-center">
                            <Calendar className="h-4 w-4 mr-1" />
                            <span>{payment.shiftsCount || 0} shifts</span>
                          </div>
                          {payment.jobsCount && (
                            <div className="flex items-center">
                              <Briefcase className="h-4 w-4 mr-1" />
                              <span>{payment.jobsCount} {payment.jobsCount === 1 ? 'job' : 'jobs'}</span>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-4">
                      <div className="sm:text-right">
                        <div className="text-2xl font-bold text-gray-900">
                          {formatCurrency(payment.totalEarnings)}
                        </div>
                        <div className="flex items-center sm:justify-end space-x-2 mt-1">
                          <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full uppercase ${getStatusColor(payment.status)}`}>
                            {payment.status || 'pending'}
                          </span>
                          {payment.completedAt && (
                            <span className="text-xs text-gray-500">
                              {formatDate(payment.completedAt)}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex-shrink-0">
                        {expandedPayments.has(payment.id) ? (
                          <ChevronUp className="h-5 w-5 text-gray-500" />
                        ) : (
                          <ChevronDown className="h-5 w-5 text-gray-500" />
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Expandable Job Details Section */}
                {expandedPayments.has(payment.id) && payment.jobDetails && (
                  <div className="bg-gray-50 border-t border-gray-200 p-4">
                    <h4 className="font-semibold text-gray-900 mb-3">Job Breakdown</h4>
                    <div className="space-y-3">
                      {payment.jobDetails.map((job, index) => (
                        <div key={index} className="bg-white rounded-lg p-3 border border-gray-200">
                          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                            <div className="flex-1">
                              <h5 className="font-medium text-gray-900">{job.jobTitle}</h5>
                              <div className="flex flex-wrap gap-3 text-sm text-gray-600 mt-1">
                                <span>{job.shiftsCount} {job.shiftsCount === 1 ? 'shift' : 'shifts'}</span>
                                <span>{job.totalHours?.toFixed(1)} hours</span>
                                <span>{formatCurrency(job.hourlyRate)}/hr</span>
                              </div>
                            </div>
                            <div className="text-right">
                              <p className="font-semibold text-green-600">
                                {formatCurrency(job.totalEarnings)}
                              </p>
                              {job.percentageOfTotalPay && (
                                <p className="text-sm text-gray-500 mt-1">
                                  {job.percentageOfTotalPay.toFixed(1)}% of total
                                </p>
                              )}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Additional Payment Details */}
                    <div className="mt-4 pt-4 border-t border-gray-200">
                      <div className="grid grid-cols-2 gap-2 text-sm">
                        {payment.averageHourlyRate && (
                          <div>
                            <span className="text-gray-600">Average Rate:</span>
                            <span className="ml-2 font-medium">{formatCurrency(payment.averageHourlyRate)}/hr</span>
                          </div>
                        )}
                        {payment.calculatedAt && (
                          <div>
                            <span className="text-gray-600">Calculated:</span>
                            <span className="ml-2 font-medium">{formatDate(payment.calculatedAt)}</span>
                          </div>
                        )}
                        {payment.issuedAt && (
                          <div>
                            <span className="text-gray-600">Issued:</span>
                            <span className="ml-2 font-medium">{formatDate(payment.issuedAt)}</span>
                          </div>
                        )}
                        {payment.notes && (
                          <div className="col-span-2 mt-2">
                            <span className="text-gray-600">Notes:</span>
                            <p className="mt-1 text-gray-700">{payment.notes}</p>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
          ) : (
            <div className="flex items-center justify-center py-12">
              <div className="text-center">
                <DollarSign className="h-16 w-16 text-gray-400 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No Payments Yet</h3>
                <p className="text-gray-600">
                  Your payment history will appear here once you complete your first pay period.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyPaymentsPage;