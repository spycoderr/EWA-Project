import { Link } from 'react-router-dom';
import { dateTime, money } from '../format';
import StatusBadge from './StatusBadge';

export default function RequestsTable({ requests }) {
  if (!requests.length) return <div className="empty-state">No requests here yet.</div>;
  return <div className="table-wrap"><table>
    <thead><tr>{['ID', 'Title', 'Amount', 'Submitted by', 'Status', 'Date', 'View'].map(label => <th scope="col" key={label}>{label}</th>)}</tr></thead>
    <tbody>{requests.map(request => <tr key={request.id}>
      <td className="muted">#{request.id}</td><td className="title-cell">{request.title}</td><td>{money(request.amount)}</td>
      <td>{request.createdByName}</td><td><StatusBadge status={request.status} /></td><td className="muted">{dateTime(request.createdAt)}</td>
      <td><Link to={`/requests/${request.id}`} aria-label={`View request ${request.id}`}>View →</Link></td>
    </tr>)}</tbody>
  </table></div>;
}
