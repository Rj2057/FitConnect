import { useMemo, useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'
import { formatCurrency, formatDate } from '../utils/format'

const membershipTones = {
  ACTIVE: 'success',
  EXPIRED: 'warning',
  CANCELLED: 'danger',
}

export function MembershipsPage() {
  const { user } = useAuth()
  const gymsState = useAsyncData(() => request('get', '/api/gyms'), [])
  const plansState = useAsyncData(() => request('get', '/api/memberships/plans'), [])
  const gyms = gymsState.data || []
  const plans = plansState.data || []
  const [selectedGymId, setSelectedGymId] = useState('')
  const [message, setMessage] = useState('')
  const [purchaseForm, setPurchaseForm] = useState({ gymId: '', planName: '', durationMonths: '1' })

  const selectedGym = gyms.find((gym) => String(gym.id) === String(purchaseForm.gymId))
  const selectedPlan = plans.find((plan) => plan.planName === purchaseForm.planName)
  
  // Get discount percentage based on duration
  const getDiscountPercentage = (months) => {
    if (months >= 12) return 30  // Yearly: 30% discount
    if (months >= 9) return 25   // 9+ months: 25% discount
    if (months >= 6) return 20   // 6 months: 20% discount
    if (months >= 3) return 10   // Quarterly (3 months): 10% discount
    return 0                     // 1-2 months: No discount
  }
  
  // Calculate estimated amount with tiered discount
  const calculateEstimatedAmount = () => {
    if (!selectedGym || !selectedPlan) return 0
    const monthlyFee = Number(selectedGym.monthlyFee)
    const multiplier = Number(selectedPlan.multiplier)
    const duration = Number(purchaseForm.durationMonths || 1)
    
    // Base amount without discount
    const baseAmount = monthlyFee * multiplier * duration
    
    // Get applicable discount percentage
    const discountPercent = getDiscountPercentage(duration)
    
    // Apply discount
    return baseAmount * (1 - discountPercent / 100)
  }
  
  const estimatedAmount = calculateEstimatedAmount()
  const discountPercent = getDiscountPercentage(Number(purchaseForm.durationMonths || 1))

  const membershipState = useAsyncData(async () => {
    if (user?.role === 'GYM_OWNER' && selectedGymId) {
      return request('get', `/api/memberships/gym/${selectedGymId}`)
    }

    if (user?.role === 'GYM_USER') {
      return request('get', '/api/memberships/my')
    }

    return []
  }, [selectedGymId, user?.role])

  const columns = useMemo(() => {
    const base = [
      { key: 'planName', label: 'Plan' },
      { key: 'gymName', label: 'Gym' },
      { key: 'durationMonths', label: 'Duration', render: (row) => `${row.durationMonths} month(s)` },
      { key: 'amount', label: 'Amount', render: (row) => formatCurrency(row.amount) },
      { key: 'startDate', label: 'Start', render: (row) => formatDate(row.startDate) },
      { key: 'endDate', label: 'End', render: (row) => formatDate(row.endDate) },
      { key: 'status', label: 'Status', render: (row) => <Badge tone={membershipTones[row.status] || 'neutral'}>{row.status}</Badge> },
    ]

    if (user?.role === 'GYM_OWNER') {
      return [{ key: 'userName', label: 'Member' }, ...base]
    }

    return base
  }, [user?.role])

  async function handlePurchase(event) {
    event.preventDefault()
    setMessage('')

    try {
      const created = await request('post', '/api/memberships', {
        gymId: Number(purchaseForm.gymId),
        planName: purchaseForm.planName,
        durationMonths: Number(purchaseForm.durationMonths),
      })
      membershipState.setData((current) => [...(current || []), created])
      setPurchaseForm({ gymId: '', planName: '', durationMonths: '1' })
      setMessage('Membership purchased successfully.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to create membership')
    }
  }

  async function handleStatusUpdate(membershipId, status) {
    try {
      const updated = await request('put', `/api/memberships/${membershipId}/status`, { status })
      membershipState.setData((current) => current.map((item) => item.id === membershipId ? updated : item))
      setMessage('Membership status updated.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to update membership status')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Memberships" subtitle="Users can purchase memberships. Owners can inspect and manage member status per gym." />

      {user?.role === 'GYM_OWNER' ? (
        <Card>
          <Field label="Select owned gym">
            <Select value={selectedGymId} onChange={(event) => setSelectedGymId(event.target.value)}>
              <option value="">Choose a gym</option>
              {gyms.map((gym) => (
                <option key={gym.id} value={gym.id}>{gym.name}</option>
              ))}
            </Select>
          </Field>
        </Card>
      ) : null}

      {membershipState.loading ? <LoadingBlock label="Loading memberships..." /> : null}
      {membershipState.error ? <Message kind="error">{membershipState.error}</Message> : null}
      {message ? <Message kind={message.includes('success') || message.includes('updated') ? 'success' : 'error'}>{message}</Message> : null}

      <DataTable
        columns={[
          ...columns,
          ...(user?.role === 'GYM_OWNER'
            ? [{ key: 'actions', label: 'Actions', render: (row) => (
                <div className="flex flex-wrap gap-2">
                  <Button variant="secondary" onClick={() => handleStatusUpdate(row.id, 'ACTIVE')}>Activate</Button>
                  <Button variant="ghost" onClick={() => handleStatusUpdate(row.id, 'EXPIRED')}>Expire</Button>
                </div>
              ) }]
            : []),
        ]}
        rows={membershipState.data || []}
        emptyTitle="No memberships yet"
        emptyDescription="Users can create memberships here, and owners can review them by gym."
      />

      {user?.role === 'GYM_USER' ? (
        <Card>
          <SectionTitle title="Purchase membership" subtitle="Choose plan and duration. Get discounts for longer commitments: 10% for 3+ months, 20% for 6+ months, 25% for 9+ months, 30% for 12 months!" />
          <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handlePurchase}>
            <Field label="Gym">
              <Select value={purchaseForm.gymId} onChange={(event) => setPurchaseForm((current) => ({ ...current, gymId: event.target.value }))} required>
                <option value="">Select gym</option>
                {gyms.map((gym) => (
                  <option key={gym.id} value={gym.id}>{gym.name} · {formatCurrency(gym.monthlyFee)}/month</option>
                ))}
              </Select>
            </Field>
            <Field label="Plan">
              <Select value={purchaseForm.planName} onChange={(event) => setPurchaseForm((current) => ({ ...current, planName: event.target.value }))} required>
                <option value="">Select plan</option>
                {plans.map((plan) => (
                  <option key={plan.planName} value={plan.planName}>{plan.planName} · x{plan.multiplier}</option>
                ))}
              </Select>
            </Field>
            <Field label="Duration (months)">
              <Input type="number" min="1" value={purchaseForm.durationMonths} onChange={(event) => setPurchaseForm((current) => ({ ...current, durationMonths: event.target.value }))} required />
            </Field>
            <Field label={`Estimated amount ${discountPercent > 0 ? `(${discountPercent}% OFF)` : ''}`}>
              <Input value={formatCurrency(estimatedAmount)} readOnly />
            </Field>
            <div className="md:col-span-2">
              <Button className="w-fit" type="submit">Create membership</Button>
            </div>
          </form>
        </Card>
      ) : null}
    </div>
  )
}
