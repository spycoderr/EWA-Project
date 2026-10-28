import { NavLink, Outlet } from 'react-router-dom';
import { useState } from 'react';

export default function Layout({ user, onLogout }) {
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  async function logout() {
    setBusy(true); setError('');
    try { await onLogout(); } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark">E</span> Expense Lab</div>
      <div className="workspace"><span className="eyebrow">WORKSPACE</span><strong>{user.companyName}</strong></div>
      <nav aria-label="Main navigation">
        <NavLink to="/" end>Overview</NavLink><NavLink to="/requests">Requests</NavLink>
        {user.role === 'EMPLOYEE' && <NavLink to="/requests/new">New expense</NavLink>}
        {user.role === 'ADMIN' && <NavLink to="/rules">Rule settings</NavLink>}
      </nav>
      <div className="sidebar-footer"><p>Simple rules.<br />Clear decisions.</p><small>Local Java lab demo</small></div>
    </aside>
    <div className="main-area">
      <header className="topbar"><div><strong>{user.name}</strong><span className="role-label">{user.role}</span></div>
        <button className="secondary" disabled={busy} onClick={logout}>{busy ? 'Signing out…' : 'Sign out'}</button></header>
      <main>{error && <p className="message error" role="alert">{error}</p>}<Outlet /></main>
    </div>
  </div>;
}
