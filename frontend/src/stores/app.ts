import { defineStore } from 'pinia'

import { readAppSettings, writeAppSettings } from '@/app/storage'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: readAppSettings().sidebarCollapsed,
    isMobile: false,
    mobileNavVisible: false,
    pageLoading: false,
  }),
  actions: {
    setSidebarCollapsed(value: boolean) {
      this.sidebarCollapsed = value
      writeAppSettings({ sidebarCollapsed: value })
    },
    toggleSidebar() {
      if (this.isMobile) {
        this.mobileNavVisible = !this.mobileNavVisible
        return
      }
      this.setSidebarCollapsed(!this.sidebarCollapsed)
    },
    setMobileViewport(value: boolean) {
      this.isMobile = value
      if (!value) {
        this.mobileNavVisible = false
      }
    },
    setMobileNavVisible(value: boolean) {
      this.mobileNavVisible = value
    },
    closeMobileNav() {
      this.mobileNavVisible = false
    },
    setPageLoading(value: boolean) {
      this.pageLoading = value
    },
  },
})
