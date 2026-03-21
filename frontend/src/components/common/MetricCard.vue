<template>
  <el-card shadow="never" class="metric-card" :class="`tone-${tone}`">
    <div class="metric-top">
      <span class="metric-label">{{ label }}</span>
      <span class="metric-trend">{{ trend }}</span>
    </div>
    <div class="metric-value">{{ value }}</div>
    <div class="metric-hint">{{ hint }}</div>
  </el-card>
</template>

<script setup lang="ts">
import type { MetricCardItem } from '@/types/ui'

defineProps<MetricCardItem>()
</script>

<style scoped>
.metric-card {
  border-radius: 18px;
  border: 1px solid rgba(28, 39, 72, 0.08);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.92), rgba(247, 249, 255, 0.98));
  box-shadow: 0 18px 34px rgba(24, 39, 75, 0.08);
}

.metric-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.metric-label {
  font-size: 13px;
  color: #6a748f;
}

.metric-trend {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(55, 104, 255, 0.1);
  color: #2858ff;
}

.metric-value {
  font-size: 28px;
  font-weight: 800;
  color: #15203c;
  line-height: 1.1;
}

.metric-hint {
  margin-top: 10px;
  font-size: 12px;
  color: #7a839d;
}

.tone-primary .metric-trend {
  background: rgba(40, 88, 255, 0.12);
  color: #2858ff;
}

.tone-success .metric-trend {
  background: rgba(30, 174, 152, 0.12);
  color: #1eae98;
}

.tone-warning .metric-trend {
  background: rgba(227, 107, 44, 0.12);
  color: #e36b2c;
}

.tone-danger .metric-trend {
  background: rgba(212, 72, 89, 0.12);
  color: #d44859;
}

@media (max-width: 720px) {
  .metric-card {
    border-radius: 16px;
  }

  .metric-top {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;
  }

  .metric-trend {
    max-width: 100%;
    word-break: break-word;
  }

  .metric-value {
    font-size: 24px;
  }
}
</style>
