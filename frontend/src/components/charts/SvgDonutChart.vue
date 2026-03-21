<template>
  <div class="donut-chart">
    <svg viewBox="0 0 240 240" role="img" aria-label="donut chart">
      <circle cx="120" cy="120" r="82" fill="none" stroke="rgba(28, 39, 72, 0.08)" stroke-width="28" />
      <circle
        v-for="segment in segments"
        :key="segment.label"
        cx="120"
        cy="120"
        r="82"
        fill="none"
        :stroke="segment.color"
        stroke-width="28"
        stroke-linecap="round"
        :stroke-dasharray="segment.dashArray"
        :stroke-dashoffset="segment.dashOffset"
        transform="rotate(-90 120 120)"
      />
      <text x="120" y="112" text-anchor="middle" class="donut-total">{{ totalText }}</text>
      <text x="120" y="136" text-anchor="middle" class="donut-subtitle">分类占比</text>
    </svg>
    <div class="legend-list">
      <div v-for="item in data" :key="item.label" class="legend-item">
        <span class="legend-dot" :style="{ background: item.color }" />
        <span class="legend-label">{{ item.label }}</span>
        <span class="legend-value">{{ item.value }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DonutChartItem } from '@/types/ui'

const props = defineProps<{
  data: DonutChartItem[]
}>()

const total = computed(() => props.data.reduce((sum, item) => sum + item.value, 0))

const segments = computed(() => {
  const circumference = 2 * Math.PI * 82
  let consumed = 0

  return props.data.map((item) => {
    const length = total.value === 0 ? 0 : (item.value / total.value) * circumference
    const segment = {
      ...item,
      dashArray: `${length} ${circumference - length}`,
      dashOffset: -consumed,
    }
    consumed += length
    return segment
  })
})

const totalText = computed(() => `${total.value}%`)
</script>

<style scoped>
.donut-chart {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  align-items: center;
}

svg {
  width: 240px;
  height: 240px;
}

.donut-total {
  font-size: 28px;
  font-weight: 800;
  fill: #15203c;
}

.donut-subtitle {
  font-size: 12px;
  fill: #6a748f;
}

.legend-list {
  display: grid;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #15203c;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
}

.legend-label {
  flex: 1;
}

.legend-value {
  font-weight: 700;
}

@media (max-width: 960px) {
  .donut-chart {
    grid-template-columns: 1fr;
    justify-items: center;
  }
}
</style>
