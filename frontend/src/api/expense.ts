import { requestData } from '@/api/http'
import type { ExpenseHistoryDto, ExpensePageResult, ExpenseQueryReq, ExpenseRecordDto, ExpenseUpsertReq } from '@/types/expense'

export function listExpensesApi(params: ExpenseQueryReq = {}) {
  return requestData<ExpensePageResult>({
    url: '/expenses',
    method: 'GET',
    params,
  })
}

export function getExpenseApi(id: number) {
  return requestData<ExpenseRecordDto>({
    url: `/expenses/${id}`,
    method: 'GET',
  })
}

export function createExpenseApi(data: ExpenseUpsertReq) {
  return requestData<ExpenseRecordDto>({
    url: '/expenses',
    method: 'POST',
    data,
  })
}

export function updateExpenseApi(id: number, data: ExpenseUpsertReq) {
  return requestData<ExpenseRecordDto>({
    url: `/expenses/${id}`,
    method: 'PUT',
    data,
  })
}

export function deleteExpenseApi(id: number) {
  return requestData<void>({
    url: `/expenses/${id}`,
    method: 'DELETE',
  })
}

export function listExpenseHistoryApi(id: number) {
  return requestData<ExpenseHistoryDto[]>({
    url: `/expenses/${id}/history`,
    method: 'GET',
  })
}
