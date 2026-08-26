import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { PromiseItem, PromisePriority, PromiseStatus, Customer, User, PromiseActivity, PageResponse } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { Plus, Search, Filter, History, Check, X, UserCheck } from 'lucide-react';

export const PromisesPage: React.FC = () => {
  const queryClient = useQueryClient();

  const [statusFilter, setStatusFilter] = useState<string>('');
  const [priorityFilter, setPriorityFilter] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [page, setPage] = useState<number>(0);

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [selectedPromise, setSelectedPromise] = useState<PromiseItem | null>(null);
  const [activities, setActivities] = useState<PromiseActivity[]>([]);

  // Form state
  const [newTitle, setNewTitle] = useState('');
  const [newCustomerId, setNewCustomerId] = useState('');
  const [newAssignedTo, setNewAssignedTo] = useState('');
  const [newDueDate, setNewDueDate] = useState('');
  const [newPriority, setNewPriority] = useState<PromisePriority>('HIGH');
  const [newDescription, setNewDescription] = useState('');
  const [newSourceText, setNewSourceText] = useState('');

  // Fetch Customers & Users for selectors
  const { data: customers = [] } = useQuery<Customer[]>({
    queryKey: ['customers-all'],
    queryFn: async () => {
      const res = await apiClient.get('/customers?size=100');
      return res.data.data.content;
    },
  });

  const { data: users = [] } = useQuery<User[]>({
    queryKey: ['users-all'],
    queryFn: async () => {
      const res = await apiClient.get('/users');
      return res.data.data;
    },
  });

  // Fetch Promises
  const { data: promisesPage, isLoading } = useQuery<PageResponse<PromiseItem>>({
    queryKey: ['promises', statusFilter, priorityFilter, searchQuery, page],
    queryFn: async () => {
      const params = new URLSearchParams();
      if (statusFilter) params.append('status', statusFilter);
      if (priorityFilter) params.append('priority', priorityFilter);
      if (searchQuery) params.append('search', searchQuery);
      params.append('page', page.toString());
      params.append('size', '20');

      const res = await apiClient.get(`/promises?${params.toString()}`);
      return res.data.data;
    },
  });

  const createPromiseMutation = useMutation({
    mutationFn: async () => {
      await apiClient.post('/promises', {
        title: newTitle,
        customerId: newCustomerId,
        assignedTo: newAssignedTo || undefined,
        dueDate: newDueDate,
        priority: newPriority,
        description: newDescription || undefined,
        sourceText: newSourceText || undefined,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['promises'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] });
      setIsCreateOpen(false);
      resetForm();
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: async ({ id, status }: { id: string; status: PromiseStatus }) => {
      await apiClient.patch(`/promises/${id}/status`, { status });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['promises'] });
    },
  });

  const resetForm = () => {
    setNewTitle('');
    setNewCustomerId('');
    setNewAssignedTo('');
    setNewDueDate('');
    setNewPriority('HIGH');
    setNewDescription('');
    setNewSourceText('');
  };

  const fetchActivities = async (promise: PromiseItem) => {
    setSelectedPromise(promise);
    try {
      const res = await apiClient.get(`/promises/${promise.id}/activities`);
      setActivities(res.data.data);
    } catch (err) {
      setActivities([]);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Customer Promises</h1>
          <p className="text-sm text-gray-500 mt-0.5">Manage, assign, and resolve customer commitments.</p>
        </div>
        <button
          onClick={() => setIsCreateOpen(true)}
          className="flex items-center space-x-2 bg-sky-600 hover:bg-sky-700 text-white px-4 py-2.5 rounded-lg text-sm font-semibold shadow-sm transition-colors"
        >
          <Plus className="w-4 h-4" />
          <span>New Promise</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-sm flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search promise title or description..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0);
            }}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
          >
            <option value="">All Statuses</option>
            <option value="OPEN">OPEN</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) => {
              setPriorityFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-sky-500"
          >
            <option value="">All Priorities</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="URGENT">URGENT</option>
          </select>
        </div>
      </div>

      {/* Promises List */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden divide-y divide-gray-100">
        {isLoading ? (
          <div className="p-8 text-center text-sm text-gray-500">Loading promises...</div>
        ) : promisesPage?.content.length === 0 ? (
          <div className="p-8 text-center text-sm text-gray-500">No promises match the criteria.</div>
        ) : (
          promisesPage?.content.map((promise) => (
            <div key={promise.id} className="p-5 hover:bg-gray-50/60 transition-colors flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="space-y-1.5 flex-1">
                <div className="flex items-center space-x-3 flex-wrap gap-y-1">
                  <span className="font-bold text-gray-900 text-base">{promise.title}</span>
                  <PriorityBadge priority={promise.priority} />
                  <StatusBadge status={promise.status} />
                  {promise.overdue && (
                    <span className="bg-rose-100 text-rose-700 px-2 py-0.5 rounded text-xs font-bold">OVERDUE</span>
                  )}
                </div>

                <div className="text-xs text-gray-600 flex flex-wrap items-center gap-x-3 gap-y-1">
                  <span className="font-semibold text-gray-800">Customer: {promise.customerName}</span>
                  <span>•</span>
                  <span>Assigned: <strong className="text-gray-800">{promise.assignedToName}</strong></span>
                  <span>•</span>
                  <span className={promise.overdue ? 'text-rose-600 font-bold' : 'text-gray-600'}>
                    Due: {promise.dueDate}
                  </span>
                </div>

                {promise.sourceText && (
                  <p className="text-xs italic bg-gray-50 text-gray-600 p-2 rounded border border-gray-200 mt-2">
                    "{promise.sourceText}"
                  </p>
                )}
              </div>

              {/* Action Buttons */}
              <div className="flex items-center space-x-2 shrink-0">
                <select
                  value={promise.status}
                  onChange={(e) => updateStatusMutation.mutate({ id: promise.id, status: e.target.value as PromiseStatus })}
                  className="px-2.5 py-1.5 border border-gray-300 rounded-lg text-xs font-medium focus:ring-sky-500"
                >
                  <option value="OPEN">OPEN</option>
                  <option value="IN_PROGRESS">IN PROGRESS</option>
                  <option value="COMPLETED">COMPLETED</option>
                  <option value="CANCELLED">CANCELLED</option>
                </select>

                <button
                  onClick={() => fetchActivities(promise)}
                  title="Audit Trail / History"
                  className="p-2 border border-gray-200 hover:bg-gray-100 rounded-lg text-gray-600 transition-colors"
                >
                  <History className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* CREATE MODAL */}
      {isCreateOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <h3 className="font-bold text-gray-900 text-lg">Create New Promise</h3>
              <button onClick={() => setIsCreateOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                createPromiseMutation.mutate();
              }}
              className="space-y-4"
            >
              <div>
                <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Promise Title *</label>
                <input
                  type="text"
                  required
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="Send quotation for software license"
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Customer *</label>
                  <select
                    required
                    value={newCustomerId}
                    onChange={(e) => setNewCustomerId(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  >
                    <option value="">Select Customer...</option>
                    {customers.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name} {c.companyName ? `(${c.companyName})` : ''}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Assigned To</label>
                  <select
                    value={newAssignedTo}
                    onChange={(e) => setNewAssignedTo(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  >
                    <option value="">Assign to Me</option>
                    {users.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.firstName} {u.lastName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Due Date *</label>
                  <input
                    type="date"
                    required
                    value={newDueDate}
                    onChange={(e) => setNewDueDate(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Priority *</label>
                  <select
                    value={newPriority}
                    onChange={(e) => setNewPriority(e.target.value as PromisePriority)}
                    className="w-full px-3 py-2 border rounded-lg text-sm focus:ring-2 focus:ring-sky-500"
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="URGENT">URGENT</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase text-gray-700 mb-1">Source Text (Optional)</label>
                <textarea
                  rows={2}
                  value={newSourceText}
                  onChange={(e) => setNewSourceText(e.target.value)}
                  placeholder="Original text selected or copied from email/chat..."
                  className="w-full px-3 py-2 border rounded-lg text-xs font-mono focus:ring-2 focus:ring-sky-500"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setIsCreateOpen(false)}
                  className="px-4 py-2 border text-gray-600 rounded-lg text-sm hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createPromiseMutation.isPending}
                  className="px-4 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-lg text-sm font-semibold"
                >
                  {createPromiseMutation.isPending ? 'Saving...' : 'Create Promise'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AUDIT TRAIL MODAL */}
      {selectedPromise && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl space-y-4">
            <div className="flex justify-between items-center border-b pb-3">
              <div>
                <h3 className="font-bold text-gray-900 text-base">Audit Trail</h3>
                <p className="text-xs text-gray-500">{selectedPromise.title}</p>
              </div>
              <button onClick={() => setSelectedPromise(null)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 max-h-80 overflow-y-auto pr-1">
              {activities.length === 0 ? (
                <p className="text-xs text-gray-500 text-center py-4">No audit logs available.</p>
              ) : (
                activities.map((act) => (
                  <div key={act.id} className="text-xs border-l-2 border-sky-500 pl-3 py-1 space-y-0.5">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-gray-800">{act.userName}</span>
                      <span className="text-gray-400 font-mono">{new Date(act.createdAt).toLocaleString()}</span>
                    </div>
                    <p className="text-gray-600">
                      Action: <strong className="text-sky-700">{act.action}</strong>
                      {act.oldStatus && act.newStatus && ` (${act.oldStatus} → ${act.newStatus})`}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
