import { useEffect, useMemo, useState } from 'react'
import { request } from '../api/client'
import { AuthContext } from './auth-context'

const storageKeys = {
  token: 'fitconnect.token',
  user: 'fitconnect.user',
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(storageKeys.token))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(storageKeys.user)
    return raw ? JSON.parse(raw) : null
  })
  const [authLoading, setAuthLoading] = useState(false)
  const [authError, setAuthError] = useState('')

  useEffect(() => {
    if (token) {
      localStorage.setItem(storageKeys.token, token)
    } else {
      localStorage.removeItem(storageKeys.token)
    }
  }, [token])

  useEffect(() => {
    if (user) {
      localStorage.setItem(storageKeys.user, JSON.stringify(user))
    } else {
      localStorage.removeItem(storageKeys.user)
    }
  }, [user])

  async function authenticate(endpoint, payload) {
    setAuthLoading(true)
    setAuthError('')

    try {
      const response = await request('post', endpoint, payload)
      setToken(response.token)
      setUser({
        id: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
      })
      return response
    } catch (error) {
      const message = error?.response?.data?.message || 'Authentication failed'
      setAuthError(message)
      throw error
    } finally {
      setAuthLoading(false)
    }
  }

  function logout() {
    setToken('')
    setUser(null)
    setAuthError('')
  }

  const value = useMemo(() => ({
    token,
    user,
    isAuthenticated: Boolean(token && user),
    authLoading,
    authError,
    login: (payload) => authenticate('/api/auth/login', payload),
    register: (payload) => authenticate('/api/auth/register', payload),
    logout,
    setAuthError,
  }), [authError, authLoading, token, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
