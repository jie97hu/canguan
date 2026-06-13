import { requestData } from '@/api/http'
import type { UnitOptionQueryReq } from '@/types/unit'

export function listUnitOptionsApi(params: UnitOptionQueryReq = {}) {
  return requestData<string[]>({
    url: '/units/options',
    method: 'GET',
    params,
  })
}
