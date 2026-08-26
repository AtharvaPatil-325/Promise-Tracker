import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  CheckCircle2, 
  Clock, 
  Users, 
  BarChart3, 
  LogOut, 
  Building2, 
  UserCheck,
  ShieldAlert
} from 'lucide-react';

export const Layout: React.FC = () => {
  const { user, logout } = useAuth();

  const navItems = [
    { label: 'Dashboard', path: '/', icon: CheckCircle2 },
    { label: 'Promises', path: '/promises', icon: Clock },
    { label: 'Customers', path: '/customers', icon: Users },
    ...(user?.role === 'OWNER' || user?.role === 'ADMIN'
      ? [
          { label: 'Analytics', path: '/analytics', icon: BarChart3 },
          { label: 'Team', path: '/users', icon: UserCheck },
        ]
      : []),
  ];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Header Bar */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 bg-sky-600 rounded-lg flex items-center justify-center text-white font-bold text-lg shadow-sm">
              P
            </div>
            <div>
              <span className="font-extrabold text-gray-900 text-lg tracking-tight">PromiseTracker</span>
              <span className="ml-2 px-2 py-0.5 text-xs bg-sky-100 text-sky-800 rounded font-medium">SaaS</span>
            </div>
          </div>

          <div className="flex items-center space-x-6">
            <div className="hidden md:flex items-center space-x-2 text-xs text-gray-500 bg-gray-100 px-3 py-1.5 rounded-full">
              <Building2 className="w-3.5 h-3.5 text-gray-600" />
              <span className="font-semibold text-gray-700">{user?.organizationName}</span>
              <span className="text-gray-400">|</span>
              <span className="font-medium text-sky-700">{user?.role}</span>
            </div>

            <div className="flex items-center space-x-3">
              <div className="text-right text-xs hidden sm:block">
                <p className="font-semibold text-gray-900">{user?.firstName} {user?.lastName}</p>
                <p className="text-gray-500">{user?.email}</p>
              </div>
              <button
                onClick={logout}
                title="Logout"
                className="p-2 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
              >
                <LogOut className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Body */}
      <div className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 flex gap-6">
        {/* Navigation Sidebar */}
        <aside className="w-56 shrink-0 hidden md:block">
          <nav className="space-y-1">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/'}
                className={({ isActive }) =>
                  `flex items-center space-x-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-sky-50 text-sky-700 shadow-sm font-semibold'
                      : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                  }`
                }
              >
                <item.icon className="w-4 h-4" />
                <span>{item.label}</span>
              </NavLink>
            ))}
          </nav>
        </aside>

        {/* Dynamic Page Content */}
        <main className="flex-1 min-w-0">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
