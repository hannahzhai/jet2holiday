import { formatDate } from '../../utils/formatters'

const delay = (ms = 180) => new Promise((resolve) => setTimeout(resolve, ms))
const clone = (value) => JSON.parse(JSON.stringify(value))

const today = new Date()
const toIsoDate = (date) => date.toISOString().slice(0, 10)

// In-memory mock "database" used when VITE_USE_MOCK_API=true.
// It mirrors backend concepts so UI and store flows stay identical across mock/real modes.
const state = {
  account: {
    id: 1,
    accountName: 'Default Portfolio Account',
    cashBalance: 12000,
    currency: 'USD'
  },
  holdings: [
    {
      id: 1,
      symbol: 'AAPL',
      companyName: 'Apple Inc.',
      assetType: 'STOCK',
      shares: 10,
      costBasis: 180,
      currency: 'USD'
    },
    {
      id: 2,
      symbol: 'TSLA',
      companyName: 'Tesla, Inc.',
      assetType: 'STOCK',
      shares: 8,
      costBasis: 220,
      currency: 'USD'
    },
    {
      id: 3,
      symbol: 'AGG',
      companyName: 'iShares Core U.S. Aggregate Bond ETF',
      assetType: 'BOND',
      shares: 40,
      costBasis: 98.5,
      currency: 'USD'
    },
    {
      id: 4,
      symbol: 'BTC-USD',
      companyName: 'Bitcoin',
      assetType: 'CRYPTO',
      shares: 0.35,
      costBasis: 42000,
      currency: 'USD'
    },
    {
      id: 5,
      symbol: 'MSFT',
      companyName: 'Microsoft Corporation',
      assetType: 'STOCK',
      shares: 12,
      costBasis: 320,
      currency: 'USD'
    }
  ],
  prices: {
    AAPL: 195,
    TSLA: 246,
    AGG: 100.2,
    'BTC-USD': 45550,
    MSFT: 338
  }
}

const dayCounts = {
  '1W': 7,
  '1M': 30,
  '3M': 90,
  '1Y': 365
}

const round4 = (value) => Number(value.toFixed(4))

// Enrich base holdings with price-dependent fields expected by dashboard tables.
const computeItems = () => {
  const snapshotDate = toIsoDate(today)
  return state.holdings.map((item) => {
    const currentPrice = Number(state.prices[item.symbol] ?? item.costBasis)
    const marketValue = Number(item.shares) * currentPrice
    const totalCost = Number(item.shares) * Number(item.costBasis)
    const profitLoss = marketValue - totalCost
    return {
      ...item,
      currentPrice: round4(currentPrice),
      marketValue: round4(marketValue),
      profitLoss: round4(profitLoss),
      profitLossPercent: totalCost > 0 ? round4((profitLoss / totalCost) * 100) : 0,
      snapshotDate
    }
  })
}

// Build dashboard summary from current in-memory account + holdings + prices.
// This keeps financial aggregates centralized in one place, similar to backend behavior.
const computeSummary = () => {
  const items = computeItems()
  const cashBalance = Number(state.account.cashBalance || 0)
  const totalMarketValue = items.reduce((sum, item) => sum + Number(item.marketValue || 0), 0)
  const totalCost = items.reduce((sum, item) => sum + Number(item.shares) * Number(item.costBasis), 0)
  const totalProfitLoss = totalMarketValue - totalCost
  const totalAssets = cashBalance + totalMarketValue

  const categorySummary = {
    cash: cashBalance,
    stocks: items.filter((item) => item.assetType === 'STOCK').reduce((sum, item) => sum + item.marketValue, 0),
    bonds: items.filter((item) => item.assetType === 'BOND').reduce((sum, item) => sum + item.marketValue, 0),
    crypto: items.filter((item) => item.assetType === 'CRYPTO').reduce((sum, item) => sum + item.marketValue, 0)
  }

  const allocationBase = totalAssets > 0 ? totalAssets : 1
  const allocation = {
    stocks: round4((categorySummary.stocks / allocationBase) * 100),
    bonds: round4((categorySummary.bonds / allocationBase) * 100),
    crypto: round4((categorySummary.crypto / allocationBase) * 100),
    cash: round4((categorySummary.cash / allocationBase) * 100)
  }

  return {
    cashBalance: round4(cashBalance),
    totalAssets: round4(totalAssets),
    totalMarketValue: round4(totalMarketValue),
    totalCost: round4(totalCost),
    totalProfitLoss: round4(totalProfitLoss),
    totalProfitLossPercent: totalCost > 0 ? round4((totalProfitLoss / totalCost) * 100) : 0,
    allocation,
    categorySummary: {
      cash: round4(categorySummary.cash),
      stocks: round4(categorySummary.stocks),
      bonds: round4(categorySummary.bonds),
      crypto: round4(categorySummary.crypto)
    },
    items
  }
}

