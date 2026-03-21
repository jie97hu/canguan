export interface OverviewDto {
  todayAmount: number
  monthAmount: number
  rangeAmount: number
  storeCount: number
  topStoreName: string
  topStoreAmount: number
}

export interface TrendPointDto {
  label: string
  amount: number
}

export interface CategoryBreakdownDto {
  categoryId: number
  categoryName: string
  amount: number
  ratio: number
}

export interface ItemRankingDto {
  itemName: string
  amount: number
  rankNo: number
}

export interface StoreComparisonDto {
  storeId: number
  storeName: string
  amount: number
  rankNo: number
}
