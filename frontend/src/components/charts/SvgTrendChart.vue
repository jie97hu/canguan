<template>
  <div class="trend-chart">
    <svg viewBox="0 0 720 280" role="img" aria-label="trend chart">
      <defs>
        <linearGradient id="trendFill" x1="0" x2="0" y1="0" y2="1">
          <stop offset="0%" stop-color="#2858ff" stop-opacity="0.35" />
          <stop offset="100%" stop-color="#2858ff" stop-opacity="0.02" />
        </linearGradient>
      </defs>
      <g v-for="gridY in [40, 90, 140, 190, 240]" :key="gridY">
        <line x1="24" :y1="gridY" x2="696" :y2="gridY" stroke="rgba(27, 37, 70, 0.08)" />
      </g>
      <path :d="areaPath" fill="url(#trendFill)" />
      <path :d="linePath" fill="none" stroke="#2858ff" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" />
      <g v-for="point in points" :key="point.label">
        <title>{{ point.label }}</title>
        <circle :cx="point.x" :cy="point.y" r="5" fill="#fff" stroke="#2858ff" stroke-width="3" />
        <text
          v-if="point.showLabel"
          :x="point.x"
          y="270"
          text-anchor="middle"
          fill="#6a748f"
          font-size="11"
        >
          {{ point.displayLabel }}
        </text>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TrendChartPoint } from '@/types/ui'

const props = defineProps<{
  data: TrendChartPoint[]
}>()

const width = 672
const height = 200
const maxAxisLabels = 7

function formatAxisLabel(label: string) {
  if (/^\d{4}-\d{2}-\d{2}$/.test(label)) {
    return label.slice(5)
  }
  return label
}

const points = computed(() => {
  const maxValue = Math.max(...props.data.map((item) => item.value), 1)
  const labelStep = props.data.length <= maxAxisLabels ? 1 : Math.ceil((props.data.length - 1) / (maxAxisLabels - 1))
  return props.data.map((item, index) => {
    const step = props.data.length <= 1 ? 0 : width / (props.data.length - 1)
    const x = 24 + step * index
    const y = 240 - (item.value / maxValue) * height
    const isLastPoint = index === props.data.length - 1
    const showLabel = index === 0 || isLastPoint || index % labelStep === 0
    return {
      ...item,
      x,
      y,
      showLabel,
      displayLabel: formatAxisLabel(item.label),
    }
  })
})

const linePath = computed(() => {
  return points.value
    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`)
    .join(' ')
})

const areaPath = computed(() => {
  if (!points.value.length) return ''
  const first = points.value[0]
  const last = points.value[points.value.length - 1]
  return `${linePath.value} L ${last.x} 240 L ${first.x} 240 Z`
})
</script>

<style scoped>
.trend-chart {
  width: 100%;
  overflow: hidden;
}

svg {
  width: 100%;
  height: auto;
  display: block;
}
</style>
