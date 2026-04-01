<script setup>
import { computed, onMounted, ref } from 'vue'
import { usePortfolioStore } from '../store/portfolioStore'
import BaseButton from '../components/common/BaseButton.vue'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import SummaryCards from '../components/dashboard/SummaryCards.vue'
import AllocationPieChart from '../components/dashboard/AllocationPieChart.vue'
import PerformanceLineChart from '../components/dashboard/PerformanceLineChart.vue'
import CategorySummaryCards from '../components/dashboard/CategorySummaryCards.vue'
import HoldingsOverviewTable from '../components/dashboard/HoldingsOverviewTable.vue'
import CashBalanceModal from '../components/account/CashBalanceModal.vue'

const store = usePortfolioStore()
const selectedRange = ref('1M')
const cashModalOpen = ref(false)

const summary = computed(() => store.summary)
const account = computed(() => store.account)
const performance = computed(() => store.performance)
const loading = computed(() => store.loading)
const holdings = computed(() => store.holdings || [])
const latestSnapshots = computed(() => store.latestSnapshots || [])
const refreshResult = computed(() => store.marketRefreshResult)
const currency = computed(() => account.value?.currency || summary.value?.currency || 'USD')

const dashboardItems = computed(() => {
  if (Array.isArray(summary.value?.items) && summary.value.items.length) {
    return summary.value.items
  }

  const latestBySymbol = new Map(latestSnapshots.value.map((item) => [item.symbol, item]))
  return holdings.value.map((item) => {
    const latest = latestBySymbol.get(`${item.symbol || ''}`.toUpperCase()) || {}
    const currentPrice = latest.currentPrice
    const marketValue = currentPrice != null ? Number(item.shares || 0) * Number(currentPrice || 0) : null
    const totalCost = Number(item.shares || 0) * Number(item.costBasis || 0)
    const profitLoss = marketValue != null ? marketValue - totalCost : null

    return {
      ...item,
      currentPrice,
      marketValue,
      profitLoss,
      profitLossPercent: profitLoss != null && totalCost > 0 ? (profitLoss / totalCost) * 100 : null,
      snapshotDate: latest.snapshotDate
    }
  })
})

const loadData = async () => {
  await Promise.allSettled([
    store.loadAccount(),
    store.loadHoldings(),
    store.loadDashboard(selectedRange.value)
  ])
}

const refreshMarket = async () => {
  try {
    await store.refreshMarketDataAndReload(selectedRange.value)
  } catch {
  }
}

const onRangeChange = async (range) => {
  selectedRange.value = range
  try {
    await store.loadPerformance(range)
  } catch {
  }
}

const saveCashBalance = async (value) => {
  try {
    await store.saveCashBalance(value)
    cashModalOpen.value = false
  } catch {
  }
}

onMounted(loadData)
</script>

<template>
  <section class="page">
    <div class="page-header">
      <h1>Dashboard</h1>
      <div class="actions">
        <BaseButton variant="secondary" :loading="loading.refreshMarket" @click="refreshMarket">
          Refresh Market Data
        </BaseButton>
        <BaseButton @click="cashModalOpen = true">Edit Cash Balance</BaseButton>
      </div>
    </div>

    <div v-if="refreshResult" class="refresh-status" :class="refreshResult.success ? 'ok' : 'warn'">
      <div>
        <strong>{{ refreshResult.message }}</strong>
        <span>
          Date: {{ refreshResult.snapshotDate }} ¡¤ Success: {{ refreshResult.refreshedCount || 0 }}
        </span>
      </div>
      <small v-if="refreshResult.failedSymbols?.length">
        Failed: {{ refreshResult.failedSymbols.join(', ') }}
      </small>
    </div>

    <LoadingSpinner v-if="loading.dashboard && !summary && !holdings.length" />

    <template v-else>
      <SummaryCards :summary="summary" :currency="currency" />
      <div class="grid-two">
        <AllocationPieChart :allocation="summary?.allocation" :category-summary="summary?.categorySummary" />
        <CategorySummaryCards :category-summary="summary?.categorySummary" :currency="currency" />
      </div>
      <HoldingsOverviewTable :items="dashboardItems" :currency="currency" />
      <PerformanceLineChart
        :points="performance?.points || []"
        :range="selectedRange"
        :loading="loading.performance"
        @change-range="onRangeChange"
      />
    </template>

    <CashBalanceModal
      v-model="cashModalOpen"
      :current-balance="account?.cashBalance || 0"
      :loading="loading.updateCash"
      @submit="saveCashBalance"
    />
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.8rem;
}

.page-header h1 {
  margin: 0;
}

.actions {
  display: flex;
  gap: 0.6rem;
  flex-wrap: wrap;
}

.refresh-status {
  border-radius: 0.65rem;
  padding: 0.7rem 0.85rem;
  display: grid;
  gap: 0.35rem;
}

.refresh-status strong {
  display: block;
}

.refresh-status span {
  color: #54607c;
  font-size: 0.86rem;
}

.refresh-status.ok {
  background: #e7f8ef;
  border: 1px solid #afe3c6;
}

.refresh-status.warn {
  background: #fff4e4;
  border: 1px solid #ffd6a2;
}

.grid-two {
  display: grid;
  gap: 1rem;
  grid-template-columns: 1fr;
}

@media (min-width: 1100px) {
  .grid-two {
    grid-template-columns: 1.2fr 1fr;
  }
}
</style>