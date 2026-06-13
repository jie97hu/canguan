<template>
  <el-drawer
    v-model="visible"
    :title="title"
    :size="drawerSize"
    class="expense-batch-drawer"
    destroy-on-close
  >
    <div class="expense-batch">
      <el-form ref="sharedFormRef" :model="sharedForm" :rules="sharedRules" label-position="top" class="shared-form">
        <el-form-item label="门店" prop="storeId">
          <el-select v-model="sharedForm.storeId" filterable :disabled="storeLocked" placeholder="选择门店">
            <el-option
              v-for="item in stores"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="item.status === 'DISABLED'"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支出日期" prop="expenseDate">
          <el-date-picker v-model="sharedForm.expenseDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <div class="shared-grid">
          <el-form-item label="一级分类" prop="categoryLevel1Id">
            <el-select v-model="sharedForm.categoryLevel1Id" filterable placeholder="选择一级分类" @change="handleLevel1Change">
              <el-option
                v-for="item in level1Options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
                :disabled="item.status === 'DISABLED'"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="二级分类" prop="categoryLevel2Id">
            <el-select v-model="sharedForm.categoryLevel2Id" filterable placeholder="选择二级分类" @change="handleLevel2Change">
              <el-option
                v-for="item in level2Options"
                :key="item.id"
                :label="item.name"
                :value="item.id"
                :disabled="item.status === 'DISABLED'"
              />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <div class="batch-header">
        <div>
          <div class="batch-title">明细行</div>
          <div class="batch-subtitle">共享门店、日期和分类后，逐行填写品项、金额、数量、单位和备注。</div>
        </div>
        <el-button type="primary" plain @click="appendRow">新增一行</el-button>
      </div>

      <div class="batch-row-list">
        <article v-for="(row, index) in rows" :key="row.key" class="batch-row">
          <div class="batch-row__header">
            <strong>第 {{ index + 1 }} 条</strong>
            <el-button text type="danger" :disabled="rows.length <= 1" @click="removeRow(index)">删除</el-button>
          </div>

          <div class="shared-grid">
            <el-form-item :error="rowErrors[row.key]?.itemName" required label="品项名称" class="batch-form-item">
              <el-select
                v-model="row.itemName"
                filterable
                allow-create
                default-first-option
                clearable
                reserve-keyword
                :loading="itemOptionsLoading"
                placeholder="先下拉筛选，也可手动输入"
              >
                <el-option v-for="item in itemOptionsForRow(row.itemName)" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item :error="rowErrors[row.key]?.amount" required label="金额" class="batch-form-item">
              <el-input-number v-model="row.amount" :min="0" :precision="2" :step="10" controls-position="right" />
            </el-form-item>
          </div>

          <div class="shared-grid">
            <el-form-item label="数量" class="batch-form-item">
              <el-input-number v-model="row.quantity" :min="0" :precision="3" :step="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="单位" class="batch-form-item">
              <el-select
                v-model="row.unit"
                filterable
                allow-create
                clearable
                reserve-keyword
                :loading="unitOptionsLoading"
                placeholder="可选择已有单位，也可手动输入"
              >
                <el-option v-for="item in unitOptionsForRow(row.unit)" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </div>

          <el-form-item label="备注" class="batch-form-item">
            <el-input v-model="row.remark" placeholder="可选备注" />
          </el-form-item>

          <div v-if="row.errorMessage" class="batch-row__error">{{ row.errorMessage }}</div>
        </article>
      </div>
    </div>

    <template #footer>
      <div class="drawer-actions">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitBatch">批量保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

import {
  createExpenseBatchRowTemplate,
  createExpenseBatchSharedTemplate,
  getCategoryById,
  getCategoryChildren,
} from '@/app/canguan'
import { listExpenseItemOptionsApi } from '@/api/expense'
import { listUnitOptionsApi } from '@/api/unit'
import { useAppStore } from '@/stores/app'
import type { CategoryNodeDto } from '@/types/category'
import type { StoreDto } from '@/types/store'
import type { ExpenseBatchRowForm, ExpenseBatchSharedForm } from '@/types/ui'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    stores: StoreDto[]
    categories: CategoryNodeDto[]
    storeLocked?: boolean
    loading?: boolean
    initialStoreId?: number | '' | null
  }>(),
  {
    title: '批量新增支出',
    storeLocked: false,
    loading: false,
    initialStoreId: null,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', value: { shared: ExpenseBatchSharedForm; rows: ExpenseBatchRowForm[] }): void
}>()

