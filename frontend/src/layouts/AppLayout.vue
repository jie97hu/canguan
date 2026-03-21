<template>
  <div
    class="workspace-layout"
    :class="{ 'is-mobile': appStore.isMobile, 'aside-collapsed': appStore.sidebarCollapsed && !appStore.isMobile }"
  >
    <aside
      v-if="!appStore.isMobile"
      class="workspace-layout__aside"
      :class="{ collapsed: appStore.sidebarCollapsed }"
    >
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
          :default-active="menuActiveIndex"
          :collapse-transition="false"
          router
          unique-opened
          @select="handleMenuSelect"
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

    <el-drawer
      v-model="appStore.mobileNavVisible"
      class="workspace-mobile-drawer"
      direction="ltr"
      size="280px"
      :with-header="false"
      destroy-on-close
    >
      <div class="workspace-mobile-drawer__body">
        <div class="workspace-brand mobile">
          <div class="workspace-brand__logo">CG</div>
          <div class="workspace-brand__text">
            <strong>{{ APP_NAME }}</strong>
            <span>{{ APP_SUBTITLE }}</span>
          </div>
        </div>

        <el-scrollbar class="workspace-menu-scroll">
          <el-menu
            class="workspace-menu"
            :default-active="menuActiveIndex"
            router
            unique-opened
            @select="handleMenuSelect"
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
      </div>
    </el-drawer>

    <div class="workspace-layout__main">
      <header class="workspace-header">
        <div class="workspace-header__left">
          <el-button text class="collapse-btn" @click="appStore.toggleSidebar">
            <el-icon>
              <Menu v-if="appStore.isMobile" />
              <Expand v-else-if="appStore.sidebarCollapsed" />
              <Fold v-else />
            </el-icon>
          </el-button>
          <div v-if="!appStore.isMobile" class="workspace-breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item">
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div v-else class="workspace-header__title">
            <strong>{{ currentPageTitle }}</strong>
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
import { computed, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Expand, Fold, Menu } from '@element-plus/icons-vue'
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
let mobileMediaQuery: MediaQueryList | null = null

const mockEnabled = isMockEnabled()

const visibleNavigation = computed(() => filterNavigationTree(navigationTree, authStore.role))
const menuActiveIndex = computed(() => route.path)

const breadcrumbItems = computed(() =>
  route.matched
    .map((item) => item.meta.title)
    .filter((title): title is string => Boolean(title)),
)
const currentPageTitle = computed(() => breadcrumbItems.value[breadcrumbItems.value.length - 1] ?? '工作台')

function syncMobileViewport(matcher: MediaQueryList | MediaQueryListEvent) {
  appStore.setMobileViewport(matcher.matches)
}

function handleMenuSelect() {
  if (appStore.isMobile) {
    appStore.closeMobileNav()
  }
}

function bindViewportListener(matcher: MediaQueryList) {
  const legacyMatcher = matcher as MediaQueryList & {
    addListener?: (listener: (event: MediaQueryListEvent) => void) => void
  }

  if (typeof matcher.addEventListener === 'function') {
    matcher.addEventListener('change', syncMobileViewport)
    return
  }
  legacyMatcher.addListener?.(syncMobileViewport)
}

function unbindViewportListener(matcher: MediaQueryList) {
  const legacyMatcher = matcher as MediaQueryList & {
    removeListener?: (listener: (event: MediaQueryListEvent) => void) => void
  }

  if (typeof matcher.removeEventListener === 'function') {
    matcher.removeEventListener('change', syncMobileViewport)
    return
  }
  legacyMatcher.removeListener?.(syncMobileViewport)
}

onMounted(() => {
  mobileMediaQuery = window.matchMedia('(max-width: 768px)')
  syncMobileViewport(mobileMediaQuery)
  bindViewportListener(mobileMediaQuery)
})

onBeforeUnmount(() => {
  if (!mobileMediaQuery) {
    return
  }
  unbindViewportListener(mobileMediaQuery)
})

watch(
  () => route.fullPath,
  () => {
    appStore.closeMobileNav()
  },
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
