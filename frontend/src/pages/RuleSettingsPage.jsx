import { useEffect, useState } from 'react';
import { api } from '../api';
import { money } from '../format';

export default function RuleSettingsPage({ user }) {
  const [rule, setRule] = useState(null);
  const [threshold, setThreshold] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  useEffect(() => {
    let active = true;
    api('/rules').then(data => { if (active) { setRule(data); setThreshold(String(data.financeThreshold)); } })
      .catch(err => { if (active) setError(err.message); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  async function save(event) {
    event.preventDefault(); setError(''); setMessage('');
    if (threshold === '' || Number(threshold) < 0 || Number(threshold) > 1000000) { setError('Enter a threshold from ₹0 to ₹10,00,000.'); return; }
    setBusy(true);
    try {
      setRule(await api('/rules', { method: 'PUT', body: JSON.stringify({ financeThreshold: threshold }) }));
      setMessage('Threshold updated. New requests will use this value; existing approval routes stay the same.');
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }
  return <>
    <div className="page-heading"><div><p className="eyebrow">ADMIN / {user.companyName.toUpperCase()}</p><h1>Rule settings</h1><p className="muted">A simple amount rule for your company’s approval flow.</p></div></div>
    {error && <p className="message error" role="alert">{error}</p>}
    {loading ? <p role="status">Loading rule…</p> : rule && <form className="card form-card" onSubmit={save}>
      <h2>Finance approval threshold</h2>
      <p className="muted small">Expenses above this amount need Finance approval after Manager approval. New requests use the updated value.</p>
      <div className="rule-summary"><span className="muted small">Current threshold</span><strong>{money(rule.financeThreshold)}</strong></div>
      <label htmlFor="threshold">New threshold (₹)</label><input id="threshold" type="number" inputMode="decimal" min="0" max="1000000" step="0.01" required value={threshold} onChange={e => setThreshold(e.target.value)} />
      <p className="form-help">₹0 to ₹10,00,000. Exactly equal to the threshold needs Manager approval only.</p>
      {message && <p className="message success" role="status">{message}</p>}
      <div className="form-actions"><button className="primary" disabled={busy}>{busy ? 'Saving…' : 'Save threshold'}</button></div>
    </form>}
  </>;
}
