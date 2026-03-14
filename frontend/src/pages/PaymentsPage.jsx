import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Badge, Card, Field, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { useAsyncData } from '../hooks/useAsyncData'
import { useAuth } from '../hooks/useAuth'
import { formatCurrency, formatDateTime } from '../utils/format'

const tones = {
  SUCCESS: 'success',
  PENDING: 'warning',
  FAILED: 'danger',
  REFUNDED: 'accent',
}

export function PaymentsPage() {
  const { user } = useAuth()
  const gymsState = useAsyncData(() => request('get', '/api/gyms'), [])
  const [selectedGymId, setSelectedGymId] = useState('')

  const paymentsState = useAsyncData(() => {
    if (user?.role === 'GYM_OWNER' && selectedGymId) {
      return request('get', `/api/payments/gym/${selectedGymId}`)
    }

    if (user?.role === 'GYM_USER') {
      return request('get', '/api/payments/my')
    }

    return []
  }, [selectedGymId, user?.role])

  return (
    <div className="space-y-6">
      <SectionTitle title="Payments" subtitle="Track member payments for owners or personal payment history for users." />

      {user?.role === 'GYM_OWNER' ? (
        <Card>
          <Field label="Select owned gym">
            <Select value={selectedGymId} onChange={(event) => setSelectedGymId(event.target.value)}>
              <option value="">Choose a gym</option>
              {(gymsState.data || []).map((gym) => (
                <option key={gym.id} value={gym.id}>{gym.name}</option>
              ))}
            </Select>
          </Field>
        </Card>
      ) : null}

      {paymentsState.loading ? <LoadingBlock label="Loading payments..." /> : null}
      {paymentsState.error ? <Message kind="error">{paymentsState.error}</Message> : null}

      <DataTable
        columns={[
          ...(user?.role === 'GYM_OWNER' ? [{ key: 'userName', label: 'Member' }] : []),
          { key: 'gymName', label: 'Gym' },
          { key: 'amount', label: 'Amount', render: (row) => formatCurrency(row.amount) },
          { key: 'status', label: 'Status', render: (row) => <Badge tone={tones[row.status] || 'neutral'}>{row.status}</Badge> },
          { key: 'paidAt', label: 'Paid At', render: (row) => formatDateTime(row.paidAt) },
        ]}
        rows={paymentsState.data || []}
        emptyTitle="No payments yet"
        emptyDescription="Payments appear automatically when a membership is purchased."
      />
    </div>
  )
}
