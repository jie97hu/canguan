<template>
  <div class="login-card">
    <div class="login-card__header">
      <div>
        <h2>登录系统</h2>
        <p>使用已分配的真实账号进入分店支出管理台</p>
      </div>
      <el-tag round effect="light" type="success">OWNER / CLERK</el-tag>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          placeholder="请输入密码"
          autocomplete="current-password"
        />
      </el-form-item>

      <el-button type="primary" size="large" class="login-submit" :loading="loading" @click="submit">
        进入系统
      </el-button>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'
import { resolveHomePath } from '@/app/permission'
import type { LoginReq } from '@/types/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive<LoginReq>({
  username: '',
  password: '',
})

const rules: FormRules<LoginReq> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请先补全登录信息')
    return
  }

  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success(`欢迎回来，${authStore.displayName}`)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    await router.replace(redirect || resolveHomePath(authStore.role))
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
