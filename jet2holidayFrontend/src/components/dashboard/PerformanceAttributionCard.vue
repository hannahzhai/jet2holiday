<script setup>
import BaseCard from '../common/BaseCard.vue';
import EmptyState from '../common/EmptyState.vue';
import { formatCurrency, formatPercent, profitLossClass } from '../../utils/formatters';

defineProps({
  attribution: { type: Object, default: null },
  currency: { type: String, default: 'USD' },
});
</script>

<template>
  <BaseCard title="Performance Attribution">
    <EmptyState v-if="!attribution" title="No attribution data" description="Attribution data will appear when summary data is available." />
    <div v-else class="grid">
      <div class="panel">
        <h4>Top Contributors</h4>
        <p v-if="!attribution.topContributors?.length" class="empty">No positive contributors.</p>
        <ul v-else>
          <li v-for="item in attribution.topContributors" :key="`c-${item.symbol}`">
            <div>
              <strong>{{ item.symbol }}</strong>
              <small>{{ item.companyName }}</small>
            </div>
            <div class="value positive">
              <span>{{ formatCurrency(item.profitLoss, currency) }}</span>
              &nbsp;
              <small>{{ formatPercent(item.contributionPercent) }}</small>
            </div>
          </li>
        </ul>
      </div>

      <div class="panel">
        <h4>Top Detractors</h4>
        <p v-if="!attribution.topDetractors?.length" class="empty">No negative detractors.</p>
        <ul v-else>
          <li v-for="item in attribution.topDetractors" :key="`d-${item.symbol}`">
            <div>
              <strong>{{ item.symbol }}</strong>
              <small>{{ item.companyName }}</small>
            </div>
            <div class="value negative">
              <span>{{ formatCurrency(item.profitLoss, currency) }}</span>
              <small>{{ formatPercent(item.contributionPercent) }}</small>
            </div>
          </li>
        </ul>
      </div>

      <div class="panel">
        <h4>Performance by Asset Type</h4>
        <ul>
          <li v-for="item in attribution.byAssetType || []" :key="`a-${item.assetType}`">
            <div>
              <strong>{{ item.assetType?.toUpperCase() }}</strong>
            </div>
            <div class="value" :class="profitLossClass(item.profitLoss)">
              <span>{{ formatCurrency(item.profitLoss, currency) }}</span>
              &nbsp;
              <small>{{ formatPercent(item.contributionPercent) }}</small>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </BaseCard>
</template>

<style scoped>
.grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.panel h4 {
  margin: 0 0 0.6rem;
  font-size: 0.95rem;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 0.55rem;
}

li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.6rem;
  padding: 0.45rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: 0.55rem;
}

strong {
  display: block;
}

small {
  color: #6a7390;
}

.value {
  text-align: right;
}

.empty {
  color: #6a7390;
  margin: 0.2rem 0;
}
</style>
