<template>
  <div class="empty-state">
    <el-empty :description="description">
      <slot />
    </el-empty>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description: string
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 180px;
}
</style>
