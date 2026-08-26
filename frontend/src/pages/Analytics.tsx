import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { AnalyticsOverview, AnalyticsPromises } from '../types';
import { BarChart3, CheckCircle2, AlertTriangle, Clock, TrendingUp, Users } from 'lucide-react';

export const AnalyticsPage: React.FC = () => {
  const { data: overview } = useQuery<AnalyticsOverview>({
    queryKey: ['analytics-overview'],
    queryFn: async () => {
      const res = await apiClient.get('/analytics/overview');
      return res.data.data;
    },
  });

  const { data: promiseAnalytics } = useQuery<AnalyticsPromises>({
    queryKey: ['analytics-promises'],
    queryFn: async () => {
      const res = await apiClient.get('/analytics/promises');
      return res.data.data;
    },
  });

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Promise Analytics</h1>
        <p className="text-sm text-gray-500 mt-1">Measure commitment reliability, team performance, and resolution velocity.</p>
      </div>

      {/* High-level metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs font-semibold text-gray-500 uppercase">Total Promises</p>
          <p className="text-2xl font-bold text-gray-900 mt-1">{overview?.totalPromises ?? 0}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs font-semibold text-emerald-600 uppercase">Completed</p>
          <p className="text-2xl font-bold text-emerald-600 mt-1">{overview?.completedPromises ?? 0}</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs font-semibold text-sky-600 uppercase">Completion Rate</p>
          <p className="text-2xl font-bold text-sky-600 mt-1">{overview?.completionRatePercent ?? 0}%</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm">
          <p className="text-xs font-semibold text-purple-600 uppercase">Avg Resolution Time</p>
          <p className="text-2xl font-bold text-purple-600 mt-1">{overview?.avgCompletionDays ?? 0} days</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Promises by Priority */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm space-y-4">
          <h2 className="text-base font-bold text-gray-900 flex items-center space-x-2">
            <BarChart3 className="w-5 h-5 text-sky-600" />
            <span>Promises by Priority</span>
          </h2>
          <div className="space-y-3">
            {promiseAnalytics?.byPriority &&
              Object.entries(promiseAnalytics.byPriority).map(([priority, count]) => (
                <div key={priority} className="space-y-1">
                  <div className="flex justify-between text-xs font-medium text-gray-700">
                    <span>{priority}</span>
                    <span>{count}</span>
                  </div>
                  <div className="w-full bg-gray-100 h-2.5 rounded-full overflow-hidden">
                    <div
                      className={`h-full ${
                        priority === 'URGENT'
                          ? 'bg-rose-500'
                          : priority === 'HIGH'
                          ? 'bg-orange-500'
                          : priority === 'MEDIUM'
                          ? 'bg-sky-500'
                          : 'bg-gray-400'
                      }`}
                      style={{
                        width: `${
                          (overview?.totalPromises ?? 0) > 0
                            ? (count / (overview?.totalPromises ?? 1)) * 100
                            : 0
                        }%`,
                      }}
                    />
                  </div>
                </div>
              ))}
          </div>
        </div>

        {/* Employee Performance */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-sm space-y-4">
          <h2 className="text-base font-bold text-gray-900 flex items-center space-x-2">
            <Users className="w-5 h-5 text-sky-600" />
            <span>Team Performance</span>
          </h2>

          <div className="divide-y divide-gray-100">
            {promiseAnalytics?.byAssignee?.map((member) => (
              <div key={member.userId} className="py-3 flex items-center justify-between">
                <div>
                  <p className="font-semibold text-xs text-gray-900">{member.userName}</p>
                  <p className="text-xs text-gray-500">Total Promises: {member.totalPromises}</p>
                </div>
                <div className="text-right">
                  <p className="text-xs font-bold text-emerald-600">{member.completedPromises} Done</p>
                  <p className="text-xs text-gray-400">
                    {member.totalPromises > 0
                      ? Math.round((member.completedPromises / member.totalPromises) * 100)
                      : 0}
                    % rate
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
