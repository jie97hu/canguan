<template>
  <div class="system-page">
    <el-card class="system-card" shadow="never">
      <div class="system-code">404</div>
      <h2>页面不存在</h2>
      <p>你访问的地址未在当前前端底座中注册，可能是后续业务页面尚未接入。</p>
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
