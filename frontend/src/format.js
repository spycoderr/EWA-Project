export const money = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
export const dateTime = value => new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
export const statusLabels = {
  PENDING_MANAGER: 'Pending Manager', PENDING_FINANCE: 'Pending Finance', APPROVED: 'Approved', REJECTED: 'Rejected',
};
export const actionLabels = {
  SUBMITTED: 'Submitted', MANAGER_APPROVED: 'Manager approved', FINANCE_APPROVED: 'Finance approved',
  MANAGER_REJECTED: 'Manager rejected', FINANCE_REJECTED: 'Finance rejected',
};
