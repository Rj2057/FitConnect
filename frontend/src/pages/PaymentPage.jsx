import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useAsyncData } from '../hooks/useAsyncData';
import { client } from '../api/client';
import DataTable from '../components/DataTable';

export default function PaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { auth } = useAuth();
  
  // URL params for pre-filled payment
  const prefilledGymId = searchParams.get('gymId');
  const prefilledAmount = searchParams.get('amount');
  const prefilledDescription = searchParams.get('description');

  // State
  const [form, setForm] = useState({
    gymId: prefilledGymId ? parseInt(prefilledGymId) : '',
    amount: prefilledAmount || '',
    description: prefilledDescription || '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [paymentId, setPaymentId] = useState(null);
  const [paymentStatus, setPaymentStatus] = useState(null);
  const [refunding, setRefunding] = useState(false);

  // Fetch gyms for dropdown
  const { data: gyms = [] } = useAsyncData(
    async () => {
      const response = await client.get('/api/gyms');
      return response.data || [];
    },
    { immediate: true }
  );

  // Fetch payment details if verifying
  useEffect(() => {
    const verifyPaymentId = searchParams.get('verify');
    if (verifyPaymentId) {
      verifyExistingPayment(parseInt(verifyPaymentId));
    }
  }, [searchParams]);

  const verifyExistingPayment = async (id) => {
    try {
      setLoading(true);
      const response = await client.get(`/api/payments/${id}`);
      setPaymentStatus(response.data);
      setPaymentId(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch payment details');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === 'gymId' ? (value ? parseInt(value) : '') : value,
    }));
    setError('');
  };

  const handleProcessPayment = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (!form.gymId || !form.amount) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      const response = await client.post('/api/payments/process', {
        gymId: form.gymId,
        amount: parseFloat(form.amount),
        description: form.description,
      });

      setPaymentStatus(response.data);
      setPaymentId(response.data.id);
      setSuccess(true);
      setForm({ gymId: '', amount: '', description: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Payment processing failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleRefund = async () => {
    if (!window.confirm('Are you sure you want to refund this payment?')) return;

    try {
      setRefunding(true);
      const response = await client.put(`/api/payments/${paymentId}/refund`);
      setPaymentStatus(response.data);
      setSuccess(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Refund failed. Please try again.');
    } finally {
      setRefunding(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white p-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Payment Hub</h1>
          <p className="text-gray-600">Manage your gym membership payments and transactions</p>
        </div>

        <div className="grid md:grid-cols-2 gap-8">
          {/* Payment Form */}
          {!paymentStatus && (
            <div className="bg-white rounded-lg shadow-lg p-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">New Payment</h2>
              <form onSubmit={handleProcessPayment} className="space-y-5">
                {/* Gym Selection */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Select Gym *
                  </label>
                  <select
                    name="gymId"
                    value={form.gymId}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">-- Choose a gym --</option>
                    {gyms.map((gym) => (
                      <option key={gym.id} value={gym.id}>
                        {gym.name} (₹{gym.monthlyFee.toFixed(2)}/month)
                      </option>
                    ))}
                  </select>
                </div>

                {/* Amount */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Amount (₹) *
                  </label>
                  <input
                    type="number"
                    name="amount"
                    value={form.amount}
                    onChange={handleInputChange}
                    placeholder="Enter amount"
                    min="0.01"
                    step="0.01"
                    required
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                {/* Description */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Description
                  </label>
                  <input
                    type="text"
                    name="description"
                    value={form.description}
                    onChange={handleInputChange}
                    placeholder="e.g., Monthly membership - PRO"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>

                {/* Error */}
                {error && (
                  <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                    <p className="text-red-800 font-medium">❌ {error}</p>
                  </div>
                )}

                {/* Success */}
                {success && paymentStatus && (
                  <div className="p-4 bg-green-50 border border-green-200 rounded-lg">
                    <p className="text-green-800 font-medium">✓ Payment Successful</p>
                    <p className="text-green-700 text-sm mt-1">
                      Transaction ID: {paymentStatus.id}
                    </p>
                  </div>
                )}

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-400 text-white font-bold py-3 px-4 rounded-lg transition duration-200"
                >
                  {loading ? 'Processing...' : 'Process Payment'}
                </button>
              </form>
            </div>
          )}

          {/* Payment Status */}
          {paymentStatus && (
            <div className="bg-white rounded-lg shadow-lg p-8">
              <h2 className="text-2xl font-bold text-gray-900 mb-6">Payment Status</h2>

              <div className="space-y-4">
                {/* Status Badge */}
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Status:</span>
                  <span
                    className={`px-4 py-2 rounded-full font-bold text-white ${
                      paymentStatus.status === 'SUCCESS'
                        ? 'bg-green-600'
                        : paymentStatus.status === 'PENDING'
                        ? 'bg-yellow-600'
                        : paymentStatus.status === 'FAILED'
                        ? 'bg-red-600'
                        : 'bg-gray-600'
                    }`}
                  >
                    {paymentStatus.status}
                  </span>
                </div>

                {/* Details */}
                <div className="border-t pt-4 space-y-3">
                  <div className="flex justify-between">
                    <span className="text-gray-600">Transaction ID:</span>
                    <span className="font-semibold text-gray-900">{paymentStatus.id}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Gym:</span>
                    <span className="font-semibold text-gray-900">{paymentStatus.gymName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Amount:</span>
                    <span className="font-bold text-lg text-green-600">₹{paymentStatus.amount.toFixed(2)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">Date:</span>
                    <span className="font-semibold text-gray-900">
                      {new Date(paymentStatus.paidAt).toLocaleString()}
                    </span>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3 pt-4">
                  {paymentStatus.status === 'SUCCESS' && (
                    <button
                      onClick={handleRefund}
                      disabled={refunding}
                      className="flex-1 bg-red-600 hover:bg-red-700 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded-lg transition duration-200"
                    >
                      {refunding ? 'Processing...' : 'Request Refund'}
                    </button>
                  )}
                  <button
                    onClick={() => setPaymentStatus(null)}
                    className="flex-1 bg-gray-400 hover:bg-gray-500 text-white font-bold py-2 px-4 rounded-lg transition duration-200"
                  >
                    New Payment
                  </button>
                </div>

                {/* Refund Confirmation */}
                {paymentStatus.status === 'REFUNDED' && (
                  <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg mt-4">
                    <p className="text-blue-800 font-medium">✓ Refund Processed</p>
                    <p className="text-blue-700 text-sm mt-1">
                      Your refund has been initiated. It may take 3-5 business days to reflect in your account.
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Payment History */}
        <div className="mt-12 bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Your Payment History</h2>
          <PaymentHistory />
        </div>

        {/* Back Button */}
        <div className="mt-8">
          <button
            onClick={() => navigate(-1)}
            className="text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-2"
          >
            ← Go Back
          </button>
        </div>
      </div>
    </div>
  );
}

// Payment History Component
function PaymentHistory() {
  const { data: payments = [], loading } = useAsyncData(
    async () => {
      const response = await client.get('/api/payments/my');
      return response.data || [];
    },
    { immediate: true }
  );

  const columns = [
    { header: 'Date', accessor: (row) => new Date(row.paidAt).toLocaleDateString() },
    { header: 'Gym', accessor: 'gymName' },
    { header: 'Amount', accessor: (row) => `₹${row.amount.toFixed(2)}` },
    {
      header: 'Status',
      accessor: (row) => (
        <span
          className={`px-3 py-1 rounded-full text-white font-semibold text-sm ${
            row.status === 'SUCCESS'
              ? 'bg-green-600'
              : row.status === 'PENDING'
              ? 'bg-yellow-600'
              : row.status === 'REFUNDED'
              ? 'bg-blue-600'
              : 'bg-red-600'
          }`}
        >
          {row.status}
        </span>
      ),
    },
  ];

  if (loading) return <p className="text-center text-gray-600">Loading payments...</p>;
  if (payments.length === 0)
    return <p className="text-center text-gray-600">No payments yet</p>;

  return <DataTable data={payments} columns={columns} />;
}
