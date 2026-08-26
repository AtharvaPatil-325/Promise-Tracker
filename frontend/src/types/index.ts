export type UserRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type PromisePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type PromiseStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface User {
  id: string;
  organizationId: string;
  organizationName: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  user: User;
}

export interface Customer {
  id: string;
  organizationId: string;
  name: string;
  email?: string;
  phone?: string;
  companyName?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PromiseItem {
  id: string;
  organizationId: string;
  customerId: string;
  customerName: string;
  customerCompanyName?: string;
  createdById: string;
  createdByName: string;
  assignedToId: string;
  assignedToName: string;
  title: string;
  description?: string;
  sourceText?: string;
  dueDate: string;
  priority: PromisePriority;
  status: PromiseStatus;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
  overdue: boolean;
}

export interface PromiseActivity {
  id: string;
  promiseId: string;
  userId: string;
  userName: string;
  action: 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'STATUS_CHANGED' | 'COMPLETED' | 'CANCELLED';
  oldStatus?: string;
  newStatus?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DashboardSummary {
  todayCount: number;
  overdueCount: number;
  completedThisWeekCount: number;
  completionRatePercent: number;
}

export interface AnalyticsOverview {
  totalPromises: number;
  completedPromises: number;
  overduePromises: number;
  completionRatePercent: number;
  avgCompletionDays: number;
}

export interface AnalyticsPromises {
  byPriority: Record<PromisePriority, number>;
  byAssignee: Array<{
    userId: string;
    userName: string;
    totalPromises: number;
    completedPromises: number;
  }>;
  byMonth: Array<{
    month: string;
    totalPromises: number;
    completedPromises: number;
  }>;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
}
