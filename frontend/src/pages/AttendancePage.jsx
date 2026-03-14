import { useState } from 'react'
import { request } from '../api/client'
import { Button, Card, Message, SectionTitle } from '../components/ui'

export function AttendancePage() {
  const [message, setMessage] = useState('')

  async function handleCheckIn(event) {
    event.preventDefault()
    setMessage('')

    try {
      const result = await request('post', '/api/attendance/check-in', {})
      setMessage(`Checked in successfully at ${new Date(result.checkInTime).toLocaleTimeString('en-IN')}.`)
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to check in')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Attendance" subtitle="Check in using your active gym membership and keep your streak from breaking." />
      <Card>
        <form className="grid gap-4 md:max-w-xl" onSubmit={handleCheckIn}>
          <p className="text-sm text-[var(--ink-soft)]">Gym is selected automatically based on your active membership.</p>
          {message ? <Message kind={message.includes('successfully') ? 'success' : 'error'}>{message}</Message> : null}
          <Button className="w-fit" type="submit">Check in now</Button>
        </form>
      </Card>
    </div>
  )
}
