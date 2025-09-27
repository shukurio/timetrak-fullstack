import { useState } from 'react';
import { 
  Plus, Search, Filter, RefreshCw, Users, Clock, AlertTriangle, 
  TrendingUp, Eye, Ban, MoreHorizontal, Calendar, UserCheck
} from 'lucide-react';
import { format, formatDistanceToNow } from 'date-fns';
import toast from 'react-hot-toast';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import inviteService from '../../api/inviteService';
import companyService from '../../api/companyService';
import InviteShareModal from '../../components/invites/InviteShareModal';
import InviteQuickShare from '../../components/invites/InviteQuickShare';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const MultiUserInviteManagement = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [selectedInvite, setSelectedInvite] = useState(null);
  const [showShareModal, setShowShareModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [formData, setFormData] = useState({
    departmentId: '',
    maxUses: 30,
    expiryHours: 168, // 7 days
    description: ''
  });

  const queryClient = useQueryClient();

  // Fetch company data
  const { data: companyData } = useQuery({
    queryKey: ['company'],
    queryFn: companyService.getCompany
  });

  // Fetch departments
  const { data: departmentsData } = useQuery({
    queryKey: ['departments'],
    queryFn: companyService.getAllDepartments
  });

  // Fetch invites with auto-refresh
  const { data: invitesData, isLoading, error } = useQuery({
    queryKey: ['invites'],
    queryFn: () => inviteService.getActiveInvites(),
    refetchInterval: 30000, // Auto-refresh every 30 seconds
    refetchIntervalInBackground: true
  });

  // Handle both array and paginated response formats
  const invites = Array.isArray(invitesData) ? invitesData : (invitesData?.content || []);
  const stats = inviteService.getInviteStats(invites);

  // Create invite mutation
  const createInviteMutation = useMutation({
    mutationFn: inviteService.createInvite,
    onSuccess: () => {
      queryClient.invalidateQueries(['invites']);
      toast.success('Multi-user invite created successfully!');
      setShowCreateForm(false);
      resetForm();
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to create invite');
    }
  });

  // Deactivate invite mutation
  const deactivateMutation = useMutation({
    mutationFn: inviteService.deactivateInvite,
    onSuccess: () => {
      queryClient.invalidateQueries(['invites']);
      toast.success('Invite deactivated successfully!');
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Failed to deactivate invite');
    }
  });

  const resetForm = () => {
    setFormData({
      departmentId: '',
      maxUses: 30,
      expiryHours: 168,
      description: ''
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!formData.departmentId) {
      toast.error('Please select a department');
      return;
    }

    const payload = {
      companyId: companyData?.id,
      departmentId: parseInt(formData.departmentId),
      maxUses: parseInt(formData.maxUses),
      expiryHours: parseInt(formData.expiryHours),
      description: formData.description
    };

    createInviteMutation.mutate(payload);
  };

  const handleDeactivate = (inviteCode) => {
    if (window.confirm('Are you sure you want to deactivate this invite? This cannot be undone.')) {
      deactivateMutation.mutate(inviteCode);
    }
  };

  const handleShare = (invite) => {
    setSelectedInvite(invite);
    setShowShareModal(true);
  };

  const refreshData = () => {
    queryClient.invalidateQueries(['invites']);
    toast.success('Data refreshed!');
  };

  // Filter invites based on search and status
  const filteredInvites = invites.filter(invite => {
    const matchesSearch = 
      invite.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      invite.inviteCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
      invite.departmentName?.toLowerCase().includes(searchQuery.toLowerCase());

    const matchesStatus = 
      statusFilter === 'all' || 
      inviteService.getInviteStatus(invite) === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const getStatusBadge = (invite) => {
    const status = inviteService.getInviteStatus(invite);
    const colors = {
      active: 'bg-green-100 text-green-800 border-green-200',
      exhausted: 'bg-blue-100 text-blue-800 border-blue-200',
      expired: 'bg-yellow-100 text-yellow-800 border-yellow-200'
    };

    const labels = {
      active: 'Active',
      exhausted: 'Full',
      expired: 'Expired'
    };

    return (
      <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium border ${colors[status] || 'bg-gray-100 text-gray-800 border-gray-200'}`}>
        {labels[status] || 'Unknown'}
      </span>
    );
  };

  const getProgressBar = (invite) => {
    const percentage = inviteService.getUsagePercentage(invite);
    const status = inviteService.getInviteStatus(invite);

    const colors = {
      active: 'bg-green-500',
      exhausted: 'bg-blue-500',
      expired: 'bg-yellow-500',
      inactive: 'bg-gray-400'
    };

    return (
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div
          className={`h-2 rounded-full transition-all ${colors[status] || colors.inactive}`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    );
  };

  if (isLoading) return <LoadingSpinner size="lg" text="Loading invite management..." />;
  if (error) return (
    <div className="flex items-center justify-center h-64">
      <div className="text-center">
        <AlertTriangle className="h-12 w-12 text-red-500 mx-auto mb-4" />
        <p className="text-red-600">Error loading invites: {error.message}</p>
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Main container with header and content */}
      <div className="bg-white rounded-lg shadow border border-gray-200 overflow-hidden">
        <div className="flex items-center justify-center py-4 px-6 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5 text-blue-600" />
            <h1 className="text-lg font-semibold text-gray-900">Multi-User Invite Management</h1>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-end gap-2">
            <button
              onClick={refreshData}
              className="flex items-center gap-2 px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <RefreshCw size={16} />
              Refresh
            </button>
            <button
              onClick={() => setShowCreateForm(true)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus size={20} />
              Create Multi-User Invite
            </button>
          </div>
        </div>

        {/* Quick Stats Dashboard */}
        <div className="p-6 border-b border-gray-200">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-gray-50 rounded-lg shadow-sm p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Active Invites</p>
                  <p className="text-2xl font-bold text-green-600">{stats.active}</p>
                </div>
                <div className="p-2 bg-green-100 rounded-lg">
                  <UserCheck className="h-6 w-6 text-green-600" />
                </div>
              </div>
            </div>

            <div className="bg-gray-50 rounded-lg shadow-sm p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Total Registrations</p>
                  <p className="text-2xl font-bold text-blue-600">{stats.totalRegistrations}</p>
                </div>
                <div className="p-2 bg-blue-100 rounded-lg">
                  <Users className="h-6 w-6 text-blue-600" />
                </div>
              </div>
            </div>

            <div className="bg-gray-50 rounded-lg shadow-sm p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Expiring Soon</p>
                  <p className="text-2xl font-bold text-yellow-600">{stats.expiringSoon}</p>
                </div>
                <div className="p-2 bg-yellow-100 rounded-lg">
                  <Clock className="h-6 w-6 text-yellow-600" />
                </div>
              </div>
            </div>

            <div className="bg-gray-50 rounded-lg shadow-sm p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Full Invites</p>
                  <p className="text-2xl font-bold text-purple-600">{stats.full}</p>
                </div>
                <div className="p-2 bg-purple-100 rounded-lg">
                  <TrendingUp className="h-6 w-6 text-purple-600" />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Search and Filters */}
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="text"
                  placeholder="Search invites by code, description, or department..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Filter size={16} className="text-gray-400" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="full">Full</option>
                <option value="expired">Expired</option>
              </select>
            </div>
          </div>
        </div>

        {/* Invites Cards */}
        <div className="p-6">
          {filteredInvites.length === 0 ? (
            <div className="text-center py-12">
              <Users className="h-16 w-16 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {searchQuery || statusFilter !== 'all'
                  ? 'No invites match your filters'
                  : 'No invites created yet'
                }
              </h3>
              {!searchQuery && statusFilter === 'all' && (
                <p className="text-gray-400 text-sm">Create your first multi-user invite to get started</p>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredInvites.map((invite) => (
                <div key={invite.id} className="bg-gray-50 rounded-lg shadow-sm border hover:shadow-md transition-shadow p-6">
                  {/* Header */}
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg font-medium text-gray-900 truncate">
                        {invite.description || 'Multi-user invite'}
                      </h3>
                      <p className="text-sm text-gray-500 font-mono mt-1">
                        Code: {invite.inviteCode}
                      </p>
                      <p className="text-xs text-gray-400 mt-1">
                        Created {formatDistanceToNow(new Date(invite.createdAt))} ago
                      </p>
                    </div>
                    {getStatusBadge(invite)}
                  </div>

                  {/* Department */}
                  <div className="mb-4">
                    <span className="text-sm font-medium text-gray-500">Department:</span>
                    <p className="text-sm text-gray-900">{invite.departmentName || 'Any Department'}</p>
                  </div>

                  {/* Usage Progress */}
                  <div className="mb-4">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-gray-500">Usage Progress</span>
                      <span className="text-sm font-medium text-gray-900">
                        {invite.currentUses || 0}/{invite.maxUses}
                      </span>
                    </div>
                    {getProgressBar(invite)}
                  </div>

                  {/* Expiry Info */}
                  <div className="mb-4">
                    <span className="text-sm font-medium text-gray-500">Expires:</span>
                    <div className="flex items-center gap-1 mt-1">
                      <Calendar size={14} className="text-gray-400" />
                      <span className="text-sm text-gray-900">
                        {format(new Date(invite.expiresAt), 'MMM d, yyyy')}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500">
                      {formatDistanceToNow(new Date(invite.expiresAt))} remaining
                    </p>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center justify-end space-x-2 pt-4 border-t border-gray-100">
                    <InviteQuickShare
                      invite={invite}
                      companyName={companyData?.name}
                    />

                    <button
                      onClick={() => handleShare(invite)}
                      className="p-2 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-md transition-colors"
                      title="Share invite"
                    >
                      <Eye size={16} />
                    </button>

                    {inviteService.getInviteStatus(invite) === 'active' && (
                      <button
                        onClick={() => handleDeactivate(invite.inviteCode)}
                        className="p-2 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-md transition-colors"
                        title="Deactivate invite"
                      >
                        <Ban size={16} />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Create Invite Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg w-full max-w-md">
            <div className="p-6 border-b">
              <h2 className="text-lg font-semibold text-gray-900">Create Multi-User Invite</h2>
              <p className="text-sm text-gray-600 mt-1">Create an invite link for multiple employees</p>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Department <span className="text-red-500">*</span>
                </label>
                <select
                  value={formData.departmentId}
                  onChange={(e) => setFormData({...formData, departmentId: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                >
                  <option value="">Select Department</option>
                  {departmentsData?.content?.map(dept => (
                    <option key={dept.id} value={dept.id}>
                      {dept.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Maximum Users
                </label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={formData.maxUses}
                  onChange={(e) => setFormData({...formData, maxUses: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <p className="mt-1 text-xs text-gray-500">Number of people who can use this invite</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expires In
                </label>
                <select
                  value={formData.expiryHours}
                  onChange={(e) => setFormData({...formData, expiryHours: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="24">24 Hours</option>
                  <option value="72">3 Days</option>
                  <option value="168">1 Week</option>
                  <option value="336">2 Weeks</option>
                  <option value="720">1 Month</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. Q1 2024 new hires"
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateForm(false);
                    resetForm();
                  }}
                  className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createInviteMutation.isPending}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
                >
                  {createInviteMutation.isPending ? 'Creating...' : 'Create Invite'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Share Modal */}
      <InviteShareModal
        invite={selectedInvite}
        companyName={companyData?.name}
        isOpen={showShareModal}
        onClose={() => setShowShareModal(false)}
      />
    </div>
  );
};

export default MultiUserInviteManagement;