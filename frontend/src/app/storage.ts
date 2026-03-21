import type { AuthSession } from '@/types/auth'
import type { AppSettings } from '@/types/navigation'

import { STORAGE_KEYS } from '@/app/config'

function readJson<T>(key: string): T | null {
  try {
    const raw = window.localStorage.getItem(key)
    if (!raw) {
      return null
    }
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

function writeJson(key: string, value: unknown) {
  window.localStorage.setItem(key, JSON.stringify(value))
}

export function readAuthSession() {
  return readJson<AuthSession>(STORAGE_KEYS.authSession)
}

export function writeAuthSession(session: AuthSession | null) {
  if (!session) {
    window.localStorage.removeItem(STORAGE_KEYS.authSession)
    return
  }
  writeJson(STORAGE_KEYS.authSession, session)
}

export function readAppSettings() {
  return (
    readJson<AppSettings>(STORAGE_KEYS.appSettings) ?? {
      sidebarCollapsed: false,
    }
  )
}

export function writeAppSettings(settings: AppSettings) {
  writeJson(STORAGE_KEYS.appSettings, settings)
}
