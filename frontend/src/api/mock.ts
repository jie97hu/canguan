import type { AxiosRequestConfig } from 'axios'

import { APP_NAME } from '@/app/config'
import { readAuthSession, writeAuthSession } from '@/app/storage'
import type { ApiResponse, PageResult } from '@/types/common'
import type { AuthSession, CurrentUserDto, LoginRes, StatusValue } from '@/types/auth'
import type { CategoryNodeDto } from '@/types/category'
import type { ExpenseHistoryDto, ExpenseRecordDto } from '@/types/expense'
import type { ItemRankingDto, OverviewDto, StoreComparisonDto, TrendPointDto, CategoryBreakdownDto } from '@/types/report'
import type { OptionItem } from '@/types/common'
import type { StoreDto } from '@/types/store'
import type { UserDto } from '@/types/user'

const MOCK_SUCCESS = 'SUCCESS'

function now() {
  return new Date()
}

function formatDateTime(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  const hour = `${date.getHours()}`.padStart(2, '0')
  const minute = `${date.getMinutes()}`.padStart(2, '0')
  const second = `${date.getSeconds()}`.padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function clone<T>(value: T) {
  return JSON.parse(JSON.stringify(value)) as T
}

function toSnapshot<T extends object>(value: T) {
  return clone(value) as Record<string, unknown>
}

function sumAmount(items: ExpenseRecordDto[]) {
  return Number(items.reduce((total, item) => total + item.amount, 0).toFixed(2))
}

function parseUrl(config: AxiosRequestConfig) {
  const base = config.baseURL ?? '/api'
  const url = config.url ?? ''
  const full = new URL(url, base.startsWith('http') ? base : `http://mock.local${base}`)
  return {
    pathname: full.pathname.replace(/\/+$/, ''),
    query: Object.fromEntries(full.searchParams.entries()),
  }
}

function createSession(userInfo: CurrentUserDto, expiresIn = 7200): AuthSession {
  return {
    accessToken: `mock-access.${userInfo.role.toLowerCase()}.${userInfo.id}`,
    refreshToken: `mock-refresh.${userInfo.role.toLowerCase()}.${userInfo.id}`,
    expiresIn,
    userInfo,
    savedAt: new Date().toISOString(),
  }
}

const stores: StoreDto[] = [
  { id: 1, name: '总店', code: 'STORE001', status: 'ENABLED', createdAt: '2026-03-21 12:00:00', updatedAt: '2026-03-21 12:00:00' },
  { id: 2, name: '南坪店', code: 'STORE002', status: 'ENABLED', createdAt: '2026-03-21 12:00:00', updatedAt: '2026-03-21 12:00:00' },
  { id: 3, name: '观音桥店', code: 'STORE003', status: 'DISABLED', createdAt: '2026-03-21 12:00:00', updatedAt: '2026-03-21 12:00:00' },
]

const users: UserDto[] = [
  {
    id: 1,
    username: 'owner',
    displayName: '老板',
    role: 'OWNER',
    storeId: null,
    storeName: null,
    status: 'ENABLED',
    tokenVersion: 1,
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
  },
  {
    id: 2,
    username: 'clerk01',
    displayName: '张三',
    role: 'CLERK',
    storeId: 1,
    storeName: '总店',
    status: 'ENABLED',
    tokenVersion: 1,
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
  },
]

const categories: CategoryNodeDto[] = [
  {
    id: 1,
    parentId: 0,
    level: 1,
    name: '食材',
    code: 'FOOD',
    defaultUnit: null,
    sortNo: 1,
    status: 'ENABLED',
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
    children: [
      {
        id: 2,
        parentId: 1,
        level: 2,
        name: '牛肉类',
        code: 'BEEF',
        defaultUnit: '斤',
        sortNo: 1,
        status: 'ENABLED',
        createdAt: '2026-03-21 12:00:00',
        updatedAt: '2026-03-21 12:00:00',
        children: [],
      },
      {
        id: 3,
        parentId: 1,
        level: 2,
        name: '猪肉类',
        code: 'PORK',
        defaultUnit: '斤',
        sortNo: 2,
        status: 'ENABLED',
        createdAt: '2026-03-21 12:00:00',
        updatedAt: '2026-03-21 12:00:00',
        children: [],
      },
    ],
  },
  {
    id: 4,
    parentId: 0,
    level: 1,
    name: '费用',
    code: 'FEE',
    defaultUnit: null,
    sortNo: 2,
    status: 'ENABLED',
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
    children: [
      {
        id: 5,
        parentId: 4,
        level: 2,
        name: '固定费用',
        code: 'FIXED',
        defaultUnit: '月',
        sortNo: 1,
        status: 'ENABLED',
        createdAt: '2026-03-21 12:00:00',
        updatedAt: '2026-03-21 12:00:00',
        children: [],
      },
    ],
  },
]

const expenseRecords: ExpenseRecordDto[] = [
  {
    id: 1,
    storeId: 1,
    storeName: '总店',
    expenseDate: '2026-03-21',
    categoryLevel1Id: 1,
    categoryLevel1Name: '食材',
    categoryLevel2Id: 2,
    categoryLevel2Name: '牛肉类',
    itemName: '牛腩',
    amount: 236.5,
    quantity: 20,
    unit: '斤',
    remark: '早市采购',
    createdBy: 1,
    createdByName: '老板',
    updatedBy: 1,
    updatedByName: '老板',
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
    deleted: false,
    version: 1,
  },
  {
    id: 2,
    storeId: 2,
    storeName: '南坪店',
    expenseDate: '2026-03-21',
    categoryLevel1Id: 4,
    categoryLevel1Name: '费用',
    categoryLevel2Id: 5,
    categoryLevel2Name: '固定费用',
    itemName: '房租',
    amount: 12000,
    quantity: null,
    unit: '月',
    remark: '3 月房租',
    createdBy: 1,
    createdByName: '老板',
    updatedBy: 1,
    updatedByName: '老板',
    createdAt: '2026-03-21 12:00:00',
    updatedAt: '2026-03-21 12:00:00',
    deleted: false,
    version: 1,
  },
]

const histories: ExpenseHistoryDto[] = [
  {
    id: 1,
    expenseRecordId: 1,
    action: 'CREATE',
    beforeSnapshot: null,
    afterSnapshot: toSnapshot(expenseRecords[0]),
    operatorId: 1,
    operatorName: '老板',
    operateTime: '2026-03-21 12:00:00',
  },
]

function currentUserFromSession(): CurrentUserDto {
  const session = readAuthSession()
  if (session?.userInfo) {
    return session.userInfo
  }
  return {
    id: 1,
    username: 'owner',
    displayName: '老板',
    role: 'OWNER',
    storeId: null,
    storeName: null,
    status: 'ENABLED',
  }
}

function createResponse<T>(data: T, message = 'success'): ApiResponse<T> {
  return {
    code: MOCK_SUCCESS,
    message,
    data,
  }
}

function createError<T = never>(code: ApiResponse<T>['code'], message: string, data?: T): ApiResponse<T> {
  return {
    code,
    message,
    data: data as T,
  }
}

function filterStoreList(query: Record<string, string>) {
  const keyword = query.keyword?.trim().toLowerCase() ?? ''
  const status = query.status?.trim()
  return stores.filter((item) => {
    const keywordHit = !keyword || [item.name, item.code].some((text) => text.toLowerCase().includes(keyword))
    const statusHit = !status || item.status === status
    return keywordHit && statusHit
  })
}

function filterUsers(query: Record<string, string>) {
  const keyword = query.keyword?.trim().toLowerCase() ?? ''
  const role = query.role?.trim()
  const status = query.status?.trim()
  const storeId = query.storeId?.trim()
  return users.filter((item) => {
    const keywordHit = !keyword || [item.username, item.displayName].some((text) => text.toLowerCase().includes(keyword))
    const roleHit = !role || item.role === role
    const statusHit = !status || item.status === status
    const storeHit = !storeId || `${item.storeId ?? ''}` === storeId
    return keywordHit && roleHit && statusHit && storeHit
  })
}

function filterCategories(query: Record<string, string>, tree = false) {
  const keyword = query.keyword?.trim().toLowerCase() ?? ''
  const status = query.status?.trim()
  const level = query.level?.trim()
  const flat = categories.flatMap((item) => [item, ...item.children])
  const result = flat.filter((item) => {
    const keywordHit = !keyword || [item.name, item.code].some((text) => text.toLowerCase().includes(keyword))
    const statusHit = !status || item.status === status
    const levelHit = !level || `${item.level}` === level
    return keywordHit && statusHit && levelHit
  })

  if (!tree) {
    return result
  }

  return categories
}

function filterExpenses(query: Record<string, string>) {
  const storeId = query.storeId?.trim()
  const dateStart = query.dateStart?.trim().slice(0, 10)
  const dateEnd = query.dateEnd?.trim().slice(0, 10)
  const categoryLevel1Id = query.categoryLevel1Id?.trim()
  const categoryLevel2Id = query.categoryLevel2Id?.trim()
  const itemName = query.itemName?.trim().toLowerCase() ?? ''

  return expenseRecords.filter((item) => {
    const storeHit = !storeId || `${item.storeId}` === storeId
    const startHit = !dateStart || item.expenseDate >= dateStart
    const endHit = !dateEnd || item.expenseDate <= dateEnd
    const level1Hit = !categoryLevel1Id || `${item.categoryLevel1Id}` === categoryLevel1Id
    const level2Hit = !categoryLevel2Id || `${item.categoryLevel2Id}` === categoryLevel2Id
    const nameHit = !itemName || item.itemName.toLowerCase().includes(itemName)
    return storeHit && startHit && endHit && level1Hit && level2Hit && nameHit && !item.deleted
  })
}

function paginate<T>(items: T[], pageNo = 1, pageSize = 10): PageResult<T> {
  const start = (pageNo - 1) * pageSize
  return {
    list: items.slice(start, start + pageSize),
    total: items.length,
    pageNo,
    pageSize,
    extra: {},
  }
}

function makeLoginResponse(user: CurrentUserDto): LoginRes {
  return {
    accessToken: `mock-access.${user.role.toLowerCase()}.${user.id}.${Date.now()}`,
    refreshToken: `mock-refresh.${user.role.toLowerCase()}.${user.id}.${Date.now()}`,
    expiresIn: 7200,
    userInfo: user,
  }
}

function buildOverview(params: Record<string, string>): OverviewDto {
  const filtered = filterExpenses(params)
  const today = formatDate(now())
  const month = today.slice(0, 7)
  const todayAmount = sumAmount(filtered.filter((item) => item.expenseDate === today))
  const monthAmount = sumAmount(filtered.filter((item) => item.expenseDate.startsWith(month)))
  const rangeAmount = sumAmount(filtered)
  const topStore = stores
    .map((store) => ({
      storeName: store.name,
      amount: sumAmount(filtered.filter((item) => item.storeId === store.id)),
    }))
    .sort((a, b) => b.amount - a.amount)[0]

  return {
    todayAmount,
    monthAmount,
    rangeAmount,
    storeCount: stores.length,
    topStoreName: topStore?.storeName ?? '总店',
    topStoreAmount: topStore?.amount ?? 0,
  }
}

function buildTrend(params: Record<string, string>): TrendPointDto[] {
  const filtered = filterExpenses(params)
  const bucket = new Map<string, number>()
  for (const item of filtered) {
    bucket.set(item.expenseDate, (bucket.get(item.expenseDate) ?? 0) + item.amount)
  }
  return Array.from(bucket.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([label, amount]) => ({ label, amount: Number(amount.toFixed(2)) }))
}

function buildCategoryBreakdown(params: Record<string, string>): CategoryBreakdownDto[] {
  const filtered = filterExpenses(params)
  const bucket = new Map<number, { name: string; amount: number }>()
  for (const item of filtered) {
    const key = item.categoryLevel1Id
    const current = bucket.get(key) ?? { name: item.categoryLevel1Name, amount: 0 }
    current.amount += item.amount
    bucket.set(key, current)
  }
  const total = sumAmount(filtered) || 1
  return Array.from(bucket.entries())
    .sort((a, b) => b[1].amount - a[1].amount)
    .map(([categoryId, value]) => ({
      categoryId,
      categoryName: value.name,
      amount: Number(value.amount.toFixed(2)),
      ratio: Number((value.amount / total).toFixed(4)),
    }))
}

function buildItemRanking(params: Record<string, string>): ItemRankingDto[] {
  const filtered = filterExpenses(params)
  const topN = Number(params.topN ?? 10)
  const bucket = new Map<string, number>()
  for (const item of filtered) {
    bucket.set(item.itemName, (bucket.get(item.itemName) ?? 0) + item.amount)
  }
  return Array.from(bucket.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, topN)
    .map(([itemName, amount], index) => ({
      itemName,
      amount: Number(amount.toFixed(2)),
      rankNo: index + 1,
    }))
}

function buildStoreComparison(params: Record<string, string>): StoreComparisonDto[] {
  const filtered = filterExpenses(params)
  return stores
    .map((store) => ({
      storeId: store.id,
      storeName: store.name,
      amount: Number(sumAmount(filtered.filter((item) => item.storeId === store.id)).toFixed(2)),
      rankNo: 0,
    }))
    .sort((a, b) => b.amount - a.amount)
    .map((item, index) => ({ ...item, rankNo: index + 1 }))
}

function parseBody<T>(config: AxiosRequestConfig) {
  return (config.data ?? {}) as T
}

function updateCurrentUserSession(userInfo: CurrentUserDto) {
  const session = createSession(userInfo)
  writeAuthSession(session)
  return session
}

function handleAuth(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  if (pathname === '/api/auth/login' && method === 'POST') {
    const body = parseBody<{ username: string; password: string }>(config)
    const normalized = body.username.trim().toLowerCase()
    const userInfo: CurrentUserDto = normalized.includes('clerk')
      ? {
          id: 2,
          username: 'clerk01',
          displayName: '张三',
          role: 'CLERK',
          storeId: 1,
          storeName: '总店',
          status: 'ENABLED',
        }
      : {
          id: 1,
          username: 'owner',
          displayName: '老板',
          role: 'OWNER',
          storeId: null,
          storeName: null,
          status: 'ENABLED',
        }

    const session = updateCurrentUserSession(userInfo)
    return createResponse(makeLoginResponse({ ...userInfo, ...session.userInfo }))
  }

  if (pathname === '/api/auth/refresh' && method === 'POST') {
    const session = readAuthSession()
    const userInfo = session?.userInfo ?? currentUserFromSession()
    const newSession = updateCurrentUserSession(userInfo)
    return createResponse(makeLoginResponse(newSession.userInfo))
  }

  if (pathname === '/api/auth/logout' && method === 'POST') {
    writeAuthSession(null)
    return createResponse(undefined)
  }

  if (pathname === '/api/auth/me' && method === 'GET') {
    const userInfo = readAuthSession()?.userInfo ?? currentUserFromSession()
    return createResponse(userInfo)
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

function handleStores(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  if (pathname === '/api/stores/options' && method === 'GET') {
    return createResponse(
      stores
        .filter((item) => item.status === 'ENABLED')
        .map((item) => ({ label: item.name, value: item.id, disabled: item.status !== 'ENABLED' })),
    )
  }

  if (pathname === '/api/stores' && method === 'GET') {
    const query = parseUrl(config).query
    const list = filterStoreList(query)
    return createResponse({ ...paginate(list, Number(query.pageNo ?? 1), Number(query.pageSize ?? 10)), extra: { totalAmount: 0 } })
  }

  if (pathname === '/api/stores' && method === 'POST') {
    const body = parseBody<{ name: string; code: string; status: StatusValue }>(config)
    const next = {
      id: stores.length + 1,
      name: body.name,
      code: body.code,
      status: body.status,
      createdAt: formatDateTime(now()),
      updatedAt: formatDateTime(now()),
    }
    stores.unshift(next)
    return createResponse(next)
  }

  if (/^\/api\/stores\/\d+$/.test(pathname) && method === 'PUT') {
    const id = Number(pathname.split('/').pop())
    const body = parseBody<{ name: string; code: string; status: StatusValue }>(config)
    const target = stores.find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '分店不存在')
    }
    Object.assign(target, body, { updatedAt: formatDateTime(now()) })
    return createResponse(target)
  }

  if (/^\/api\/stores\/\d+\/status$/.test(pathname) && method === 'PATCH') {
    const id = Number(pathname.split('/')[3])
    const body = parseBody<{ status: StatusValue }>(config)
    const target = stores.find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '分店不存在')
    }
    target.status = body.status
    target.updatedAt = formatDateTime(now())
    return createResponse(target)
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

function handleUsers(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  if (pathname === '/api/users' && method === 'GET') {
    const query = parseUrl(config).query
    const list = filterUsers(query)
    return createResponse({ ...paginate(list, Number(query.pageNo ?? 1), Number(query.pageSize ?? 10)), extra: { totalAmount: 0 } })
  }

  if (pathname === '/api/users' && method === 'POST') {
    const body = parseBody<{ username: string; displayName: string; role: 'OWNER' | 'CLERK'; storeId: number | null; status: StatusValue }>(config)
    const store = stores.find((item) => item.id === body.storeId) ?? null
    const next = {
      id: users.length + 1,
      username: body.username,
      displayName: body.displayName,
      role: body.role,
      storeId: body.role === 'OWNER' ? null : body.storeId,
      storeName: body.role === 'OWNER' ? null : store?.name ?? null,
      status: body.status,
      tokenVersion: 1,
      createdAt: formatDateTime(now()),
      updatedAt: formatDateTime(now()),
    }
    users.unshift(next)
    return createResponse(next)
  }

  if (/^\/api\/users\/\d+$/.test(pathname) && method === 'PUT') {
    const id = Number(pathname.split('/').pop())
    const body = parseBody<{ username: string; displayName: string; role: 'OWNER' | 'CLERK'; storeId: number | null; status: StatusValue }>(config)
    const target = users.find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '账号不存在')
    }
    Object.assign(target, body, {
      storeId: body.role === 'OWNER' ? null : body.storeId,
      storeName: body.role === 'OWNER' ? null : stores.find((item) => item.id === body.storeId)?.name ?? null,
      updatedAt: formatDateTime(now()),
    })
    return createResponse(target)
  }

  if (/^\/api\/users\/\d+\/status$/.test(pathname) && method === 'PATCH') {
    const id = Number(pathname.split('/')[3])
    const body = parseBody<{ status: StatusValue }>(config)
    const target = users.find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '账号不存在')
    }
    target.status = body.status
    target.updatedAt = formatDateTime(now())
    return createResponse(target)
  }

  if (/^\/api\/users\/\d+\/password$/.test(pathname) && method === 'PATCH') {
    const id = Number(pathname.split('/')[3])
    const target = users.find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '账号不存在')
    }
    target.updatedAt = formatDateTime(now())
    return createResponse(target)
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

