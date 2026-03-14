import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'

export function TrainersPage() {
  const { user } = useAuth()
  const { data, loading, error, setData } = useAsyncData(() => request('get', '/api/trainers'), [])
  const gymsState = useAsyncData(() => request('get', '/api/gyms'), [])
  const [form, setForm] = useState({ gymId: '', experience: '', specialization: '' })
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')

    try {
      const updated = await request('put', '/api/trainers/profile', {
        gymId: Number(form.gymId),
        experience: Number(form.experience),
        specialization: form.specialization,
      })

      setData((current) => {
        const existing = current || []
        const next = existing.filter((item) => item.id !== updated.id)
        return [updated, ...next]
      })
      setMessage('Trainer profile updated.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Failed to update trainer profile')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Trainers" subtitle="Explore coaches across gyms. Trainers can also update their own profile here." />

      {loading ? <LoadingBlock label="Loading trainers..." /> : null}
      {error ? <Message kind="error">{error}</Message> : null}

      <DataTable
        columns={[
          { key: 'trainerName', label: 'Trainer' },
          { key: 'specialization', label: 'Specialization' },
          { key: 'experience', label: 'Experience', render: (row) => `${row.experience} yrs` },
          { key: 'rating', label: 'Rating', render: (row) => <Badge tone="accent">{Number(row.rating).toFixed(1)} / 5 ({row.ratingCount || 0})</Badge> },
          { key: 'gymId', label: 'Gym ID' },
        ]}
        rows={data || []}
        emptyTitle="No trainer profiles yet"
        emptyDescription="Trainers can create their profile from this page once authenticated."
      />

      {user?.role === 'GYM_TRAINER' ? (
        <Card>
          <SectionTitle title="Update trainer profile" subtitle="Connect your trainer account to a gym and publish your coaching details." />
          <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
            <Field label="Gym">
              <Select value={form.gymId} onChange={(event) => setForm((current) => ({ ...current, gymId: event.target.value }))} required>
                <option value="">Select gym</option>
                {(gymsState.data || []).map((gym) => (
                  <option key={gym.id} value={gym.id}>{gym.name}</option>
                ))}
              </Select>
            </Field>
            <Field label="Experience (years)">
              <Input type="number" min="0" value={form.experience} onChange={(event) => setForm((current) => ({ ...current, experience: event.target.value }))} required />
            </Field>
            <Field label="Specialization">
              <Input value={form.specialization} onChange={(event) => setForm((current) => ({ ...current, specialization: event.target.value }))} required />
            </Field>
            <div className="md:col-span-2 flex flex-col gap-3">
              {message ? <Message kind={message.includes('updated') ? 'success' : 'error'}>{message}</Message> : null}
              <Button className="w-fit" type="submit">Save trainer profile</Button>
            </div>
          </form>
        </Card>
      ) : null}
    </div>
  )
}
