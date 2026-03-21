import type { AppRole } from '@/types/auth'
import type { CategoryNodeDto } from '@/types/category'
import type { ExpenseHistoryDto, ExpenseRecordDto } from '@/types/expense'
import type { StoreDto } from '@/types/store'
import type { BarChartItem, DonutChartItem, ExpenseFormModel, ExpenseHistoryEntry, TrendChartPoint } from '@/types/ui'

export const unitOptions = ['斤', '袋', '桶', '瓶', '箱', '包', '卷', '本', '支', '张', '月', '次']

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
    expenseDate: options.expenseDate ?? new Date().toISOString().slice(0, 10),
    categoryLevel1Id: level1?.id ?? '',
    categoryLevel2Id: level2?.id ?? '',
    itemName: '',
    amount: null,
    quantity: null,
    unit: level2?.defaultUnit ?? '斤',
    remark: '',
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

export function mapDonutItems(items: Array<{ categoryName: string; ratio: number }>): DonutChartItem[] {
  return items.map((item, index) => ({
    label: item.categoryName,
    value: Number(item.ratio ?? 0) * 100,
    color: chartColors[index % chartColors.length],
  }))
}

export function buildBarChart(items: Array<{ label: string; value: number }>): BarChartItem[] {
  const maxValue = Math.max(...items.map((item) => item.value), 0)
  return items.map((item, index) => ({
    label: item.label,
    value: item.value,
    percentage: maxValue <= 0 ? 0 : Math.max(18, Math.round((item.value / maxValue) * 100)),
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
