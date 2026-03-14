import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Button, Card, Field, Input, LoadingBlock, Message, SectionTitle } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { formatDateTime } from '../utils/format'

export function WorkoutsPage() {
  const historyState = useAsyncData(() => request('get', '/api/workouts/history'), [])
  const weeklyState = useAsyncData(() => request('get', '/api/workouts/weekly-calories'), [])
  const [form, setForm] = useState({ exerciseName: '', weight: '', reps: '', duration: '' })
  const [message, setMessage] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')

    try {
      const created = await request('post', '/api/workouts', {
        exerciseName: form.exerciseName,
        weight: Number(form.weight),
        reps: Number(form.reps),
        duration: Number(form.duration),
      })

      historyState.setData((current) => [created, ...(current || [])])
      weeklyState.setData(await request('get', '/api/workouts/weekly-calories'))
      setForm({ exerciseName: '', weight: '', reps: '', duration: '' })
      setMessage('Workout logged and streak updated.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to log workout')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Workouts" subtitle="Log exercises, monitor calorie burn, and keep your streak alive." />

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[var(--ink-soft)]">Weekly calories</p>
          {weeklyState.loading ? <p className="mt-4 text-sm text-[var(--ink-soft)]">Loading...</p> : null}
          {!weeklyState.loading ? <p className="mt-4 text-4xl font-semibold text-[var(--ink)]">{weeklyState.data?.totalCalories ?? 0}</p> : null}
          <p className="mt-2 text-sm text-[var(--ink-soft)]">Current week burn from backend analytics.</p>
        </Card>

        <Card>
          <SectionTitle title="Log workout" subtitle="Calories are calculated on the backend using exercise type, body weight, and duration." />
          <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
            <Field label="Exercise name">
              <Input value={form.exerciseName} onChange={(event) => setForm((current) => ({ ...current, exerciseName: event.target.value }))} required />
            </Field>
            <Field label="Weight (kg)">
              <Input type="number" min="0" step="0.01" value={form.weight} onChange={(event) => setForm((current) => ({ ...current, weight: event.target.value }))} required />
            </Field>
            <Field label="Reps">
              <Input type="number" min="0" value={form.reps} onChange={(event) => setForm((current) => ({ ...current, reps: event.target.value }))} required />
            </Field>
            <Field label="Duration (minutes)">
              <Input type="number" min="1" value={form.duration} onChange={(event) => setForm((current) => ({ ...current, duration: event.target.value }))} required />
            </Field>
            <div className="md:col-span-2 flex flex-col gap-3">
              {message ? <Message kind={message.includes('updated') ? 'success' : 'error'}>{message}</Message> : null}
              <Button className="w-fit" type="submit">Log workout</Button>
            </div>
          </form>
        </Card>
      </div>

      {historyState.loading ? <LoadingBlock label="Loading workout history..." /> : null}
      {historyState.error ? <Message kind="error">{historyState.error}</Message> : null}

      <DataTable
        columns={[
          { key: 'exerciseName', label: 'Exercise' },
          { key: 'weight', label: 'Weight' },
          { key: 'reps', label: 'Reps' },
          { key: 'duration', label: 'Duration' },
          { key: 'caloriesBurned', label: 'Calories' },
          { key: 'createdAt', label: 'Logged At', render: (row) => formatDateTime(row.createdAt) },
        ]}
        rows={historyState.data || []}
        emptyTitle="No workouts logged yet"
        emptyDescription="Start by logging your first workout above."
      />
    </div>
  )
}
