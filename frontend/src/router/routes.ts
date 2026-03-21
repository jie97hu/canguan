import type { RouteRecordRaw } from 'vue-router'

import AppLayout from '@/layouts/AppLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import ForbiddenPage from '@/app/pages/ForbiddenPage.vue'
import LoginPage from '@/app/pages/LoginPage.vue'
import NotFoundPage from '@/app/pages/NotFoundPage.vue'
import CategoryManagementView from '@/views/CategoryManagementView.vue'
import ExpenseListView from '@/views/ExpenseListView.vue'
import OwnerDashboardView from '@/views/OwnerDashboardView.vue'
import StoreAnalysisView from '@/views/StoreAnalysisView.vue'
import StoreManagementView from '@/views/StoreManagementView.vue'
import UserManagementView from '@/views/UserManagementView.vue'

export const appRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: AuthLayout,
    meta: {
      title: '登录',
      public: true,
      hideInMenu: true,
    },
    children: [
      {
        path: '',
        name: 'login',
        component: LoginPage,
        meta: {
          title: '登录',
          public: true,
          hideInMenu: true,
        },
      },
    ],
  },
  {
    path: '/',
    component: AppLayout,
    meta: {
      title: '工作台',
      hideInMenu: true,
    },
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: OwnerDashboardView,
        meta: {
          title: '老板首页',
          roles: ['OWNER'],
          icon: 'Odometer',
          order: 1,
        },
      },
      {
        path: 'expenses',
        name: 'expenses',
        component: ExpenseListView,
        meta: {
          title: '支出记账',
          roles: ['OWNER', 'CLERK'],
          icon: 'Coin',
          order: 2,
        },
      },
      {
        path: 'stores',
        name: 'stores',
        component: StoreManagementView,
        meta: {
          title: '分店管理',
          roles: ['OWNER'],
          icon: 'OfficeBuilding',
          order: 3,
        },
      },
      {
        path: 'stores/:id/analysis',
        name: 'store-analysis',
        component: StoreAnalysisView,
        meta: {
          title: '分店分析',
          roles: ['OWNER'],
          hideInMenu: true,
        },
      },
      {
        path: 'categories',
        name: 'categories',
        component: CategoryManagementView,
        meta: {
          title: '分类管理',
          roles: ['OWNER'],
          icon: 'Collection',
          order: 4,
        },
      },
      {
        path: 'users',
        name: 'users',
        component: UserManagementView,
        meta: {
          title: '账号管理',
          roles: ['OWNER'],
          icon: 'User',
          order: 5,
        },
      },
      {
        path: 'reports',
        name: 'reports',
        component: StoreAnalysisView,
        meta: {
          title: '统计分析',
          roles: ['OWNER'],
          icon: 'DataLine',
          order: 6,
        },
      },
    ],
  },
  {
    path: '/403',
    name: 'forbidden',
    component: ForbiddenPage,
    meta: {
      title: '无权限',
      public: true,
      hideInMenu: true,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundPage,
    meta: {
      title: '页面不存在',
      public: true,
      hideInMenu: true,
    },
  },
]
