import { createRouter, createWebHistory } from 'vue-router'
import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'

import { APP_NAME } from '@/app/config'
import { hasRoleAccess, isRoutePublic, resolveHomePath } from '@/app/permission'
import type { RouteAccessMeta } from '@/types/auth'
import { useAuthStore } from '@/stores/auth'

import { appRoutes } from './routes'

export function createAppRouter() {
  return createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: appRoutes,
    scrollBehavior() {
      return { top: 0 }
    },
  })
}

export function installRouterGuards(router: Router, pinia: Pinia) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore(pinia)
    await authStore.bootstrap()

    if (to.path === '/') {
      return authStore.isAuthenticated ? resolveHomePath(authStore.role) : { name: 'login' }
    }

    // 未登录时统一收口到登录页，避免直接打开业务地址或公共兜底页时绕过登录入口。
    if (!authStore.isAuthenticated && to.name !== 'login') {
      return {
        name: 'login',
        query: {
          redirect: to.fullPath,
        },
      }
    }

    if (isRoutePublic(to.meta)) {
      if (to.name === 'login' && authStore.isAuthenticated) {
        return resolveHomePath(authStore.role)
      }
      return true
    }

    const meta = to.meta as Partial<RouteAccessMeta>
    if (!hasRoleAccess(meta.roles, authStore.role)) {
      return { name: 'forbidden' }
    }

    return true
  })

  router.afterEach((to) => {
    const title = typeof to.meta.title === 'string' ? `${to.meta.title} · ${APP_NAME}` : APP_NAME
    document.title = title
  })
}
