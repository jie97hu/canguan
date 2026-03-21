import { DataLine, Collection, Coin, Odometer, OfficeBuilding, Setting, User } from '@element-plus/icons-vue'

import type { NavigationNode } from '@/types/navigation'

export const navigationTree: NavigationNode[] = [
  {
    key: 'dashboard-group',
    title: '老板首页',
    icon: Odometer,
    roles: ['OWNER'],
    children: [
      {
        key: 'dashboard',
        title: '总览看板',
        path: '/dashboard',
        icon: Odometer,
        roles: ['OWNER'],
        order: 1,
      },
    ],
  },
  {
    key: 'expense-group',
    title: '支出管理',
    icon: Coin,
    roles: ['OWNER', 'CLERK'],
    children: [
      {
        key: 'expenses',
        title: '支出明细',
        path: '/expenses',
        icon: Coin,
        roles: ['OWNER', 'CLERK'],
        order: 2,
      },
    ],
  },
  {
    key: 'master-data-group',
    title: '基础管理',
    icon: Setting,
    roles: ['OWNER'],
    children: [
      {
        key: 'stores',
        title: '分店管理',
        path: '/stores',
        icon: OfficeBuilding,
        roles: ['OWNER'],
        order: 3,
      },
      {
        key: 'categories',
        title: '分类管理',
        path: '/categories',
        icon: Collection,
        roles: ['OWNER'],
        order: 4,
      },
      {
        key: 'users',
        title: '账号管理',
        path: '/users',
        icon: User,
        roles: ['OWNER'],
        order: 5,
      },
    ],
  },
  {
    key: 'report-group',
    title: '统计分析',
    icon: DataLine,
    roles: ['OWNER'],
    children: [
      {
        key: 'reports',
        title: '统计报表',
        path: '/reports',
        icon: DataLine,
        roles: ['OWNER'],
        order: 6,
      },
    ],
  },
]
