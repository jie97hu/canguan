import { defineStore } from 'pinia'

import { APP_NAME } from '@/app/config'
import { readAuthSession, writeAuthSession } from '@/app/storage'
import { loginApi, logoutApi, meApi, refreshTokenApi } from '@/api/auth'
import type { AuthSession, AppRole, CurrentUserDto, LoginReq } from '@/types/auth'

function nowIso() {
  return new Date().toISOString()
}

function createEmptyState() {
  return {
    accessToken: '',
    refreshToken: '',
    expiresIn: 0,
    userInfo: null as CurrentUserDto | null,
    hydrated: false,
    hydrating: false,
    lastError: '',
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => createEmptyState(),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken && state.refreshToken && state.userInfo),
    role: (state) => state.userInfo?.role ?? null,
    displayName: (state) => state.userInfo?.displayName ?? APP_NAME,
    storeName: (state) => state.userInfo?.storeName ?? null,
    homePath: (state) => (state.userInfo?.role === 'OWNER' ? '/dashboard' : '/expenses'),
  },
  actions: {
    applySession(session: AuthSession | null) {
      if (!session) {
        Object.assign(this, createEmptyState())
        writeAuthSession(null)
        return
      }

      this.accessToken = session.accessToken
      this.refreshToken = session.refreshToken
      this.expiresIn = session.expiresIn
      this.userInfo = session.userInfo
      this.lastError = ''
      this.hydrated = true
      this.hydrating = false
      writeAuthSession(session)
    },
    clearSession() {
      Object.assign(this, createEmptyState())
      writeAuthSession(null)
    },
    async bootstrap() {
      if (this.hydrated || this.hydrating) {
        return
      }

      this.hydrating = true
      const session = readAuthSession()
      if (!session) {
        this.hydrated = true
        this.hydrating = false
        return
      }

      this.accessToken = session.accessToken
      this.refreshToken = session.refreshToken
      this.expiresIn = session.expiresIn
      this.userInfo = session.userInfo

      try {
        const latest = await meApi()
        this.userInfo = latest
        this.applySession({
          ...session,
          userInfo: latest,
          savedAt: nowIso(),
        })
      } catch (error) {
        this.lastError = error instanceof Error ? error.message : '登录态已失效'
        this.clearSession()
      } finally {
        this.hydrated = true
        this.hydrating = false
      }
    },
    async login(payload: LoginReq) {
      const result = await loginApi(payload)
      this.applySession({
        accessToken: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        userInfo: result.userInfo,
        savedAt: nowIso(),
      })
    },
    async refreshSession() {
      if (!this.refreshToken) {
        throw new Error('缺少刷新令牌')
      }

      const result = await refreshTokenApi({ refreshToken: this.refreshToken })
      this.applySession({
        accessToken: result.accessToken,
        refreshToken: result.refreshToken,
        expiresIn: result.expiresIn,
        userInfo: result.userInfo,
        savedAt: nowIso(),
      })
    },
    async logout() {
      const refreshToken = this.refreshToken
      try {
        if (refreshToken) {
          await logoutApi({ refreshToken })
        }
      } finally {
        this.clearSession()
      }
    },
    hasRole(role: AppRole | AppRole[] | undefined) {
      if (!role) {
        return true
      }
      const roleList = Array.isArray(role) ? role : [role]
      return Boolean(this.role && roleList.includes(this.role))
    },
  },
})
