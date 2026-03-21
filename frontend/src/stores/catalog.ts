import { defineStore } from 'pinia'

import { listCategoryTreeApi, listStoreOptionsApi } from '@/api/catalog'
import type { CategoryNodeDto } from '@/types/category'
import type { StoreDto } from '@/types/store'

export const useCatalogStore = defineStore('catalog', {
  state: () => ({
    storeOptions: [] as StoreDto[],
    categoryTree: [] as CategoryNodeDto[],
    loaded: false,
    loading: false,
  }),
  getters: {
    categoryLevel1Options: (state) => state.categoryTree,
  },
  actions: {
    async bootstrap() {
      if (this.loading) {
        return
      }

      this.loading = true
      try {
        const [stores, categories] = await Promise.all([listStoreOptionsApi(), listCategoryTreeApi()])
        this.storeOptions = stores
        this.categoryTree = categories
        this.loaded = true
      } finally {
        this.loading = false
      }
    },
  },
})
