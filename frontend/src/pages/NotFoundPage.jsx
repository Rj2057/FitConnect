import { Link } from 'react-router-dom'
import { Button, Card } from '../components/ui'

export function NotFoundPage() {
  return (
    <div className="flex min-h-[70vh] items-center justify-center">
      <Card className="max-w-lg text-center">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[var(--ink-soft)]">404</p>
        <h1 className="mt-4 text-4xl font-semibold text-[var(--ink)]">The route you requested does not exist.</h1>
        <p className="mt-4 text-sm text-[var(--ink-soft)]">Return to the dashboard and continue exploring the platform.</p>
        <Link to="/dashboard">
          <Button className="mt-6">Go to dashboard</Button>
        </Link>
      </Card>
    </div>
  )
}
