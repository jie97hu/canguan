export type RoleType = 'OWNER' | 'CLERK'

export type StatusFlag = 'ENABLED' | 'DISABLED'

export interface LoginAccount {
  username: string
  password: string
  displayName: string
  role: RoleType
  storeId?: string
  storeName?: string
  description: string
}

export interface StoreNode {
  id: string
  code: string
  name: string
  city: string
  address: string
  manager: string
  phone: string
  status: StatusFlag
  createdAt: string
}

export interface UserNode {
  id: string
  username: string
  displayName: string
  role: RoleType
  storeId?: string
  storeName?: string
  phone: string
  status: StatusFlag
  updatedAt: string
}

export interface CategoryNode {
  id: string
  parentId: string | null
  level: 1 | 2
  name: string
  code: string
  defaultUnit: string
  sortNo: number
  status: StatusFlag
}

export interface ExpenseHistoryEntry {
  id: string
  operator: string
  operateAt: string
  action: 'CREATE' | 'UPDATE' | 'DELETE'
  beforeText: string
  afterText: string
}

export interface ExpenseRecord {
  id: string
  storeId: string
  storeName: string
  expenseDate: string
  categoryLevel1Id: string
  categoryLevel1Name: string
  categoryLevel2Id: string
  categoryLevel2Name: string
  itemName: string
  amount: number
  quantity: number | null
  unit: string
  remark: string
  createdBy: string
  updatedAt: string
  status: 'NORMAL' | 'DELETED'
  history: ExpenseHistoryEntry[]
}

export interface ExpenseFormModel {
  storeId: string
  expenseDate: string
  categoryLevel1Id: string
  categoryLevel2Id: string
  itemName: string
  amount: number | null
  quantity: number | null
  unit: string
  remark: string
}

export interface MetricCardItem {
  label: string
  value: string
  hint: string
  trend: string
  tone: 'primary' | 'success' | 'warning' | 'danger'
}

export interface TrendPoint {
  label: string
  value: number
}

export interface CategoryShare {
  label: string
  value: number
  color: string
}

export interface RankingItem {
  label: string
  value: number
  count: number
}

export interface StoreComparisonItem {
  storeId: string
  storeName: string
  today: number
  month: number
  lastMonth: number
  growth: number
}

export const mockLoginAccounts: LoginAccount[] = [
  {
    username: 'owner',
    password: '123456',
    displayName: '张老板',
    role: 'OWNER',
    description: '老板账号，可查看全部门店数据',
  },
  {
    username: 'downtown',
    password: '123456',
    displayName: '李小七',
    role: 'CLERK',
    storeId: 'store-001',
    storeName: '城南总店',
    description: '录入员账号，默认绑定城南总店',
  },
]

export const mockStores: StoreNode[] = [
  {
    id: 'store-001',
    code: 'CN001',
    name: '城南总店',
    city: '成都',
    address: '武侯区人民南路 88 号',
    manager: '张老板',
    phone: '13800000001',
    status: 'ENABLED',
    createdAt: '2025-12-02 09:00',
  },
  {
    id: 'store-002',
    code: 'CD002',
    name: '春熙旗舰店',
    city: '成都',
    address: '锦江区春熙路 18 号',
    manager: '王店长',
    phone: '13800000002',
    status: 'ENABLED',
    createdAt: '2026-01-14 11:20',
  },
  {
    id: 'store-003',
    code: 'CD003',
    name: '高新体验店',
    city: '成都',
    address: '高新区天府大道 230 号',
    manager: '赵店长',
    phone: '13800000003',
    status: 'DISABLED',
    createdAt: '2026-02-20 15:10',
  },
]

export const mockUsers: UserNode[] = [
  {
    id: 'user-001',
    username: 'owner',
    displayName: '张老板',
    role: 'OWNER',
    phone: '13800000000',
    status: 'ENABLED',
    updatedAt: '2026-03-18 08:30',
  },
  {
    id: 'user-002',
    username: 'szh',
    displayName: '李小七',
    role: 'CLERK',
    storeId: 'store-001',
    storeName: '城南总店',
    phone: '13811110000',
    status: 'ENABLED',
    updatedAt: '2026-03-20 17:10',
  },
  {
    id: 'user-003',
    username: 'wxd',
    displayName: '王小东',
    role: 'CLERK',
    storeId: 'store-002',
    storeName: '春熙旗舰店',
    phone: '13822220000',
    status: 'DISABLED',
    updatedAt: '2026-03-10 12:05',
  },
]

