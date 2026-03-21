import type { AppRole, StatusValue } from '@/types/auth'

export interface UserDto {
  id: number
  username: string
  displayName: string
  role: AppRole
  storeId: number | null
  storeName: string | null
  status: StatusValue
  tokenVersion?: number
  createdAt: string
  updatedAt: string
}

export interface UserUpsertReq {
  username: string
  password?: string
  displayName: string
  role: AppRole
  storeId: number | null
  status: StatusValue
}

export interface UserQueryReq {
  keyword?: string
  role?: AppRole | ''
  status?: StatusValue | ''
  storeId?: number | ''
  pageNo?: number
  pageSize?: number
}
