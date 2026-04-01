import httpClient, { useMockApi } from './httpClient'
import {
  mockGetPortfolioItems,
  mockGetPortfolioItem,
  mockCreatePortfolioItem,
  mockUpdatePortfolioItem,
  mockDeletePortfolioItem
} from './mock/mockDataSource'

export const getPortfolioItems = async () => {
  if (useMockApi) return mockGetPortfolioItems()
  const { data } = await httpClient.get('/api/portfolio-items')
  return data
}

export const getPortfolioItem = async (id) => {
  if (useMockApi) return mockGetPortfolioItem(id)
  const { data } = await httpClient.get(`/api/portfolio-items/${id}`)
  return data
}

export const createPortfolioItem = async (payload) => {
  if (useMockApi) return mockCreatePortfolioItem(payload)
  const { data } = await httpClient.post('/api/portfolio-items', payload)
  return data
}

export const updatePortfolioItem = async (id, payload) => {
  if (useMockApi) return mockUpdatePortfolioItem(id, payload)
  const { data } = await httpClient.put(`/api/portfolio-items/${id}`, payload)
  return data
}

export const deletePortfolioItem = async (id) => {
  if (useMockApi) return mockDeletePortfolioItem(id)
  const { data } = await httpClient.delete(`/api/portfolio-items/${id}`)
  return data
}