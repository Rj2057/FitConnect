import { request } from '../api/client'
import { Card, LoadingBlock, Message, SectionTitle } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'
import { formatDate } from '../utils/format'

export function StreakPage() {
  const { user } = useAuth()
  const streakState = useAsyncData(() => request('get', `/api/streak/${user.id}`), [user.id])

  return (
    <div className="space-y-6">
      <SectionTitle title="Streak" subtitle="Your streak changes when you log workouts or check into a gym. Monthly pause tokens protect consistency gaps." />
      {streakState.loading ? <LoadingBlock label="Loading streak..." /> : null}
      {streakState.error ? <Message kind="error">{streakState.error}</Message> : null}
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
