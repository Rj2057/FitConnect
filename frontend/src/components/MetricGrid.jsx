import { StatCard } from './ui'

export function MetricGrid({ items }) {
  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {items.map((item) => (
        <StatCard key={item.label} label={item.label} value={item.value} hint={item.hint} />
      ))}
    </div>
  )
}