export const mockCategories: CategoryNode[] = [
  { id: 'c-1', parentId: null, level: 1, name: '食材', code: 'FOOD', defaultUnit: '斤', sortNo: 1, status: 'ENABLED' },
  { id: 'c-2', parentId: null, level: 1, name: '调料/配料', code: 'SEASON', defaultUnit: '袋', sortNo: 2, status: 'ENABLED' },
  { id: 'c-3', parentId: null, level: 1, name: '耗材', code: 'SUPPLY', defaultUnit: '包', sortNo: 3, status: 'ENABLED' },
  { id: 'c-4', parentId: null, level: 1, name: '费用', code: 'FEE', defaultUnit: '月', sortNo: 4, status: 'ENABLED' },
  { id: 'c-5', parentId: null, level: 1, name: '其他', code: 'OTHER', defaultUnit: '次', sortNo: 5, status: 'ENABLED' },
  { id: 'c-1-1', parentId: 'c-1', level: 2, name: '牛肉类', code: 'BEEF', defaultUnit: '斤', sortNo: 1, status: 'ENABLED' },
  { id: 'c-1-2', parentId: 'c-1', level: 2, name: '猪肉类', code: 'PORK', defaultUnit: '斤', sortNo: 2, status: 'ENABLED' },
  { id: 'c-1-3', parentId: 'c-1', level: 2, name: '面类', code: 'NOODLE', defaultUnit: '斤', sortNo: 3, status: 'ENABLED' },
  { id: 'c-2-1', parentId: 'c-2', level: 2, name: '日常调味', code: 'DAILY', defaultUnit: '瓶', sortNo: 1, status: 'ENABLED' },
  { id: 'c-2-2', parentId: 'c-2', level: 2, name: '香料干货', code: 'SPICE', defaultUnit: '袋', sortNo: 2, status: 'ENABLED' },
  { id: 'c-3-1', parentId: 'c-3', level: 2, name: '打包耗材', code: 'PACKAGE', defaultUnit: '箱', sortNo: 1, status: 'ENABLED' },
  { id: 'c-3-2', parentId: 'c-3', level: 2, name: '办公耗材', code: 'OFFICE', defaultUnit: '包', sortNo: 2, status: 'ENABLED' },
  { id: 'c-4-1', parentId: 'c-4', level: 2, name: '固定费用', code: 'FIXED', defaultUnit: '月', sortNo: 1, status: 'ENABLED' },
  { id: 'c-4-2', parentId: 'c-4', level: 2, name: '经营费用', code: 'OPERATE', defaultUnit: '次', sortNo: 2, status: 'ENABLED' },
]

export const mockExpenseRecords: ExpenseRecord[] = [
  {
    id: 'exp-001',
    storeId: 'store-001',
    storeName: '城南总店',
    expenseDate: '2026-03-21',
    categoryLevel1Id: 'c-1',
    categoryLevel1Name: '食材',
    categoryLevel2Id: 'c-1-1',
    categoryLevel2Name: '牛肉类',
    itemName: '牛腩',
    amount: 468.5,
    quantity: 18.5,
    unit: '斤',
    remark: '早市采购，含分装',
    createdBy: '李小七',
    updatedAt: '2026-03-21 09:40',
    status: 'NORMAL',
    history: [],
  },
  {
    id: 'exp-002',
    storeId: 'store-001',
    storeName: '城南总店',
    expenseDate: '2026-03-21',
    categoryLevel1Id: 'c-3',
    categoryLevel1Name: '耗材',
    categoryLevel2Id: 'c-3-1',
    categoryLevel2Name: '打包耗材',
    itemName: '一次性餐盒',
    amount: 286,
    quantity: 4,
    unit: '箱',
    remark: '中号带盖',
    createdBy: '李小七',
    updatedAt: '2026-03-21 10:12',
    status: 'NORMAL',
    history: [],
  },
  {
    id: 'exp-003',
    storeId: 'store-002',
    storeName: '春熙旗舰店',
    expenseDate: '2026-03-20',
    categoryLevel1Id: 'c-4',
    categoryLevel1Name: '费用',
    categoryLevel2Id: 'c-4-1',
    categoryLevel2Name: '固定费用',
    itemName: '房租',
    amount: 18000,
    quantity: null,
    unit: '月',
    remark: '3 月房租',
    createdBy: '王小东',
    updatedAt: '2026-03-20 08:10',
    status: 'NORMAL',
    history: [],
  },
]

