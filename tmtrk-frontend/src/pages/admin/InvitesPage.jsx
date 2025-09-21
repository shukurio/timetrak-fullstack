import { useState, useEffect } from 'react';
import { Plus, Search, Calendar, Users, Clock, MoreVertical, Ban, Eye } from 'lucide-react';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import inviteService from '../../api/inviteService';
import companyService from '../../api/companyService';
import InviteSharing from '../../components/admin/InviteSharing';
import LoadingSpinner from '../../components/common/LoadingSpinner';

const InvitesPage = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedInvite, setSelectedInvite] = useState(null);
  const [showShareModal, setShowShareModal] = useState(false);
  const [createFormData, setCreateFormData] = useState({
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

  // Fetch departments data
  const { data: departmentsData } = useQuery({
    queryKey: ['departments'],
    queryFn: companyService.getAllDepartments
  });

  // Fetch invites
  const { data: invitesData, isLoading, error } = useQuery({
    queryKey: ['invites'],
    queryFn: inviteService.getInvites
  });

  // Create invite mutation
  const createInviteMutation = useMutation({
    mutationFn: inviteService.createInvite,
    onSuccess: () => {
      queryClient.invalidateQueries(['invites']);
      toast.success('Invite created successfully!');
      setShowCreateForm(false);
      resetCreateForm();
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

  const resetCreateForm = () => {
    setCreateFormData({
      departmentId: '',
      maxUses: 30,
      expiryHours: 168,
      description: ''
    });
  };

  const handleCreateInvite = (e) => {
    e.preventDefault();
    
    // Validate department is selected
    if (!createFormData.departmentId) {
      toast.error('Please select a department');
      return;
    }
    
    const payload = {
      companyId: companyData?.id,
      departmentId: parseInt(createFormData.departmentId),
      maxUses: parseInt(createFormData.maxUses),
      expiryHours: parseInt(createFormData.expiryHours),
      description: createFormData.description
    };
    createInviteMutation.mutate(payload);
  };

  const handleDeactivate = (inviteCode) => {
    if (window.confirm('Are you sure you want to deactivate this invite?')) {
      deactivateMutation.mutate(inviteCode);
    }
  };

  const handleShare = (invite) => {
    setSelectedInvite(invite);
    setShowShareModal(true);
  };

  const filteredInvites = invitesData?.content?.filter(invite =>
    invite.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
    invite.inviteCode.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

  if (isLoading) return <LoadingSpinner size="lg" text="Loading invites..." />;
  if (error) return <div className="text-red-600">Error loading invites: {error.message}</div>;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Team Invites</h1>
          <p className="text-gray-600 mt-1">Manage employee invitation links</p>
        </div>
        <button
          onClick={() => setShowCreateForm(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus size={20} />
          Create Invite
        </button>
      </div>

      {/* Search and Stats */}
      <div className="flex gap-4 items-center">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Search invites..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>
        <div className="flex gap-4 text-sm text-gray-600">
          <div className="flex items-center gap-1">
            <div className="w-3 h-3 bg-green-500 rounded-full"></div>
            Active: {filteredInvites.filter(invite => invite.isActive).length}
          </div>
          <div className="flex items-center gap-1">
            <div className="w-3 h-3 bg-gray-400 rounded-full"></div>
            Expired: {filteredInvites.filter(invite => !invite.isActive).length}
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
                  Invite Details
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Department
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usage
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Expires
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredInvites.map((invite) => (
                <tr key={invite.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">
                        {invite.description || 'No description'}
                      </div>
                      <div className="text-sm text-gray-500 font-mono">
                        {invite.inviteCode}
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {invite.departmentName || 'N/A'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <Users size={16} className="text-gray-400" />
                      <span className="text-sm text-gray-900">
                        {invite.currentUses}/{invite.maxUses}
                      </span>
                      <div className="w-16 bg-gray-200 rounded-full h-2">
                        <div
                          className="bg-blue-600 h-2 rounded-full"
                          style={{ width: `${(invite.currentUses / invite.maxUses) * 100}%` }}
                        />
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                      invite.isActive 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-gray-100 text-gray-800'
                    }`}>
                      {invite.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2 text-sm text-gray-900">
                      <Clock size={16} className="text-gray-400" />
                      {format(new Date(invite.expiresAt), 'MMM d, yyyy')}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center gap-2 justify-end">
                      <button
                        onClick={() => handleShare(invite)}
                        className="text-blue-600 hover:text-blue-900"
                        title="Share invite"
                      >
                        <Eye size={16} />
                      </button>
                      {invite.isActive && (
                        <button
                          onClick={() => handleDeactivate(invite.inviteCode)}
                          className="text-red-600 hover:text-red-900"
                          title="Deactivate invite"
                        >
                          <Ban size={16} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredInvites.length === 0 && (
          <div className="text-center py-8">
            <div className="text-gray-500">No invites found</div>
          </div>
        )}
      </div>

      {/* Create Invite Modal */}
      {showCreateForm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Create New Invite</h2>
            
            <form onSubmit={handleCreateInvite} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Department <span className="text-red-500">*</span>
                </label>
                <select
                  value={createFormData.departmentId}
                  onChange={(e) => setCreateFormData({...createFormData, departmentId: e.target.value})}
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
                <p className="mt-1 text-xs text-gray-500">
                  New employees will be assigned to this department
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Maximum Uses
                </label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={createFormData.maxUses}
                  onChange={(e) => setCreateFormData({...createFormData, maxUses: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expires In (Hours)
                </label>
                <select
                  value={createFormData.expiryHours}
                  onChange={(e) => setCreateFormData({...createFormData, expiryHours: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="24">1 Day</option>
                  <option value="72">3 Days</option>
                  <option value="168">1 Week</option>
                  <option value="336">2 Weeks</option>
                  <option value="720">1 Month</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <input
                  type="text"
                  placeholder="e.g. New hires for Q1 2024"
                  value={createFormData.description}
                  onChange={(e) => setCreateFormData({...createFormData, description: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              <div className="flex justify-end gap-2 pt-4">
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateForm(false);
                    resetCreateForm();
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
      {showShareModal && selectedInvite && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-lg font-semibold text-gray-900">Share Invite</h2>
              <button
                onClick={() => setShowShareModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
            
            <div className="mb-4">
              <div className="text-sm text-gray-600 mb-2">
                <strong>{selectedInvite.description || 'Team Invite'}</strong>
              </div>
              <div className="text-xs text-gray-500">
                {selectedInvite.departmentName && `Department: ${selectedInvite.departmentName} • `}
                Uses: {selectedInvite.currentUses}/{selectedInvite.maxUses} • 
                Expires: {format(new Date(selectedInvite.expiresAt), 'MMM d, yyyy')}
              </div>
            </div>

            <InviteSharing
              inviteCode={selectedInvite.inviteCode}
              onQRCodeGenerate={(url) => {
                // Handle QR code generation - could open a modal with QR code
                console.log('Generate QR for:', url);
                toast.success('QR code functionality can be implemented with qr-code library');
              }}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default InvitesPage;