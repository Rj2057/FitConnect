import { useMemo, useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'

const tones = {
  PENDING: 'warning',
  CONFIRMED: 'accent',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}

function toSlot(hour) {
  const start = String(hour).padStart(2, '0')
  const end = String(hour + 1).padStart(2, '0')
  return `${start}:00-${end}:00`
}

function getAvailableSlots(dateValue) {
  if (!dateValue) {
    return []
  }

  const selected = new Date(`${dateValue}T00:00:00`)
  const isSunday = selected.getDay() === 0
  const openHour = isSunday ? 8 : 6
  const closeHour = isSunday ? 21 : 22
  const slots = []

  for (let hour = openHour; hour < closeHour; hour += 1) {
    slots.push(toSlot(hour))
  }

  return slots
}

function hasSessionEnded(booking) {
  if (!booking?.date || !booking?.timeSlot) {
    return false
  }

  const parts = booking.timeSlot.split('-')
  if (parts.length !== 2) {
    return false
  }

  const sessionEnd = new Date(`${booking.date}T${parts[1]}:00`)
  return !Number.isNaN(sessionEnd.getTime()) && sessionEnd.getTime() <= Date.now()
}

export function BookingsPage() {
  const { user } = useAuth()
  const trainersState = useAsyncData(() => request('get', '/api/trainers'), [])
  const [form, setForm] = useState({ trainerId: '', date: '', timeSlot: '' })
  const [ratingForm, setRatingForm] = useState({ bookingId: '', rating: '5', review: '' })
  const [trainerReply, setTrainerReply] = useState({ bookingId: '', message: '', proposedTimeSlot: '' })
  const [message, setMessage] = useState('')
  const availableSlots = useMemo(() => getAvailableSlots(form.date), [form.date])

  const bookingsState = useAsyncData(() => {
    if (user?.role === 'GYM_TRAINER') {
      return request('get', '/api/bookings/trainer')
    }

    if (user?.role === 'GYM_USER') {
      return request('get', '/api/bookings/user')
    }

    return []
  }, [user?.role])

  const rateableBookings = useMemo(() => (bookingsState.data || []).filter((booking) => hasSessionEnded(booking)), [bookingsState.data])

  const columns = useMemo(() => {
    const base = [
      { key: 'date', label: 'Date' },
      { key: 'timeSlot', label: 'Time Slot' },
      { key: 'status', label: 'Status', render: (row) => <Badge tone={tones[row.status] || 'neutral'}>{row.status}</Badge> },
    ]

    if (user?.role === 'GYM_USER') {
      return [
        { key: 'trainerName', label: 'Trainer' },
        ...base,
        {
          key: 'trainerResponseMessage',
          label: 'Trainer Response',
          render: (row) => row.trainerResponseMessage || '-',
        },
        {
          key: 'trainerProposedTimeSlot',
          label: 'Alternate Time',
          render: (row) => row.trainerProposedTimeSlot || '-',
        },
        {
          key: 'userRating',
          label: 'Your Rating',
          render: (row) => (row.userRating ? `${row.userRating} / 5` : 'Not rated'),
        },
      ]
    }

    return [
      { key: 'userName', label: 'Client' },
      { key: 'trainerName', label: 'Trainer' },
      ...base,
      {
        key: 'trainerResponseMessage',
        label: 'Message',
        render: (row) => row.trainerResponseMessage || '-',
      },
      {
        key: 'trainerProposedTimeSlot',
        label: 'Alternate Time',
        render: (row) => row.trainerProposedTimeSlot || '-',
      },
    ]
  }, [user?.role])

  async function handleSubmit(event) {
    event.preventDefault()
    setMessage('')

    try {
      const created = await request('post', '/api/bookings', {
        trainerId: Number(form.trainerId),
        date: form.date,
        timeSlot: form.timeSlot,
      })
      bookingsState.setData((current) => [...(current || []), created])
      setForm({ trainerId: '', date: '', timeSlot: '' })
      setMessage('Booking created successfully.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to create booking')
    }
  }

  async function handleRatingSubmit(event) {
    event.preventDefault()
    setMessage('')

    try {
      const updated = await request('post', `/api/bookings/${ratingForm.bookingId}/rating`, {
        rating: Number(ratingForm.rating),
        review: ratingForm.review,
      })

      bookingsState.setData((current) => (current || []).map((item) => item.id === updated.id ? updated : item))
      setRatingForm({ bookingId: '', rating: '5', review: '' })
      setMessage('Trainer rating submitted successfully.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to submit trainer rating')
    }
  }

  async function handleTrainerAction(event) {
    event.preventDefault()
    setMessage('')

    try {
      const updated = await request('put', `/api/bookings/${trainerReply.bookingId}/status`, {
        action: event.nativeEvent.submitter?.value || 'ACCEPT',
        message: trainerReply.message,
        proposedTimeSlot: trainerReply.proposedTimeSlot,
      })
      bookingsState.setData((current) => (current || []).map((item) => item.id === updated.id ? updated : item))
      setTrainerReply({ bookingId: '', message: '', proposedTimeSlot: '' })
      setMessage('Booking decision updated successfully.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to update booking decision')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title={user?.role === 'GYM_TRAINER' ? 'Client sessions' : 'Bookings'} subtitle="Users can book trainers; trainers can review their incoming sessions." />

      {bookingsState.loading ? <LoadingBlock label="Loading bookings..." /> : null}
      {bookingsState.error ? <Message kind="error">{bookingsState.error}</Message> : null}
      {message ? <Message kind={message.includes('success') ? 'success' : 'error'}>{message}</Message> : null}

      <DataTable
        columns={columns}
        rows={bookingsState.data || []}
        emptyTitle="No bookings yet"
        emptyDescription="Book a trainer to see sessions appear here. Slots are 1 hour each."
      />

      {user?.role === 'GYM_USER' ? (
        <>
          <Card>
            <SectionTitle title="Book a trainer" subtitle="Create a pending session using valid one-hour slots. Monday-Saturday: 6 AM to 10 PM. Sunday: 8 AM to 9 PM." />
            <form className="mt-6 grid gap-4 md:grid-cols-3" onSubmit={handleSubmit}>
              <Field label="Trainer">
                <Select value={form.trainerId} onChange={(event) => setForm((current) => ({ ...current, trainerId: event.target.value }))} required>
                  <option value="">Select trainer</option>
                  {(trainersState.data || []).map((trainer) => (
                    <option key={trainer.id} value={trainer.id}>{trainer.trainerName} · {trainer.specialization}</option>
                  ))}
                </Select>
              </Field>
              <Field label="Date">
                <Input type="date" min={new Date().toISOString().split('T')[0]} value={form.date} onChange={(event) => setForm((current) => ({ ...current, date: event.target.value, timeSlot: '' }))} required />
              </Field>
              <Field label="Time slot">
                <Select value={form.timeSlot} onChange={(event) => setForm((current) => ({ ...current, timeSlot: event.target.value }))} required>
                  <option value="">Select one-hour slot</option>
                  {availableSlots.map((slot) => (
                    <option key={slot} value={slot}>{slot}</option>
                  ))}
                </Select>
              </Field>
              <div className="md:col-span-3">
                <Button className="w-fit" type="submit">Create booking</Button>
              </div>
            </form>
          </Card>

          <Card>
            <SectionTitle title="Rate your trainer" subtitle="Only your bookings with completed session time are available for rating." />
            <form className="mt-6 grid gap-4 md:grid-cols-3" onSubmit={handleRatingSubmit}>
              <Field label="Booking">
                <Select value={ratingForm.bookingId} onChange={(event) => setRatingForm((current) => ({ ...current, bookingId: event.target.value }))} required>
                  <option value="">Select booking</option>
                  {rateableBookings.map((booking) => (
                    <option key={booking.id} value={booking.id}>{booking.trainerName} · {booking.date} · {booking.timeSlot}</option>
                  ))}
                </Select>
              </Field>
              <Field label="Rating (1-5)">
                <Input type="number" min="1" max="5" value={ratingForm.rating} onChange={(event) => setRatingForm((current) => ({ ...current, rating: event.target.value }))} required />
              </Field>
              <Field label="Review">
                <Input value={ratingForm.review} onChange={(event) => setRatingForm((current) => ({ ...current, review: event.target.value }))} required />
              </Field>
              <div className="md:col-span-3">
                <Button className="w-fit" type="submit">Submit trainer rating</Button>
              </div>
            </form>
          </Card>
        </>
      ) : null}

      {user?.role === 'GYM_TRAINER' ? (
        <Card>
          <SectionTitle title="Respond to booking request" subtitle="Accept or reject pending requests with a message. You can suggest a different time slot." />
          <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleTrainerAction}>
            <Field label="Pending booking">
              <Select value={trainerReply.bookingId} onChange={(event) => setTrainerReply((current) => ({ ...current, bookingId: event.target.value }))} required>
                <option value="">Select booking</option>
                {(bookingsState.data || []).filter((booking) => booking.status === 'PENDING').map((booking) => (
                  <option key={booking.id} value={booking.id}>{booking.userName} · {booking.date} · {booking.timeSlot}</option>
                ))}
              </Select>
            </Field>
            <Field label="Suggested alternate time (optional)">
              <Input value={trainerReply.proposedTimeSlot} onChange={(event) => setTrainerReply((current) => ({ ...current, proposedTimeSlot: event.target.value }))} placeholder="e.g. 18:00-19:00" />
            </Field>
            <div className="md:col-span-2">
              <Field label="Message to user">
              <Input value={trainerReply.message} onChange={(event) => setTrainerReply((current) => ({ ...current, message: event.target.value }))} placeholder="I am available at a different time..." />
              </Field>
            </div>
            <div className="md:col-span-2 flex gap-3">
              <Button className="w-fit" type="submit" value="ACCEPT">Accept booking</Button>
              <Button className="w-fit" type="submit" value="REJECT" variant="secondary">Reject booking</Button>
            </div>
          </form>
        </Card>
      ) : null}
    </div>
  )
}
