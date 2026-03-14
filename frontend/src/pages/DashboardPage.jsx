import { useAuth } from '../hooks/useAuth'
import { Card, LoadingBlock, SectionTitle } from '../components/ui'
import { MetricGrid } from '../components/MetricGrid'
import { useAsyncData } from '../hooks/useAsyncData'
import { request } from '../api/client'
import { formatCurrency, roleLabel } from '../utils/format'

function overviewLoader(role) {
  return async () => {
    if (role === 'GYM_USER') {
      const [memberships, bookings, workouts, payments] = await Promise.all([
        request('get', '/api/memberships/my'),
        request('get', '/api/bookings/user'),
        request('get', '/api/workouts/history'),
        request('get', '/api/payments/my'),
      ])

      return {
        cards: [
          { label: 'Memberships', value: memberships.length, hint: 'Active and historical plans' },
          { label: 'Bookings', value: bookings.length, hint: 'Personal training sessions booked' },
          { label: 'Workouts', value: workouts.length, hint: 'Logged exercise entries' },
          {
            label: 'Payments',
            value: formatCurrency(payments.reduce((sum, payment) => sum + Number(payment.amount), 0)),
            hint: 'Total spend recorded in FitConnect',
          },
        ],
      }
    }

    if (role === 'GYM_TRAINER') {
      const [trainers, bookings] = await Promise.all([
        request('get', '/api/trainers'),
        request('get', '/api/bookings/trainer'),
      ])

      return {
        cards: [
          { label: 'Trainer profiles', value: trainers.length, hint: 'Visible on the platform' },
          { label: 'Client sessions', value: bookings.length, hint: 'Assigned to you' },
          { label: 'Status', value: 'Live', hint: 'Profile can be edited from trainer workspace' },
          { label: 'Role', value: 'Trainer', hint: 'Coaching view active' },
        ],
      }
    }

    const [gyms, trainers] = await Promise.all([
      request('get', '/api/gyms'),
      request('get', '/api/trainers'),
    ])

    return {
      cards: [
        { label: 'Gyms', value: gyms.length, hint: 'Locations on the platform' },
        { label: 'Trainers', value: trainers.length, hint: 'Profiles currently registered' },
        { label: 'Operations', value: 'Owner', hint: 'Manage equipment, members, and payments' },
        { label: 'Readiness', value: 'Online', hint: 'Swagger and APIs are connected' },
      ],
    }
  }
}

export function DashboardPage() {
  const { user } = useAuth()
  const { data, loading, error } = useAsyncData(overviewLoader(user?.role), [user?.role])

  return (
    <div className="space-y-6">
      <SectionTitle
        title={`Welcome, ${user?.name?.split(' ')[0] || 'Athlete'}`}
        subtitle={`You are signed in as ${roleLabel(user?.role)}. This workspace adapts to your backend permissions and active modules.`}
      />

      {loading ? <LoadingBlock label="Loading dashboard metrics..." /> : null}
      {error ? <Card className="text-sm text-[#9b2525]">{error}</Card> : null}
      {data?.cards ? <MetricGrid items={data.cards} /> : null}

      <div className="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
        <Card className="bg-[linear-gradient(135deg,rgba(216,90,38,0.14),rgba(255,255,255,0.78))]">
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[var(--ink-soft)]">Platform Brief</p>
          <h3 className="mt-4 text-2xl font-semibold text-[var(--ink)]">Full-stack fitness operations, centered on actual gym workflows.</h3>
          <p className="mt-4 max-w-2xl text-sm leading-7 text-[var(--ink-soft)]">
            The frontend is connected to your Spring Boot backend for authentication, memberships, training sessions, workouts, streaks, attendance, equipment, payments, and owner oversight. Use the side navigation to move through role-relevant features.
          </p>
        </Card>

        <Card>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[var(--ink-soft)]">Backend Status</p>
          <ul className="mt-4 space-y-3 text-sm text-[var(--ink-soft)]">
            <li>JWT auth enabled and reusable from the frontend token store.</li>
            <li>Swagger UI supports bearer authentication for manual API verification.</li>
            <li>Integration tests cover auth, bookings, equipment, streaks, calories, memberships, and payments.</li>
          </ul>
        </Card>
      </div>
    </div>
  )
}
