import { request } from '../api/client'
import { Card, LoadingBlock, Message, SectionTitle } from '../components/ui'
import { MetricGrid } from '../components/MetricGrid'
import { useAsyncData } from '../hooks/useAsyncData'

export function OwnerHubPage() {
  const dataState = useAsyncData(async () => {
    const [gyms, trainers] = await Promise.all([
      request('get', '/api/gyms'),
      request('get', '/api/trainers'),
    ])

    return {
      gyms,
      trainers,
    }
  }, [])

  const items = dataState.data
    ? [
        { label: 'Gyms visible', value: dataState.data.gyms.length, hint: 'Network locations' },
        { label: 'Trainer profiles', value: dataState.data.trainers.length, hint: 'Available on the platform' },
        { label: 'Equipment control', value: 'Ready', hint: 'Manage through the equipment page' },
        { label: 'Payments', value: 'Tracked', hint: 'Membership purchases create payment records' },
      ]
    : []

  return (
    <div className="space-y-6">
      <SectionTitle title="Owner Hub" subtitle="Use this hub to understand owner capabilities before diving into equipment, memberships, and payments." />
      {dataState.loading ? <LoadingBlock label="Loading owner hub..." /> : null}
      {dataState.error ? <Message kind="error">{dataState.error}</Message> : null}
      {items.length ? <MetricGrid items={items} /> : null}
      <Card>
        <h3 className="text-xl font-semibold text-[var(--ink)]">Operational checklist</h3>
        <ul className="mt-4 space-y-3 text-sm text-[var(--ink-soft)]">
          <li>Create and maintain gym locations from the gyms page.</li>
          <li>Add or remove equipment from the equipment page.</li>
          <li>Review memberships and payments by selecting a gym on their dedicated pages.</li>
        </ul>
      </Card>
    </div>
  )
}
