import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/v1';

interface Customer {
  id: string;
  name: string;
  companyName?: string;
}

interface User {
  id: string;
  firstName: string;
  lastName: string;
}

const Popup: React.FC = () => {
  const [token, setToken] = useState<string | null>(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // Form state
  const [title, setTitle] = useState('');
  const [customerId, setCustomerId] = useState('');
  const [assignedTo, setAssignedTo] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [priority, setPriority] = useState('HIGH');
  const [sourceText, setSourceText] = useState('');
  const [description, setDescription] = useState('');

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [statusMsg, setStatusMsg] = useState<{ type: 'error' | 'success'; text: string } | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // Load auth token
    chrome.storage.local.get(['pt_jwt_token', 'pendingPromiseText'], (res) => {
      if (res.pt_jwt_token) {
        setToken(res.pt_jwt_token);
      }
      if (res.pendingPromiseText) {
        setSourceText(res.pendingPromiseText);
        // Pre-suggest title from text snippet
        const snippet = res.pendingPromiseText.split('\n')[0].substring(0, 60);
        setTitle(snippet);
      }

      // Default due date to Friday of current week or 3 days ahead
      const d = new Date();
      d.setDate(d.getDate() + 3);
      setDueDate(d.toISOString().split('T')[0]);
    });
  }, []);

  useEffect(() => {
    if (token) {
      fetchDropdowns(token);
    }
  }, [token]);

  const fetchDropdowns = async (authToken: string) => {
    try {
      const headers = { Authorization: `Bearer ${authToken}` };
      const [custRes, userRes] = await Promise.all([
        axios.get(`${API_BASE}/customers?size=100`, { headers }),
        axios.get(`${API_BASE}/users`, { headers }),
      ]);
      setCustomers(custRes.data.data.content || []);
      setUsers(userRes.data.data || []);
      if (custRes.data.data.content?.length > 0) {
        setCustomerId(custRes.data.data.content[0].id);
      }
    } catch (err: any) {
      if (err.response?.status === 401) {
        setToken(null);
        chrome.storage.local.remove('pt_jwt_token');
      }
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatusMsg(null);
    setIsSubmitting(true);
    try {
      const res = await axios.post(`${API_BASE}/auth/login`, { email, password });
      const jwt = res.data.data.accessToken;
      setToken(jwt);
      chrome.storage.local.set({ pt_jwt_token: jwt });
    } catch (err: any) {
      setStatusMsg({ type: 'error', text: err.response?.data?.message || 'Login failed' });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCreatePromise = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !customerId || !title || !dueDate) {
      setStatusMsg({ type: 'error', text: 'Please complete required fields' });
      return;
    }
    setStatusMsg(null);
    setIsSubmitting(true);
    try {
      await axios.post(
        `${API_BASE}/promises`,
        {
          title,
          customerId,
          assignedTo: assignedTo || undefined,
          dueDate,
          priority,
          sourceText: sourceText || undefined,
          description: description || undefined,
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      setStatusMsg({ type: 'success', text: 'Promise created successfully!' });
      chrome.storage.local.remove('pendingPromiseText');
      setTimeout(() => window.close(), 1200);
    } catch (err: any) {
      setStatusMsg({ type: 'error', text: err.response?.data?.message || 'Failed to create promise' });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!token) {
    return (
      <div style={{ padding: '20px' }}>
        <h2 style={{ fontSize: '16px', fontWeight: 'bold', marginBottom: '4px' }}>PromiseTracker Login</h2>
        <p style={{ fontSize: '12px', color: '#6b7280', marginBottom: '16px' }}>Sign in to connect Chrome extension</p>

        {statusMsg && (
          <div style={{ padding: '8px', background: '#fef2f2', border: '1px solid #fecaca', color: '#b91c1c', fontSize: '12px', borderRadius: '6px', marginBottom: '12px' }}>
            {statusMsg.text}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div>
            <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '13px', boxSizing: 'border-box' }}
            />
          </div>

          <div>
            <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '13px', boxSizing: 'border-box' }}
            />
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            style={{ padding: '10px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '6px', fontWeight: 'bold', fontSize: '13px', cursor: 'pointer', marginTop: '8px' }}
          >
            {isSubmitting ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
      </div>
    );
  }

  return (
    <div style={{ padding: '16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px', paddingBottom: '8px', borderBottom: '1px solid #e5e7eb' }}>
        <span style={{ fontWeight: 'bold', color: '#0284c7', fontSize: '15px' }}>Create Promise</span>
        <button
          onClick={() => {
            chrome.storage.local.remove('pt_jwt_token');
            setToken(null);
          }}
          style={{ fontSize: '11px', color: '#6b7280', background: 'none', border: 'none', cursor: 'pointer' }}
        >
          Sign Out
        </button>
      </div>

      {statusMsg && (
        <div style={{ padding: '8px', background: statusMsg.type === 'error' ? '#fef2f2' : '#ecfdf5', color: statusMsg.type === 'error' ? '#b91c1c' : '#047857', fontSize: '12px', borderRadius: '6px', marginBottom: '12px' }}>
          {statusMsg.text}
        </div>
      )}

      <form onSubmit={handleCreatePromise} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <div>
          <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Customer *</label>
          <select
            required
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '12px', boxSizing: 'border-box' }}
          >
            {customers.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name} {c.companyName ? `(${c.companyName})` : ''}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Promise Title *</label>
          <input
            type="text"
            required
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Send quotation"
            style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '12px', boxSizing: 'border-box' }}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
          <div>
            <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Due Date *</label>
            <input
              type="date"
              required
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              style={{ width: '100%', padding: '6px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '12px', boxSizing: 'border-box' }}
            />
          </div>

          <div>
            <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Priority</label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              style={{ width: '100%', padding: '6px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '12px', boxSizing: 'border-box' }}
            >
              <option value="LOW">LOW</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="HIGH">HIGH</option>
              <option value="URGENT">URGENT</option>
            </select>
          </div>
        </div>

        <div>
          <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Assigned To</label>
          <select
            value={assignedTo}
            onChange={(e) => setAssignedTo(e.target.value)}
            style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '12px', boxSizing: 'border-box' }}
          >
            <option value="">Assign to Me</option>
            {users.map((u) => (
              <option key={u.id} value={u.id}>
                {u.firstName} {u.lastName}
              </option>
            ))}
          </select>
        </div>

        {sourceText && (
          <div>
            <label style={{ fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase', color: '#374151' }}>Captured Text Snippet</label>
            <textarea
              rows={2}
              value={sourceText}
              onChange={(e) => setSourceText(e.target.value)}
              style={{ width: '100%', padding: '6px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '11px', fontFamily: 'monospace', boxSizing: 'border-box' }}
            />
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          style={{ padding: '10px', background: '#0284c7', color: '#fff', border: 'none', borderRadius: '6px', fontWeight: 'bold', fontSize: '13px', cursor: 'pointer', marginTop: '6px' }}
        >
          {isSubmitting ? 'Creating Promise...' : 'Create Promise'}
        </button>
      </form>
    </div>
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Popup />
  </React.StrictMode>
);
