import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import RequestsTable from '../components/RequestsTable';

export default function DashboardPage({ user }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    let active = true;
    api('/requests').then(data => { if (active) setRequests(data); })
      .catch(err => { if (active) setError(err.message); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  const pending = requests.filter(r => r.status.startsWith('PENDING')).length;
  const approved = requests.filter(r => r.status === 'APPROVED').length;
  const quick = user.role === 'EMPLOYEE' ? ['Submit an expense', 'Start a request and follow its approval progress.', '/requests/new', 'New expense +']
    : user.role === 'ADMIN' ? ['Your company, your threshold', 'Choose when expenses need an additional Finance review.', '/rules', 'Rule settings →']
    : ['Ready for your review', 'Review the requests waiting at your approval stage.', `/requests?status=PENDING_${user.role}`, 'Review pending →'];
  return <>
    <div className="page-heading"><div><p className="eyebrow">{user.companyName.toUpperCase()} / OVERVIEW</p>
      <h1>Welcome, {user.name}</h1><p className="muted">{user.role === 'EMPLOYEE' ? 'Your expenses and their next steps, in one place.' : 'A clear view of your company’s expense requests.'}</p></div></div>
    {error ? <p className="message error" role="alert">{error}</p> : loading ? <p role="status">Loading requests…</p> : <>
      <div className="stats">{[['Total Requests', requests.length], ['Pending', pending], ['Approved', approved]].map(([label, count]) =>
        <div className="card stat" key={label}><span>{label}</span><strong>{count}</strong><small>{user.role === 'EMPLOYEE' ? 'Your requests' : user.companyName}</small></div>)}</div>
      <section className="card quick-actions"><div><h2>{quick[0]}</h2><p>{quick[1]}</p></div><Link className="button primary" to={quick[2]}>{quick[3]}</Link></section>
      <section className="card"><div className="section-heading"><h2>Recent requests</h2><Link className="small" to="/requests">View all requests →</Link></div>
        <RequestsTable requests={requests.slice(0, 5)} /></section>
    </>}
  </>;
}
