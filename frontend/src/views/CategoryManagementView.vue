<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Category Management"
      title="分类管理"
      description="统一维护一级/二级分类、默认单位和排序号，保证记账录入和报表统计口径一致。"
    >
      <template #meta>
        <el-tag effect="light" round>一级分类：{{ firstLevelCount }}</el-tag>
        <el-tag effect="light" round type="success">二级分类：{{ secondLevelCount }}</el-tag>
      </template>
      <template #actions>
        <el-button @click="openCreateLevel1">新增一级分类</el-button>
        <el-button type="primary" @click="openCreateLevel2">新增二级分类</el-button>
      </template>
    </PageHero>

    <div class="category-grid">
      <PageSection title="分类树" description="左侧树展示真实分类结构，支持直接切换到明细。">
        <el-tree
          v-loading="loading"
          :data="treeData"
          node-key="id"
          default-expand-all
          highlight-current
          :current-node-key="selectedId || undefined"
          :props="{ children: 'children', label: 'name' }"
          @node-click="handleSelect"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <span>{{ data.name }}</span>
              <StatusTag :value="data.status" />
            </div>
          </template>
        </el-tree>
      </PageSection>

      <PageSection :title="selectedCategory?.name ?? '分类详情'" description="右侧展示当前节点详情和同级子分类。">
        <div v-if="selectedCategory" class="detail-card">
          <div class="detail-row"><span>层级</span><strong>第 {{ selectedCategory.level }} 级</strong></div>
          <div class="detail-row"><span>编码</span><strong>{{ selectedCategory.code }}</strong></div>
          <div class="detail-row"><span>默认单位</span><strong>{{ selectedCategory.defaultUnit || '-' }}</strong></div>
          <div class="detail-row"><span>排序</span><strong>{{ selectedCategory.sortNo }}</strong></div>
          <div class="detail-row"><span>状态</span><StatusTag :value="selectedCategory.status" /></div>
        </div>

        <div class="table-shell table-shell--wide">
          <el-table :data="childrenRows" border stripe class="children-table">
            <el-table-column prop="name" label="名称" min-width="150" />
            <el-table-column prop="code" label="编码" width="140" />
            <el-table-column prop="defaultUnit" label="默认单位" width="120">
              <template #default="{ row }">{{ row.defaultUnit || '-' }}</template>
            </el-table-column>
            <el-table-column prop="sortNo" label="排序" width="90" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <StatusTag :value="row.status" />
              </template>
            </el-table-column>
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
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="appStore.isMobile ? 'calc(100vw - 24px)' : '600px'">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="grid">
          <el-form-item label="层级" prop="level">
            <el-select v-model="form.level" @change="handleLevelChange">
              <el-option label="一级分类" :value="1" />
              <el-option label="二级分类" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="父分类" prop="parentId">
            <el-select v-model="form.parentId" clearable :disabled="form.level === 1">
              <el-option v-for="item in firstLevelCategories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="编码" prop="code">
            <el-input v-model="form.code" />
          </el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="默认单位" prop="defaultUnit">
            <el-select v-model="form.defaultUnit" clearable allow-create filterable>
              <el-option v-for="unit in unitOptions" :key="unit" :label="unit" :value="unit" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序号" prop="sortNo">
            <el-input-number v-model="form.sortNo" :min="0" />
          </el-form-item>
        </div>
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
          <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

import { flattenCategoryTree, unitOptions } from '@/app/canguan'
import PageHero from '@/components/common/PageHero.vue'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useAppStore } from '@/stores/app'
import { createCategoryApi, listCategoryTreeApi, patchCategoryStatusApi, updateCategoryApi } from '@/api/catalog'
import type { CategoryNodeDto, CategoryUpsertReq } from '@/types/category'

const appStore = useAppStore()
const loading = ref(false)
const saving = ref(false)
const treeData = ref<CategoryNodeDto[]>([])
const selectedId = ref<number | null>(null)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CategoryUpsertReq>({
  parentId: 0,
  level: 1,
  name: '',
  code: '',
  defaultUnit: null,
  sortNo: 0,
  status: 'ENABLED',
})

