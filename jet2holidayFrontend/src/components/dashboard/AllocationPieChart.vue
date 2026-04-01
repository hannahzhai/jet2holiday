<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { LegendComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import BaseCard from '../common/BaseCard.vue'
import EmptyState from '../common/EmptyState.vue'

use([CanvasRenderer, PieChart, LegendComponent, TooltipComponent])

const props = defineProps({
  allocation: { type: Object, default: null },
  categorySummary: { type: Object, default: null }
})

const chartData = computed(() => {
  const mapping = [
    { key: 'stocks', label: 'STOCK' },
    { key: 'bonds', label: 'BOND' },
    { key: 'crypto', label: 'CRYPTO' },
    { key: 'cash', label: 'CASH' }
  ]
  if (props.allocation) {
    return mapping.map((item) => ({ name: item.label, value: Number(props.allocation[item.key] ?? 0) }))
  }
  if (!props.categorySummary) return []
  const total = Object.values(props.categorySummary).reduce((sum, value) => sum + Number(value || 0), 0)
  if (!total) return []
  return mapping.map((item) => ({
    name: item.label,
    value: ((Number(props.categorySummary[item.key] ?? 0) / total) * 100).toFixed(2)
  }))
})

const option = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['45%', '70%'],
      data: chartData.value,
      label: {
        formatter: '{b}: {d}%'
      }
    }
  ]
}))
</script>

<template>
  <BaseCard title="Allocation by Asset Type">
    <EmptyState
      v-if="!chartData.length"
      title="No allocation data"
      description="Allocation chart appears when summary data is available."
    />
    <VChart v-else class="chart" :option="option" autoresize />
  </BaseCard>
</template>

<style scoped>
.chart {
  height: 300px;
}
</style>