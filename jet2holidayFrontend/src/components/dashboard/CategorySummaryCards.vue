<script setup>
import BaseCard from '../common/BaseCard.vue'
import { formatCurrency } from '../../utils/formatters'

defineProps({
  categorySummary: { type: Object, default: null },
  currency: { type: String, default: 'USD' }
})

const categories = [
  { key: 'cash', label: 'Cash' },
  { key: 'stocks', label: 'Stocks' },
  { key: 'bonds', label: 'Bonds' },
  { key: 'crypto', label: 'Crypto' }
]
</script>

<template>
  <div class="grid">
    <BaseCard v-for="item in categories" :key="item.key">
      <p class="label">{{ item.label }}</p>
      <h4>{{ formatCurrency(categorySummary?.[item.key], currency) }}</h4>
    </BaseCard>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  gap: 0.8rem;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
}

.label {
  margin: 0;
  color: #6a7390;
  font-size: 0.8rem;
}

h4 {
  margin: 0.4rem 0 0;
  font-size: 1rem;
}
</style>