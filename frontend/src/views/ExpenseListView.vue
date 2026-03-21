<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Expense Ledger"
      title="支出记账"
      description="围绕门店、日期和分类快速筛选支出明细，支持新增、编辑、删除和历史记录查看。"
    >
      <template #meta>
        <el-tag v-for="item in summaryTags" :key="item" effect="light" round>{{ item }}</el-tag>
      </template>
      <template #actions>
        <el-button @click="resetFilters">重置筛选</el-button>
        <el-button type="primary" @click="openCreate">新增支出</el-button>
      </template>
    </PageHero>

    <div class="metric-grid">
      <MetricCard v-for="item in visibleMetrics" :key="item.label" v-bind="item" />
    </div>

    <PageSection title="支出明细" description="当前列表支持分页、合计和历史记录查看。">
      <ExpenseFilterBar
        v-model="query"
        :stores="filterStores"
        :categories="categories"
        @search="applyFilters"
        @reset="resetFilters"
      />

      <div class="table-header">
        <div class="table-summary">
          当前合计金额：<strong>{{ formatCurrency(totalAmount) }}</strong>
        </div>
        <div class="table-actions">
          <el-input
            v-model="quickKeyword"
            placeholder="快速搜索品项"
            clearable
            class="quick-search"
            @change="applyQuickSearch"
          />
        </div>
      </div>

      <el-table v-loading="loading" :data="records" border stripe height="560">
        <el-table-column prop="expenseDate" label="日期" width="110" />
        <el-table-column prop="storeName" label="门店" width="130" />
        <el-table-column label="一级/二级分类" min-width="180">
          <template #default="{ row }">
            {{ row.categoryLevel1Name }} / {{ row.categoryLevel2Name }}
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="品项名称" min-width="150" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">{{ formatCurrency(Number(row.amount)) }}</template>
        </el-table-column>
        <el-table-column label="数量/单位" width="120">
          <template #default="{ row }">
            {{ row.quantity ?? '-' }} {{ row.unit || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="120" />
        <el-table-column label="状态" width="90">
          <template #default>
            <StatusTag value="NORMAL" />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="180" prop="updatedAt" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openHistory(row)">历史</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @current-change="loadExpenses"
          @size-change="handlePageSizeChange"
        />
      </div>
    </PageSection>

    <ExpenseFormDrawer
      v-model="drawerVisible"
      :title="drawerTitle"
      :stores="formStores"
      :categories="categories"
      :store-locked="isClerk"
      :loading="drawerSaving"
      :initial-value="editingForm"
      @submit="saveExpense"
    />

    <ExpenseHistoryDrawer v-model="historyVisible" :record="selectedRow" :history="selectedHistory" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { createExpenseTemplate, flattenCategoryTree, formatCurrency, mapExpenseRecordToForm, mapHistoryEntry } from '@/app/canguan'
import PageHero from '@/components/common/PageHero.vue'
import PageSection from '@/components/common/PageSection.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import ExpenseFilterBar from '@/components/expense/ExpenseFilterBar.vue'
import ExpenseFormDrawer from '@/components/expense/ExpenseFormDrawer.vue'
import ExpenseHistoryDrawer from '@/components/expense/ExpenseHistoryDrawer.vue'
import { listCategoryTreeApi, listStoresApi } from '@/api/catalog'
import { createExpenseApi, deleteExpenseApi, listExpenseHistoryApi, listExpensesApi, updateExpenseApi } from '@/api/expense'
import { useAuthStore } from '@/stores/auth'
import type { CategoryNodeDto } from '@/types/category'
import type { ExpenseRecordDto, ExpenseUpsertReq } from '@/types/expense'
import type { StoreDto } from '@/types/store'
import type { ExpenseFilterModel, ExpenseFormModel, ExpenseHistoryEntry, MetricCardItem } from '@/types/ui'

function resolveDefaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  const toText = (value: Date) => value.toISOString().slice(0, 10)
  return [toText(start), toText(end)]
}

const authStore = useAuthStore()
const loading = ref(false)
const drawerSaving = ref(false)
const stores = ref<StoreDto[]>([])
const categoryTree = ref<CategoryNodeDto[]>([])
const categories = computed(() => flattenCategoryTree(categoryTree.value))
const records = ref<ExpenseRecordDto[]>([])
const total = ref(0)
const totalAmount = ref(0)
const query = ref<ExpenseFilterModel>({
  storeId: '',
  dateRange: resolveDefaultDateRange(),
  categoryLevel1Id: '',
  categoryLevel2Id: '',
  itemName: '',
})
const quickKeyword = ref('')
const pageNo = ref(1)
const pageSize = ref(10)
const drawerVisible = ref(false)
const historyVisible = ref(false)
const selectedRow = ref<ExpenseRecordDto | null>(null)
const selectedHistory = ref<ExpenseHistoryEntry[]>([])
const editingForm = ref<Partial<ExpenseFormModel> | null>(null)
const drawerTitle = ref('新增支出')

const isClerk = computed(() => authStore.role === 'CLERK')
const clerkStoreId = computed(() => authStore.userInfo?.storeId ?? '')
const clerkStoreName = computed(() => authStore.userInfo?.storeName ?? '')
const summaryTags = computed(() => [
  isClerk.value ? '录入员仅看本店' : '老板可跨店查看',
  '分类快照保留历史',
  '删除采用逻辑删除',
])
const filterStores = computed<StoreDto[]>(() => {
  if (isClerk.value && clerkStoreId.value) {
    return [
      {
        id: clerkStoreId.value,
        name: clerkStoreName.value || '当前分店',
        code: '',
        status: 'ENABLED',
        createdAt: '',
        updatedAt: '',
      },
    ]
  }
  return stores.value
})
const formStores = computed(() => {
  if (isClerk.value) {
    return filterStores.value
  }
  return stores.value
})
const visibleMetrics = computed<MetricCardItem[]>(() => [
  {
    label: '当前筛选金额',
    value: formatCurrency(totalAmount.value),
    hint: '后端按同口径汇总返回',
    trend: `${total.value} 条`,
    tone: 'primary',
  },
  {
    label: '当前页记录',
    value: `${records.value.length} 条`,
    hint: `第 ${pageNo.value} 页 / 每页 ${pageSize.value} 条`,
    trend: `${records.value.length}`,
    tone: 'success',
  },
  {
    label: '数据范围',
    value: isClerk.value ? '本店' : '全部门店',
    hint: isClerk.value ? `${clerkStoreName.value || '当前分店'} 数据隔离` : '老板视角可跨店筛选',
    trend: isClerk.value ? 'CLERK' : 'OWNER',
    tone: 'warning',
  },
  {
    label: '时间范围',
    value: query.value.dateRange.length === 2 ? `${query.value.dateRange[0]} ~ ${query.value.dateRange[1]}` : '未限定',
    hint: '列表与合计同步刷新',
    trend: '实时查询',
    tone: 'danger',
  },
])

async function loadBaseData() {
  const categoryPromise = listCategoryTreeApi()
  const storePromise = isClerk.value
    ? Promise.resolve(filterStores.value)
    : listStoresApi({
        pageNo: 1,
        pageSize: 200,
      }).then((result) => result.list)

  const [categoryResult, storeResult] = await Promise.all([categoryPromise, storePromise])
  categoryTree.value = categoryResult
  stores.value = storeResult

  if (isClerk.value && clerkStoreId.value) {
    query.value.storeId = clerkStoreId.value
  }
}

// 支出列表直接按后端分页和金额汇总返回，避免前端二次筛选破坏统计口径。
async function loadExpenses() {
  loading.value = true
  try {
    const result = await listExpensesApi({
      storeId: isClerk.value ? clerkStoreId.value : query.value.storeId,
      dateStart: query.value.dateRange[0] || undefined,
      dateEnd: query.value.dateRange[1] || undefined,
      categoryLevel1Id: query.value.categoryLevel1Id || '',
      categoryLevel2Id: query.value.categoryLevel2Id || '',
      itemName: query.value.itemName || undefined,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    })
    records.value = result.list
    total.value = result.total
    totalAmount.value = Number(result.extra?.totalAmount ?? 0)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载支出失败')
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  pageNo.value = 1
  query.value.itemName = quickKeyword.value.trim()
  loadExpenses()
}

function applyQuickSearch() {
  query.value.itemName = quickKeyword.value.trim()
  applyFilters()
}

function resetFilters() {
  query.value = {
    storeId: isClerk.value ? clerkStoreId.value : '',
    dateRange: resolveDefaultDateRange(),
    categoryLevel1Id: '',
    categoryLevel2Id: '',
    itemName: '',
  }
  quickKeyword.value = ''
  pageNo.value = 1
  loadExpenses()
}

function openCreate() {
  drawerTitle.value = '新增支出'
  selectedRow.value = null
  editingForm.value = createExpenseTemplate({
    stores: formStores.value,
    categories: categories.value,
    storeId: isClerk.value ? clerkStoreId.value : formStores.value.find((item) => item.status === 'ENABLED')?.id ?? formStores.value[0]?.id ?? '',
  })
  drawerVisible.value = true
}

function openEdit(row: ExpenseRecordDto) {
  drawerTitle.value = '编辑支出'
  selectedRow.value = row
  editingForm.value = mapExpenseRecordToForm(row)
  drawerVisible.value = true
}

async function saveExpense(form: ExpenseFormModel) {
  drawerSaving.value = true
  try {
    const payload: ExpenseUpsertReq = {
      storeId: Number(isClerk.value ? clerkStoreId.value : form.storeId),
      expenseDate: form.expenseDate,
      categoryLevel1Id: Number(form.categoryLevel1Id),
      categoryLevel2Id: Number(form.categoryLevel2Id),
      itemName: form.itemName,
      amount: Number(form.amount ?? 0),
      quantity: form.quantity == null ? null : Number(form.quantity),
      unit: form.unit,
      remark: form.remark,
    }
    if (selectedRow.value) {
      await updateExpenseApi(selectedRow.value.id, payload)
      ElMessage.success('支出已更新')
    } else {
      await createExpenseApi(payload)
      ElMessage.success('支出已新增')
    }
    drawerVisible.value = false
    await loadExpenses()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存支出失败')
  } finally {
    drawerSaving.value = false
  }
}

async function openHistory(row: ExpenseRecordDto) {
  selectedRow.value = row
  historyVisible.value = true
  try {
    const histories = await listExpenseHistoryApi(row.id)
    selectedHistory.value = histories.map(mapHistoryEntry)
  } catch (error) {
    selectedHistory.value = []
    ElMessage.error(error instanceof Error ? error.message : '加载历史记录失败')
  }
}

async function deleteRow(row: ExpenseRecordDto) {
  try {
    await ElMessageBox.confirm(`确认删除支出「${row.itemName}」？`, '删除支出', { type: 'warning' })
    await deleteExpenseApi(row.id)
    ElMessage.success('支出已删除')
    await loadExpenses()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error instanceof Error ? error.message : '删除支出失败')
  }
}

function handlePageSizeChange(size: number) {
  pageSize.value = size
  pageNo.value = 1
  loadExpenses()
}

onMounted(async () => {
  try {
    await loadBaseData()
    await loadExpenses()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '初始化支出页失败')
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 20px;
  display: grid;
  gap: 18px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.table-header {
  margin: 14px 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-summary {
  color: #17203d;
}

.table-actions {
  display: flex;
  justify-content: flex-end;
  flex: 1;
}

.quick-search {
  width: 280px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1280px) {
  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }

  .table-header {
    flex-direction: column;
    align-items: stretch;
  }

  .quick-search {
    width: 100%;
  }
}
</style>
