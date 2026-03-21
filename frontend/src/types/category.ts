import type { StatusValue } from '@/types/auth'

export interface CategoryNodeDto {
  id: number
  parentId: number
  level: 1 | 2
  name: string
  code: string
  defaultUnit: string | null
  sortNo: number
  status: StatusValue
  children: CategoryNodeDto[]
  createdAt?: string
  updatedAt?: string
}

export interface CategoryUpsertReq {
  parentId: number
  level: 1 | 2
  name: string
  code: string
  defaultUnit: string | null
  sortNo: number
  status: StatusValue
}

export interface CategoryQueryReq {
  keyword?: string
  level?: 1 | 2 | ''
  status?: StatusValue | ''
}
