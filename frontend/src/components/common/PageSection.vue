<template>
  <el-card shadow="never" class="page-section">
    <div class="section-head">
      <div>
        <div class="section-title">{{ title }}</div>
        <div v-if="description" class="section-description">{{ description }}</div>
      </div>
      <div v-if="$slots.actions" class="section-actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="section-body">
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
.page-section {
  border: 1px solid rgba(28, 39, 72, 0.08);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(14px);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #17203d;
}

.section-description {
  margin-top: 6px;
  font-size: 13px;
  color: #6a748f;
}

.section-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.section-body {
  min-height: 20px;
}
</style>
