<script setup>
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import BaseCard from '../common/BaseCard.vue'
import BaseButton from '../common/BaseButton.vue'
import EmptyState from '../common/EmptyState.vue'
import { formatDate } from '../../utils/formatters'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps({
  points: { type: Array, default: () => [] },
  range: { type: String, default: '1M' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['change-range'])
const ranges = ['1W', '1M', '3M', '1Y']

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 16, right: 16, top: 20, bottom: 30, containLabel: true },
  xAxis: {
    type: 'category',
    data: props.points.map((point) => formatDate(point.date))
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: 'Total Assets',
      type: 'line',
      data: props.points.map((point) => Number(point.totalAssets ?? 0)),
      smooth: true,
      showSymbol: false,
      areaStyle: {
        opacity: 0.1
      }
    }
  ]
}))
</script>

<template>
  <BaseCard title="Historical Portfolio Performance">
    <div class="range-buttons">
      <BaseButton
        v-for="item in ranges"
        :key="item"
        :variant="item === range ? 'primary' : 'secondary'"
        :disabled="loading"
        @click="emit('change-range', item)"
      >
        {{ item }}
      </BaseButton>
    </div>
    <EmptyState
      v-if="!points.length && !loading"
      title="No performance data"
      description="Try a different range or refresh market data."
    />
    <VChart v-else class="chart" :option="option" autoresize />
  </BaseCard>
</template>

<style scoped>
.range-buttons {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.9rem;
}

.chart {
  height: 300px;
}
</style>