const appStore = useAppStore()
const sharedFormRef = ref<FormInstance>()
const itemOptions = ref<string[]>([])
const unitOptions = ref<string[]>([])
const itemOptionsLoading = ref(false)
const unitOptionsLoading = ref(false)
const rowErrors = reactive<Record<string, { itemName?: string; amount?: string }>>({})
let itemOptionsRequestId = 0
let unitOptionsRequestId = 0

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})
const drawerSize = computed(() => (appStore.isMobile ? '100%' : '720px'))

const sharedForm = reactive<ExpenseBatchSharedForm>(createExpenseBatchSharedTemplate({ stores: [], categories: [] }))
const rows = ref<ExpenseBatchRowForm[]>([])

const sharedRules: FormRules<ExpenseBatchSharedForm> = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  expenseDate: [{ required: true, message: '请选择支出日期', trigger: 'change' }],
  categoryLevel1Id: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
  categoryLevel2Id: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
}

const level1Options = computed(() => props.categories.filter((item) => item.level === 1))
const level2Options = computed(() => getCategoryChildren(props.categories, sharedForm.categoryLevel1Id))

function resolveDefaultUnit(categoryId: number | ''): string {
  const category = getCategoryById(props.categories, categoryId)
  return category?.defaultUnit ?? '斤'
}

function resetRows(defaultUnit: string) {
  rows.value = [createExpenseBatchRowTemplate(defaultUnit)]
  Object.keys(rowErrors).forEach((key) => delete rowErrors[key])
}

function itemOptionsForRow(currentItemName: string) {
  const current = currentItemName.trim()
  if (!current || itemOptions.value.includes(current)) {
    return itemOptions.value
  }
  return [current, ...itemOptions.value]
}

function unitOptionsForRow(currentUnit: string) {
  const current = currentUnit.trim()
  if (!current || unitOptions.value.includes(current)) {
    return unitOptions.value
  }
  return [current, ...unitOptions.value]
}

async function loadItemOptions() {
  const requestId = ++itemOptionsRequestId
  if (!visible.value || !sharedForm.categoryLevel1Id || !sharedForm.categoryLevel2Id) {
    itemOptions.value = []
    return
  }

  itemOptionsLoading.value = true
  try {
    const options = await listExpenseItemOptionsApi({
      storeId: sharedForm.storeId || undefined,
      categoryLevel1Id: Number(sharedForm.categoryLevel1Id),
      categoryLevel2Id: Number(sharedForm.categoryLevel2Id),
      limit: 100,
    })
    if (requestId !== itemOptionsRequestId) {
      return
    }
    itemOptions.value = options
  } catch (error) {
    if (requestId !== itemOptionsRequestId) {
      return
    }
    itemOptions.value = []
    ElMessage.error(error instanceof Error ? error.message : '加载品项候选失败')
  } finally {
    if (requestId === itemOptionsRequestId) {
      itemOptionsLoading.value = false
    }
  }
}

async function loadUnitOptions() {
  const requestId = ++unitOptionsRequestId
  if (!visible.value) {
    return
  }
  unitOptionsLoading.value = true
  try {
    const options = await listUnitOptionsApi({ limit: 200 })
    if (requestId !== unitOptionsRequestId) {
      return
    }
    unitOptions.value = options
  } catch (error) {
    if (requestId !== unitOptionsRequestId) {
      return
    }
    // 单位候选失败时仍允许逐行手输，避免阻断批量录入。
    unitOptions.value = []
    ElMessage.error(error instanceof Error ? error.message : '加载单位候选失败')
  } finally {
    if (requestId === unitOptionsRequestId) {
      unitOptionsLoading.value = false
    }
  }
}

function clearRowError(rowKey: string) {
  delete rowErrors[rowKey]
}

function appendRow() {
  rows.value.push(createExpenseBatchRowTemplate(resolveDefaultUnit(sharedForm.categoryLevel2Id)))
}

function removeRow(index: number) {
  if (rows.value.length <= 1) {
    return
  }
  clearRowError(rows.value[index].key)
  rows.value.splice(index, 1)
}

function handleLevel1Change(categoryId: number | '') {
  const next = getCategoryChildren(props.categories, categoryId).find((item) => item.status === 'ENABLED')
  sharedForm.categoryLevel2Id = next?.id ?? ''
  const defaultUnit = next?.defaultUnit ?? resolveDefaultUnit(categoryId)
  rows.value = rows.value.map((row) => ({
    ...row,
    unit: row.unit.trim() || defaultUnit,
    errorMessage: '',
  }))
}

function handleLevel2Change(categoryId: number | '') {
  const defaultUnit = resolveDefaultUnit(categoryId)
  rows.value = rows.value.map((row) => ({
    ...row,
    unit: row.unit.trim() || defaultUnit,
    errorMessage: '',
  }))
}

function isRowEmpty(row: ExpenseBatchRowForm) {
  return !row.itemName.trim() && row.amount == null && row.quantity == null && !row.unit.trim() && !row.remark.trim()
}

