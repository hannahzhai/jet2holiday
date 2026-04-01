<script setup>
import BaseCard from '../common/BaseCard.vue'
import BaseTable from '../common/BaseTable.vue'
import BaseButton from '../common/BaseButton.vue'
import EmptyState from '../common/EmptyState.vue'
import { formatCurrency, formatPercent, profitLossClass } from '../../utils/formatters'

defineProps({
  rows: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  currency: { type: String, default: 'USD' }
})

defineEmits(['edit', 'delete'])
</script>

<template>
  <BaseCard title="Holdings">
    <EmptyState v-if="!rows.length && !loading" title="No holdings found" description="Add your first holding to get started." />
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
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in rows" :key="item.id">
          <td>{{ item.symbol }}</td>
          <td>{{ item.companyName }}</td>
          <td>{{ item.assetType }}</td>
          <td>{{ Number(item.shares ?? 0).toFixed(4) }}</td>
          <td>{{ formatCurrency(item.costBasis, item.currency || currency) }}</td>
          <td>{{ item.currentPrice != null ? formatCurrency(item.currentPrice, item.currency || currency) : '-' }}</td>
          <td>{{ item.marketValue != null ? formatCurrency(item.marketValue, item.currency || currency) : '-' }}</td>
          <td :class="profitLossClass(item.profitLoss)">
            <div>{{ item.profitLoss != null ? formatCurrency(item.profitLoss, item.currency || currency) : '-' }}</div>
            <small v-if="item.profitLossPercent != null">{{ formatPercent(item.profitLossPercent) }}</small>
          </td>
          <td class="actions">
            <BaseButton variant="secondary" @click="$emit('edit', item)">Edit</BaseButton>
            <BaseButton variant="danger" @click="$emit('delete', item)">Delete</BaseButton>
          </td>
        </tr>
      </tbody>
    </BaseTable>
  </BaseCard>
</template>

<style scoped>
.actions {
  display: flex;
  gap: 0.45rem;
}
</style>