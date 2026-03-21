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
        <circle :cx="point.x" :cy="point.y" r="5" fill="#fff" stroke="#2858ff" stroke-width="3" />
        <text :x="point.x" y="270" text-anchor="middle" fill="#6a748f" font-size="12">{{ point.label }}</text>
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

const points = computed(() => {
  const maxValue = Math.max(...props.data.map((item) => item.value), 1)
  return props.data.map((item, index) => {
    const step = props.data.length <= 1 ? 0 : width / (props.data.length - 1)
    const x = 24 + step * index
    const y = 240 - (item.value / maxValue) * height
    return { ...item, x, y }
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
