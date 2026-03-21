<template>
  <div class="bar-chart">
    <div v-for="item in data" :key="item.label" class="bar-row">
      <div class="bar-label">{{ item.label }}</div>
      <div class="bar-track">
        <div class="bar-fill" :style="{ width: `${item.percentage}%`, background: item.color }" />
      </div>
      <div class="bar-meta">
        <span>{{ valueFormatter(item.value) }}</span>
        <span>{{ item.percentage }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  data: Array<{
    label: string
    value: number
    percentage: number
    color: string
  }>
  formatValue?: (value: number) => string
}>()

const valueFormatter = computed(() => props.formatValue ?? ((value: number) => `${value.toLocaleString('zh-CN')}`))
</script>

<style scoped>
.bar-chart {
  display: grid;
  gap: 14px;
}

.bar-row {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) 96px;
  gap: 12px;
  align-items: center;
}

.bar-label {
  font-size: 13px;
  color: #17203d;
  font-weight: 600;
}

.bar-track {
  height: 12px;
  background: rgba(28, 39, 72, 0.08);
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: inherit;
}

.bar-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6a748f;
}

@media (max-width: 960px) {
  .bar-row {
    grid-template-columns: 1fr;
  }
}
</style>
