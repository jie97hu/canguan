<template>
  <el-tag :type="tagType" effect="light" round>{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AppStatus } from '@/types/ui'

const props = defineProps<{
  value: AppStatus
  enabledText?: string
  disabledText?: string
}>()

const label = computed(() => {
  if (props.value === 'DELETED') return '已删除'
  if (props.value === 'NORMAL') return props.enabledText ?? '正常'
  return props.value === 'ENABLED' ? props.enabledText ?? '启用' : props.disabledText ?? '停用'
})

const tagType = computed(() => {
  if (props.value === 'DELETED') return 'info'
  if (props.value === 'NORMAL') return 'success'
  return props.value === 'ENABLED' ? 'success' : 'warning'
})
</script>
