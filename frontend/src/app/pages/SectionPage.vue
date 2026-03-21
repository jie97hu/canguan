<template>
  <div class="placeholder-page">
    <el-card class="placeholder-card" shadow="never">
      <div class="placeholder-top">
        <div class="placeholder-badge">{{ route.meta.section || 'Base' }}</div>
        <el-tag effect="plain" type="info" round>{{ route.meta.title }}</el-tag>
      </div>

      <h2>{{ route.meta.title }}</h2>
      <p>{{ route.meta.description }}</p>

      <div class="placeholder-grid">
        <div class="placeholder-stat">
          <span>菜单裁剪</span>
          <strong>{{ roleLabel }}</strong>
        </div>
        <div class="placeholder-stat">
          <span>当前入口</span>
          <strong>{{ route.path }}</strong>
        </div>
        <div class="placeholder-stat">
          <span>联调状态</span>
          <strong>{{ mockEnabled ? 'Mock On' : 'API On' }}</strong>
        </div>
      </div>

      <el-empty description="业务页面由后续子代理接入，这里先保留路由与权限壳层。" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import { isMockEnabled } from '@/api/mock'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const mockEnabled = isMockEnabled()
const roleLabel = computed(() => authStore.role ?? 'UNKNOWN')
</script>
