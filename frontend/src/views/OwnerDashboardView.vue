<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Owner Dashboard"
      title="老板首页"
      description="从今日支出、本月支出、分类占比、门店排行和趋势变化几个角度快速掌握经营状态。"
    >
      <template #meta>
        <el-tag effect="light" round>分析门店：{{ selectedStoreName }}</el-tag>
        <el-tag effect="light" round type="success">时间范围：{{ dateLabel }}</el-tag>
      </template>
      <template #actions>
        <el-select v-model="selectedStoreId" placeholder="选择门店" class="store-select">
          <el-option label="全部门店" value="" />
          <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
        </el-select>
        <el-button type="primary" :loading="loading" @click="loadDashboard">刷新看板</el-button>
      </template>
    </PageHero>

    <div class="metric-grid">
      <MetricCard v-for="item in currentMetrics" :key="item.label" v-bind="item" />
    </div>

    <div class="board-grid">
      <ChartCard title="支出趋势" description="按日展示当前时间范围的支出变化。">
        <SvgTrendChart :data="trendData" />
      </ChartCard>
      <ChartCard title="分类占比" description="从一级分类看整体支出结构。">
        <SvgDonutChart :data="categoryData" />
      </ChartCard>
    </div>

    <div class="board-grid secondary">
      <ChartCard title="品项排行" description="当前时间范围内金额最高的品项。">
        <SvgBarChart :data="rankingChart" :format-value="formatCurrency" />
      </ChartCard>
      <PageSection title="门店对比" description="当前时间范围内各门店累计支出排名。">
        <el-table v-loading="loading" :data="comparisonData" border stripe>
          <el-table-column prop="rankNo" label="排名" width="90" />
          <el-table-column prop="storeName" label="门店" min-width="140" />
          <el-table-column label="累计支出" width="150">
            <template #default="{ row }">{{ formatCurrency(row.amount) }}</template>
          </el-table-column>
        </el-table>
      </PageSection>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
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
import { getCategoryBreakdownApi, getItemRankingApi, getOverviewApi, getStoreComparisonApi, getTrendApi } from '@/api/report'
import type { CategoryBreakdownDto, ItemRankingDto, OverviewDto, StoreComparisonDto, TrendPointDto } from '@/types/report'
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

const loading = ref(false)
const stores = ref<StoreDto[]>([])
const selectedStoreId = ref<number | ''>('')
const overview = ref<OverviewDto | null>(null)
const trendRows = ref<TrendPointDto[]>([])
const categoryRows = ref<CategoryBreakdownDto[]>([])
const rankingRows = ref<ItemRankingDto[]>([])
const comparisonRows = ref<StoreComparisonDto[]>([])
const dateRange = resolveDateRange()

const dateLabel = `${dateRange.start} 至 ${dateRange.end}`
const selectedStoreName = computed(() => stores.value.find((item) => item.id === selectedStoreId.value)?.name ?? '全部门店')
const trendData = computed(() => mapTrendPoints(trendRows.value.map((item) => ({ label: item.label, amount: Number(item.amount) }))))
const categoryData = computed(() => mapDonutItems(categoryRows.value.map((item) => ({ categoryName: item.categoryName, ratio: Number(item.ratio) }))))
const comparisonData = computed(() => comparisonRows.value.map((item) => ({ ...item, amount: Number(item.amount) })))
const rankingChart = computed(() =>
  buildBarChart(
    rankingRows.value.map((item) => ({
      label: item.itemName,
      value: Number(item.amount),
    })),
  ),
)

const currentMetrics = computed<MetricCardItem[]>(() => {
  const data = overview.value
  if (!data) {
    return [
      { label: '今日支出', value: formatCurrency(0), hint: '等待加载数据', trend: '0.00', tone: 'primary' },
      { label: '本月支出', value: formatCurrency(0), hint: '等待加载数据', trend: '0.00', tone: 'success' },
      { label: '当前区间', value: formatCurrency(0), hint: dateLabel, trend: '0.00', tone: 'warning' },
      { label: '支出最高门店', value: '-', hint: '暂无统计结果', trend: '0.00', tone: 'danger' },
    ]
  }

  return [
    { label: '今日支出', value: formatCurrency(Number(data.todayAmount)), hint: '按自然日统计', trend: `${selectedStoreName.value}`, tone: 'primary' },
    { label: '本月支出', value: formatCurrency(Number(data.monthAmount)), hint: '从月初累计到今天', trend: `${data.storeCount} 家门店`, tone: 'success' },
    { label: '当前区间', value: formatCurrency(Number(data.rangeAmount)), hint: dateLabel, trend: `${comparisonRows.value.length} 条排行`, tone: 'warning' },
    {
      label: '支出最高门店',
      value: data.topStoreName || '-',
      hint: data.topStoreName ? formatCurrency(Number(data.topStoreAmount)) : '当前范围暂无数据',
      trend: data.topStoreName ? 'TOP 1' : '无',
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

// 看板数据统一按同一时间范围并发拉取，保证卡片、图表和表格口径一致。
async function loadDashboard() {
  loading.value = true
  try {
    const params = {
      storeId: selectedStoreId.value,
      dateStart: dateRange.start,
      dateEnd: dateRange.end,
    }
    const [overviewRes, trendRes, categoryRes, rankingRes, comparisonRes] = await Promise.all([
      getOverviewApi(params),
      getTrendApi(params),
      getCategoryBreakdownApi({ ...params, categoryLevel: 1 }),
      getItemRankingApi({ ...params, topN: 5 }),
      getStoreComparisonApi({ dateStart: dateRange.start, dateEnd: dateRange.end }),
    ])
    overview.value = overviewRes
    trendRows.value = trendRes
    categoryRows.value = categoryRes
    rankingRows.value = rankingRes
    comparisonRows.value = comparisonRes
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载看板失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadStores()
    await loadDashboard()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '初始化看板失败')
  }
})

watch(selectedStoreId, () => {
  loadDashboard()
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

.board-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.board-grid.secondary {
  align-items: start;
}

.store-select {
  width: 180px;
}

@media (max-width: 1280px) {
  .metric-grid,
  .board-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }

  .metric-grid,
  .board-grid {
    grid-template-columns: 1fr;
  }

  .store-select {
    width: 100%;
  }
}
</style>