// Generate deterministic demo performance points from current total assets.
// The shape matches /api/dashboard/performance.
const buildPerformancePoints = (range = '1M') => {
  const days = dayCounts[range] || dayCounts['1M']
  const summary = computeSummary()
  const base = Number(summary.totalAssets || 0)
  const points = []

  for (let index = days - 1; index >= 0; index -= 1) {
    const date = new Date(today)
    date.setDate(today.getDate() - index)
    const wave = Math.sin((days - index) / 5) * (base * 0.008)
    const trend = ((days - index) / days) * (base * 0.012)
    const totalAssets = Math.max(0, base - base * 0.03 + wave + trend)
    points.push({
      date: toIsoDate(date),
      totalAssets: round4(totalAssets)
    })
  }

  return points
}

// Shared guard used by read/update/delete operations.
const findHoldingOrThrow = (id) => {
  const holding = state.holdings.find((item) => Number(item.id) === Number(id))
  if (!holding) {
    throw new Error('Holding not found.')
  }
  return holding
}

// Enforce unique symbol invariant (aligned with account_id + symbol uniqueness).
const ensureUniqueSymbol = (symbol, excludeId = null) => {
  const normalized = symbol.trim().toUpperCase()
  const conflict = state.holdings.some(
    (item) => item.symbol.toUpperCase() === normalized && Number(item.id) !== Number(excludeId)
  )
  if (conflict) {
    throw new Error('A holding with this symbol already exists.')
  }
}

export const mockGetAccount = async () => {
  await delay()
  return clone(state.account)
}

export const mockUpdateCashBalance = async ({ cashBalance }) => {
  await delay()
  const numericValue = Number(cashBalance)
  if (Number.isNaN(numericValue) || numericValue < 0) {
    throw new Error('Cash balance must be non-negative.')
  }
  state.account.cashBalance = round4(numericValue)
  return clone(state.account)
}

export const mockGetPortfolioItems = async () => {
  await delay()
  return clone(state.holdings)
}

export const mockGetPortfolioItem = async (id) => {
  await delay()
  return clone(findHoldingOrThrow(id))
}

export const mockCreatePortfolioItem = async (payload) => {
  await delay()
  ensureUniqueSymbol(payload.symbol)
  // IDs are generated incrementally to mimic auto-increment persistence.
  const nextId = state.holdings.length ? Math.max(...state.holdings.map((item) => Number(item.id))) + 1 : 1
  const created = {
    id: nextId,
    symbol: payload.symbol.trim().toUpperCase(),
    companyName: payload.companyName,
    assetType: payload.assetType,
    shares: Number(payload.shares),
    costBasis: Number(payload.costBasis),
    currency: payload.currency?.toUpperCase?.() || 'USD'
  }
  state.holdings.push(created)
  if (!(created.symbol in state.prices)) {
    state.prices[created.symbol] = Number(created.costBasis)
  }
  return clone(created)
}

export const mockUpdatePortfolioItem = async (id, payload) => {
  await delay()
  const holding = findHoldingOrThrow(id)
  ensureUniqueSymbol(payload.symbol, id)
  holding.symbol = payload.symbol.trim().toUpperCase()
  holding.companyName = payload.companyName
  holding.assetType = payload.assetType
  holding.shares = Number(payload.shares)
  holding.costBasis = Number(payload.costBasis)
  holding.currency = payload.currency?.toUpperCase?.() || 'USD'
  if (!(holding.symbol in state.prices)) {
    state.prices[holding.symbol] = Number(holding.costBasis)
  }
  return clone(holding)
}

export const mockDeletePortfolioItem = async (id) => {
  await delay()
  const index = state.holdings.findIndex((item) => Number(item.id) === Number(id))
  if (index < 0) {
    throw new Error('Holding not found.')
  }
  state.holdings.splice(index, 1)
  return { success: true }
}

export const mockGetDashboardSummary = async () => {
  await delay()
  return clone(computeSummary())
}

export const mockGetPerformance = async (range = '1M') => {
  await delay()
  const normalizedRange = dayCounts[range] ? range : '1M'
  return {
    range: normalizedRange,
    points: buildPerformancePoints(normalizedRange)
  }
}

export const mockRefreshMarketData = async () => {
  await delay()
  // Apply a small deterministic drift so users can observe data refresh effects.
  const symbols = Array.from(new Set(state.holdings.map((item) => item.symbol)))
  symbols.forEach((symbol, index) => {
    const basePrice = Number(state.prices[symbol] || 1)
    const drift = 1 + ((index % 3) - 1) * 0.01
    state.prices[symbol] = round4(Math.max(0.01, basePrice * drift))
  })
  return {
    success: true,
    snapshotDate: toIsoDate(today),
    refreshedCount: symbols.length,
    symbols,
    message: symbols.length
      ? 'Mock market data refreshed successfully.'
      : 'No holdings available to refresh market data.'
  }
}

export const mockGetStatePreview = () => ({
  summary: computeSummary(),
  account: clone(state.account),
  holdings: clone(state.holdings),
  todayLabel: formatDate(toIsoDate(today))
})
export const mockGetLatestMarketData = async (symbols = []) => {
  await delay()
  const snapshotDate = toIsoDate(today)

  return symbols
    .map((rawSymbol) => `${rawSymbol || ''}`.trim().toUpperCase())
    .filter(Boolean)
    .filter((symbol) => symbol in state.prices)
    .map((symbol) => ({
      symbol,
      snapshotDate,
      currentPrice: round4(Number(state.prices[symbol])),
      currency: 'USD',
      source: 'MOCK'
    }))
}