function handleCategories(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  if (pathname === '/api/categories/tree' && method === 'GET') {
    return createResponse(categories)
  }

  if (pathname === '/api/categories' && method === 'GET') {
    const flat = categories.flatMap((item) => [item, ...item.children])
    return createResponse(flat)
  }

  if (pathname === '/api/categories' && method === 'POST') {
    const body = parseBody<{ parentId: number; level: 1 | 2; name: string; code: string; defaultUnit: string | null; sortNo: number; status: StatusValue }>(config)
    const next = {
      id: Date.now(),
      parentId: body.parentId,
      level: body.level,
      name: body.name,
      code: body.code,
      defaultUnit: body.defaultUnit,
      sortNo: body.sortNo,
      status: body.status,
      createdAt: formatDateTime(now()),
      updatedAt: formatDateTime(now()),
      children: [],
    }
    if (body.level === 1) {
      categories.push(next)
    } else {
      const parent = categories.find((item) => item.id === body.parentId)
      parent?.children.push(next)
    }
    return createResponse(next)
  }

  if (/^\/api\/categories\/\d+$/.test(pathname) && method === 'PUT') {
    const id = Number(pathname.split('/').pop())
    const body = parseBody<{ parentId: number; level: 1 | 2; name: string; code: string; defaultUnit: string | null; sortNo: number; status: StatusValue }>(config)
    const target = categories.flatMap((item) => [item, ...item.children]).find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '分类不存在')
    }
    Object.assign(target, body, { updatedAt: formatDateTime(now()) })
    return createResponse(target)
  }

  if (/^\/api\/categories\/\d+\/status$/.test(pathname) && method === 'PATCH') {
    const id = Number(pathname.split('/')[3])
    const body = parseBody<{ status: StatusValue }>(config)
    const target = categories.flatMap((item) => [item, ...item.children]).find((item) => item.id === id)
    if (!target) {
      return createError('DATA_NOT_FOUND', '分类不存在')
    }
    target.status = body.status
    target.updatedAt = formatDateTime(now())
    return createResponse(target)
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

function handleExpenses(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  if (pathname === '/api/expenses/item-options' && method === 'GET') {
    const query = parseUrl(config).query
    const storeId = query.storeId?.trim()
    const categoryLevel1Id = query.categoryLevel1Id?.trim()
    const categoryLevel2Id = query.categoryLevel2Id?.trim()
    const limit = Number(query.limit ?? 100)

    const itemOptions = filterExpenses(query)
      .filter((item) => {
        const level1Hit = !categoryLevel1Id || `${item.categoryLevel1Id}` === categoryLevel1Id
        const level2Hit = !categoryLevel2Id || `${item.categoryLevel2Id}` === categoryLevel2Id
        const storeHit = !storeId || `${item.storeId}` === storeId
        return level1Hit && level2Hit && storeHit
      })
      .map((item) => item.itemName)
      .filter((item, index, list) => list.indexOf(item) === index)
      .sort((a, b) => a.localeCompare(b, 'zh-CN'))
      .slice(0, limit)

    return createResponse(itemOptions)
  }

  if (pathname === '/api/expenses' && method === 'GET') {
    const query = parseUrl(config).query
    const list = filterExpenses(query)
    const pageNo = Number(query.pageNo ?? 1)
    const pageSize = Number(query.pageSize ?? 10)
    const page = paginate(list, pageNo, pageSize)
    page.extra = { totalAmount: sumAmount(list) }
    return createResponse(page)
  }

  if (/^\/api\/expenses\/\d+$/.test(pathname) && method === 'GET') {
    const id = Number(pathname.split('/').pop())
    const target = expenseRecords.find((item) => item.id === id)
    if (!target || target.deleted) {
      return createError('DATA_NOT_FOUND', '支出不存在')
    }
    return createResponse(target)
  }

  if (pathname === '/api/expenses' && method === 'POST') {
    const body = parseBody<{
      storeId: number
      expenseDate: string
      categoryLevel1Id: number
      categoryLevel2Id: number
      itemName: string
      amount: number
      quantity: number | null
      unit: string
      remark: string
    }>(config)
    const store = stores.find((item) => item.id === body.storeId)
    const category1 = categories.find((item) => item.id === body.categoryLevel1Id)
    const category2 = categories.flatMap((item) => [item, ...item.children]).find((item) => item.id === body.categoryLevel2Id)

    const next: ExpenseRecordDto = {
      id: expenseRecords.length + 1,
      storeId: body.storeId,
      storeName: store?.name ?? '总店',
      expenseDate: body.expenseDate,
      categoryLevel1Id: body.categoryLevel1Id,
      categoryLevel1Name: category1?.name ?? '食材',
      categoryLevel2Id: body.categoryLevel2Id,
      categoryLevel2Name: category2?.name ?? '牛肉类',
      itemName: body.itemName,
      amount: Number(body.amount.toFixed(2)),
      quantity: body.quantity,
      unit: body.unit,
      remark: body.remark,
      createdBy: currentUserFromSession().id,
      createdByName: currentUserFromSession().displayName,
      updatedBy: currentUserFromSession().id,
      updatedByName: currentUserFromSession().displayName,
      createdAt: formatDateTime(now()),
      updatedAt: formatDateTime(now()),
      deleted: false,
      version: 1,
    }
    expenseRecords.unshift(next)
    histories.unshift({
      id: histories.length + 1,
      expenseRecordId: next.id,
      action: 'CREATE',
      beforeSnapshot: null,
      afterSnapshot: toSnapshot(next),
      operatorId: currentUserFromSession().id,
      operatorName: currentUserFromSession().displayName,
      operateTime: formatDateTime(now()),
    })
    return createResponse(next)
  }

  if (/^\/api\/expenses\/\d+$/.test(pathname) && method === 'PUT') {
    const id = Number(pathname.split('/').pop())
    const body = parseBody<{
      storeId: number
      expenseDate: string
      categoryLevel1Id: number
      categoryLevel2Id: number
      itemName: string
      amount: number
      quantity: number | null
      unit: string
      remark: string
    }>(config)
    const target = expenseRecords.find((item) => item.id === id)
    if (!target || target.deleted) {
      return createError('EXPENSE_ALREADY_DELETED', '支出已删除')
    }
    const before = toSnapshot(target)
    const category1 = categories.find((item) => item.id === body.categoryLevel1Id)
    const category2 = categories.flatMap((item) => [item, ...item.children]).find((item) => item.id === body.categoryLevel2Id)
    Object.assign(target, {
      storeId: body.storeId,
      storeName: stores.find((item) => item.id === body.storeId)?.name ?? target.storeName,
      expenseDate: body.expenseDate,
      categoryLevel1Id: body.categoryLevel1Id,
      categoryLevel1Name: category1?.name ?? target.categoryLevel1Name,
      categoryLevel2Id: body.categoryLevel2Id,
      categoryLevel2Name: category2?.name ?? target.categoryLevel2Name,
      itemName: body.itemName,
      amount: Number(body.amount.toFixed(2)),
      quantity: body.quantity,
      unit: body.unit,
      remark: body.remark,
      updatedBy: currentUserFromSession().id,
      updatedByName: currentUserFromSession().displayName,
      updatedAt: formatDateTime(now()),
      version: (target.version ?? 0) + 1,
    })
    histories.unshift({
      id: histories.length + 1,
      expenseRecordId: target.id,
      action: 'UPDATE',
      beforeSnapshot: before,
      afterSnapshot: toSnapshot(target),
      operatorId: currentUserFromSession().id,
      operatorName: currentUserFromSession().displayName,
      operateTime: formatDateTime(now()),
    })
    return createResponse(target)
  }

  if (/^\/api\/expenses\/\d+$/.test(pathname) && method === 'DELETE') {
    const id = Number(pathname.split('/').pop())
    const target = expenseRecords.find((item) => item.id === id)
    if (!target || target.deleted) {
      return createError('EXPENSE_ALREADY_DELETED', '支出已删除')
    }
    const before = toSnapshot(target)
    target.deleted = true
    target.updatedBy = currentUserFromSession().id
    target.updatedByName = currentUserFromSession().displayName
    target.updatedAt = formatDateTime(now())
    target.version = (target.version ?? 0) + 1
    histories.unshift({
      id: histories.length + 1,
      expenseRecordId: target.id,
      action: 'DELETE',
      beforeSnapshot: before,
      afterSnapshot: toSnapshot(target),
      operatorId: currentUserFromSession().id,
      operatorName: currentUserFromSession().displayName,
      operateTime: formatDateTime(now()),
    })
    return createResponse(undefined)
  }

  if (/^\/api\/expenses\/\d+\/history$/.test(pathname) && method === 'GET') {
    const id = Number(pathname.split('/')[3])
    return createResponse(histories.filter((item) => item.expenseRecordId === id))
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

function handleReports(pathname: string, method: string, config: AxiosRequestConfig): ApiResponse<unknown> {
  const query = parseUrl(config).query
  if (pathname === '/api/reports/overview' && method === 'GET') {
    return createResponse(buildOverview(query))
  }
  if (pathname === '/api/reports/trend' && method === 'GET') {
    return createResponse(buildTrend(query))
  }
  if (pathname === '/api/reports/category-breakdown' && method === 'GET') {
    return createResponse(buildCategoryBreakdown(query))
  }
  if (pathname === '/api/reports/item-ranking' && method === 'GET') {
    return createResponse(buildItemRanking(query))
  }
  if (pathname === '/api/reports/store-comparison' && method === 'GET') {
    return createResponse(buildStoreComparison(query))
  }
  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`)
}

export function isMockEnabled() {
  return import.meta.env.VITE_USE_MOCK === 'true'
}

export async function mockRequest<T>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  const { pathname } = parseUrl(config)
  const method = (config.method ?? 'GET').toUpperCase()

  if (pathname.startsWith('/api/auth/')) {
    return handleAuth(pathname, method, config) as ApiResponse<T>
  }
  if (pathname.startsWith('/api/stores')) {
    return handleStores(pathname, method, config) as ApiResponse<T>
  }
  if (pathname.startsWith('/api/users')) {
    return handleUsers(pathname, method, config) as ApiResponse<T>
  }
  if (pathname.startsWith('/api/categories')) {
    return handleCategories(pathname, method, config) as ApiResponse<T>
  }
  if (pathname.startsWith('/api/expenses')) {
    return handleExpenses(pathname, method, config) as ApiResponse<T>
  }
  if (pathname.startsWith('/api/reports')) {
    return handleReports(pathname, method, config) as ApiResponse<T>
  }

  return createError('DATA_NOT_FOUND', `未找到接口：${pathname}`) as ApiResponse<T>
}
