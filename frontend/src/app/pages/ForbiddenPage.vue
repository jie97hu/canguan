<template>
  <div class="system-page">
    <el-card class="system-card" shadow="never">
      <div class="system-code">403</div>
      <h2>无权限访问</h2>
      <p>当前账号没有访问该页面的权限，请联系老板账号调整角色或门店绑定。</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>
