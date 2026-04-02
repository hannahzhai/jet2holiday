<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import BaseCard from '../components/common/BaseCard.vue'
import BaseTable from '../components/common/BaseTable.vue'
import BaseButton from '../components/common/BaseButton.vue'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import EmptyState from '../components/common/EmptyState.vue'
import MarketAddHoldingModal from '../components/market/MarketAddHoldingModal.vue'
import { usePortfolioStore } from '../store/portfolioStore'
import { getMarketInstruments } from '../api/marketDataApi'
import { normalizeApiError } from '../api/httpClient'

const store = usePortfolioStore()

const assetType = ref('STOCK')
const page = ref(1)
const marketPage = ref({ page: 1, size: 15, total: 0, totalPages: 0, items: [] })
const loading = ref(false)
const loadError = ref('')

const addModalOpen = ref(false)
const selectedInstrument = ref(null)

const holdings = computed(() => store.holdings || [])
const defaultCurrency = computed(() => store.account?.currency || 'USD')
const existingSymbols = computed(
  () => new Set(holdings.value.map((item) => `${item.symbol || ''}`.trim().toUpperCase()).filter(Boolean))
)

const rows = computed(() => marketPage.value.items || [])
const hasPrev = computed(() => page.value > 1)
const hasNext = computed(() => page.value < (marketPage.value.totalPages || 0))

const loadMarketPage = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const response = await getMarketInstruments(assetType.value, page.value)
    marketPage.value = {
      page: response?.page || page.value,
      size: response?.size || 15,
      total: response?.total || 0,
      totalPages: response?.totalPages || 0,
      items: Array.isArray(response?.items) ? response.items : []
    }
  } catch (error) {
    loadError.value = normalizeApiError(error, 'Failed to load market instruments.')
    marketPage.value = { page: page.value, size: 15, total: 0, totalPages: 0, items: [] }
  } finally {
    loading.value = false
  }
}

const isAlreadyHeld = (symbol) => existingSymbols.value.has(`${symbol || ''}`.trim().toUpperCase())

const openAdd = (row) => {
  selectedInstrument.value = row
  addModalOpen.value = true
}

const submitAdd = async (payload) => {
  if (!selectedInstrument.value) return
  const row = selectedInstrument.value
  try {
    await store.createHolding({
      symbol: row.symbol,
      companyName: row.companyName,
      assetType: row.assetType,
      shares: payload.shares,
      costBasis: payload.costBasis,
      currency: row.currency || defaultCurrency.value
    })
    addModalOpen.value = false
    selectedInstrument.value = null
    await loadMarketPage()
  } catch {
  }
}

watch(assetType, async () => {
  page.value = 1
  await loadMarketPage()
})

const goPrev = async () => {
  if (!hasPrev.value) return
  page.value -= 1
  await loadMarketPage()
}

const goNext = async () => {
  if (!hasNext.value) return
  page.value += 1
  await loadMarketPage()
}

onMounted(async () => {
  await Promise.allSettled([store.loadAccount(), store.loadHoldings()])
  await loadMarketPage()
})
</script>

<template>
  <section class="page">
    <div class="page-header">
      <h1>Market</h1>
      <div class="toolbar">
        <BaseButton
          :variant="assetType === 'STOCK' ? 'primary' : 'secondary'"
          @click="assetType = 'STOCK'"
        >
          Stock
        </BaseButton>
        <BaseButton
          :variant="assetType === 'BOND' ? 'primary' : 'secondary'"
          @click="assetType = 'BOND'"
        >
          Bond
        </BaseButton>
      </div>
    </div>

    <LoadingSpinner v-if="loading && !rows.length" />
    <BaseCard v-else title="Instruments">
      <p v-if="loadError" class="error-text">{{ loadError }}</p>
      <EmptyState
        v-if="!rows.length"
        title="No instruments found"
        description="Please switch asset type or retry later."
      />
      <BaseTable v-else>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Company Name</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.symbol">
            <td>{{ row.symbol }}</td>
            <td>{{ row.companyName }}</td>
            <td>
              <BaseButton
                :variant="isAlreadyHeld(row.symbol) ? 'secondary' : 'primary'"
                :disabled="isAlreadyHeld(row.symbol)"
                @click="openAdd(row)"
              >
                {{ isAlreadyHeld(row.symbol) ? 'Already In Holdings' : 'Add Into Holdings' }}
              </BaseButton>
            </td>
          </tr>
        </tbody>
      </BaseTable>

      <div class="pagination">
        <BaseButton variant="secondary" :disabled="!hasPrev || loading" @click="goPrev">Prev</BaseButton>
        <span>Page {{ page }} / {{ marketPage.totalPages || 1 }}</span>
        <BaseButton variant="secondary" :disabled="!hasNext || loading" @click="goNext">Next</BaseButton>
      </div>
    </BaseCard>

    <MarketAddHoldingModal
      v-model="addModalOpen"
      :instrument="selectedInstrument"
      :loading="store.loading.mutateHolding"
      :default-currency="defaultCurrency"
      @submit="submitAdd"
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
  flex-wrap: wrap;
  gap: 0.6rem;
}

.page-header h1 {
  margin: 0;
}

.toolbar {
  display: flex;
  gap: 0.5rem;
}

.pagination {
  margin-top: 0.9rem;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 0.7rem;
}

.error-text {
  margin: 0 0 0.7rem;
  color: #c33636;
}
</style>
