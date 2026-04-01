<script setup>
import BaseCard from '../common/BaseCard.vue'
import EmptyState from '../common/EmptyState.vue'

const props = defineProps({
  insight: { type: Object, default: null }
})

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return `${value}`
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}
</script>

<template>
  <BaseCard title="AI Portfolio Insight">
    <EmptyState
      v-if="!props.insight?.insight"
      title="No AI insight yet"
      description='Click "Generate AI Insight" to produce a portfolio summary.'
    />
    <div v-else class="insight-body">
      <pre class="insight-text">{{ props.insight.insight }}</pre>
      <div class="insight-meta">
        <span>Model: {{ props.insight.model || 'N/A' }}</span>
        <span>Generated: {{ formatDateTime(props.insight.generatedAt) }}</span>
        <span v-if="props.insight.fallbackUsed" class="fallback">Fallback used</span>
      </div>
    </div>
  </BaseCard>
</template>

<style scoped>
.insight-body {
  display: grid;
  gap: 0.65rem;
}

.insight-text {
  margin: 0;
  padding: 0;
  white-space: pre-wrap;
  font-family: inherit;
  line-height: 1.45;
}

.insight-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.8rem;
  color: #6a7390;
  font-size: 0.82rem;
}

.fallback {
  color: #b56a00;
  font-weight: 600;
}
</style>
