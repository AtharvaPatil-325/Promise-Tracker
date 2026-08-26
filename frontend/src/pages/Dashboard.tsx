import React from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { DashboardSummary, PromiseItem } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { CheckCircle2, AlertTriangle, Clock, TrendingUp, Check } from 'lucide-react';

export const Dashboard: React.FC = () => {
  const queryClient = useQueryClient();

  const { data: summary } = useQuery<DashboardSummary>({
    queryKey: ['dashboard-summary'],
    queryFn: async () => {
      const res = await apiClient.get('/dashboard/summary');
      return res.data.data;
    },
  });

  const { data: todayPromises = [] } = useQuery<PromiseItem[]>({
    queryKey: ['dashboard-today'],
    queryFn: async () => {
      const res = await apiClient.get('/dashboard/today');
      return res.data.data;
    },
  });

  const { data: overduePromises = [] } = useQuery<PromiseItem[]>({
    queryKey: ['dashboard-overdue'],
    queryFn: async () => {
      const res = await apiClient.get('/dashboard/overdue');
      return res.data.data;
    },
  });

  const completeMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.patch(`/promises/${id}/status`, { status: 'COMPLETED' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dashboard-summary'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-today'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard-overdue'] });
    },
  });

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Promise Overview</h1>
        <p className="text-sm text-gray-500 mt-1">Track commitments made to customers and stay on top of deadlines.</p>
      </div>

      {/* Summary Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-500 uppercase">Today's Promises</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{summary?.todayCount ?? 0}</p>
          </div>
          <div className="w-10 h-10 bg-blue-50 text-blue-600 rounded-lg flex items-center justify-center">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-rose-200 bg-rose-50/20 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-rose-600 uppercase">Overdue</p>
            <p className="text-2xl font-bold text-rose-700 mt-1">{summary?.overdueCount ?? 0}</p>
          </div>
          <div className="w-10 h-10 bg-rose-100 text-rose-600 rounded-lg flex items-center justify-center">
            <AlertTriangle className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-500 uppercase">Completed This Week</p>
            <p className="text-2xl font-bold text-emerald-600 mt-1">{summary?.completedThisWeekCount ?? 0}</p>
          </div>
          <div className="w-10 h-10 bg-emerald-50 text-emerald-600 rounded-lg flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm flex items-center justify-between">
          <div>
            <p className="text-xs font-medium text-gray-500 uppercase">Completion Rate</p>
            <p className="text-2xl font-bold text-sky-600 mt-1">{summary?.completionRatePercent ?? 0}%</p>
          </div>
          <div className="w-10 h-10 bg-sky-50 text-sky-600 rounded-lg flex items-center justify-center">
            <TrendingUp className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* OVERDUE SECTION */}
      {overduePromises.length > 0 && (
        <section className="bg-rose-50/50 border border-rose-200 rounded-xl p-5 space-y-4">
          <div className="flex items-center space-x-2 text-rose-800">
            <AlertTriangle className="w-5 h-5 text-rose-600" />
            <h2 className="text-base font-bold uppercase tracking-wider">Overdue Promises ({overduePromises.length})</h2>
          </div>

          <div className="bg-white rounded-lg border border-rose-200 divide-y divide-rose-100 overflow-hidden">
            {overduePromises.map((item) => (
              <div key={item.id} className="p-4 flex items-center justify-between hover:bg-rose-50/30 transition-colors">
                <div className="space-y-1">
                  <div className="flex items-center space-x-3">
                    <span className="font-bold text-gray-900">{item.title}</span>
                    <PriorityBadge priority={item.priority} />
                  </div>
                  <div className="text-xs text-gray-500 flex items-center space-x-2">
                    <span className="font-semibold text-gray-700">{item.customerName}</span>
                    <span>•</span>
                    <span>Assigned to: {item.assignedToName}</span>
                    <span>•</span>
                    <span className="text-rose-600 font-semibold">Due: {item.dueDate}</span>
                  </div>
                </div>

                <button
                  onClick={() => completeMutation.mutate(item.id)}
                  className="flex items-center space-x-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm transition-colors"
                >
                  <Check className="w-4 h-4" />
                  <span>Mark Done</span>
                </button>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* TODAY SECTION */}
      <section className="bg-white border border-gray-200 rounded-xl p-5 space-y-4 shadow-sm">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2 text-gray-900">
            <Clock className="w-5 h-5 text-sky-600" />
            <h2 className="text-base font-bold tracking-tight">Today's Commitments</h2>
          </div>
          <span className="text-xs font-medium text-gray-500">{todayPromises.length} promises due today</span>
        </div>

        {todayPromises.length === 0 ? (
          <div className="text-center py-8 text-sm text-gray-500 border border-dashed border-gray-200 rounded-lg">
            No promises due today. Great job staying ahead!
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {todayPromises.map((item) => (
              <div key={item.id} className="py-3.5 flex items-center justify-between hover:bg-gray-50/50 transition-colors px-2 rounded-lg">
                <div className="space-y-1">
                  <div className="flex items-center space-x-3">
                    <span className="font-semibold text-gray-900">{item.title}</span>
                    <PriorityBadge priority={item.priority} />
                    <StatusBadge status={item.status} />
                  </div>
                  <div className="text-xs text-gray-500 flex items-center space-x-2">
                    <span className="font-medium text-gray-700">{item.customerName}</span>
                    {item.customerCompanyName && <span>({item.customerCompanyName})</span>}
                    <span>•</span>
                    <span>Assigned to: {item.assignedToName}</span>
                  </div>
                </div>

                <button
                  onClick={() => completeMutation.mutate(item.id)}
                  className="flex items-center space-x-1.5 px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-sm transition-colors"
                >
                  <Check className="w-4 h-4" />
                  <span>Mark Done</span>
                </button>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};
