<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Store Management"
      title="分店管理"
      description="统一维护门店名称、编码和状态。停用后不可新增支出，但历史数据仍可查询。"
    >
      <template #meta>
        <el-tag effect="light" round>当前门店数：{{ stores.length }}</el-tag>
        <el-tag effect="light" round type="success">营业中：{{ activeCount }}</el-tag>
        <el-tag effect="light" round type="warning">停用：{{ disabledCount }}</el-tag>
      </template>
      <template #actions>
        <el-button type="primary" @click="openCreate">新增分店</el-button>
      </template>
    </PageHero>

    <PageSection title="分店列表" description="支持按名称或编码筛选，并直接启停分店。">
      <el-form :model="filters" inline label-position="top" class="responsive-filter-form">
        <el-form-item label="关键字">
          <el-input v-model="filters.keyword" clearable placeholder="搜索名称或编码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部状态">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <div class="form-actions">
            <el-button type="primary" :loading="loading" @click="applyFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div class="table-shell table-shell--wide">
        <el-table v-loading="loading" :data="stores" border stripe>
          <el-table-column prop="code" label="编码" width="160" />
          <el-table-column prop="name" label="名称" min-width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <StatusTag :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <div class="mobile-row-actions">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link :type="row.status === 'ENABLED' ? 'warning' : 'success'" @click="toggleStatus(row)">
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </PageSection>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="appStore.isMobile ? 'calc(100vw - 24px)' : '520px'">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：南坪店" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="例如：NP001" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveStore">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

import PageHero from '@/components/common/PageHero.vue'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useAppStore } from '@/stores/app'
import { createStoreApi, listStoresApi, patchStoreStatusApi, updateStoreApi } from '@/api/catalog'
import type { StoreDto, StoreQueryReq, StoreUpsertReq } from '@/types/store'

const appStore = useAppStore()
const loading = ref(false)
const saving = ref(false)
const stores = ref<StoreDto[]>([])
const filters = reactive<StoreQueryReq>({
  keyword: '',
  status: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增分店')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<StoreUpsertReq>({
  name: '',
  code: '',
  status: 'ENABLED',
})

const rules: FormRules<StoreUpsertReq> = {
  name: [{ required: true, message: '请输入分店名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分店编码', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const activeCount = computed(() => stores.value.filter((item) => item.status === 'ENABLED').length)
const disabledCount = computed(() => stores.value.filter((item) => item.status === 'DISABLED').length)

// 分店列表直接走后端筛选，避免前端和后端口径不一致。
async function loadStores() {
  loading.value = true
  try {
    const result = await listStoresApi({
      ...filters,
      pageNo: 1,
      pageSize: 200,
    })
    stores.value = result.list
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载分店失败')
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  loadStores()
}

function resetFilter() {
  filters.keyword = ''
  filters.status = ''
  loadStores()
}

function openCreate() {
  dialogTitle.value = '新增分店'
  editingId.value = null
  Object.assign(form, {
    name: '',
    code: '',
    status: 'ENABLED',
  })
  dialogVisible.value = true
}

function openEdit(row: StoreDto) {
  dialogTitle.value = '编辑分店'
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    code: row.code,
    status: row.status,
  })
  dialogVisible.value = true
}

async function saveStore() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateStoreApi(editingId.value, { ...form })
      ElMessage.success('分店已更新')
    } else {
      await createStoreApi({ ...form })
      ElMessage.success('分店已新增')
    }
    dialogVisible.value = false
    await loadStores()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存分店失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: StoreDto) {
  try {
    await patchStoreStatusApi(row.id, {
      status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED',
    })
    ElMessage.success(`已${row.status === 'ENABLED' ? '停用' : '启用'}${row.name}`)
    await loadStores()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '更新分店状态失败')
  }
}

onMounted(() => {
  loadStores()
})
</script>

<style scoped>
.page-wrap {
  padding: 20px;
  display: grid;
  gap: 18px;
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }
}
</style>
