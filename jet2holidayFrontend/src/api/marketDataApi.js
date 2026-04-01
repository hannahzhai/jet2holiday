import httpClient, { useMockApi } from './httpClient'
import { mockRefreshMarketData, mockGetLatestMarketData } from './mock/mockDataSource'

export const refreshMarketData = async () => {
  if (useMockApi) return mockRefreshMarketData()
  const { data } = await httpClient.post('/api/market-data/refresh')
  return data
}

export const getLatestMarketData = async (symbols = []) => {
  const normalizedSymbols = symbols
    .map((symbol) => `${symbol || ''}`.trim().toUpperCase())
    .filter(Boolean)

  if (!normalizedSymbols.length) {
    return []
  }

  if (useMockApi) return mockGetLatestMarketData(normalizedSymbols)

  const { data } = await httpClient.get('/api/market-data/latest', {
    params: {
      symbols: normalizedSymbols.join(',')
    }
  })

  return Array.isArray(data) ? data : []
}