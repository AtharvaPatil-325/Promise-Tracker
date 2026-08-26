import React from 'react';
import { PromisePriority } from '../types';

interface Props {
  priority: PromisePriority;
}

export const PriorityBadge: React.FC<Props> = ({ priority }) => {
  const styles: Record<PromisePriority, string> = {
    LOW: 'bg-gray-100 text-gray-700 border-gray-300',
    MEDIUM: 'bg-blue-100 text-blue-800 border-blue-300',
    HIGH: 'bg-orange-100 text-orange-800 border-orange-300',
    URGENT: 'bg-rose-100 text-rose-800 border-rose-300 font-bold',
  };

  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded text-xs border ${styles[priority]}`}>
      {priority}
    </span>
  );
};
