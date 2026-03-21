<template>
  <div class="login-page">
    <div class="login-ambient ambient-left" />
    <div class="login-ambient ambient-right" />
    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-badge">Canguan</div>
        <h1>面馆多分店支出记账系统</h1>
        <p>
          面向老板与录入员的轻量化支出管理台，支持多门店隔离、快速录入、统一分类和报表分析。
        </p>
        <div class="brand-points">
          <div class="point">
            <strong>10 秒录入</strong>
            <span>最少字段快速完成一笔支出</span>
          </div>
          <div class="point">
            <strong>多店隔离</strong>
            <span>录入员只看本店，老板全局掌控</span>
          </div>
          <div class="point">
            <strong>趋势分析</strong>
            <span>分类占比、门店排行、时间趋势一目了然</span>
          </div>
        </div>
      </section>
      <section class="login-panel">
        <div class="login-card">
          <div class="login-card-head">
            <div>
              <div class="login-card-title">登录系统</div>
              <div class="login-card-subtitle">请选择账号并输入密码</div>
            </div>
            <el-tag effect="light" round type="success">OWNER / CLERK</el-tag>
          </div>

          <div class="quick-grid">
            <button v-for="account in accounts" :key="account.username" type="button" class="quick-card" @click="fillAccount(account)">
              <div class="quick-role">{{ account.role === 'OWNER' ? '老板' : '录入员' }}</div>
              <div class="quick-name">{{ account.displayName }}</div>
              <div class="quick-desc">{{ account.description }}</div>
            </button>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" autocomplete="current-password" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="currentRole" disabled>
                <el-option label="老板 OWNER" value="OWNER" />
                <el-option label="录入员 CLERK" value="CLERK" />
              </el-select>
            </el-form-item>

            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submitLogin">
              进入系统
            </el-button>
          </el-form>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { mockLoginAccounts, resolveRoleText, type LoginAccount, type RoleType } from '../features/canguan'

interface LoginForm {
  username: string
  password: string
}

const emit = defineEmits<{
  (e: 'submit', payload: LoginForm & { role: RoleType }): void
}>()

const accounts = mockLoginAccounts
const formRef = ref<FormInstance>()
const loading = ref(false)
const currentRole = ref<RoleType>('OWNER')
const form = reactive<LoginForm>({
  username: 'owner',
  password: '123456',
})

const rules: FormRules<LoginForm> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const currentAccount = computed(() => accounts.find((item) => item.username === form.username))

function fillAccount(account: LoginAccount) {
  form.username = account.username
  form.password = account.password
  currentRole.value = account.role
}

async function submitLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请先完善登录信息')
    return
  }
  loading.value = true
  try {
    const account = currentAccount.value ?? accounts.find((item) => item.role === currentRole.value)
    if (!account || account.password !== form.password) {
      ElMessage.error('用户名或密码不正确')
      return
    }
    emit('submit', { ...form, role: account.role })
    ElMessage.success(`欢迎回来，${account.displayName}（${resolveRoleText(account.role)}）`)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(227, 107, 44, 0.18), transparent 28%),
    radial-gradient(circle at bottom right, rgba(40, 88, 255, 0.18), transparent 28%),
    linear-gradient(135deg, #eff3ff 0%, #f8fafc 55%, #eef4ff 100%);
}

.login-ambient {
  position: absolute;
  border-radius: 50%;
  filter: blur(34px);
  opacity: 0.85;
}

.ambient-left {
  top: 6%;
  left: -40px;
  width: 160px;
  height: 160px;
  background: rgba(227, 107, 44, 0.28);
}

.ambient-right {
  right: 6%;
  bottom: 8%;
  width: 220px;
  height: 220px;
  background: rgba(40, 88, 255, 0.18);
}

.login-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(420px, 0.9fr);
  gap: 26px;
  max-width: 1320px;
  margin: 0 auto;
  align-items: center;
  min-height: calc(100vh - 64px);
}

.brand-panel {
  color: #15203c;
}

.brand-badge {
  display: inline-flex;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(40, 88, 255, 0.1);
  color: #2858ff;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.brand-panel h1 {
  margin: 18px 0 12px;
  font-size: clamp(34px, 5vw, 60px);
  line-height: 1.04;
}

.brand-panel p {
  margin: 0;
  max-width: 700px;
  color: #5f6b86;
  line-height: 1.8;
  font-size: 16px;
}

.brand-points {
  margin-top: 28px;
  display: grid;
  gap: 14px;
  max-width: 620px;
}

.point {
  padding: 18px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(28, 39, 72, 0.08);
  box-shadow: 0 16px 28px rgba(28, 39, 72, 0.06);
}

.point strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
}

.point span {
  color: #6f7892;
  font-size: 13px;
}

.login-panel {
  display: flex;
  justify-content: flex-end;
}

.login-card {
  width: 100%;
  max-width: 520px;
  padding: 26px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(28, 39, 72, 0.08);
  box-shadow: 0 26px 44px rgba(24, 39, 75, 0.12);
  backdrop-filter: blur(18px);
}

.login-card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.login-card-title {
  font-size: 24px;
  font-weight: 800;
  color: #15203c;
}

.login-card-subtitle {
  margin-top: 6px;
  color: #6f7892;
  font-size: 13px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}

.quick-card {
  text-align: left;
  border: 1px solid rgba(28, 39, 72, 0.08);
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.96), rgba(247, 249, 255, 0.96));
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 28px rgba(24, 39, 75, 0.08);
}

.quick-role {
  color: #2858ff;
  font-size: 12px;
  font-weight: 700;
}

.quick-name {
  margin-top: 6px;
  font-size: 16px;
  font-weight: 700;
  color: #15203c;
}

.quick-desc {
  margin-top: 6px;
  color: #6f7892;
  font-size: 12px;
  line-height: 1.6;
}

.login-form {
  display: grid;
  gap: 4px;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
}

@media (max-width: 1120px) {
  .login-shell {
    grid-template-columns: 1fr;
    align-items: start;
  }

  .login-panel {
    justify-content: stretch;
  }

  .login-card {
    max-width: none;
  }
}

@media (max-width: 720px) {
  .login-page {
    padding: 18px;
  }

  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
