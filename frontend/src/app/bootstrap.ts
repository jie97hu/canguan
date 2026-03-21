import type { Pinia } from 'pinia'
import type { Router } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { useCatalogStore } from '@/stores/catalog'
import { installRouterGuards } from '@/router'

export async function bootstrapFrontend(options: { pinia: Pinia; router: Router }) {
  installRouterGuards(options.router, options.pinia)

  const authStore = useAuthStore(options.pinia)
  await authStore.bootstrap()

  if (authStore.isAuthenticated) {
    const catalogStore = useCatalogStore(options.pinia)
    await catalogStore.bootstrap()
  }
}
