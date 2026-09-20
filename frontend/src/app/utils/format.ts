export function formatUGX(amount: number | null | undefined): string {
  if (amount === null || amount === undefined) return '—';
  return new Intl.NumberFormat('en-UG', { maximumFractionDigits: 0 }).format(amount) + ' UGX';
}

export function formatDate(value: string | number | Date | null | undefined): string {
  if (!value) return '—';
  const date = new Date(value);
  return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTimeLocalInputValue(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function toEpochMillis(datetimeLocalValue: string | null | undefined): number | null {
  if (!datetimeLocalValue) return null;
  return new Date(datetimeLocalValue).getTime();
}
