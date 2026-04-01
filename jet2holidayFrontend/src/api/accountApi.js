import httpClient, { useMockApi } from './httpClient'
import { mockGetAccount, mockUpdateCashBalance } from './mock/mockDataSource'

const DEFAULT_ACCOUNT_ID = 1

export const getAccount = async () => {
  if (useMockApi) return mockGetAccount()
  const { data } = await httpClient.get(`/api/account/${DEFAULT_ACCOUNT_ID}`)
  return data
}

export const updateCashBalance = async (payload) => {
  if (useMockApi) return mockUpdateCashBalance(payload)
  const amount = Number(payload?.cashBalance)
  const { data } = await httpClient.put(`/api/account/${DEFAULT_ACCOUNT_ID}/cash-balance`, { amount })
  return data
}