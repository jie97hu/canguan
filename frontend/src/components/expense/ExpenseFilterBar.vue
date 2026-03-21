<template>
  <el-form class="filter-bar responsive-filter-form" :model="model" inline label-position="top">
    <el-form-item label="门店">
      <el-select v-model="model.storeId" clearable placeholder="全部门店" class="filter-item">
        <el-option v-for="item in stores" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="日期">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        class="filter-item"
      />
    </el-form-item>
    <el-form-item label="一级分类">
      <el-select v-model="model.categoryLevel1Id" clearable placeholder="全部一级分类" class="filter-item">
        <el-option v-for="item in level1Options" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="二级分类">
      <el-select v-model="model.categoryLevel2Id" clearable placeholder="全部二级分类" class="filter-item">
        <el-option v-for="item in level2Options" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="品项">
      <el-input v-model="model.itemName" clearable placeholder="搜索品项名称" class="filter-item" />
    </el-form-item>
    <el-form-item label="操作">
      <div class="action-group">
        <el-button type="primary" @click="$emit('search')">查询</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
        <slot name="extra" />
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { CategoryNodeDto } from '@/types/category'
import type { StoreDto } from '@/types/store'
import type { ExpenseFilterModel } from '@/types/ui'

const props = defineProps<{
  modelValue: ExpenseFilterModel
  stores: StoreDto[]
  categories: CategoryNodeDto[]
}>()

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
  (e: 'update:modelValue', value: ExpenseFilterModel): void
}>()

const model = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const level1Options = computed(() => props.categories.filter((item) => item.level === 1))
const level2Options = computed(() => props.categories.filter((item) => item.level === 2))

const dateRange = computed({
  get: () => model.value.dateRange,
  set: (value?: string[]) => {
    emit('update:modelValue', { ...model.value, dateRange: value ?? [] })
  },
})
</script>

<style scoped>
.filter-item {
  width: 240px;
}

.action-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

@media (max-width: 720px) {
  .filter-item,
  .action-group,
  .action-group :deep(.el-button) {
    width: 100%;
  }

  .action-group {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
