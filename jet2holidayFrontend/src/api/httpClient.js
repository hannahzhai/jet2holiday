import axios from 'axios'

const configuredBaseURL = import.meta.env.VITE_API_BASE_URL
const resolvedBaseURL = configuredBaseURL ?? (import.meta.env.DEV ? '' : 'http://localhost:8080')

const httpClient = axios.create({
  baseURL: resolvedBaseURL,
  timeout: 15000
})

const useMockValue = `${import.meta.env.VITE_USE_MOCK_API ?? 'false'}`.toLowerCase()
export const useMockApi = useMockValue === 'true' || useMockValue === '1' || useMockValue === 'yes'

export const normalizeApiError = (error, fallback = 'Something went wrong.') => {
  if (error?.response?.data?.message) return error.response.data.message
  if (typeof error?.response?.data === 'string') return error.response.data
  if (error?.message) return error.message
  return fallback
}

export default httpClient