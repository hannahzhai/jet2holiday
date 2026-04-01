import httpClient, { useMockApi } from './httpClient'
import { mockGetDashboardSummary, mockGetPerformance } from './mock/mockDataSource'

export const getDashboardSummary = async () => {
  if (useMockApi) return mockGetDashboardSummary()
  const { data } = await httpClient.get('/api/dashboard/summary')
  return data
}

export const getPerformance = async (range = '1M') => {
  if (useMockApi) return mockGetPerformance(range)
  const { data } = await httpClient.get('/api/dashboard/performance', {
    params: { range }
  })
  return data
}