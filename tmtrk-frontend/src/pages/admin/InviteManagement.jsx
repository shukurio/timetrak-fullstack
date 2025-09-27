import { useState, useEffect } from 'react';
import { Plus, Copy, Trash2, Mail, Calendar, Clock, AlertCircle, Check } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import inviteService from '../../api/inviteService';
import companyService from '../../api/companyService';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const InviteManagement = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [copiedCode, setCopiedCode] = useState(null);
  const [formData, setFormData] = useState({
    departmentId: '',
    email: '',
    expirationHours: 72
  });

  const queryClient = useQueryClient();

  // Fetch departments for dropdown
  const { data: departmentsData } = useQuery({
    queryKey: ['departments'],
    queryFn: companyService.getAllDepartments
  });

  // Fetch invites
  const { data: invitesData, isLoading, error } = useQuery({
    queryKey: ['invites'],
    queryFn: () => inviteService.getActiveInvites(),
    refetchInterval: 30000 // Refresh every 30 seconds
  });

  // Extract invites array from paginated response
  const invites = invitesData?.content || [];

  // Create invite mutation
  const createInviteMutation = useMutation({
    mutationFn: inviteService.createInvite,
    onSuccess: () => {
      queryClient.invalidateQueries(['invites']);
      toast.success('Invite created successfully!');
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
      email: '',
      expirationHours: 72
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!formData.departmentId) {
      toast.error('Please select a department');
      return;
    }

    const payload = {
      companyId: 0, // Backend sets this from auth context
      departmentId: parseInt(formData.departmentId),
      maxUses: 1, // Default to 1 use per invite
      expiryHours: parseInt(formData.expirationHours)
    };

    createInviteMutation.mutate(payload);
  };

  const copyInviteLink = async (inviteLink, inviteCode) => {
    try {
      await inviteService.copyToClipboard(inviteLink);
      setCopiedCode(inviteCode);
      toast.success('Invite link copied to clipboard!');
      
      // Reset copied state after 2 seconds
      setTimeout(() => setCopiedCode(null), 2000);
    } catch (error) {
      toast.error('Failed to copy link');
    }
  };

  const handleDeactivate = (inviteCode) => {
    if (window.confirm('Are you sure you want to deactivate this invite?')) {
      deactivateMutation.mutate(inviteCode);
    }
  };

  const getStatusColor = (invite) => {
    if (!invite.isActive) return 'bg-gray-100 text-gray-800';
    
    const now = new Date();
    const expiresAt = new Date(invite.expiresAt);
    const hoursUntilExpiry = (expiresAt - now) / (1000 * 60 * 60);
    
    if (hoursUntilExpiry < 24) return 'bg-yellow-100 text-yellow-800';
    return 'bg-green-100 text-green-800';
  };

  const getStatusText = (invite) => {
    if (!invite.isActive) return 'Inactive';
    
    const now = new Date();
    const expiresAt = new Date(invite.expiresAt);
    
    if (expiresAt < now) return 'Expired';
    return 'Active';
  };

  if (isLoading) return <LoadingSpinner size="lg" text="Loading invites..." />;
  if (error) return (
    <div className="flex items-center justify-center h-64">
      <div className="text-center">
        <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
        <p className="text-red-600">Error loading invites: {error.message}</p>
      </div>
    </div>
  );

  const activeInvites = invites.filter(invite => invite.isActive);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Invite Management</h1>
          <p className="text-gray-600 mt-1">Create and manage employee invitation links</p>
        </div>
        <button
          onClick={() => setShowCreateForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus size={20} />
          Create Invite
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Invites</p>
              <p className="text-2xl font-bold text-gray-900">{invites.length}</p>
            </div>
            <Clock className="h-8 w-8 text-gray-400" />
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Active Invites</p>
              <p className="text-2xl font-bold text-green-600">{activeInvites.length}</p>
            </div>
            <Check className="h-8 w-8 text-green-400" />
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">With Email</p>
              <p className="text-2xl font-bold text-blue-600">
                {invites.filter(i => i.email).length}
              </p>
            </div>
            <Mail className="h-8 w-8 text-blue-400" />
          </div>
        </div>
      </div>

      {/* Invites Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Invite Code
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Department
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Expires
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {invites.map((invite) => (
                <tr key={invite.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-sm text-gray-900">
                        {invite.inviteCode}
                      </span>
                      <button
                        onClick={() => copyInviteLink(invite.inviteLink, invite.inviteCode)}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        title="Copy invite link"
                      >
                        {copiedCode === invite.inviteCode ? (
                          <Check size={16} className="text-green-500" />
                        ) : (
                          <Copy size={16} />
                        )}
                      </button>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-sm text-gray-900">
                      {invite.departmentName || 'N/A'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {invite.email || '-'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      getStatusColor(invite)
                    }`}>
                      {getStatusText(invite)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-1 text-sm text-gray-900">
                      <Calendar size={14} />
                      {format(new Date(invite.expiresAt), 'MMM d, yyyy HH:mm')}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {format(new Date(invite.createdAt), 'MMM d, yyyy')}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {invite.isActive && (
                      <button
                        onClick={() => handleDeactivate(invite.inviteCode)}
                        className="text-red-600 hover:text-red-900 transition-colors"
                        title="Deactivate invite"
                      >
                        <Trash2 size={16} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {invites.length === 0 && (
          <div className="text-center py-12">
            <Mail className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500">No invites created yet</p>
            <p className="text-gray-400 text-sm mt-1">Create your first invite to get started</p>
          </div>
        )}
      </div>

      {/* Create Invite Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Create New Invite</h2>
            
            <form onSubmit={handleSubmit} className="space-y-4">
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
                  Email (Optional)
                </label>
                <input
                  type="email"
                  placeholder="john@example.com"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <p className="mt-1 text-xs text-gray-500">
                  If specified, only this email can use the invite
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expires In
                </label>
                <select
                  value={formData.expirationHours}
                  onChange={(e) => setFormData({...formData, expirationHours: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="24">24 Hours</option>
                  <option value="48">48 Hours</option>
                  <option value="72">72 Hours</option>
                  <option value="168">1 Week</option>
                  <option value="336">2 Weeks</option>
                  <option value="720">1 Month</option>
                </select>
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
    </div>
  );
};

export default InviteManagement;