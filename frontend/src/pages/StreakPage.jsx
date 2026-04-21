import { useState } from 'react'
import { request } from '../api/client'
import { Button, Card, LoadingBlock, Message, SectionTitle } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'
import { formatDate } from '../utils/format'

export function StreakPage() {
  const { user } = useAuth()
  const streakState = useAsyncData(() => request('get', `/api/streak/${user.id}`), [user.id])
  const [actionMessage, setActionMessage] = useState('')
  const [actionError, setActionError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function refreshStreak() {
    const latest = await request('get', `/api/streak/${user.id}`)
    streakState.setData(latest)
  }

  async function handleCheckIn() {
    setActionError('')
    setActionMessage('')

    try {
      setSubmitting(true)
      const result = await request('post', '/api/attendance/check-in', {})
      await refreshStreak()
      setActionMessage(`Checked in successfully at ${new Date(result.checkInTime).toLocaleTimeString('en-IN')}.`)
    } catch (err) {
      setActionError(err?.response?.data?.message || 'Unable to check in')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleUsePause() {
    setActionError('')
    setActionMessage('')

    try {
      setSubmitting(true)
      const updated = await request('post', '/api/streak/pause', {})
      streakState.setData(updated)
      setActionMessage('Pause token used successfully. Your streak is protected for today.')
    } catch (err) {
      setActionError(err?.response?.data?.message || 'Unable to use pause token')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Streak" subtitle="Attendance and streak controls are unified here. Check in and use pause tokens from a single place." />
      {streakState.loading ? <LoadingBlock label="Loading streak..." /> : null}
      {streakState.error ? <Message kind="error">{streakState.error}</Message> : null}
      {actionError ? <Message kind="error">{actionError}</Message> : null}
      {actionMessage ? <Message kind="success">{actionMessage}</Message> : null}

      <Card>
        <div className="flex flex-wrap gap-3">
          <Button className="w-fit" onClick={handleCheckIn} disabled={submitting}>Check in now</Button>
          <Button className="w-fit" variant="secondary" onClick={handleUsePause} disabled={submitting}>Use pause token</Button>
        </div>
        <p className="mt-3 text-sm text-[var(--ink-soft)]">Check-in updates activity. Pause token protects streak when you miss attendance/workout for the day.</p>
      </Card>

      {streakState.data ? (
        <div className="grid gap-4 md:grid-cols-3">
          <Card>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[var(--ink-soft)]">Current streak</p>
            <p className="mt-5 text-5xl font-semibold text-[var(--ink)]">{streakState.data.streakCount}</p>
          </Card>
          <Card>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[var(--ink-soft)]">Pause tokens</p>
            <p className="mt-5 text-5xl font-semibold text-[var(--ink)]">{streakState.data.pauseTokensRemaining}</p>
          </Card>
          <Card>
            <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[var(--ink-soft)]">Last activity</p>
            <p className="mt-5 text-2xl font-semibold text-[var(--ink)]">{formatDate(streakState.data.lastActivityDate)}</p>
          </Card>
        </div>
      ) : null}
    </div>
  )
}
