import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { User, UserRole } from '../types';
import { useAuth } from '../context/AuthContext';
import { Plus, X, ShieldCheck, UserCheck, Shield } from 'lucide-react';

export const UsersPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();

  const [isAddOpen, setIsAddOpen] = useState(false);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<UserRole>('MEMBER');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const { data: users = [], isLoading } = useQuery<User[]>({
    queryKey: ['users'],
    queryFn: async () => {
      const res = await apiClient.get('/users');
      return res.data.data;
    },
  });

  const createUserMutation = useMutation({
    mutationFn: async () => {
      setErrorMessage('');
      await apiClient.post('/users', {
        firstName,
        lastName,
        email,
        role,
        password: password || undefined,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['users-all'] });
      setIsAddOpen(false);
      resetForm();
    },
    onError: (err: any) => {
      const msg = err.response?.data?.message || 'Failed to add team member';
      setErrorMessage(msg);
    },
  });

  const updateRoleMutation = useMutation({
    mutationFn: async ({ userId, role }: { userId: string; role: UserRole }) => {
      await apiClient.patch(`/users/${userId}/role`, { role });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['users-all'] });
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: async ({ userId, enabled }: { userId: string; enabled: boolean }) => {
      await apiClient.patch(`/users/${userId}/status`, { enabled });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['users-all'] });
    },
  });

  const resetForm = () => {
    setFirstName('');
    setLastName('');
    setEmail('');
    setRole('MEMBER');
    setPassword('');
    setErrorMessage('');
  };

  const canAddMember = currentUser?.role === 'OWNER' || currentUser?.role === 'ADMIN';

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Team & Members</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage organization members, roles, and access controls.</p>
        </div>

        {canAddMember && (
          <button
            onClick={() => setIsAddOpen(true)}
            className="flex items-center space-x-2 bg-sky-600 hover:bg-sky-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>Add Team Member</span>
          </button>
        )}
      </div>

      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden divide-y divide-gray-100">
        {isLoading ? (
          <div className="p-8 text-center text-sm text-gray-500">Loading team members...</div>
        ) : (
          users.map((member) => (
            <div key={member.id} className="p-5 flex items-center justify-between hover:bg-gray-50/50 transition-colors">
              <div className="space-y-1">
                <div className="flex items-center space-x-3">
                  <span className="font-bold text-gray-900 text-sm">
                    {member.firstName} {member.lastName}
                  </span>
                  <span className="px-2 py-0.5 bg-sky-50 text-sky-700 text-xs font-semibold rounded border border-sky-200">
                    {member.role}
                  </span>
                  {!member.enabled && (
                    <span className="px-2 py-0.5 bg-rose-100 text-rose-700 text-xs font-bold rounded">
                      DISABLED
                    </span>
                  )}
                </div>
                <p className="text-xs text-gray-500">{member.email}</p>
              </div>

              {currentUser?.role === 'OWNER' && member.id !== currentUser.id && (
                <div className="flex items-center space-x-3">
                  <select
                    value={member.role}
                    onChange={(e) => updateRoleMutation.mutate({ userId: member.id, role: e.target.value as UserRole })}
                    className="px-2.5 py-1 border rounded-lg text-xs font-medium focus:ring-sky-500"
                  >
                    <option value="ADMIN">ADMIN</option>
                    <option value="MEMBER">MEMBER</option>
                  </select>

                  <button
                    onClick={() => updateStatusMutation.mutate({ userId: member.id, enabled: !member.enabled })}
                    className={`px-3 py-1 rounded-lg text-xs font-semibold transition-colors ${
                      member.enabled
                        ? 'bg-rose-50 text-rose-700 hover:bg-rose-100 border border-rose-200'
                        : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100 border border-emerald-200'
                    }`}
                  >
                    {member.enabled ? 'Disable' : 'Enable'}
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* ADD TEAM MEMBER MODAL */}
      {isAddOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="font-bold text-gray-900 text-lg">Add New Team Member</h3>
              <button onClick={() => setIsAddOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            {errorMessage && (
              <div className="bg-rose-50 text-rose-700 text-xs p-3 rounded-lg border border-rose-200">
                {errorMessage}
              </div>
            )}

            <form
              onSubmit={(e) => {
                e.preventDefault();
                createUserMutation.mutate();
              }}
              className="space-y-4"
            >
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">First Name *</label>
                  <input
                    type="text"
                    required
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    placeholder="John"
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Last Name *</label>
                  <input
                    type="text"
                    required
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    placeholder="Doe"
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Email Address *</label>
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="john.doe@company.com"
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Role *</label>
                  <select
                    value={role}
                    onChange={(e) => setRole(e.target.value as UserRole)}
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  >
                    <option value="MEMBER">MEMBER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Password (Optional)</label>
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Defaults to Password123!"
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setIsAddOpen(false)}
                  className="px-4 py-2 border text-gray-600 rounded-lg text-sm hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createUserMutation.isPending}
                  className="px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-lg text-sm font-semibold"
                >
                  {createUserMutation.isPending ? 'Saving...' : 'Add Member'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
