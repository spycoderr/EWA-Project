import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';

export default function NewRequestPage() {
  const [title, setTitle] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  async function submit(event) {
    event.preventDefault(); setError('');
    if (!title.trim() || Number(amount) <= 0 || Number(amount) > 1000000) { setError('Enter a title and an amount between ₹0.01 and ₹10,00,000.'); return; }
    setBusy(true);
    try {
      const request = await api('/requests', { method: 'POST', body: JSON.stringify({ title, amount, description }) });
      navigate(`/requests/${request.id}`, { state: { message: 'Expense submitted successfully. Your request is waiting for Manager approval.' } });
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }
  return <>
    <div className="page-heading"><div><p className="eyebrow">EMPLOYEE / NEW REQUEST</p><h1>New expense request</h1><p className="muted">Share the details. We’ll route it for review.</p></div></div>
    <form className="card form-card" onSubmit={submit}>
      <h2>Expense details</h2><p className="muted small">Requests first go to your Manager. High-value expenses may also require Finance review.</p>
      <label htmlFor="title">Title</label><input id="title" value={title} onChange={e => setTitle(e.target.value)} maxLength={120} required placeholder="e.g. Travel reimbursement" />
      <label htmlFor="amount">Amount (₹)</label><input id="amount" type="number" inputMode="decimal" min="0.01" max="1000000" step="0.01" value={amount} onChange={e => setAmount(e.target.value)} required placeholder="0.00" />
      <p className="form-help">Up to ₹10,00,000.00, with at most two decimal places.</p>
      <label htmlFor="description">Description <span className="muted">(optional)</span></label><textarea id="description" maxLength={500} value={description} onChange={e => setDescription(e.target.value)} placeholder="What was this expense for?" />
      {error && <p className="message error" role="alert">{error}</p>}
      <div className="form-actions"><button className="primary" disabled={busy}>{busy ? 'Submitting…' : 'Submit request'}</button><Link to="/requests">Cancel</Link></div>
    </form>
  </>;
}
