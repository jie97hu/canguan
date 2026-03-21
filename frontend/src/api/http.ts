import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'

import { readAuthSession, writeAuthSession } from '@/app/storage'
import { APP_NAME } from '@/app/config'
import { isMockEnabled } from '@/api/mock'
import type { ApiExceptionPayload, ApiResponse } from '@/types/common'
import type { AuthSession } from '@/types/auth'

export class ApiException extends Error implements ApiExceptionPayload {
  code: ApiExceptionPayload['code']
  data?: unknown

  constructor(payload: ApiExceptionPayload) {
    super(payload.message)
    this.name = 'ApiException'
    this.code = payload.code
    this.data = payload.data
  }
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const session = readAuthSession()
  if (session?.accessToken) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${session.accessToken}`
  }
  return config
})

function normalizeSession(session: AuthSession | null) {
  if (!session) {
    return
  }
  writeAuthSession(session)
}

function normalizeError(error: unknown): ApiException {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiResponse<unknown> | undefined
    if (data && typeof data.code === 'string') {
      return new ApiException({ code: data.code, message: data.message, data: data.data })
    }
    return new ApiException({ code: 'SYSTEM_ERROR', message: error.message || `${APP_NAME} 请求失败` })
  }

  if (error instanceof ApiException) {
    return error
  }

  return new ApiException({ code: 'SYSTEM_ERROR', message: error instanceof Error ? error.message : '系统错误' })
}

export async function requestEnvelope<T>(config: AxiosRequestConfig): Promise<ApiResponse<T>> {
  if (isMockEnabled()) {
    const { mockRequest } = await import('@/api/mock')
    return mockRequest<T>(config)
  }

  try {
    const response = await http.request<ApiResponse<T>>(config)
    return response.data
  } catch (error) {
    throw normalizeError(error)
  }
}

export async function requestData<T>(config: AxiosRequestConfig): Promise<T> {
  const envelope = await requestEnvelope<T>(config)
  if (envelope.code !== 'SUCCESS') {
    throw new ApiException({ code: envelope.code, message: envelope.message, data: envelope.data })
  }
  return envelope.data
}

export { normalizeSession }