function validateRows() {
  let valid = true
  Object.keys(rowErrors).forEach((key) => delete rowErrors[key])

  const nonEmptyRows = rows.value.filter((row) => !isRowEmpty(row))
  if (!nonEmptyRows.length) {
    ElMessage.warning('请至少填写一条明细')
    return { valid: false, rows: [] as ExpenseBatchRowForm[] }
  }

  for (const row of nonEmptyRows) {
    const errors: { itemName?: string; amount?: string } = {}
    if (!row.itemName.trim()) {
      errors.itemName = '请输入品项名称'
    }
    if (row.amount == null || Number(row.amount) <= 0) {
      errors.amount = '请输入大于 0 的金额'
    }
    if (errors.itemName || errors.amount) {
      valid = false
      rowErrors[row.key] = errors
    }
  }

  return { valid, rows: nonEmptyRows }
}

watch(
  () => props.modelValue,
  (value) => {
    if (!value) {
      itemOptions.value = []
      unitOptions.value = []
      return
    }

    const template = createExpenseBatchSharedTemplate({
      stores: props.stores,
      categories: props.categories,
      storeId: props.initialStoreId ?? props.stores[0]?.id ?? '',
    })
    Object.assign(sharedForm, template)
    resetRows(resolveDefaultUnit(template.categoryLevel2Id))
    void loadItemOptions()
    void loadUnitOptions()
  },
  { immediate: true },
)

watch(
  () => [visible.value, sharedForm.storeId, sharedForm.categoryLevel1Id, sharedForm.categoryLevel2Id],
  ([opened]) => {
    if (!opened) {
      return
    }
    // 共享字段变化后，所有行都沿用同一套分类候选，避免每行出现不同口径。
    void loadItemOptions()
  },
)

async function submitBatch() {
  const sharedValid = await sharedFormRef.value?.validate().catch(() => false)
  if (!sharedValid) {
    ElMessage.warning('请先完成共享字段')
    return
  }

  const { valid, rows: nonEmptyRows } = validateRows()
  if (!valid) {
    ElMessage.warning('请先修正明细行中的必填项')
    return
  }

  emit('submit', {
    shared: { ...sharedForm },
    rows: nonEmptyRows.map((row) => ({
      ...row,
      itemName: row.itemName.trim(),
      unit: row.unit.trim(),
      remark: row.remark.trim(),
    })),
  })
}

function applySubmitResult(result: { successKeys: string[]; failures: Array<{ key: string; message: string }> }) {
  const failureMap = new Map(result.failures.map((item) => [item.key, item.message]))
  rows.value = rows.value
    .filter((row) => !result.successKeys.includes(row.key))
    .map((row) => ({
      ...row,
      errorMessage: failureMap.get(row.key) ?? '',
    }))

  Object.keys(rowErrors).forEach((key) => delete rowErrors[key])

  if (!rows.value.length) {
    resetRows(resolveDefaultUnit(sharedForm.categoryLevel2Id))
  }
}

defineExpose({
  applySubmitResult,
})
</script>

<style scoped>
.expense-batch-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
}

.expense-batch-drawer :deep(.el-drawer__body) {
  padding-bottom: 12px;
}

.expense-batch-drawer :deep(.el-drawer__footer) {
  border-top: 1px solid rgba(28, 39, 72, 0.08);
}

.expense-batch {
  display: grid;
  gap: 18px;
}

.shared-form :deep(.el-select),
.shared-form :deep(.el-date-editor),
.batch-form-item :deep(.el-select),
.batch-form-item :deep(.el-input-number) {
  width: 100%;
}

.shared-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.batch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.batch-title {
  font-size: 16px;
  font-weight: 700;
  color: #17203d;
}

.batch-subtitle {
  margin-top: 4px;
  color: #7180a1;
  font-size: 13px;
}

.batch-row-list {
  display: grid;
  gap: 14px;
}

.batch-row {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(28, 39, 72, 0.08);
  background: rgba(248, 250, 255, 0.92);
}

.batch-row__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.batch-row__error {
  margin-top: 4px;
  color: #d14343;
  font-size: 12px;
  line-height: 1.5;
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 960px) {
  .shared-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .expense-batch-drawer :deep(.el-drawer__header) {
    padding: 16px 16px 0;
  }

  .expense-batch-drawer :deep(.el-drawer__body) {
    padding: 12px 16px 16px;
  }

  .expense-batch-drawer :deep(.el-drawer__footer) {
    padding: 12px 16px 16px;
  }

  .batch-header {
    flex-direction: column;
    align-items: stretch;
  }

  .drawer-actions {
    width: 100%;
  }

  .drawer-actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
