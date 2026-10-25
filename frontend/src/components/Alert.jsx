export default function Alert({ type = 'error', children }) {
  if (!children) return null;
  const styles = {
    error: 'border-red-200 bg-red-50 text-red-700',
    success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    info: 'border-sky-200 bg-sky-50 text-sky-700',
  };
  return (
    <div className={`rounded-lg border px-4 py-3 text-sm ${styles[type]}`} role="alert">
      {children}
    </div>
  );
}
