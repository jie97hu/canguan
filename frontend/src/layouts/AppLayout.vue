<template>
  <div class="workspace-layout">
    <aside class="workspace-layout__aside" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="workspace-brand">
        <div class="workspace-brand__logo">CG</div>
        <div v-if="!appStore.sidebarCollapsed" class="workspace-brand__text">
          <strong>{{ APP_NAME }}</strong>
          <span>{{ APP_SUBTITLE }}</span>
        </div>
      </div>

      <el-scrollbar class="workspace-menu-scroll">
        <el-menu
          class="workspace-menu"
          :collapse="appStore.sidebarCollapsed"
          :default-active="route.path"
          :collapse-transition="false"
          router
          unique-opened
        >
          <template v-for="group in visibleNavigation" :key="group.key">
            <el-sub-menu v-if="group.children?.length" :index="group.key">
              <template #title>
                <el-icon v-if="group.icon">
                  <component :is="group.icon" />
                </el-icon>
                <span>{{ group.title }}</span>
              </template>
              <el-menu-item
                v-for="item in group.children"
                :key="item.key"
                :index="item.path ?? '/'"
              >
                <el-icon v-if="item.icon">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="group.path ?? '/'">
              <el-icon v-if="group.icon">
                <component :is="group.icon" />
              </el-icon>
              <span>{{ group.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="workspace-layout__main">
      <header class="workspace-header">
        <div class="workspace-header__left">
          <el-button text class="collapse-btn" @click="appStore.toggleSidebar">
            <el-icon>
              <Expand v-if="appStore.sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div class="workspace-breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item">
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>

        <div class="workspace-header__right">
          <el-tag v-if="mockEnabled" effect="dark" type="warning" round>MOCK</el-tag>
          <el-tag effect="light" round>{{ authStore.role ?? 'UNKNOWN' }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="workspace-user">
              <el-avatar size="small">{{ authStore.displayName.slice(0, 1) }}</el-avatar>
              <span class="workspace-user__name">{{ authStore.displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="workspace-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { APP_NAME, APP_SUBTITLE } from '@/app/config'
import { filterNavigationTree } from '@/app/permission'
import { isMockEnabled } from '@/api/mock'
import { navigationTree } from '@/app/navigation'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const mockEnabled = isMockEnabled()

const visibleNavigation = computed(() => filterNavigationTree(navigationTree, authStore.role))

const breadcrumbItems = computed(() =>
  route.matched
    .map((item) => item.meta.title)
    .filter((title): title is string => Boolean(title)),
)

async function handleCommand(command: string) {
  if (command !== 'logout') {
    return
  }

  try {
    await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await authStore.logout()
    ElMessage.success('已退出登录')
    await router.push({ name: 'login' })
  } catch {
    // 用户取消退出
  }
}
</script>
