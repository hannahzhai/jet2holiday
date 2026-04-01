<script setup>
import BaseCard from '../common/BaseCard.vue'
import BaseTable from '../common/BaseTable.vue'
import EmptyState from '../common/EmptyState.vue'
import { formatCurrency, formatPercent, profitLossClass } from '../../utils/formatters'

defineProps({
  items: { type: Array, default: () => [] },
  currency: { type: String, default: 'USD' }
})
</script>

<template>
  <BaseCard title="Holdings Overview">
    <EmptyState
      v-if="!items.length"
      title="No holdings yet"
      description="Add holdings in the Holdings page to see overview data."
    />
    <BaseTable v-else>
      <thead>
        <tr>
          <th>Symbol</th>
          <th>Company</th>
          <th>Asset Type</th>
          <th>Shares</th>
          <th>Cost Basis</th>
          <th>Current Price</th>
          <th>Market Value</th>
          <th>Profit/Loss</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in items" :key="row.id">
          <td>{{ row.symbol }}</td>
          <td>{{ row.companyName }}</td>
          <td>{{ row.assetType }}</td>
          <td>{{ Number(row.shares ?? 0).toFixed(4) }}</td>
          <td>{{ formatCurrency(row.costBasis, row.currency || currency) }}</td>
          <td>{{ row.currentPrice != null ? formatCurrency(row.currentPrice, row.currency || currency) : '-' }}</td>
          <td>{{ row.marketValue != null ? formatCurrency(row.marketValue, row.currency || currency) : '-' }}</td>
          <td :class="profitLossClass(row.profitLoss)">
            <div>{{ row.profitLoss != null ? formatCurrency(row.profitLoss, row.currency || currency) : '-' }}</div>
            <small v-if="row.profitLossPercent != null">{{ formatPercent(row.profitLossPercent) }}</small>
          </td>
        </tr>
      </tbody>
    </BaseTable>
  </BaseCard>
</template>