import { requestData } from '@/api/http'
import type { CategoryBreakdownDto, ItemRankingDto, OverviewDto, StoreComparisonDto, TrendPointDto } from '@/types/report'

export function getOverviewApi(params: { storeId?: number | ''; dateStart?: string; dateEnd?: string } = {}) {
  return requestData<OverviewDto>({
    url: '/reports/overview',
    method: 'GET',
    params,
  })
}

export function getTrendApi(params: {
  storeId?: number | ''
  dateStart?: string
  dateEnd?: string
  categoryLevel1Id?: number | ''
  categoryLevel2Id?: number | ''
} = {}) {
  return requestData<TrendPointDto[]>({
    url: '/reports/trend',
    method: 'GET',
    params,
  })
}

export function getCategoryBreakdownApi(params: {
  storeId?: number | ''
  dateStart?: string
  dateEnd?: string
  categoryLevel?: 1 | 2 | ''
} = {}) {
  return requestData<CategoryBreakdownDto[]>({
    url: '/reports/category-breakdown',
    method: 'GET',
    params,
  })
}

export function getItemRankingApi(params: {
  storeId?: number | ''
  dateStart?: string
  dateEnd?: string
  categoryLevel1Id?: number | ''
  categoryLevel2Id?: number | ''
  topN?: number
} = {}) {
  return requestData<ItemRankingDto[]>({
    url: '/reports/item-ranking',
    method: 'GET',
    params,
  })
}

export function getStoreComparisonApi(params: {
  dateStart?: string
  dateEnd?: string
  categoryLevel1Id?: number | ''
  categoryLevel2Id?: number | ''
} = {}) {
  return requestData<StoreComparisonDto[]>({
    url: '/reports/store-comparison',
    method: 'GET',
    params,
  })
}
