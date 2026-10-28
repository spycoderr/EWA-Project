import { useEffect, useState } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { api } from './api';
import LoginPage from './pages/LoginPage';
import Layout from './components/Layout';
import DashboardPage from './pages/DashboardPage';
import RequestsPage from './pages/RequestsPage';
import NewRequestPage from './pages/NewRequestPage';
import RequestDetailPage from './pages/RequestDetailPage';
import RuleSettingsPage from './pages/RuleSettingsPage';

export default function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    api('/auth/me').then(setUser).catch(() => setUser(null)).finally(() => setLoading(false));
    const expired = () => setUser(null);
    window.addEventListener('session-expired', expired);
    return () => window.removeEventListener('session-expired', expired);
  }, []);
  async function logout() { await api('/auth/logout', { method: 'POST' }); setUser(null); }
  if (loading) return <p className="page-loading" role="status">Loading workspace…</p>;
  return <BrowserRouter><Routes>
    {!user ? <>
      <Route path="/login" element={<LoginPage onLogin={setUser} />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </> : <>
      <Route element={<Layout user={user} onLogout={logout} />}>
        <Route path="/" element={<DashboardPage user={user} />} />
        <Route path="/requests" element={<RequestsPage user={user} />} />
        <Route path="/requests/new" element={user.role === 'EMPLOYEE' ? <NewRequestPage /> : <Navigate to="/" replace />} />
        <Route path="/requests/:id" element={<RequestDetailPage user={user} />} />
        <Route path="/rules" element={user.role === 'ADMIN' ? <RuleSettingsPage user={user} /> : <Navigate to="/" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </>}
  </Routes></BrowserRouter>;
}
