import type { PageResult } from '@/types/common'
import type { StatusValue } from '@/types/auth'

export interface ExpenseRecordDto {
  id: number
  storeId: number
  storeName: string
  expenseDate: string
  categoryLevel1Id: number
  categoryLevel1Name: string
  categoryLevel2Id: number
  categoryLevel2Name: string
  itemName: string
  amount: number
  quantity: number | null
  unit: string
  remark: string
  createdBy: number
  createdByName: string
  updatedBy: number
  updatedByName: string
  createdAt: string
  updatedAt: string
  deleted?: boolean
  version?: number
}

export interface ExpenseUpsertReq {
  storeId: number
  expenseDate: string
  categoryLevel1Id: number
  categoryLevel2Id: number
  itemName: string
  amount: number
  quantity: number | null
  unit: string
  remark: string
}

export interface ExpenseQueryReq {
  storeId?: number | ''
  dateStart?: string
  dateEnd?: string
  categoryLevel1Id?: number | ''
  categoryLevel2Id?: number | ''
  itemName?: string
  pageNo?: number
  pageSize?: number
}

export interface ExpenseHistoryDto {
  id: number
  expenseRecordId: number
  action: 'CREATE' | 'UPDATE' | 'DELETE'
  beforeSnapshot: Record<string, unknown> | null
  afterSnapshot: Record<string, unknown> | null
  operatorId: number
  operatorName: string
  operateTime: string
}

export type ExpensePageResult = PageResult<ExpenseRecordDto>

export interface ExpenseStatusView {
  status: StatusValue
}
