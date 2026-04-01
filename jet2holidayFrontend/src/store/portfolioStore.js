import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAccount, updateCashBalance } from '../api/accountApi'
import { getPortfolioItems, createPortfolioItem, updatePortfolioItem, deletePortfolioItem } from '../api/portfolioApi'
import { getDashboardSummary, getPerformance } from '../api/dashboardApi'
import { refreshMarketData, getLatestMarketData } from '../api/marketDataApi'
import { normalizeApiError } from '../api/httpClient'

const isNotFoundError = (error) => Number(error?.response?.status) === 404

export const usePortfolioStore = defineStore('portfolio', () => {
  const account = ref(null)
  const summary = ref(null)
  const holdings = ref([])
  const performance = ref({ range: '1M', points: [] })
  const latestSnapshots = ref([])
  const marketRefreshResult = ref(null)

  const loading = ref({
    dashboard: false,
    holdings: false,
    performance: false,
    mutateHolding: false,
    updateCash: false,
    refreshMarket: false,
    latestMarket: false
  })

  const notification = ref({ type: 'success', message: '' })

  const setNotification = (type, message) => {
    notification.value = { type, message }
  }

  const clearNotification = () => {
    notification.value = { type: 'success', message: '' }
  }

  const loadAccount = async () => {
    try {
      account.value = await getAccount()
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to load account info.'))
      throw error
    }
  }

  const loadSummary = async () => {
    loading.value.dashboard = true
    try {
      summary.value = await getDashboardSummary()
    } catch (error) {
      if (isNotFoundError(error)) {
        summary.value = null
        return
      }
      setNotification('error', normalizeApiError(error, 'Failed to load dashboard summary.'))
      throw error
    } finally {
      loading.value.dashboard = false
    }
  }

  const loadPerformance = async (range = '1M') => {
    loading.value.performance = true
    try {
      const response = await getPerformance(range)
      performance.value = {
        range: response?.range || range,
        points: Array.isArray(response?.points) ? response.points : []
      }
    } catch (error) {
      if (isNotFoundError(error)) {
        performance.value = { range, points: [] }
        return
      }
      setNotification('error', normalizeApiError(error, 'Failed to load performance chart.'))
      throw error
    } finally {
      loading.value.performance = false
    }
  }

  const loadLatestSnapshots = async (symbols = null) => {
    const symbolList = Array.isArray(symbols)
      ? symbols
      : holdings.value.map((item) => item.symbol)

    const normalized = symbolList
      .map((symbol) => `${symbol || ''}`.trim().toUpperCase())
      .filter(Boolean)

    if (!normalized.length) {
      latestSnapshots.value = []
      return
    }

    loading.value.latestMarket = true
    try {
      latestSnapshots.value = await getLatestMarketData(normalized)
    } catch (error) {
      if (isNotFoundError(error)) {
        latestSnapshots.value = []
        return
      }
      setNotification('error', normalizeApiError(error, 'Failed to load latest market data.'))
      throw error
    } finally {
      loading.value.latestMarket = false
    }
  }

  const loadDashboard = async (range = '1M') => {
    await Promise.all([loadSummary(), loadPerformance(range)])
  }

  const loadHoldings = async () => {
    loading.value.holdings = true
    try {
      holdings.value = await getPortfolioItems()
      await loadLatestSnapshots(holdings.value.map((item) => item.symbol))
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to load holdings.'))
      throw error
    } finally {
      loading.value.holdings = false
    }
  }

  const createHolding = async (payload) => {
    loading.value.mutateHolding = true
    try {
      await createPortfolioItem(payload)
      await Promise.all([loadHoldings(), loadSummary()])
      setNotification('success', 'Holding added successfully.')
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to add holding.'))
      throw error
    } finally {
      loading.value.mutateHolding = false
    }
  }

  const editHolding = async (id, payload) => {
    loading.value.mutateHolding = true
    try {
      await updatePortfolioItem(id, payload)
      await Promise.all([loadHoldings(), loadSummary()])
      setNotification('success', 'Holding updated successfully.')
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to update holding.'))
      throw error
    } finally {
      loading.value.mutateHolding = false
    }
  }

  const removeHolding = async (id) => {
    loading.value.mutateHolding = true
    try {
      await deletePortfolioItem(id)
      await Promise.all([loadHoldings(), loadSummary()])
      setNotification('success', 'Holding deleted successfully.')
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to delete holding.'))
      throw error
    } finally {
      loading.value.mutateHolding = false
    }
  }

  const saveCashBalance = async (cashBalance) => {
    loading.value.updateCash = true
    try {
      await updateCashBalance({ cashBalance })
      await Promise.all([loadAccount(), loadSummary()])
      setNotification('success', 'Cash balance updated successfully.')
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to update cash balance.'))
      throw error
    } finally {
      loading.value.updateCash = false
    }
  }

  const refreshMarketDataAndReload = async (range = performance.value.range || '1M') => {
    loading.value.refreshMarket = true
    try {
      const response = await refreshMarketData()
      marketRefreshResult.value = response

      await Promise.all([
        loadSummary(),
        loadPerformance(range),
        loadLatestSnapshots()
      ])

      if (response?.success === false) {
        const failed = Array.isArray(response?.failedSymbols) ? response.failedSymbols : []
        const suffix = failed.length ? ` Failed: ${failed.join(', ')}` : ''
        setNotification('error', `${response?.message || 'Market refresh completed with failures.'}${suffix}`)
      } else {
        setNotification('success', response?.message || 'Market data refreshed successfully.')
      }

      return response
    } catch (error) {
      setNotification('error', normalizeApiError(error, 'Failed to refresh market data.'))
      throw error
    } finally {
      loading.value.refreshMarket = false
    }
  }

  return {
    account,
    summary,
    holdings,
    performance,
    latestSnapshots,
    marketRefreshResult,
    loading,
    notification,
    clearNotification,
    loadAccount,
    loadSummary,
    loadPerformance,
    loadDashboard,
    loadHoldings,
    loadLatestSnapshots,
    createHolding,
    editHolding,
    removeHolding,
    saveCashBalance,
    refreshMarketDataAndReload
  }
})