import type { RouteMeta } from 'vue-router'

export type AppRole = 'OWNER' | 'CLERK'
export type StatusValue = 'ENABLED' | 'DISABLED'

export interface CurrentUserDto {
  id: number
  username: string
  displayName: string
  role: AppRole
  storeId: number | null
  storeName: string | null
  status: StatusValue
}

export interface LoginReq {
  username: string
  password: string
}

export interface LoginRes {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: CurrentUserDto
}

export interface AuthSession {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: CurrentUserDto
  savedAt: string
}

export interface RouteAccessMeta extends RouteMeta {
  title: string
  public?: boolean
  hideInMenu?: boolean
  roles?: AppRole[]
  icon?: string
  order?: number
  section?: string
  description?: string
}
