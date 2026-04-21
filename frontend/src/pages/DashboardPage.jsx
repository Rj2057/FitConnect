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
    </div>
  )
}