export const mockHistoryEntries: ExpenseHistoryEntry[] = [
  {
    id: 'hist-001',
    operator: '李小七',
    operateAt: '2026-03-21 09:40',
    action: 'CREATE',
    beforeText: '无',
    afterText: '新增牛腩 18.5 斤，金额 468.50 元',
  },
  {
    id: 'hist-002',
    operator: '张老板',
    operateAt: '2026-03-21 12:15',
    action: 'UPDATE',
    beforeText: '一次性餐盒 4 箱，金额 260.00 元',
    afterText: '一次性餐盒 4 箱，金额 286.00 元',
  },
]

export const mockMetrics: MetricCardItem[] = [
  { label: '今日支出', value: '¥ 8,764.50', hint: '对比昨日 +9.2%', trend: '+9.2%', tone: 'primary' },
  { label: '本月支出', value: '¥ 136,280.80', hint: '较上月 -3.1%', trend: '-3.1%', tone: 'success' },
  { label: '高频品项', value: '牛腩 / 纸巾 / 房租', hint: '覆盖 48 笔记录', trend: '48 笔', tone: 'warning' },
  { label: '门店覆盖', value: '3 家', hint: '2 家营业中，1 家停用', trend: '1 停用', tone: 'danger' },
]

export const mockTrendPoints: TrendPoint[] = [
  { label: '03-15', value: 4800 },
  { label: '03-16', value: 5300 },
  { label: '03-17', value: 4900 },
  { label: '03-18', value: 6800 },
  { label: '03-19', value: 7200 },
  { label: '03-20', value: 6100 },
  { label: '03-21', value: 8764 },
]

export const mockCategoryShares: CategoryShare[] = [
  { label: '食材', value: 58, color: '#E36B2C' },
  { label: '费用', value: 21, color: '#2858FF' },
  { label: '耗材', value: 14, color: '#1EAE98' },
  { label: '其他', value: 7, color: '#9B6BFF' },
]

export const mockRankings: RankingItem[] = [
  { label: '牛腩', value: 16240, count: 32 },
  { label: '房租', value: 18000, count: 1 },
  { label: '一次性餐盒', value: 9280, count: 18 },
  { label: '纸巾', value: 5460, count: 24 },
  { label: '花椒', value: 3840, count: 12 },
]

export const mockStoreComparisons: StoreComparisonItem[] = [
  { storeId: 'store-001', storeName: '城南总店', today: 8764.5, month: 136280.8, lastMonth: 140680.2, growth: -3.1 },
  { storeId: 'store-002', storeName: '春熙旗舰店', today: 7064.2, month: 117640.4, lastMonth: 106800.9, growth: 10.1 },
  { storeId: 'store-003', storeName: '高新体验店', today: 0, month: 0, lastMonth: 0, growth: 0 },
]

export function formatCurrency(value: number): string {
  return `¥ ${value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function formatDateTime(value: string): string {
  return value
}

export function resolveStatusText(value: StatusFlag): string {
  return value === 'ENABLED' ? '启用' : '停用'
}

export function resolveRoleText(value: RoleType): string {
  return value === 'OWNER' ? '老板' : '录入员'
}

export function resolveActionText(value: ExpenseHistoryEntry['action']): string {
  if (value === 'CREATE') return '新增'
  if (value === 'UPDATE') return '修改'
  return '删除'
}

export function sumExpense(records: ExpenseRecord[]): number {
  return records.reduce((total, item) => total + (item.status === 'DELETED' ? 0 : item.amount), 0)
}

export function getCategoryChildren(parentId: string): CategoryNode[] {
  return mockCategories.filter((item) => item.parentId === parentId)
}

export function getStoreById(storeId: string): StoreNode | undefined {
  return mockStores.find((item) => item.id === storeId)
}

export function getCategoryById(categoryId: string): CategoryNode | undefined {
  return mockCategories.find((item) => item.id === categoryId)
}

export function createExpenseTemplate(storeId = mockStores[0]?.id ?? ''): ExpenseFormModel {
  const firstLevel = mockCategories.find((item) => item.level === 1 && item.status === 'ENABLED')
  const secondLevel = firstLevel ? getCategoryChildren(firstLevel.id).find((item) => item.status === 'ENABLED') : undefined

  return {
    storeId,
    expenseDate: '2026-03-21',
    categoryLevel1Id: firstLevel?.id ?? '',
    categoryLevel2Id: secondLevel?.id ?? '',
    itemName: '',
    amount: null,
    quantity: null,
    unit: secondLevel?.defaultUnit ?? '斤',
    remark: '',
  }
}
