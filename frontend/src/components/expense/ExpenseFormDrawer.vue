<template>
  <el-drawer v-model="visible" :title="title" size="520px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="expense-form">
      <el-form-item label="门店" prop="storeId">
        <el-select v-model="form.storeId" filterable :disabled="storeLocked" placeholder="选择门店">
          <el-option v-for="item in stores" :key="item.id" :label="item.name" :value="item.id" :disabled="item.status === 'DISABLED'" />
        </el-select>
      </el-form-item>
      <el-form-item label="支出日期" prop="expenseDate">
        <el-date-picker v-model="form.expenseDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <div class="category-grid">
        <el-form-item label="一级分类" prop="categoryLevel1Id">
          <el-select v-model="form.categoryLevel1Id" filterable placeholder="选择一级分类" @change="handleLevel1Change">
            <el-option v-for="item in level1Options" :key="item.id" :label="item.name" :value="item.id" :disabled="item.status === 'DISABLED'" />
          </el-select>
        </el-form-item>
        <el-form-item label="二级分类" prop="categoryLevel2Id">
          <el-select v-model="form.categoryLevel2Id" filterable placeholder="选择二级分类" @change="handleLevel2Change">
            <el-option v-for="item in level2Options" :key="item.id" :label="item.name" :value="item.id" :disabled="item.status === 'DISABLED'" />
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="品项名称" prop="itemName">
        <el-input v-model="form.itemName" placeholder="例如：牛腩、一次性餐盒" />
      </el-form-item>
      <div class="category-grid">
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="10" controls-position="right" />
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="0" :precision="3" :step="1" controls-position="right" />
        </el-form-item>
      </div>
      <div class="category-grid">
        <el-form-item label="单位" prop="unit">
          <el-select v-model="form.unit" filterable placeholder="单位">
            <el-option v-for="item in unitOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="可选备注" />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <div class="drawer-actions">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitForm">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createExpenseTemplate, getCategoryById, getCategoryChildren, unitOptions } from '@/app/canguan'
import type { CategoryNodeDto } from '@/types/category'
import type { StoreDto } from '@/types/store'
import type { ExpenseFormModel } from '@/types/ui'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    stores: StoreDto[]
    categories: CategoryNodeDto[]
    storeLocked?: boolean
    loading?: boolean
    initialValue?: Partial<ExpenseFormModel> | null
  }>(),
  {
    title: '新增支出',
    storeLocked: false,
    loading: false,
    initialValue: null,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', value: ExpenseFormModel): void
}>()

const formRef = ref<FormInstance>()

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const form = reactive<ExpenseFormModel>(createExpenseTemplate({ stores: [], categories: [] }))

const rules: FormRules<ExpenseFormModel> = {
  storeId: [{ required: true, message: '请选择门店', trigger: 'change' }],
  expenseDate: [{ required: true, message: '请选择支出日期', trigger: 'change' }],
  categoryLevel1Id: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
  categoryLevel2Id: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
  itemName: [{ required: true, message: '请输入品项名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'change' }],
}

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      // Rebuild the local form each time the drawer opens to avoid stale edits.
      const template = createExpenseTemplate({
        stores: props.stores,
        categories: props.categories,
        storeId: props.initialValue?.storeId ?? props.stores[0]?.id ?? '',
      })
      Object.assign(form, template, props.initialValue ?? {})
      if (!form.unit) {
        form.unit = resolveDefaultUnit(form.categoryLevel2Id)
      }
    }
  },
  { immediate: true },
)

const level1Options = computed(() => props.categories.filter((item) => item.level === 1))
const level2Options = computed(() => getCategoryChildren(props.categories, form.categoryLevel1Id))

function resolveDefaultUnit(categoryId: number | ''): string {
  const category = getCategoryById(props.categories, categoryId)
  return category?.defaultUnit ?? '斤'
}

function handleLevel1Change(categoryId: number | '') {
  const next = getCategoryChildren(props.categories, categoryId).find((item) => item.status === 'ENABLED')
  form.categoryLevel2Id = next?.id ?? ''
  form.unit = next?.defaultUnit ?? resolveDefaultUnit(categoryId)
}

function handleLevel2Change(categoryId: number | '') {
  form.unit = resolveDefaultUnit(categoryId)
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请先完成必填项')
    return
  }
  emit('submit', { ...form, amount: form.amount ?? 0 })
}
</script>

<style scoped>
.expense-form {
  display: grid;
  gap: 4px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 960px) {
  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>
