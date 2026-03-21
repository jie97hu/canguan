import type { Component } from 'vue'
import type { AppRole } from '@/types/auth'

export interface NavigationNode {
  key: string
  title: string
  path?: string
  icon?: Component
  roles?: AppRole[]
  order?: number
  children?: NavigationNode[]
}

export interface AppSettings {
  sidebarCollapsed: boolean
}
