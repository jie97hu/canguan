export type ApiErrorCode =
  | 'SUCCESS'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'VALIDATION_ERROR'
  | 'DATA_NOT_FOUND'
  | 'STORE_DISABLED'
  | 'USER_DISABLED'
  | 'CATEGORY_DISABLED'
  | 'EXPENSE_ALREADY_DELETED'
  | 'TOKEN_INVALID'
  | 'TOKEN_EXPIRED'
  | 'REFRESH_TOKEN_INVALID'
  | 'SYSTEM_ERROR'

export interface ApiResponse<T = unknown> {
  code: ApiErrorCode | string
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
  extra: Record<string, unknown>
}

export interface OptionItem {
  label: string
  value: string | number
  disabled?: boolean
  children?: OptionItem[]
}

export interface ApiExceptionPayload {
  code: ApiErrorCode | string
  message: string
  data?: unknown
}
