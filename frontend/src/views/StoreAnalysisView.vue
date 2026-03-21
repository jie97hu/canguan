<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Store Analysis"
      title="分店分析"
      description="按单店维度查看支出趋势、分类占比和高频品项，帮助老板快速定位异常。"
    >
      <template #meta>
        <el-tag effect="light" round>当前门店：{{ currentStore?.name ?? '未选择' }}</el-tag>
        <el-tag effect="light" round type="success">状态：{{ currentStore ? (currentStore.status === 'ENABLED' ? '营业中' : '停用') : '未选择' }}</el-tag>
      </template>
      <template #actions>
        <el-select v-model="selectedStoreId" placeholder="选择门店" class="store-select">
          <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadAnalysis">刷新分析</el-button>
      </template>
    </PageHero>

    <div class="metric-grid">
      <MetricCard v-for="item in analysisMetrics" :key="item.label" v-bind="item" />
    </div>

    <div class="analysis-grid">
      <ChartCard title="门店支出趋势" description="按天展示当前时间范围内的支出变化。">
        <SvgTrendChart :data="trendData" />
      </ChartCard>
      <ChartCard title="一级分类占比" description="查看当前门店的分类结构。">
        <SvgDonutChart :data="categoryData" />
      </ChartCard>
    </div>

    <div class="analysis-grid secondary">
      <ChartCard title="高频品项排行" description="按金额排序，快速识别本店成本大头。">
        <SvgBarChart :data="rankingChart" :format-value="formatCurrency" />
      </ChartCard>
      <PageSection title="最近支出" description="取当前门店最近录入的 5 条支出。">
        <el-table v-loading="loading" :data="recentRows" border stripe>
          <el-table-column prop="expenseDate" label="日期" width="110" />
          <el-table-column prop="categoryLevel1Name" label="一级分类" width="120" />
          <el-table-column prop="categoryLevel2Name" label="二级分类" width="120" />
          <el-table-column prop="itemName" label="品项" min-width="130" />
          <el-table-column label="金额" width="110">
            <template #default="{ row }">{{ formatCurrency(Number(row.amount)) }}</template>
          </el-table-column>
        </el-table>
      </PageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

import { buildBarChart, formatCurrency, mapDonutItems, mapTrendPoints } from '@/app/canguan'
import PageHero from '@/components/common/PageHero.vue'
import PageSection from '@/components/common/PageSection.vue'
import MetricCard from '@/components/common/MetricCard.vue'
import ChartCard from '@/components/common/ChartCard.vue'
import SvgTrendChart from '@/components/charts/SvgTrendChart.vue'
import SvgDonutChart from '@/components/charts/SvgDonutChart.vue'
import SvgBarChart from '@/components/charts/SvgBarChart.vue'
import { listStoresApi } from '@/api/catalog'
import { listExpensesApi } from '@/api/expense'
import { getCategoryBreakdownApi, getItemRankingApi, getOverviewApi, getTrendApi } from '@/api/report'
import type { ExpenseRecordDto } from '@/types/expense'
import type { CategoryBreakdownDto, ItemRankingDto, OverviewDto, TrendPointDto } from '@/types/report'
import type { StoreDto } from '@/types/store'
import type { MetricCardItem } from '@/types/ui'

function resolveDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  const toText = (value: Date) => value.toISOString().slice(0, 10)
  return {
    start: toText(start),
    end: toText(end),
  }
}

const route = useRoute()
const loading = ref(false)
const initialized = ref(false)
const stores = ref<StoreDto[]>([])
const selectedStoreId = ref<number | ''>('')
const overview = ref<OverviewDto | null>(null)
const trendRows = ref<TrendPointDto[]>([])
const categoryRows = ref<CategoryBreakdownDto[]>([])
const rankingRows = ref<ItemRankingDto[]>([])
const recentRows = ref<ExpenseRecordDto[]>([])
const dateRange = resolveDateRange()

