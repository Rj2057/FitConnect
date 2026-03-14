import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Button, Card, Field, Input, Message, Select } from '../components/ui'

const roleOptions = [
  { value: 'GYM_USER', label: 'Gym User' },
  { value: 'GYM_TRAINER', label: 'Gym Trainer' },
  { value: 'GYM_OWNER', label: 'Gym Owner' },
]

export function AuthPage() {
  const navigate = useNavigate()
  const { login, register, authError, authLoading, setAuthError } = useAuth()
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    role: 'GYM_USER',
  })

  async function handleSubmit(event) {
    event.preventDefault()
    setAuthError('')

    try {
      if (mode === 'login') {
        await login({ email: form.email, password: form.password })
      } else {
        await register(form)
      }
      navigate('/dashboard')
    } catch {
      return
    }
  }

  function updateField(key, value) {
    setForm((current) => ({ ...current, [key]: value }))
  }

  return (
    <div className="min-h-screen bg-[var(--canvas)] px-4 py-10">
      <div className="mx-auto flex min-h-[calc(100vh-5rem)] max-w-lg items-center">
        <Card className="w-full rounded-[32px] bg-[rgba(255,255,255,0.92)] p-6 shadow-[0_28px_80px_rgba(25,30,41,0.14)] md:p-8">
          <div className="flex rounded-full bg-[rgba(26,31,43,0.05)] p-1">
            <button
              className={`flex-1 rounded-full px-4 py-3 text-sm font-semibold ${mode === 'login' ? 'bg-white text-[var(--ink)] shadow-sm' : 'text-[var(--ink-soft)]'}`}
              onClick={() => setMode('login')}
              type="button"
            >
              Login
            </button>
            <button
              className={`flex-1 rounded-full px-4 py-3 text-sm font-semibold ${mode === 'register' ? 'bg-white text-[var(--ink)] shadow-sm' : 'text-[var(--ink-soft)]'}`}
              onClick={() => setMode('register')}
              type="button"
            >
              Register
            </button>
          </div>

          <div className="mt-6">
            <h1 className="text-3xl font-semibold text-[var(--ink)]">{mode === 'login' ? 'Login' : 'Register'}</h1>
            <p className="mt-2 text-sm text-[var(--ink-soft)]">
              {mode === 'login' ? 'Enter your email and password to continue.' : 'Create an account and choose the role you want to use.'}
            </p>
          </div>

          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            {mode === 'register' ? (
              <Field label="Full name">
                <Input value={form.name} onChange={(event) => updateField('name', event.target.value)} placeholder="Aarav Sharma" required />
              </Field>
            ) : null}

            <Field label="Email">
              <Input type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} placeholder="you@fitconnect.com" required />
            </Field>

            <Field label="Password">
              <Input type="password" value={form.password} onChange={(event) => updateField('password', event.target.value)} placeholder="StrongPass@123" required />
            </Field>

            {mode === 'register' ? (
              <Field label="Role">
                <Select value={form.role} onChange={(event) => updateField('role', event.target.value)}>
                  {roleOptions.map((role) => (
                    <option key={role.value} value={role.value}>{role.label}</option>
                  ))}
                </Select>
              </Field>
            ) : null}

            {authError ? <Message kind="error">{authError}</Message> : null}

            <Button className="w-full" disabled={authLoading} type="submit">
              {authLoading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Register'}
            </Button>
          </form>
        </Card>
      </div>
    </div>
  )
}
