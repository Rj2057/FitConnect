import clsx from 'clsx'

export function Button({ className, variant = 'primary', ...props }) {
  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center rounded-full px-5 py-3 text-sm font-semibold transition duration-200 disabled:cursor-not-allowed disabled:opacity-50',
        variant === 'primary' && 'bg-[var(--accent)] text-white shadow-[0_20px_45px_rgba(216,90,38,0.28)] hover:bg-[var(--accent-strong)]',
        variant === 'secondary' && 'bg-white/70 text-[var(--ink)] ring-1 ring-[var(--line)] backdrop-blur hover:bg-white',
        variant === 'ghost' && 'bg-transparent text-[var(--ink-soft)] hover:bg-white/60',
        className,
      )}
      {...props}
    />
  )
}

export function Card({ className, children }) {
  return (
    <section className={clsx('rounded-[28px] border border-[var(--line)] bg-white/75 p-5 shadow-[var(--card-shadow)] backdrop-blur', className)}>
      {children}
    </section>
  )
}

export function SectionTitle({ title, subtitle, actions }) {
  return (
    <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
      <div>
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[var(--ink-soft)]">FitConnect</p>
        <h2 className="mt-2 text-2xl font-semibold text-[var(--ink)] md:text-3xl">{title}</h2>
        {subtitle ? <p className="mt-2 max-w-2xl text-sm text-[var(--ink-soft)]">{subtitle}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap gap-3">{actions}</div> : null}
    </div>
  )
}

export function StatCard({ label, value, hint }) {
  return (
    <Card className="min-h-[140px]">
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-[var(--ink-soft)]">{label}</p>
      <p className="mt-6 text-4xl font-semibold text-[var(--ink)]">{value}</p>
      {hint ? <p className="mt-3 text-sm text-[var(--ink-soft)]">{hint}</p> : null}
    </Card>
  )
}

export function EmptyState({ title, description }) {
  return (
    <Card className="border-dashed text-center">
      <h3 className="text-lg font-semibold text-[var(--ink)]">{title}</h3>
      <p className="mt-2 text-sm text-[var(--ink-soft)]">{description}</p>
    </Card>
  )
}

export function Field({ label, children, hint }) {
  return (
    <label className="flex flex-col gap-2 text-sm text-[var(--ink-soft)]">
      <span className="font-medium text-[var(--ink)]">{label}</span>
      {children}
      {hint ? <span className="text-xs text-[var(--ink-soft)]">{hint}</span> : null}
    </label>
  )
}

export function Input(props) {
  return <input className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm text-[var(--ink)] outline-none transition focus:border-[var(--accent)]" {...props} />
}

export function Select(props) {
  return <select className="h-12 rounded-2xl border border-[var(--line)] bg-white px-4 text-sm text-[var(--ink)] outline-none transition focus:border-[var(--accent)]" {...props} />
}

export function TextArea(props) {
  return <textarea className="min-h-28 rounded-2xl border border-[var(--line)] bg-white px-4 py-3 text-sm text-[var(--ink)] outline-none transition focus:border-[var(--accent)]" {...props} />
}

export function Badge({ children, tone = 'neutral' }) {
  return (
    <span className={clsx(
      'inline-flex rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em]',
      tone === 'neutral' && 'bg-[rgba(26,31,43,0.08)] text-[var(--ink)]',
      tone === 'success' && 'bg-[rgba(28,145,114,0.14)] text-[#0f7158]',
      tone === 'warning' && 'bg-[rgba(212,118,33,0.16)] text-[#8f4a0e]',
      tone === 'danger' && 'bg-[rgba(206,59,59,0.14)] text-[#9b2525]',
      tone === 'accent' && 'bg-[rgba(216,90,38,0.14)] text-[var(--accent-strong)]',
    )}>
      {children}
    </span>
  )
}

export function Message({ kind = 'info', children }) {
  return (
    <div className={clsx(
      'rounded-2xl px-4 py-3 text-sm',
      kind === 'error' && 'bg-[rgba(206,59,59,0.12)] text-[#9b2525]',
      kind === 'success' && 'bg-[rgba(28,145,114,0.14)] text-[#0f7158]',
      kind === 'info' && 'bg-[rgba(32,96,204,0.12)] text-[#1b4da7]',
    )}>
      {children}
    </div>
  )
}

export function LoadingBlock({ label = 'Loading...' }) {
  return <Card className="text-sm text-[var(--ink-soft)]">{label}</Card>
}
