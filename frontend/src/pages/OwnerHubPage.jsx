import { useState } from 'react'
import { request } from '../api/client'
import { DataTable } from '../components/DataTable'
import { Button, Card, Field, Input, LoadingBlock, Message, SectionTitle, Select } from '../components/ui'
import { MetricGrid } from '../components/MetricGrid'
import { useAsyncData } from '../hooks/useAsyncData'
import { formatCurrency, formatDateTime } from '../utils/format'

export function OwnerHubPage() {
  const [listingForm, setListingForm] = useState({ sellerGymId: '', equipmentName: '', monthlyRentPrice: '', details: '' })
  const [buyGymByListing, setBuyGymByListing] = useState({})
  const [actionMessage, setActionMessage] = useState('')
  const [marketplaceSearch, setMarketplaceSearch] = useState('')
  const [marketplaceMinPrice, setMarketplaceMinPrice] = useState('')
  const [marketplaceMaxPrice, setMarketplaceMaxPrice] = useState('')

  const dataState = useAsyncData(async () => {
    const [gyms, trainers] = await Promise.all([
      request('get', '/api/gyms'),
      request('get', '/api/trainers'),
    ])

    return {
      gyms,
      trainers,
    }
  }, [])
  const listingsState = useAsyncData(() => request('get', '/api/equipment-marketplace/listings'), [])
  const myListingsState = useAsyncData(() => request('get', '/api/equipment-marketplace/listings/my'), [])
  const transactionsState = useAsyncData(() => request('get', '/api/equipment-marketplace/transactions/my'), [])

  const items = dataState.data
    ? [
        { label: 'Gyms visible', value: dataState.data.gyms.length, hint: 'Network locations' },
        { label: 'Trainer profiles', value: dataState.data.trainers.length, hint: 'Available on the platform' },
        { label: 'Equipment control', value: 'Ready', hint: 'Manage through the equipment page' },
        { label: 'Payments', value: 'Tracked', hint: 'Membership purchases create payment records' },
      ]
    : []
  const filteredListings = (listingsState.data || []).filter((row) => {
    const q = marketplaceSearch.trim().toLowerCase()
    const matchesText = !q
      || row.equipmentName?.toLowerCase().includes(q)
      || row.sellerGymName?.toLowerCase().includes(q)
      || row.sellerOwnerName?.toLowerCase().includes(q)
      || row.details?.toLowerCase().includes(q)
    const min = marketplaceMinPrice ? Number(marketplaceMinPrice) : null
    const max = marketplaceMaxPrice ? Number(marketplaceMaxPrice) : null
    const price = Number(row.monthlyRentPrice || 0)
    const matchesMin = min == null || price >= min
    const matchesMax = max == null || price <= max
    return matchesText && matchesMin && matchesMax
  })

  async function handleCreateListing(event) {
    event.preventDefault()
    setActionMessage('')
    try {
      const created = await request('post', '/api/equipment-marketplace/listings', {
        sellerGymId: Number(listingForm.sellerGymId),
        equipmentName: listingForm.equipmentName,
        details: listingForm.details,
        monthlyRentPrice: Number(listingForm.monthlyRentPrice),
      })
      myListingsState.setData((current) => [created, ...(current || [])])
      setListingForm({ sellerGymId: '', equipmentName: '', monthlyRentPrice: '', details: '' })
      setActionMessage('Marketplace listing created.')
    } catch (err) {
      setActionMessage(err?.response?.data?.message || 'Unable to create listing')
    }
  }

  async function handleBuyListing(listingId) {
    const buyerGymId = buyGymByListing[listingId]
    if (!buyerGymId) {
      setActionMessage('Select your gym before buying this listing.')
      return
    }
    setActionMessage('')
    try {
      const transaction = await request('post', `/api/equipment-marketplace/listings/${listingId}/buy`, {
        buyerGymId: Number(buyerGymId),
      })
      listingsState.setData((current) => (current || []).filter((item) => item.id !== listingId))
      myListingsState.setData((current) => (current || []).filter((item) => item.id !== listingId))
      transactionsState.setData((current) => [transaction, ...(current || [])])
      setActionMessage('Listing purchased successfully. Notification removed from marketplace.')
    } catch (err) {
      setActionMessage(err?.response?.data?.message || 'Unable to buy listing')
    }
  }

  async function handleCancelListing(listingId) {
    setActionMessage('')
    try {
      await request('delete', `/api/equipment-marketplace/listings/${listingId}`)
      myListingsState.setData((current) => (current || []).filter((item) => item.id !== listingId))
      setActionMessage('Listing cancelled successfully.')
    } catch (err) {
      setActionMessage(err?.response?.data?.message || 'Unable to cancel listing')
    }
  }

  return (
    <div className="space-y-6">
      <SectionTitle title="Owner Hub" subtitle="Use this hub to understand owner capabilities before diving into equipment, memberships, and payments." />
      {dataState.loading ? <LoadingBlock label="Loading owner hub..." /> : null}
      {dataState.error ? <Message kind="error">{dataState.error}</Message> : null}
      {items.length ? <MetricGrid items={items} /> : null}
      <Card>
        <h3 className="text-xl font-semibold text-[var(--ink)]">Operational checklist</h3>
        <ul className="mt-4 space-y-3 text-sm text-[var(--ink-soft)]">
          <li>Create and maintain gym locations from the gyms page.</li>
          <li>Add or remove equipment from the equipment page.</li>
          <li>Review memberships and payments by selecting a gym on their dedicated pages.</li>
        </ul>
      </Card>

      {actionMessage ? <Message kind={actionMessage.includes('successfully') || actionMessage.includes('created') ? 'success' : 'error'}>{actionMessage}</Message> : null}

      <Card>
        <SectionTitle title="Add equipment for rent" subtitle="Post machinery with monthly rent and details so other owners can buy/rent directly." />
        <form className="mt-6 grid gap-4 md:grid-cols-2" onSubmit={handleCreateListing}>
          <Field label="Your gym">
            <Select value={listingForm.sellerGymId} onChange={(event) => setListingForm((current) => ({ ...current, sellerGymId: event.target.value }))} required>
              <option value="">Select gym</option>
              {(dataState.data?.gyms || []).map((gym) => (
                <option key={gym.id} value={gym.id}>{gym.name}</option>
              ))}
            </Select>
          </Field>
          <Field label="Machinery name">
            <Input value={listingForm.equipmentName} onChange={(event) => setListingForm((current) => ({ ...current, equipmentName: event.target.value }))} required />
          </Field>
          <Field label="Monthly rent price">
            <Input type="number" min="0.01" step="0.01" value={listingForm.monthlyRentPrice} onChange={(event) => setListingForm((current) => ({ ...current, monthlyRentPrice: event.target.value }))} required />
          </Field>
          <Field label="Details">
            <Input value={listingForm.details} onChange={(event) => setListingForm((current) => ({ ...current, details: event.target.value }))} />
          </Field>
          <div className="md:col-span-2">
            <Button className="w-fit" type="submit">Publish listing</Button>
          </div>
        </form>
      </Card>

      <SectionTitle title="Marketplace listings" subtitle="Buy/rent from other owners. After purchase, listing is removed automatically." />
      <Card>
        <div className="grid gap-4 md:grid-cols-3">
          <Field label="Search">
            <Input value={marketplaceSearch} onChange={(event) => setMarketplaceSearch(event.target.value)} placeholder="Machinery, seller, gym..." />
          </Field>
          <Field label="Min rent price">
            <Input type="number" min="0" value={marketplaceMinPrice} onChange={(event) => setMarketplaceMinPrice(event.target.value)} />
          </Field>
          <Field label="Max rent price">
            <Input type="number" min="0" value={marketplaceMaxPrice} onChange={(event) => setMarketplaceMaxPrice(event.target.value)} />
          </Field>
        </div>
      </Card>
      {listingsState.loading ? <LoadingBlock label="Loading listings..." /> : null}
      <DataTable
        columns={[
          { key: 'equipmentName', label: 'Machinery' },
          { key: 'monthlyRentPrice', label: 'Monthly rent', render: (row) => formatCurrency(row.monthlyRentPrice) },
          { key: 'sellerGymName', label: 'Seller Gym' },
          { key: 'sellerOwnerName', label: 'Seller' },
          { key: 'sellerOwnerEmail', label: 'Seller Contact' },
          { key: 'details', label: 'Details' },
          {
            key: 'buy',
            label: 'Buy',
            render: (row) => (
              <div className="flex flex-col gap-2">
                <Select value={buyGymByListing[row.id] || ''} onChange={(event) => setBuyGymByListing((current) => ({ ...current, [row.id]: event.target.value }))}>
                  <option value="">Select your gym</option>
                  {(dataState.data?.gyms || []).map((gym) => (
                    <option key={gym.id} value={gym.id}>{gym.name}</option>
                  ))}
                </Select>
                <Button className="w-fit" onClick={() => handleBuyListing(row.id)}>Buy now</Button>
              </div>
            ),
          },
        ]}
        rows={filteredListings}
        emptyTitle="No active marketplace listings"
        emptyDescription="No listings match your filter right now."
      />

      <SectionTitle title="My active listings" subtitle="Your currently published marketplace notifications." />
      {myListingsState.loading ? <LoadingBlock label="Loading your listings..." /> : null}
      <DataTable
        columns={[
          { key: 'equipmentName', label: 'Machinery' },
          { key: 'sellerGymName', label: 'Gym' },
          { key: 'monthlyRentPrice', label: 'Monthly rent', render: (row) => formatCurrency(row.monthlyRentPrice) },
          { key: 'details', label: 'Details' },
          { key: 'createdAt', label: 'Created', render: (row) => formatDateTime(row.createdAt) },
          {
            key: 'actions',
            label: 'Actions',
            render: (row) => <Button variant="ghost" onClick={() => handleCancelListing(row.id)}>Cancel listing</Button>,
          },
        ]}
        rows={myListingsState.data || []}
        emptyTitle="No active listings"
        emptyDescription="Create a listing to start receiving buyer requests."
      />

      <SectionTitle title="Buyer/Seller transaction details" subtitle="Both buyer and seller can see each other's details for completed deals." />
      {transactionsState.loading ? <LoadingBlock label="Loading transactions..." /> : null}
      <DataTable
        columns={[
          { key: 'equipmentName', label: 'Machinery' },
          { key: 'monthlyRentPrice', label: 'Monthly rent', render: (row) => formatCurrency(row.monthlyRentPrice) },
          { key: 'sellerOwnerName', label: 'Seller Name' },
          { key: 'sellerOwnerEmail', label: 'Seller Email' },
          { key: 'buyerOwnerName', label: 'Buyer Name' },
          { key: 'buyerOwnerEmail', label: 'Buyer Email' },
          { key: 'sellerGymName', label: 'Seller Gym' },
          { key: 'buyerGymName', label: 'Buyer Gym' },
          { key: 'purchasedAt', label: 'Purchased At', render: (row) => formatDateTime(row.purchasedAt) },
        ]}
        rows={transactionsState.data || []}
        emptyTitle="No marketplace transactions yet"
        emptyDescription="Once a listing is bought, buyer/seller details appear here."
      />
    </div>
  )
}
