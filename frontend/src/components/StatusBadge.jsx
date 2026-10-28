import { statusLabels } from '../format';

export default function StatusBadge({ status }) {
  return <span className={`badge ${status.toLowerCase()}`}>{statusLabels[status]}</span>;
}
