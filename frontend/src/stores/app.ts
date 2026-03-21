import { defineStore } from 'pinia'

import { readAppSettings, writeAppSettings } from '@/app/storage'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: readAppSettings().sidebarCollapsed,
    pageLoading: false,
  }),
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      writeAppSettings({ sidebarCollapsed: this.sidebarCollapsed })
    },
    setPageLoading(value: boolean) {
      this.pageLoading = value
    },
  },
})
