import type { AppRole, StatusValue } from '@/types/auth'

export interface MetricCardItem {
  label: string
  value: string
  hint: string
  trend: string
  tone: 'primary' | 'success' | 'warning' | 'danger'
}

export interface TrendChartPoint {
  label: string
  value: number
}

export interface DonutChartItem {
  label: string
  value: number
  color: string
}

export interface BarChartItem {
  label: string
  value: number
  barWidthPercentage: number
  sharePercentage: number
  color: string
}

export interface ExpenseFormModel {
  storeId: number | ''
  expenseDate: string
  categoryLevel1Id: number | ''
  categoryLevel2Id: number | ''
  itemName: string
  amount: number | null
  quantity: number | null
  unit: string
  remark: string
}

export interface ExpenseFilterModel {
  storeId: number | ''
  dateRange: string[]
  categoryLevel1Id: number | ''
  categoryLevel2Id: number | ''
  itemName: string
}

export interface ExpenseHistoryEntry {
  id: number
  operator: string
  operateAt: string
  action: 'CREATE' | 'UPDATE' | 'DELETE'
  beforeText: string
  afterText: string
}

export type AppStatus = StatusValue | 'NORMAL' | 'DELETED'
export type UiRole = AppRole
