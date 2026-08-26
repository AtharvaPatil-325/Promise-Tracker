import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { Customer, PromiseItem, PageResponse } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { PriorityBadge } from '../components/PriorityBadge';
import { ArrowLeft, Building, Mail, Phone, Clock, FileText } from 'lucide-react';

export const CustomerDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: customer } = useQuery<Customer>({
    queryKey: ['customer', id],
    queryFn: async () => {
      const res = await apiClient.get(`/customers/${id}`);
      return res.data.data;
    },
    enabled: !!id,
  });

  const { data: promises = [] } = useQuery<PromiseItem[]>({
    queryKey: ['customer-promises', id],
    queryFn: async () => {
      const res = await apiClient.get(`/promises?customerId=${id}&size=100`);
      return res.data.data.content;
    },
    enabled: !!id,
  });

  if (!customer) {
    return <div className="p-8 text-center text-sm text-gray-500">Loading customer profile...</div>;
  }

  return (
    <div className="space-y-6">
      <button
        onClick={() => navigate('/customers')}
        className="flex items-center space-x-2 text-xs font-semibold text-gray-500 hover:text-gray-900 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        <span>Back to Customers</span>
      </button>

      {/* Customer Header */}
      <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">{customer.name}</h1>
            {customer.companyName && (
              <p className="text-sm font-semibold text-sky-700 flex items-center space-x-1.5 mt-0.5">
                <Building className="w-4 h-4" />
                <span>{customer.companyName}</span>
              </p>
            )}
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-6 text-xs text-gray-600 border-t border-gray-100 pt-4">
          {customer.email && (
            <div className="flex items-center space-x-2">
              <Mail className="w-4 h-4 text-gray-400" />
              <span>{customer.email}</span>
            </div>
          )}
          {customer.phone && (
            <div className="flex items-center space-x-2">
              <Phone className="w-4 h-4 text-gray-400" />
              <span>{customer.phone}</span>
            </div>
          )}
        </div>

        {customer.notes && (
          <div className="bg-gray-50 p-3 rounded-lg border border-gray-200 text-xs text-gray-700">
            <span className="font-semibold block mb-1">Notes:</span>
            {customer.notes}
          </div>
        )}
      </div>

      {/* Promises History for Customer */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Clock className="w-5 h-5 text-sky-600" />
            <h2 className="text-base font-bold text-gray-900">Promise History ({promises.length})</h2>
          </div>
        </div>

        {promises.length === 0 ? (
          <div className="text-center py-8 text-sm text-gray-500 border border-dashed border-gray-200 rounded-lg">
            No promises recorded for this customer yet.
          </div>
        ) : (
          <div className="divide-y divide-gray-100">
            {promises.map((p) => (
              <div key={p.id} className="py-3.5 flex items-center justify-between">
                <div className="space-y-1">
                  <div className="flex items-center space-x-3">
                    <span className="font-bold text-gray-900">{p.title}</span>
                    <PriorityBadge priority={p.priority} />
                    <StatusBadge status={p.status} />
                  </div>
                  <div className="text-xs text-gray-500">
                    Assigned: <strong className="text-gray-700">{p.assignedToName}</strong> • Due: {p.dueDate}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
