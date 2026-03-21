import { requestData } from '@/api/http'
import type { CategoryNodeDto, CategoryQueryReq, CategoryUpsertReq } from '@/types/category'
import type { PageResult } from '@/types/common'
import type { StoreDto, StoreQueryReq, StoreUpsertReq } from '@/types/store'
import type { UserDto, UserQueryReq, UserUpsertReq } from '@/types/user'

export function listStoreOptionsApi() {
  return requestData<StoreDto[]>({
    url: '/stores/options',
    method: 'GET',
  })
}

export function listStoresApi(params: StoreQueryReq = {}) {
  return requestData<PageResult<StoreDto>>({
    url: '/stores',
    method: 'GET',
    params,
  })
}

export function createStoreApi(data: StoreUpsertReq) {
  return requestData<StoreDto>({
    url: '/stores',
    method: 'POST',
    data,
  })
}

export function updateStoreApi(id: number, data: StoreUpsertReq) {
  return requestData<StoreDto>({
    url: `/stores/${id}`,
    method: 'PUT',
    data,
  })
}

export function patchStoreStatusApi(id: number, data: { status: 'ENABLED' | 'DISABLED' }) {
  return requestData<StoreDto>({
    url: `/stores/${id}/status`,
    method: 'PATCH',
    data,
  })
}

export function listUsersApi(params: UserQueryReq = {}) {
  return requestData<PageResult<UserDto>>({
    url: '/users',
    method: 'GET',
    params,
  })
}

export function createUserApi(data: UserUpsertReq) {
  return requestData<UserDto>({
    url: '/users',
    method: 'POST',
    data,
  })
}

export function updateUserApi(id: number, data: UserUpsertReq) {
  return requestData<UserDto>({
    url: `/users/${id}`,
    method: 'PUT',
    data,
  })
}

export function patchUserStatusApi(id: number, data: { status: 'ENABLED' | 'DISABLED' }) {
  return requestData<UserDto>({
    url: `/users/${id}/status`,
    method: 'PATCH',
    data,
  })
}

export function resetUserPasswordApi(id: number, data: { password: string }) {
  return requestData<UserDto>({
    url: `/users/${id}/password`,
    method: 'PATCH',
    data,
  })
}

export function listCategoryTreeApi() {
  return requestData<CategoryNodeDto[]>({
    url: '/categories/tree',
    method: 'GET',
  })
}

export function listCategoriesApi(params: CategoryQueryReq = {}) {
  return requestData<PageResult<CategoryNodeDto>>({
    url: '/categories',
    method: 'GET',
    params,
  })
}

export function createCategoryApi(data: CategoryUpsertReq) {
  return requestData<CategoryNodeDto>({
    url: '/categories',
    method: 'POST',
    data,
  })
}

export function updateCategoryApi(id: number, data: CategoryUpsertReq) {
  return requestData<CategoryNodeDto>({
    url: `/categories/${id}`,
    method: 'PUT',
    data,
  })
}

export function patchCategoryStatusApi(id: number, data: { status: 'ENABLED' | 'DISABLED' }) {
  return requestData<CategoryNodeDto>({
    url: `/categories/${id}/status`,
    method: 'PATCH',
    data,
  })
}
