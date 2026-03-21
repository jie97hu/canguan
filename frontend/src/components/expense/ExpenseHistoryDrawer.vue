<template>
  <el-drawer
    v-model="visible"
    title="支出历史"
    :size="drawerSize"
    class="expense-history-drawer"
    destroy-on-close
  >
    <div v-if="record" class="history-summary">
      <div class="history-title">{{ record.itemName }}</div>
      <div class="history-subtitle">
        {{ record.storeName }} · {{ record.expenseDate }} · {{ record.categoryLevel1Name }} / {{ record.categoryLevel2Name }}
      </div>
    </div>
    <el-timeline v-if="history.length" class="history-timeline">
      <el-timeline-item v-for="item in history" :key="item.id" :timestamp="item.operateAt" :type="timelineType(item.action)">
        <div class="history-card">
          <div class="history-header">
            <span class="history-action">{{ actionText(item.action) }}</span>
            <span class="history-operator">{{ item.operator }}</span>
          </div>
          <div class="history-body">
            <div>
              <div class="history-label">修改前</div>
              <div class="history-text">{{ item.beforeText }}</div>
            </div>
            <div>
              <div class="history-label">修改后</div>
              <div class="history-text">{{ item.afterText }}</div>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无历史记录" />
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import type { ExpenseRecordDto } from '@/types/expense'
import type { ExpenseHistoryEntry } from '@/types/ui'

const props = defineProps<{
  modelValue: boolean
  record: ExpenseRecordDto | null
  history: ExpenseHistoryEntry[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const appStore = useAppStore()
const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})
const drawerSize = computed(() => (appStore.isMobile ? '100%' : '560px'))

function actionText(action: ExpenseHistoryEntry['action']) {
  return action === 'CREATE' ? '新增' : action === 'UPDATE' ? '修改' : '删除'
}

function timelineType(action: ExpenseHistoryEntry['action']) {
  return action === 'DELETE' ? 'danger' : action === 'UPDATE' ? 'warning' : 'success'
}
</script>

<style scoped>
.expense-history-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
}

.history-summary {
  padding: 0 4px 18px;
  border-bottom: 1px solid rgba(28, 39, 72, 0.08);
  margin-bottom: 18px;
}

.history-title {
  font-size: 18px;
  font-weight: 800;
  color: #17203d;
}

.history-subtitle {
  margin-top: 6px;
  color: #6a748f;
  font-size: 12px;
}

.history-timeline {
  padding-top: 8px;
}

.history-card {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(247, 249, 255, 0.88);
  border: 1px solid rgba(28, 39, 72, 0.08);
}

.history-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.history-action {
  font-weight: 700;
  color: #17203d;
}

.history-operator {
  color: #6a748f;
  font-size: 12px;
}

.history-body {
  display: grid;
  gap: 10px;
}

.history-label {
  font-size: 12px;
  color: #8a93ad;
  margin-bottom: 4px;
}

.history-text {
  color: #26314f;
  line-height: 1.6;
}

@media (max-width: 720px) {
  .expense-history-drawer :deep(.el-drawer__header) {
    padding: 16px 16px 0;
  }

  .expense-history-drawer :deep(.el-drawer__body) {
    padding: 12px 16px 16px;
  }

  .history-summary {
    padding-inline: 0;
  }

  .history-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