const currentStore = computed(() => stores.value.find((item) => item.id === selectedStoreId.value) ?? null)
const trendData = computed(() => mapTrendPoints(trendRows.value.map((item) => ({ label: item.label, amount: Number(item.amount) }))))
const categoryData = computed(() => mapDonutItems(categoryRows.value.map((item) => ({ categoryName: item.categoryName, ratio: Number(item.ratio) }))))
const rankingChart = computed(() =>
  buildBarChart(
    rankingRows.value.map((item) => ({
      label: item.itemName,
      value: Number(item.amount),
    })),
  ),
)

const analysisMetrics = computed<MetricCardItem[]>(() => {
  const data = overview.value
  if (!data) {
    return [
      { label: '今日支出', value: formatCurrency(0), hint: '等待加载数据', trend: '0.00', tone: 'primary' },
      { label: '本月支出', value: formatCurrency(0), hint: '等待加载数据', trend: '0.00', tone: 'success' },
      { label: '当前区间', value: formatCurrency(0), hint: `${dateRange.start} 至 ${dateRange.end}`, trend: '0.00', tone: 'warning' },
      { label: '当前门店', value: currentStore.value?.name ?? '-', hint: '单店视角', trend: currentStore.value?.status ?? '-', tone: 'danger' },
    ]
  }

  return [
    { label: '今日支出', value: formatCurrency(Number(data.todayAmount)), hint: '按自然日统计', trend: currentStore.value?.name ?? '-', tone: 'primary' },
    { label: '本月支出', value: formatCurrency(Number(data.monthAmount)), hint: '本月累计支出', trend: `${dateRange.end}`, tone: 'success' },
    { label: '当前区间', value: formatCurrency(Number(data.rangeAmount)), hint: `${dateRange.start} 至 ${dateRange.end}`, trend: `${recentRows.value.length} 条最近记录`, tone: 'warning' },
    {
      label: '当前门店',
      value: currentStore.value?.name || '-',
      hint: currentStore.value ? `状态：${currentStore.value.status === 'ENABLED' ? '营业中' : '停用'}` : '未选择门店',
      trend: currentStore.value?.code || '-',
      tone: 'danger',
    },
  ]
})

async function loadStores() {
  const result = await listStoresApi({
    pageNo: 1,
    pageSize: 200,
  })
  stores.value = result.list
}

function syncStoreFromRoute() {
  const routeStoreId = Number(route.params.id)
  if (Number.isFinite(routeStoreId) && routeStoreId > 0) {
    selectedStoreId.value = routeStoreId
    return
  }
  if (!selectedStoreId.value) {
    selectedStoreId.value = stores.value.find((item) => item.status === 'ENABLED')?.id ?? stores.value[0]?.id ?? ''
  }
}

// 单店分析页所有数据都强制带 storeId，避免误展示为全店统计。
async function loadAnalysis() {
  if (!selectedStoreId.value) {
    return
  }

  loading.value = true
  try {
    const params = {
      storeId: selectedStoreId.value,
      dateStart: dateRange.start,
      dateEnd: dateRange.end,
    }
    const [overviewRes, trendRes, categoryRes, rankingRes, recentRes] = await Promise.all([
      getOverviewApi(params),
      getTrendApi(params),
      getCategoryBreakdownApi({ ...params, categoryLevel: 1 }),
      getItemRankingApi({ ...params, topN: 5 }),
      listExpensesApi({
        ...params,
        pageNo: 1,
        pageSize: 5,
      }),
    ])
    overview.value = overviewRes
    trendRows.value = trendRes
    categoryRows.value = categoryRes
    rankingRows.value = rankingRes
    recentRows.value = recentRes.list
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载分店分析失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    syncStoreFromRoute()
    if (initialized.value) {
      loadAnalysis()
    }
  },
)

watch(selectedStoreId, (value, oldValue) => {
  if (!initialized.value || value === oldValue) {
    return
  }
  loadAnalysis()
})

onMounted(async () => {
  try {
    await loadStores()
    syncStoreFromRoute()
    await loadAnalysis()
    initialized.value = true
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '初始化分析页失败')
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

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.store-select {
  width: 220px;
}

@media (max-width: 1280px) {
  .metric-grid,
  .analysis-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }

  .metric-grid,
  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .store-select {
    width: 100%;
  }
}
</style>
