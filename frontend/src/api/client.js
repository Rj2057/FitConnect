import axios from 'axios'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: apiBaseUrl,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('fitconnect.token')

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

export async function request(method, url, data, config = {}) {
  const response = await api({ method, url, data, ...config })
  return response.data
}

export default api
