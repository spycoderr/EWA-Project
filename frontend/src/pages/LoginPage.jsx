import { useState } from 'react';
import { api } from '../api';

export default function LoginPage({ onLogin }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    setBusy(true);
    try {
      onLogin(await api('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }));
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }

  return <div className="login-shell">
    <section className="login-intro">
      <div className="brand"><span className="brand-mark">E</span> Expense Lab</div>
      <div><p className="eyebrow">CONFIG-DRIVEN APPROVALS</p><h1>Every expense.<br />A clear next step.</h1>
        <p>Submit, review, and follow your expense requests. One simple workflow, with every decision recorded.</p>
        <ol className="flow"><li>Employee submits</li><li>Manager reviews</li><li>Finance reviews when required</li></ol>
      </div>
      <small>Java lab project · React / Spring Boot / MySQL</small>
    </section>
    <section className="login-form card">
      <p className="eyebrow">WELCOME BACK</p><h2>Sign in to your workspace</h2><p className="muted">Use a demo account to explore your role.</p>
      <form onSubmit={submit}>
        <label htmlFor="email">Email address</label>
        <input id="email" type="email" autoComplete="username" maxLength={150} value={email} onChange={e => setEmail(e.target.value)} required />
        <label htmlFor="password">Password</label>
        <input id="password" type="password" autoComplete="current-password" maxLength={100} value={password} onChange={e => setPassword(e.target.value)} required />
        {error && <p role="alert" className="message error">{error}</p>}
        <button className="primary full" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
      </form>
      <details className="demo-help"><summary>Demo accounts</summary>
        <p>Password for all accounts: <code>Demo@123</code></p>
        {['alpha', 'beta'].map(company => <div key={company}><strong>Company {company === 'alpha' ? 'Alpha' : 'Beta'}</strong>
          {['employee', 'manager', 'finance', 'admin'].map(role => <button type="button" className="account-option" key={role}
            onClick={() => { setEmail(`${company}.${role}@demo.com`); setPassword('Demo@123'); }}>
            {company}.{role}@demo.com</button>)}
        </div>)}
      </details>
      <p className="small muted">Local educational demo. Use on localhost only.</p>
    </section>
  </div>;
}
