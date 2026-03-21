<template>
  <el-card shadow="never" class="chart-card">
    <div class="chart-head">
      <div>
        <div class="chart-title">{{ title }}</div>
        <div v-if="description" class="chart-description">{{ description }}</div>
      </div>
      <div v-if="$slots.actions" class="chart-actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="chart-body">
      <slot />
    </div>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<style scoped>
.chart-card {
  border-radius: 18px;
  border: 1px solid rgba(28, 39, 72, 0.08);
  background: rgba(255, 255, 255, 0.88);
}

.chart-card :deep(.el-card__body) {
  padding: 20px;
}

.chart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 700;
  color: #17203d;
}

.chart-description {
  margin-top: 5px;
  color: #75809d;
  font-size: 12px;
}

.chart-actions {
  display: flex;
  gap: 8px;
}

.chart-body {
  min-height: 60px;
}

@media (max-width: 720px) {
  .chart-card {
    border-radius: 16px;
  }

  .chart-card :deep(.el-card__body) {
    padding: 16px;
  }

  .chart-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-actions {
    width: 100%;
  }

  .chart-actions :deep(.el-button),
  .chart-actions :deep(.el-select) {
    width: 100%;
  }
}
</style>
