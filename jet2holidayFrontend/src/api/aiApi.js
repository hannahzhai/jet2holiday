import httpClient, { useMockApi } from './httpClient'
import { mockGeneratePortfolioInsight, mockAskMiniMaxChat } from './mock/mockDataSource'

export const generatePortfolioInsight = async (range = '1M') => {
  if (useMockApi) return mockGeneratePortfolioInsight(range)
  try {
    const { data } = await httpClient.post('/api/ai/portfolio-insight', { range })
    return data
  } catch (error) {
    if (error?.code === 'ECONNABORTED') {
      throw new Error('AI insight request timed out. Please retry; fallback summary should be returned shortly.')
    }
    throw error
  }
}

export const askMiniMaxChat = async (question, range = '1M') => {
  if (useMockApi) return mockAskMiniMaxChat(question, range)
  const { data } = await httpClient.post('/api/ai/minimax-chat', { question, range })
  return data
}
