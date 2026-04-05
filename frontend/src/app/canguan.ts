import type { AppRole } from '@/types/auth'
import type { CategoryNodeDto } from '@/types/category'
import type { ExpenseHistoryDto, ExpenseRecordDto } from '@/types/expense'
import type { StoreDto } from '@/types/store'
import type {
  BarChartItem,
  DonutChartItem,
  ExpenseBatchRowForm,
  ExpenseBatchSharedForm,
  ExpenseFormModel,
  ExpenseHistoryEntry,
  TrendChartPoint,
} from '@/types/ui'

const chartColors = ['#2858FF', '#1EAE98', '#E36B2C', '#9B6BFF', '#FF8A5C', '#2F6BFF', '#F4A261', '#3A86FF']

export function formatCurrency(value: number | null | undefined): string {
  return `¥ ${Number(value ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function resolveRoleText(value: AppRole): string {
  return value === 'OWNER' ? '老板' : '录入员'
}

export function flattenCategoryTree(tree: CategoryNodeDto[]): CategoryNodeDto[] {
  return tree.flatMap((item) => [item, ...flattenCategoryTree(item.children ?? [])])
}

export function getCategoryById(categories: CategoryNodeDto[], categoryId: number | '' | null | undefined) {
  if (!categoryId) {
    return undefined
  }
  return categories.find((item) => item.id === categoryId)
}

export function getCategoryChildren(categories: CategoryNodeDto[], parentId: number | '' | null | undefined) {
  if (!parentId) {
    return categories.filter((item) => item.level === 2)
  }
  return categories.filter((item) => item.parentId === parentId)
}

export function getStoreById(stores: StoreDto[], storeId: number | '' | null | undefined) {
  if (!storeId) {
    return undefined
  }
  return stores.find((item) => item.id === storeId)
}

export function createExpenseTemplate(options: {
  stores: StoreDto[]
  categories: CategoryNodeDto[]
  storeId?: number | ''
  expenseDate?: string
}): ExpenseFormModel {
  const level1 = options.categories.find((item) => item.level === 1 && item.status === 'ENABLED')
  const level2 = level1 ? getCategoryChildren(options.categories, level1.id).find((item) => item.status === 'ENABLED') : undefined

  return {
    storeId: options.storeId ?? options.stores[0]?.id ?? '',
    expenseDate: options.expenseDate ?? toLocalDateString(new Date()),
    categoryLevel1Id: level1?.id ?? '',
    categoryLevel2Id: level2?.id ?? '',
    itemName: '',
    amount: null,
    quantity: null,
    unit: level2?.defaultUnit ?? '斤',
    remark: '',
  }
}

export function createExpenseBatchSharedTemplate(options: {
  stores: StoreDto[]
  categories: CategoryNodeDto[]
  storeId?: number | ''
  expenseDate?: string
}): ExpenseBatchSharedForm {
  const template = createExpenseTemplate(options)
  return {
    storeId: template.storeId,
    expenseDate: template.expenseDate,
    categoryLevel1Id: template.categoryLevel1Id,
    categoryLevel2Id: template.categoryLevel2Id,
  }
}

export function createExpenseBatchRowTemplate(defaultUnit = '斤'): ExpenseBatchRowForm {
  return {
    key: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    itemName: '',
    amount: null,
    quantity: null,
    unit: defaultUnit,
    remark: '',
    errorMessage: '',
  }
}

function snapshotToText(snapshot: Record<string, unknown> | null | undefined) {
  if (!snapshot || !Object.keys(snapshot).length) {
    return '无'
  }

  const itemName = snapshot.itemName ? String(snapshot.itemName) : '未命名支出'
  const quantity = snapshot.quantity == null || snapshot.quantity === '' ? '-' : String(snapshot.quantity)
  const unit = snapshot.unit ? String(snapshot.unit) : ''
  const amount = formatCurrency(Number(snapshot.amount ?? 0))
  const parts = [itemName, `${quantity}${unit ? ` ${unit}` : ''}`, amount]
  return parts.filter(Boolean).join(' · ')
}

export function mapHistoryEntry(item: ExpenseHistoryDto): ExpenseHistoryEntry {
  return {
    id: item.id,
    operator: item.operatorName,
    operateAt: item.operateTime,
    action: item.action,
    beforeText: snapshotToText(item.beforeSnapshot),
    afterText: item.action === 'DELETE' ? '已删除' : snapshotToText(item.afterSnapshot),
  }
}

export function mapTrendPoints(points: Array<{ label: string; amount: number }>): TrendChartPoint[] {
  return points.map((item) => ({
    label: item.label,
    value: Number(item.amount ?? 0),
  }))
}

function normalizeDateText(value: string | null | undefined) {
  return value?.trim() || ''
}

export function toLocalDateString(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function withDayBoundary(value: string | null | undefined, boundary: 'start' | 'end') {
  const dateText = normalizeDateText(value)
  if (!dateText) {
    return undefined
  }
  if (dateText.includes(' ')) {
    return dateText
  }
  return `${dateText} ${boundary === 'start' ? '00:00:00' : '23:59:59'}`
}

export function toRangeQueryParams(dateRange: Array<string | null | undefined>) {
  return {
    dateStart: withDayBoundary(dateRange[0], 'start'),
    dateEnd: withDayBoundary(dateRange[1], 'end'),
  }
}

export function mapDonutItems(items: Array<{ categoryName: string; ratio: number }>): DonutChartItem[] {
  return items.map((item, index) => ({
    label: item.categoryName,
    value: Number((Number(item.ratio ?? 0) * 100).toFixed(2)),
    color: chartColors[index % chartColors.length],
  }))
}

export function buildBarChart(items: Array<{ label: string; value: number }>): BarChartItem[] {
  const maxValue = Math.max(...items.map((item) => item.value), 0)
  const totalValue = items.reduce((sum, item) => sum + item.value, 0)
  return items.map((item, index) => ({
    label: item.label,
    value: item.value,
    // 横条长度按第一名相对缩放，并保留最小可视宽度，避免小额项目完全看不见。
    barWidthPercentage: maxValue <= 0 ? 0 : Math.max(18, Math.round((item.value / maxValue) * 100)),
    // 右侧展示真实金额占比，避免把视觉宽度误读成财务占比。
    sharePercentage: totalValue <= 0 ? 0 : Number(((item.value / totalValue) * 100).toFixed(2)),
    color: chartColors[index % chartColors.length],
  }))
}

export function mapExpenseRecordToForm(record: ExpenseRecordDto): ExpenseFormModel {
  return {
    storeId: record.storeId,
    expenseDate: record.expenseDate,
    categoryLevel1Id: record.categoryLevel1Id,
    categoryLevel2Id: record.categoryLevel2Id,
    itemName: record.itemName,
    amount: Number(record.amount),
    quantity: record.quantity == null ? null : Number(record.quantity),
    unit: record.unit ?? '',
    remark: record.remark ?? '',
  }
}
