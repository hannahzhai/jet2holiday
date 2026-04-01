<script setup>
import BaseCard from '../common/BaseCard.vue'
import { formatCurrency, formatPercent, profitLossClass } from '../../utils/formatters'

const props = defineProps({
  summary: { type: Object, default: null },
  currency: { type: String, default: 'USD' }
})

const summaryItems = [
  { key: 'cashBalance', label: 'Cash Balance', type: 'currency' },
  { key: 'totalAssets', label: 'Total Assets', type: 'currency' },
  { key: 'totalMarketValue', label: 'Total Market Value', type: 'currency' },
  { key: 'totalCost', label: 'Total Cost', type: 'currency' },
  { key: 'totalProfitLoss', label: 'Total Profit/Loss', type: 'currency', pnl: true },
  { key: 'totalProfitLossPercent', label: 'Total Profit/Loss %', type: 'percent', pnl: true }
]

const displayValue = (item) => {
  const value = props.summary?.[item.key]
  if (item.type === 'percent') return formatPercent(value)
  return formatCurrency(value, props.currency)
}
</script>

<template>
  <div class="summary-grid">
    <BaseCard v-for="item in summaryItems" :key="item.key">
      <p class="label">{{ item.label }}</p>
      <h3 :class="item.pnl ? profitLossClass(summary?.[item.key]) : ''">{{ displayValue(item) }}</h3>
    </BaseCard>
  </div>
</template>

<style scoped>
.summary-grid {
  display: grid;
  gap: 0.8rem;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.label {
  margin: 0;
  color: #6a7390;
  font-size: 0.82rem;
}

h3 {
  margin: 0.4rem 0 0;
  font-size: 1.15rem;
}
</style>