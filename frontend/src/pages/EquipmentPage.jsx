import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'

const conditionTones = {
  NEW: 'success',
  GOOD: 'accent',
  MAINTENANCE: 'warning',
  DAMAGED: 'danger',
}

export function EquipmentPage() {
  const { user } = useAuth()
  const gymsState = useAsyncData(() => request('get', '/api/gyms'), [])
  const [selectedGymId, setSelectedGymId] = useState('')
  const [form, setForm] = useState({ gymId: '', equipmentName: '', quantity: '', condition: 'GOOD' })
  const [message, setMessage] = useState('')

  const equipmentState = useAsyncData(() => {
    if (!selectedGymId) {
      return []
    }
    return request('get', `/api/equipment/gym/${selectedGymId}`)
  }, [selectedGymId])

  const rows = equipmentState.data || []

  const columns = [
    { key: 'equipmentName', label: 'Equipment' },
    { key: 'quantity', label: 'Quantity' },
    { key: 'condition', label: 'Condition', render: (row) => <Badge tone={conditionTones[row.condition] || 'neutral'}>{row.condition}</Badge> },
    ...(user?.role === 'GYM_OWNER'
      ? [{ key: 'actions', label: 'Actions', render: (row) => (
          <Button variant="ghost" onClick={() => handleDelete(row.id)}>Delete</Button>
        ) }]
        : []),
      ]

  async function handleCreate(event) {
    event.preventDefault()
    setMessage('')

    try {
      const created = await request('post', '/api/equipment', {
        gymId: Number(form.gymId),
        equipmentName: form.equipmentName,
        quantity: Number(form.quantity),
        condition: form.condition,
      })
      if (String(created.gymId) === selectedGymId) {
        equipmentState.setData((current) => [...(current || []), created])
      }
      setForm({ gymId: '', equipmentName: '', quantity: '', condition: 'GOOD' })
      setMessage('Equipment created.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to create equipment')
    }
  }

  async function handleDelete(id) {
    try {
      await request('delete', `/api/equipment/${id}`)
      equipmentState.setData((current) => current.filter((item) => item.id !== id))
      setMessage('Equipment deleted.')
    } catch (err) {
      setMessage(err?.response?.data?.message || 'Unable to delete equipment')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Equipment" subtitle="Browse a gym's equipment inventory. Owners can add and remove equipment." />

      <Card>
        <Field label="Choose gym to inspect">
          <Select value={selectedGymId} onChange={(event) => setSelectedGymId(event.target.value)}>
            <option value="">Select gym</option>
            {(gymsState.data || []).map((gym) => (
              <option key={gym.id} value={gym.id}>{gym.name}</option>
            ))}
          </Select>
        </Field>
      </Card>

      {equipmentState.loading ? <LoadingBlock label="Loading equipment..." /> : null}
      {equipmentState.error ? <Message kind="error">{equipmentState.error}</Message> : null}
      {message ? <Message kind={message.includes('deleted') || message.includes('created') ? 'success' : 'error'}>{message}</Message> : null}

      <DataTable columns={columns} rows={rows} emptyTitle="No equipment listed" emptyDescription="Select a gym and populate its equipment to see records here." />

      {user?.role === 'GYM_OWNER' ? (
        <Card>
          <SectionTitle title="Add equipment" subtitle="Create equipment entries for a gym you own." />
          <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleCreate}>
            <Field label="Gym">
              <Select value={form.gymId} onChange={(event) => setForm((current) => ({ ...current, gymId: event.target.value }))} required>
                <option value="">Select gym</option>
                {(gymsState.data || []).map((gym) => (
                  <option key={gym.id} value={gym.id}>{gym.name}</option>
                ))}
              </Select>
            </Field>
            <Field label="Equipment name">
              <Input value={form.equipmentName} onChange={(event) => setForm((current) => ({ ...current, equipmentName: event.target.value }))} required />
            </Field>
            <Field label="Quantity">
              <Input type="number" min="1" value={form.quantity} onChange={(event) => setForm((current) => ({ ...current, quantity: event.target.value }))} required />
            </Field>
            <Field label="Condition">
              <Select value={form.condition} onChange={(event) => setForm((current) => ({ ...current, condition: event.target.value }))}>
                {['NEW', 'GOOD', 'MAINTENANCE', 'DAMAGED'].map((value) => (
                  <option key={value} value={value}>{value}</option>
                ))}
              </Select>
            </Field>
            <div className="md:col-span-2">
              <Button className="w-fit" type="submit">Add equipment</Button>
            </div>
          </form>
        </Card>
      ) : null}
    </div>
  )
}
