import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import { statusLabels } from '../format';
import RequestsTable from '../components/RequestsTable';

export default function RequestsPage({ user }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [params, setParams] = useSearchParams();
  const filter = params.get('status') || '';
  useEffect(() => {
    let active = true;
    api('/requests').then(data => { if (active) setRequests(data); })
      .catch(err => { if (active) setError(err.message); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);
  return <>
    <div className="page-heading"><div><p className="eyebrow">{user.companyName.toUpperCase()} / REQUESTS</p><h1>{user.role === 'EMPLOYEE' ? 'My requests' : 'Company requests'}</h1><p className="muted">Follow each expense from submission to decision.</p></div>
      {user.role === 'EMPLOYEE' && <Link className="button primary" to="/requests/new">New expense +</Link>}</div>
    <section className="card"><div className="section-heading"><h2>Expense requests</h2><div className="filter"><label htmlFor="status">Status</label>
      <select id="status" value={filter} onChange={e => setParams(e.target.value ? { status: e.target.value } : {})}><option value="">All statuses</option>
        {Object.entries(statusLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></div></div>
      {loading ? <p role="status">Loading requests…</p> : error ? <p role="alert" className="message error">{error}</p> : <RequestsTable requests={requests.filter(r => !filter || r.status === filter)} />}
    </section>
  </>;
}
