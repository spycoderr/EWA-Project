import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { api } from '../api';
import { actionLabels, dateTime, money } from '../format';
import StatusBadge from '../components/StatusBadge';

export default function RequestDetailPage({ user }) {
  const { id } = useParams();
  const location = useLocation();
  const [request, setRequest] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');
  const [comment, setComment] = useState('');
  const [rejecting, setRejecting] = useState(false);
  const [busy, setBusy] = useState(false);
  const [reviewError, setReviewError] = useState('');
  const [message, setMessage] = useState(location.state?.message || '');
  useEffect(() => {
    let active = true;
    setRequest(null); setError('');
    Promise.all([api(`/requests/${id}`), api(`/requests/${id}/history`)]).then(([detail, actions]) => {
      if (active) { setRequest(detail); setHistory(actions); }
    }).catch(err => { if (active) setError(err.message); });
    return () => { active = false; };
  }, [id]);
  const canReview = request && ((user.role === 'MANAGER' && request.status === 'PENDING_MANAGER')
    || (user.role === 'FINANCE' && request.status === 'PENDING_FINANCE'));
  async function decide(event) {
    event.preventDefault(); setReviewError(''); setMessage('');
    if (rejecting && !comment.trim()) { setReviewError('Please enter a rejection reason.'); return; }
    setBusy(true);
    try {
      const updated = await api(`/requests/${id}/${rejecting ? 'reject' : 'approve'}`, {
        method: 'POST', body: JSON.stringify({ comment }),
      });
      setRequest(updated); setComment(''); setRejecting(false);
      setMessage('Decision saved successfully.');
      setHistory(await api(`/requests/${id}/history`));
    } catch (err) { setReviewError(err.message); } finally { setBusy(false); }
  }
  return <>
    <Link className="back-link" to="/requests">← Back to requests</Link>
    {error ? <p className="message error" role="alert">{error}</p> : !request ? <p role="status">Loading request…</p> : <>
      <div className="page-heading"><div><p className="eyebrow">EXPENSE REQUEST #{request.id}</p><h1>{request.title}</h1></div><StatusBadge status={request.status} /></div>
      {message && <p className="message success" role="status">{message}</p>}
      {reviewError && <p className="message error" role="alert">{reviewError}</p>}
      <div className="detail-grid"><section className="card"><h2>Request details</h2><div className="amount-large">{money(request.amount)}</div>
        <dl><dt>Submitted by</dt><dd>{request.createdByName}</dd><dt>Submitted</dt><dd>{dateTime(request.createdAt)}</dd><dt>Last updated</dt><dd>{dateTime(request.updatedAt)}</dd>
          <dt>Approval route</dt><dd>{request.requiresFinance ? 'Manager → Finance' : 'Manager only'}</dd></dl>
        <div className="description"><h3>Description</h3>{request.description || 'No description provided.'}</div>
        {canReview && <form className="review" onSubmit={decide}>
          <h3>{rejecting ? 'Confirm rejection' : 'Your review'}</h3>
          <label htmlFor="comment">{rejecting ? 'Rejection reason (required)' : 'Comment (optional)'}</label>
          <textarea id="comment" maxLength={500} value={comment} onChange={e => setComment(e.target.value)} required={rejecting} disabled={busy} />
          <div className="form-actions"><button className={rejecting ? 'danger' : 'primary'} disabled={busy}>{busy ? 'Saving…' : rejecting ? 'Confirm rejection' : 'Approve request'}</button>
            <button className="secondary" type="button" disabled={busy} onClick={() => { setRejecting(!rejecting); setReviewError(''); }}>{rejecting ? 'Cancel rejection' : 'Reject request'}</button></div>
        </form>}
      </section>
      <section className="card"><h2>Action history</h2><p className="muted small">Every step, in chronological order.</p><ol className="timeline">{history.map((action, index) => <li key={index}>
        <strong>{actionLabels[action.action]}</strong><small>{action.actorName} · {dateTime(action.actedAt)}</small>{action.comment && <p>{action.comment}</p>}
      </li>)}</ol></section></div>
    </>}
  </>;
}
