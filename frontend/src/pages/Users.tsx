import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { User, UserRole } from '../types';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, UserCheck, Shield } from 'lucide-react';

export const UsersPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuth();

  const { data: users = [], isLoading } = useQuery<User[]>({
    queryKey: ['users'],
    queryFn: async () => {
      const res = await apiClient.get('/users');
      return res.data.data;
    },
  });

  const updateRoleMutation = useMutation({
    mutationFn: async ({ userId, role }: { userId: string; role: UserRole }) => {
      await apiClient.patch(`/users/${userId}/role`, { role });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: async ({ userId, enabled }: { userId: string; enabled: boolean }) => {
      await apiClient.patch(`/users/${userId}/status`, { enabled });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Team & Members</h1>
        <p className="text-sm text-gray-500 mt-0.5">Manage organization members, roles, and access controls.</p>
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
    </div>
  );
};
