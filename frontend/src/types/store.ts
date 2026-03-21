import type { StatusValue } from '@/types/auth'

export interface StoreDto {
  id: number
  name: string
  code: string
  status: StatusValue
  createdAt: string
  updatedAt: string
}

export interface StoreUpsertReq {
  name: string
  code: string
  status: StatusValue
}

export interface StoreQueryReq {
  keyword?: string
  status?: StatusValue | ''
  pageNo?: number
  pageSize?: number
}
