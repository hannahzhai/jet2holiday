import httpClient, { useMockApi } from './httpClient'
import { mockRefreshMarketData, mockGetLatestMarketData, mockGetMarketInstruments } from './mock/mockDataSource'

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

export const getMarketInstruments = async (assetType = 'STOCK', page = 1) => {
  const normalizedAssetType = `${assetType || ''}`.trim().toUpperCase() || 'STOCK'
  const normalizedPage = Number(page) > 0 ? Number(page) : 1

  if (useMockApi) return mockGetMarketInstruments(normalizedAssetType, normalizedPage)

  const { data } = await httpClient.get('/api/market-data/instruments', {
    params: {
      assetType: normalizedAssetType,
      page: normalizedPage
    }
  })

  return data || { page: normalizedPage, size: 15, total: 0, totalPages: 0, items: [] }
}
