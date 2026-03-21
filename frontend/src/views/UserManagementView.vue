<template>
  <div class="page-wrap">
    <PageHero
      eyebrow="Account Management"
      title="账号管理"
      description="维护老板与录入员账号，支持绑定门店、停用账号和重置密码。"
    >
      <template #meta>
        <el-tag effect="light" round>账号总数：{{ users.length }}</el-tag>
        <el-tag effect="light" round type="success">老板：{{ ownerCount }}</el-tag>
        <el-tag effect="light" round type="warning">录入员：{{ clerkCount }}</el-tag>
      </template>
      <template #actions>
        <el-button type="primary" @click="openCreate">新增账号</el-button>
      </template>
    </PageHero>

    <PageSection title="账号列表" description="支持角色筛选、门店筛选、状态切换和密码重置。">
      <el-form :model="filters" inline label-position="top" class="responsive-filter-form user-filter-form">
        <el-form-item label="关键字" class="user-filter-form__keyword">
          <el-input v-model="filters.keyword" clearable placeholder="用户名 / 姓名" />
        </el-form-item>
        <el-form-item label="角色" class="user-filter-form__select">
          <el-select v-model="filters.role" clearable placeholder="全部角色">
            <el-option label="老板" value="OWNER" />
            <el-option label="录入员" value="CLERK" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" class="user-filter-form__select">
          <el-select v-model="filters.status" clearable placeholder="全部状态">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店" class="user-filter-form__store">
          <el-select v-model="filters.storeId" clearable placeholder="全部门店">
            <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作" class="user-filter-form__actions">
          <div class="form-actions">
            <el-button type="primary" :loading="loading" @click="applyFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </el-form-item>
      </el-form>

      <div class="table-shell table-shell--wide">
        <el-table v-loading="loading" :data="users" border stripe>
          <el-table-column prop="username" label="用户名" width="160" />
          <el-table-column prop="displayName" label="姓名" width="140" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">{{ row.role === 'OWNER' ? '老板' : '录入员' }}</template>
          </el-table-column>
          <el-table-column prop="storeName" label="门店" min-width="150">
            <template #default="{ row }">{{ row.storeName || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <StatusTag :value="row.status" />
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180" />
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <div class="mobile-row-actions">
                <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button link :type="row.status === 'ENABLED' ? 'warning' : 'success'" @click="toggleStatus(row)">
                  {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="resetPassword(row)">重置密码</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </PageSection>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="appStore.isMobile ? 'calc(100vw - 24px)' : '620px'">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="grid">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="姓名" prop="displayName">
            <el-input v-model="form.displayName" />
          </el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="角色" prop="role">
            <el-select v-model="form.role" @change="handleRoleChange">
              <el-option label="老板" value="OWNER" />
              <el-option label="录入员" value="CLERK" />
            </el-select>
          </el-form-item>
          <el-form-item label="绑定门店" prop="storeId">
            <el-select v-model="form.storeId" :disabled="form.role === 'OWNER'" clearable>
              <el-option v-for="store in enabledStores" :key="store.id" :label="store.name" :value="store.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="grid">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status">
              <el-option label="启用" value="ENABLED" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </el-form-item>
          <el-form-item label="登录密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              :placeholder="editingId ? '编辑账号时需重新设置密码' : '请输入初始密码'"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

import PageHero from '@/components/common/PageHero.vue'
import PageSection from '@/components/common/PageSection.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { useAppStore } from '@/stores/app'
import { createUserApi, listStoresApi, listUsersApi, patchUserStatusApi, resetUserPasswordApi, updateUserApi } from '@/api/catalog'
import type { AppRole } from '@/types/auth'
import type { StoreDto } from '@/types/store'
import type { UserDto, UserQueryReq, UserUpsertReq } from '@/types/user'

const appStore = useAppStore()
const loading = ref(false)
const saving = ref(false)
const stores = ref<StoreDto[]>([])
const users = ref<UserDto[]>([])
const filters = reactive<UserQueryReq>({
  keyword: '',
  role: '',
  status: '',
  storeId: '',
})
const dialogVisible = ref(false)
const dialogTitle = ref('新增账号')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<UserUpsertReq>({
  username: '',
  password: '',
  displayName: '',
  role: 'CLERK',
  storeId: null,
  status: 'ENABLED',
})

const enabledStores = computed(() => stores.value.filter((item) => item.status === 'ENABLED'))
const ownerCount = computed(() => users.value.filter((item) => item.role === 'OWNER').length)
const clerkCount = computed(() => users.value.filter((item) => item.role === 'CLERK').length)

const rules: FormRules<UserUpsertReq> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  storeId: [
    {
      validator: (_rule, value, callback) => {
        if (form.role === 'CLERK' && !value) {
          callback(new Error('录入员必须绑定门店'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
}

async function loadStores() {
  const result = await listStoresApi({
    pageNo: 1,
    pageSize: 200,
  })
  stores.value = result.list
}

// 账号筛选走后端，确保页面展示和权限判断完全一致。
async function loadUsers() {
  loading.value = true
  try {
    const result = await listUsersApi({
      ...filters,
      pageNo: 1,
      pageSize: 200,
    })
    users.value = result.list
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载账号失败')
  } finally {
    loading.value = false
  }
}

function applyFilter() {
  loadUsers()
}

function resetFilter() {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  filters.storeId = ''
  loadUsers()
}

function openCreate() {
  dialogTitle.value = '新增账号'
  editingId.value = null
  Object.assign(form, {
    username: '',
    password: '',
    displayName: '',
    role: 'CLERK',
    storeId: enabledStores.value[0]?.id ?? null,
    status: 'ENABLED',
  })
  dialogVisible.value = true
}

function openEdit(row: UserDto) {
  dialogTitle.value = '编辑账号'
  editingId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    displayName: row.displayName,
    role: row.role,
    storeId: row.role === 'OWNER' ? null : row.storeId,
    status: row.status,
  })
  dialogVisible.value = true
}

function handleRoleChange(role: AppRole) {
  if (role === 'OWNER') {
    form.storeId = null
    return
  }
  if (!form.storeId) {
    form.storeId = enabledStores.value[0]?.id ?? null
  }
}

async function saveUser() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload: UserUpsertReq = {
      ...form,
      storeId: form.role === 'OWNER' ? null : form.storeId,
    }
    if (editingId.value) {
      await updateUserApi(editingId.value, payload)
      ElMessage.success('账号已更新')
    } else {
      await createUserApi(payload)
      ElMessage.success('账号已新增')
    }
    dialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存账号失败')
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: UserDto) {
  try {
    await patchUserStatusApi(row.id, {
      status: row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED',
    })
    ElMessage.success(`${row.displayName} 已${row.status === 'ENABLED' ? '停用' : '启用'}`)
    await loadUsers()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '更新账号状态失败')
  }
}

async function resetPassword(row: UserDto) {
  try {
    const { value } = await ElMessageBox.prompt(`请输入 ${row.displayName} 的新密码`, '重置密码', {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPlaceholder: '请输入新密码',
      inputValidator: (inputValue: string) => (inputValue ? true : '密码不能为空'),
    })
    await resetUserPasswordApi(row.id, { password: value })
    ElMessage.success('密码已重置')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error instanceof Error ? error.message : '重置密码失败')
  }
}

onMounted(async () => {
  try {
    await loadStores()
    await loadUsers()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '初始化页面失败')
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 20px;
  display: grid;
  gap: 18px;
}

.user-filter-form :deep(.el-form-item__content) {
  width: 100%;
}

.user-filter-form__keyword {
  width: 170px;
}

.user-filter-form__select {
  width: 96px;
}

.user-filter-form__store {
  width: 132px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 720px) {
  .page-wrap {
    padding: 12px;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
