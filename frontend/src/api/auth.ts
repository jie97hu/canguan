import { requestData } from '@/api/http'
import type { AuthSession, CurrentUserDto, LoginReq, LoginRes } from '@/types/auth'

export function loginApi(data: LoginReq) {
  return requestData<LoginRes>({
    url: '/auth/login',
    method: 'POST',
    data,
  })
}

export function refreshTokenApi(data: { refreshToken: string }) {
  return requestData<LoginRes>({
    url: '/auth/refresh',
    method: 'POST',
    data,
  })
}

export function logoutApi(data: { refreshToken: string }) {
  return requestData<void>({
    url: '/auth/logout',
    method: 'POST',
    data,
  })
}

export function meApi() {
  return requestData<CurrentUserDto>({
    url: '/auth/me',
    method: 'GET',
  })
}