const flatCategories = computed(() => flattenCategoryTree(treeData.value))
const firstLevelCategories = computed(() => treeData.value)
const secondLevelCategories = computed(() => flatCategories.value.filter((item) => item.level === 2))
const firstLevelCount = computed(() => firstLevelCategories.value.length)
const secondLevelCount = computed(() => secondLevelCategories.value.length)
const selectedCategory = computed(() => flatCategories.value.find((item) => item.id === selectedId.value) ?? flatCategories.value[0] ?? null)
const childrenRows = computed(() => {
  if (!selectedCategory.value) {
    return []
  }
  if (selectedCategory.value.level === 1) {
    return selectedCategory.value.children ?? []
  }
  const parent = flatCategories.value.find((item) => item.id === selectedCategory.value?.parentId)
  return parent?.children ?? []
})

const rules: FormRules<CategoryUpsertReq> = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入分类编码', trigger: 'blur' }],
  level: [{ required: true, message: '请选择分类层级', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  parentId: [
    {
      validator: (_rule, value, callback) => {
        if (form.level === 2 && (!value || value <= 0)) {
          callback(new Error('二级分类必须选择父分类'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

async function loadCategoryTree() {
  loading.value = true
  try {
    treeData.value = await listCategoryTreeApi()
    if (!selectedId.value || !flatCategories.value.some((item) => item.id === selectedId.value)) {
      selectedId.value = flatCategories.value[0]?.id ?? null
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载分类失败')
  } finally {
    loading.value = false
  }
}

function handleSelect(row: CategoryNodeDto) {
  selectedId.value = row.id
}

function openCreateLevel1() {
  dialogTitle.value = '新增一级分类'
  editingId.value = null
  Object.assign(form, {
    parentId: 0,
    level: 1,
    name: '',
    code: '',
    defaultUnit: null,
    sortNo: firstLevelCategories.value.length + 1,
    status: 'ENABLED',
  })
  dialogVisible.value = true
}

function openCreateLevel2() {
  dialogTitle.value = '新增二级分类'
  editingId.value = null
  const parent = selectedCategory.value?.level === 1 ? selectedCategory.value : firstLevelCategories.value[0]
  Object.assign(form, {
    parentId: parent?.id ?? 0,
    level: 2,
    name: '',
    code: '',
    defaultUnit: null,
    sortNo: (parent?.children?.length ?? 0) + 1,
    status: 'ENABLED',
  })
  dialogVisible.value = true
}

function openEdit(row: CategoryNodeDto) {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id
  Object.assign(form, {
    parentId: row.level === 1 ? 0 : row.parentId,
    level: row.level,
    name: row.name,
    code: row.code,
    defaultUnit: row.defaultUnit,
    sortNo: row.sortNo,
    status: row.status,
  })
  dialogVisible.value = true
}

function handleLevelChange(level: 1 | 2) {
  if (level === 1) {
    form.parentId = 0
    return
  }
  if (!form.parentId || form.parentId <= 0) {
    form.parentId = firstLevelCategories.value[0]?.id ?? 0
  }
}

async function saveCategory() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload: CategoryUpsertReq = {
      ...form,
      parentId: form.level === 1 ? 0 : form.parentId,
      defaultUnit: form.defaultUnit || null,
    }
    if (editingId.value) {
      await updateCategoryApi(editingId.value, payload)
      ElMessage.success('分类已更新')
    } else {
      await createCategoryApi(payload)
      ElMessage.success('分类已新增')
    }
    dialogVisible.value = false
    await loadCategoryTree()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存分类失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: CategoryNodeDto) {
  try {
    await patchCategoryStatusApi(row.id, {
      status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED',
    })
    ElMessage.success(`${row.name} 已${row.status === 'ENABLED' ? '停用' : '启用'}`)
    await loadCategoryTree()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '更新分类状态失败')
  }
}

onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped>
.page-wrap {
  padding: 20px;
  display: grid;
  gap: 18px;
}

.category-grid {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
}

.tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.detail-card {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(247, 249, 255, 0.92);
}

.detail-row span {
  color: #74809d;
}

.detail-row strong {
  color: #15203c;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 960px) {
  .category-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
