import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
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

// Temporary PaymentHistory component
function PaymentHistory() {
  const { data: payments = [], loading } = useAsyncData(
    async () => {
      const response = await request('get', '/api/payments/my')
      return response || []
    },
    []
  )

  const columns = [
    { key: 'gymName', label: 'Gym' },
    { key: 'amount', label: 'Amount', render: (row) => formatCurrency(row.amount) },
    {
      key: 'status',
      label: 'Status',
      render: (row) => (
        <Badge tone={tones[row.status] || 'neutral'}>{row.status}</Badge>
      ),
    },
    { key: 'paidAt', label: 'Date', render: (row) => formatDateTime(row.paidAt) },
  ]

  if (loading) return <LoadingBlock label="Loading payments..." />
  if (payments.length === 0)
    return <p className="text-center text-gray-600">No payments yet</p>

  return <DataTable columns={columns} rows={payments} />
}
export function PaymentsPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { user, auth } = useAuth()
  
  const membershipIdParam = searchParams.get('membershipId')
  const prefilledGymId = searchParams.get('gymId')
  const prefilledAmount = searchParams.get('amount')
  const prefilledDescription = searchParams.get('description')

  const gymsData = useAsyncData(() => request('get', '/api/gyms'), [auth])
  const gymsState = gymsData.data || []

  const [selectedGymId, setSelectedGymId] = useState('')
  const [paymentForm, setPaymentForm] = useState({
    gymId: prefilledGymId ? parseInt(prefilledGymId) : '',
    amount: prefilledAmount || '',
    description: prefilledDescription || '',
  })
  const [paymentLoading, setPaymentLoading] = useState(false)
  const [paymentError, setPaymentError] = useState('')
  const [paymentSuccess, setPaymentSuccess] = useState(null)
  const [paymentId, setPaymentId] = useState(null)
  const [refunding, setRefunding] = useState(false)
  const [membershipRefunded, setMembershipRefunded] = useState(false)
  const [membershipConfirming, setMembershipConfirming] = useState(false)
  const [membershipConfirmed, setMembershipConfirmed] = useState(false)

  // Auto-confirm membership if payment successful
  useEffect(() => {
    if (paymentSuccess && paymentSuccess.status === 'SUCCESS' && membershipIdParam && !membershipConfirming && !membershipConfirmed) {
      confirmMembership()
    }
  }, [paymentSuccess, membershipIdParam, membershipConfirming, membershipConfirmed])

  const confirmMembership = async () => {
    try {
      setMembershipConfirming(true)
      await request('put', `/api/memberships/${membershipIdParam}/confirm-payment/${paymentId}`)
      setMembershipConfirmed(true)
    } catch (err) {
      setPaymentError(err?.response?.data?.message || 'Failed to confirm membership.')
    } finally {
      setMembershipConfirming(false)
    }
  }

  const paymentsState = useAsyncData(() => {
    if (user?.role === 'GYM_OWNER' && selectedGymId) {
      return request('get', `/api/payments/gym/${selectedGymId}`)
    }

    if (user?.role === 'GYM_USER') {
      return request('get', '/api/payments/my')
    }

    return []
  }, [selectedGymId, user?.role])

  const handlePaymentInputChange = (e) => {
    const { name, value } = e.target
    setPaymentForm((prev) => ({
      ...prev,
      [name]: name === 'gymId' ? (value ? parseInt(value) : '') : value,
    }))
    setPaymentError('')
  }

  const handleProcessPayment = async (e) => {
    e.preventDefault()
    setPaymentError('')
    setPaymentSuccess(null)

    if (!paymentForm.gymId || !paymentForm.amount) {
      setPaymentError('Please fill in all required fields')
      return
    }

    try {
      setPaymentLoading(true)
      const response = await request('post', '/api/payments/process', {
        gymId: paymentForm.gymId,
        amount: parseFloat(paymentForm.amount),
        description: paymentForm.description,
      })
      setPaymentSuccess(response)
      setPaymentId(response.id)
      setPaymentForm({ gymId: '', amount: '', description: '' })
    } catch (err) {
      setPaymentError(err?.response?.data?.message || 'Payment processing failed. Please try again.')
    } finally {
      setPaymentLoading(false)
    }
  }

  const handleRefund = async (id) => {
    const confirmMessage = membershipIdParam 
      ? 'Are you sure you want to refund this payment? Your membership will be cancelled.'
      : 'Are you sure you want to refund this payment?'
    
    if (!window.confirm(confirmMessage)) return

    try {
      setRefunding(true)
      const response = await request('put', `/api/payments/${id}/refund`)
      setPaymentSuccess(response)
      if (membershipIdParam) {
        setMembershipRefunded(true)
      }
    } catch (err) {
      setPaymentError(err?.response?.data?.message || 'Refund failed. Please try again.')
    } finally {
      setRefunding(false)
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle 
        title="Payments" 
        subtitle="Manage your payments and complete membership transactions." 
      />

      {/* Payment Form */}
      {!paymentSuccess && (
        <Card>
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900">New Payment</h3>
            
            <form onSubmit={handleProcessPayment} className="space-y-4">
              <Field label="Select Gym">
                <Select 
                  name="gymId"
                  value={paymentForm.gymId}
                  onChange={handlePaymentInputChange}
                  required
                >
                  <option value="">-- Choose a gym --</option>
                  {(gymsState || []).map((gym) => (
                    <option key={gym.id} value={gym.id}>
                      {gym.name} (₹{formatCurrency(gym.monthlyFee)}/month)
                    </option>
                  ))}
                </Select>
              </Field>

              <Field label="Amount (₹)">
                <input 
                  type="number"
                  name="amount"
                  value={paymentForm.amount}
                  onChange={handlePaymentInputChange}
                  placeholder="Enter amount"
                  min="0.01"
                  step="0.01"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </Field>

              <Field label="Description (Optional)">
                <input 
                  type="text"
                  name="description"
                  value={paymentForm.description}
                  onChange={handlePaymentInputChange}
                  placeholder="e.g., Monthly membership - PRO"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </Field>

              {paymentError && <Message kind="error">{paymentError}</Message>}

              <button
                type="submit"
                disabled={paymentLoading}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-semibold py-2 px-4 rounded-md transition"
              >
                {paymentLoading ? 'Processing...' : 'Process Payment'}
              </button>
            </form>
          </div>
        </Card>
      )}

      {/* Payment Status Display */}
      {paymentSuccess && (
        <Card>
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-gray-900">Payment Status</h3>

            <div className="flex items-center justify-between">
              <span className="text-gray-600">Status:</span>
              <span
                className={`px-4 py-2 rounded-full font-bold text-white ${
                  paymentSuccess.status === 'SUCCESS'
                    ? 'bg-green-600'
                    : paymentSuccess.status === 'FAILED'
                    ? 'bg-red-600'
                    : 'bg-gray-600'
                }`}
              >
                {paymentSuccess.status}
              </span>
            </div>

            <div className="border-t pt-4 space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Transaction ID:</span>
                <span className="font-semibold text-gray-900">{paymentSuccess.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Gym:</span>
                <span className="font-semibold text-gray-900">{paymentSuccess.gymName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Amount:</span>
                <span className="font-bold text-lg text-green-600">₹{formatCurrency(paymentSuccess.amount)}</span>
              </div>
            </div>

            {membershipIdParam && membershipConfirming && (
              <Message kind="success">Activating your membership...</Message>
            )}

            {membershipConfirmed && membershipIdParam && (
              <Message kind="success">✓ Payment successful! Your membership is now active.</Message>
            )}

            {membershipRefunded && paymentSuccess.status === 'REFUNDED' && (
              <Message kind="success">✓ Refund processed! Your membership has been cancelled. The refund will reflect in 3-5 business days.</Message>
            )}

            {paymentSuccess.status === 'FAILED' && (
              <Message kind="error">Payment failed. Please try again.</Message>
            )}

            {paymentError && <Message kind="error">{paymentError}</Message>}

            <div className="flex gap-3 pt-4">
              {paymentSuccess.status === 'SUCCESS' && !membershipRefunded && (
                <button
                  onClick={() => handleRefund(paymentSuccess.id)}
                  disabled={refunding}
                  className="flex-1 bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded-lg transition"
                >
                  {refunding ? 'Processing...' : 'Request Refund'}
                </button>
              )}
              <button
                onClick={() => {
                  if (membershipIdParam && membershipConfirmed) {
                    navigate('/memberships')
                  } else {
                    setPaymentSuccess(null)
                    setMembershipConfirmed(false)
                  }
                }}
                className="flex-1 bg-gray-400 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                {membershipIdParam && membershipConfirmed ? 'Go to Memberships' : 'New Payment'}
              </button>
            </div>

            {paymentSuccess.status === 'REFUNDED' && !membershipRefunded && (
              <Message kind="success">Refund processed! 3-5 business days to reflect.</Message>
            )}
          </div>
        </Card>
      )}

      {/* Payment History for GYM_OWNER */}
      {user?.role === 'GYM_OWNER' ? (
        <Card>
          <Field label="Select owned gym">
            <Select value={selectedGymId} onChange={(event) => setSelectedGymId(event.target.value)}>
              <option value="">Choose a gym</option>
              {(gymsState || []).map((gym) => (
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
          { 
            key: 'status', 
            label: 'Status', 
            render: (row) => <Badge tone={tones[row.status] || 'neutral'}>{row.status}</Badge> 
          },
          { key: 'paidAt', label: 'Paid At', render: (row) => formatDateTime(row.paidAt) },
          ...(user?.role === 'GYM_USER' ? [
            {
              key: 'actions',
              label: 'Actions',
              render: (row) => row.status === 'SUCCESS' ? (
                <button
                  onClick={() => handleRefund(row.id)}
                  disabled={refunding}
                  className="text-red-600 hover:text-red-800 font-semibold text-sm disabled:text-gray-400"
                >
                  {refunding ? 'Processing...' : 'Refund'}
                </button>
              ) : null
            }
          ] : []),
        ]}
        rows={paymentsState.data || []}
        emptyTitle="No payments yet"
        emptyDescription={user?.role === 'GYM_USER' ? 'Start by making a payment above.' : 'No payment history for this gym.'}
      />
    </div>
  )
}
