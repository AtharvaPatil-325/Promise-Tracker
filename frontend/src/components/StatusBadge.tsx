import React from 'react';
import { PromiseStatus } from '../types';

interface Props {
  status: PromiseStatus;
}

export const StatusBadge: React.FC<Props> = ({ status }) => {
  const styles: Record<PromiseStatus, string> = {
    OPEN: 'bg-blue-50 text-blue-700 border-blue-200',
    IN_PROGRESS: 'bg-amber-50 text-amber-700 border-amber-200',
    COMPLETED: 'bg-emerald-50 text-emerald-700 border-emerald-200',
    CANCELLED: 'bg-gray-100 text-gray-600 border-gray-200',
  };

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border ${styles[status]}`}>
      {status.replace('_', ' ')}
    </span>
  );
};
