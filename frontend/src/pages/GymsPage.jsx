import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select, TextArea } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'
import { formatCurrency, formatDateTime } from '../utils/format'

export function GymsPage() {
  const { user } = useAuth()
  const { data, loading, error, setData } = useAsyncData(() => request('get', '/api/gyms'), [])
  const [form, setForm] = useState({ name: '', location: '', monthlyFee: '' })
  const [selectedGymId, setSelectedGymId] = useState('')
  const [reviewForm, setReviewForm] = useState({ rating: '5', comment: '' })
  const [feeUpdateForm, setFeeUpdateForm] = useState({ gymId: '', monthlyFee: '' })
  const [formMessage, setFormMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const detailsState = useAsyncData(() => {
    if (!selectedGymId) {
      return null
    }
    return request('get', `/api/gyms/${selectedGymId}/details`)
  }, [selectedGymId])

  const gyms = data || []
  const selectedGym = gyms.find((gym) => String(gym.id) === String(selectedGymId))

  async function handleCreate(event) {
    event.preventDefault()
    setSubmitting(true)
    setFormMessage('')

    try {
      const created = await request('post', '/api/gyms', {
        ...form,
        monthlyFee: Number(form.monthlyFee),
      })
      setData((current) => [...(current || []), created])
      setForm({ name: '', location: '', monthlyFee: '' })
      setFormMessage('Gym created successfully.')
    } catch (err) {
      setFormMessage(err?.response?.data?.message || 'Unable to create gym')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleReviewSubmit(event) {
    event.preventDefault()
    if (!selectedGymId) {
      return
    }

    setFormMessage('')

    try {
      await request('post', `/api/gyms/${selectedGymId}/reviews`, {
        rating: Number(reviewForm.rating),
        comment: reviewForm.comment,
      })

      const refreshed = await request('get', `/api/gyms/${selectedGymId}/details`)
      detailsState.setData(refreshed)
      setData((current) => (current || []).map((gym) => (
        gym.id === refreshed.id
          ? { ...gym, rating: refreshed.rating, reviewCount: refreshed.reviewCount }
          : gym
      )))
      setReviewForm({ rating: '5', comment: '' })
      setFormMessage('Review saved successfully.')
    } catch (err) {
      setFormMessage(err?.response?.data?.message || 'Unable to save review')
    }
  }

  async function handleFeeUpdate(event) {
    event.preventDefault()
    setFormMessage('')

    const gym = gyms.find((item) => String(item.id) === String(feeUpdateForm.gymId))
    if (!gym) {
      setFormMessage('Select a gym to update fee.')
      return
    }

    try {
      const updated = await request('put', `/api/gyms/${gym.id}`, {
        name: gym.name,
        location: gym.location,
        monthlyFee: Number(feeUpdateForm.monthlyFee),
      })

      setData((current) => (current || []).map((item) => (item.id === updated.id ? updated : item)))
      if (String(selectedGymId) === String(updated.id)) {
        const refreshed = await request('get', `/api/gyms/${selectedGymId}/details`)
        detailsState.setData(refreshed)
      }
      setFormMessage('Gym fee updated successfully.')
      setFeeUpdateForm((current) => ({ ...current, monthlyFee: '' }))
    } catch (err) {
      setFormMessage(err?.response?.data?.message || 'Unable to update gym fee')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Gyms" subtitle="Browse gyms, check location and equipment, see fees and ratings, and read member reviews." />

      {loading ? <LoadingBlock label="Loading gyms..." /> : null}
      {error ? <Message kind="error">{error}</Message> : null}

      <DataTable
        columns={[
          { key: 'name', label: 'Gym' },
          { key: 'location', label: 'Location' },
          { key: 'monthlyFee', label: 'Monthly Fee', render: (row) => formatCurrency(row.monthlyFee) },
          { key: 'rating', label: 'Rating', render: (row) => <Badge tone="accent">{Number(row.rating || 0).toFixed(1)} / 5</Badge> },
          { key: 'reviewCount', label: 'Reviews' },
          { key: 'ownerId', label: 'Owner ID' },
          { key: 'actions', label: 'Actions', render: (row) => <Button variant="secondary" onClick={(event) => { event.stopPropagation(); setSelectedGymId(String(row.id)) }}>View details</Button> },
        ]}
        rows={gyms}
        onRowClick={(row) => setSelectedGymId(String(row.id))}
        emptyTitle="No gyms yet"
        emptyDescription="Create a gym as an owner or wait for backend records to appear."
      />

      {selectedGymId ? (
        <Card>
          <SectionTitle title={selectedGym?.name || 'Gym details'} subtitle="Detailed location, fee, equipment inventory, and user reviews." />
          {detailsState.loading ? <LoadingBlock label="Loading gym details..." /> : null}
          {detailsState.error ? <Message kind="error">{detailsState.error}</Message> : null}

          {detailsState.data ? (
            <div className="mt-5 space-y-5">
              <div className="grid gap-3 md:grid-cols-3">
                <Card className="p-4">
                  <p className="text-xs uppercase tracking-[0.16em] text-[var(--ink-soft)]">Location</p>
                  <p className="mt-2 text-base font-semibold text-[var(--ink)]">{detailsState.data.location}</p>
                </Card>
                <Card className="p-4">
                  <p className="text-xs uppercase tracking-[0.16em] text-[var(--ink-soft)]">Monthly fee</p>
                  <p className="mt-2 text-base font-semibold text-[var(--ink)]">{formatCurrency(detailsState.data.monthlyFee)}</p>
                </Card>
                <Card className="p-4">
                  <p className="text-xs uppercase tracking-[0.16em] text-[var(--ink-soft)]">Rating</p>
                  <p className="mt-2 text-base font-semibold text-[var(--ink)]">{Number(detailsState.data.rating || 0).toFixed(1)} / 5 ({detailsState.data.reviewCount} reviews)</p>
                </Card>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-[var(--ink)]">Equipment</h3>
                <div className="mt-2 text-sm text-[var(--ink-soft)]">
                  {(detailsState.data.equipment || []).length
                    ? detailsState.data.equipment.map((item) => `${item.equipmentName} (${item.quantity})`).join(', ')
                    : 'No equipment listed yet.'}
                </div>
              </div>

              <div>
                <h3 className="text-lg font-semibold text-[var(--ink)]">Reviews</h3>
                <div className="mt-3 space-y-3">
                  {(detailsState.data.reviews || []).length ? detailsState.data.reviews.map((review) => (
                    <Card key={review.id} className="p-4">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-sm font-semibold text-[var(--ink)]">{review.userName}</p>
                        <Badge tone="accent">{review.rating} / 5</Badge>
                      </div>
                      <p className="mt-2 text-sm text-[var(--ink-soft)]">{review.comment}</p>
                      <p className="mt-2 text-xs text-[var(--ink-soft)]">{formatDateTime(review.createdAt)}</p>
                    </Card>
                  )) : <p className="text-sm text-[var(--ink-soft)]">No reviews yet.</p>}
                </div>
              </div>
            </div>
          ) : null}
        </Card>
      ) : null}

      {user?.role === 'GYM_USER' && selectedGymId ? (
        <Card>
          <SectionTitle title="Add your review" subtitle="Rate this gym out of 5 and share feedback." />
          <form className="mt-4 grid gap-4" onSubmit={handleReviewSubmit}>
            <Field label="Rating (1 to 5)">
              <Input type="number" min="1" max="5" value={reviewForm.rating} onChange={(event) => setReviewForm((current) => ({ ...current, rating: event.target.value }))} required />
            </Field>
            <Field label="Review">
              <TextArea value={reviewForm.comment} onChange={(event) => setReviewForm((current) => ({ ...current, comment: event.target.value }))} required />
            </Field>
            <Button className="w-fit" type="submit">Submit review</Button>
          </form>
        </Card>
      ) : null}

      {user?.role === 'GYM_OWNER' ? (
        <>
          <Card>
            <SectionTitle title="Create a gym" subtitle="Add gym name, location, and monthly fee." />
            <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleCreate}>
              <Field label="Gym name">
                <Input value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
              </Field>
              <Field label="Location">
                <Input value={form.location} onChange={(event) => setForm((current) => ({ ...current, location: event.target.value }))} required />
              </Field>
              <Field label="Monthly fee">
                <Input type="number" min="1" step="0.01" value={form.monthlyFee} onChange={(event) => setForm((current) => ({ ...current, monthlyFee: event.target.value }))} required />
              </Field>
              <div className="md:col-span-2 flex flex-col gap-3">
                {formMessage ? <Message kind={formMessage.includes('successfully') ? 'success' : 'error'}>{formMessage}</Message> : null}
                <Button disabled={submitting} type="submit" className="w-fit">
                  {submitting ? 'Creating...' : 'Create gym'}
                </Button>
              </div>
            </form>
          </Card>

          <Card>
            <SectionTitle title="Update monthly fee" subtitle="Gym owners can modify their gym fees anytime." />
            <form className="mt-4 grid gap-4 md:grid-cols-2" onSubmit={handleFeeUpdate}>
              <Field label="Gym">
                <Select value={feeUpdateForm.gymId} onChange={(event) => setFeeUpdateForm((current) => ({ ...current, gymId: event.target.value }))} required>
                  <option value="">Select your gym</option>
                  {gyms.filter((gym) => gym.ownerId === user.id).map((gym) => (
                    <option key={gym.id} value={gym.id}>{gym.name}</option>
                  ))}
                </Select>
              </Field>
              <Field label="New monthly fee">
                <Input type="number" min="1" step="0.01" value={feeUpdateForm.monthlyFee} onChange={(event) => setFeeUpdateForm((current) => ({ ...current, monthlyFee: event.target.value }))} required />
              </Field>
              <div className="md:col-span-2">
                <Button className="w-fit" type="submit">Update fee</Button>
              </div>
            </form>
          </Card>
        </>
      ) : null}

      {formMessage ? <Message kind={formMessage.includes('successfully') ? 'success' : 'error'}>{formMessage}</Message> : null}
    </div>
  )
}
