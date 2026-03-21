import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

import App from './App.vue'
import { createAppRouter } from '@/router'
import { bootstrapFrontend } from '@/app/bootstrap'
import '@/styles/index.css'
import 'element-plus/dist/index.css'

dayjs.locale('zh-cn')

const app = createApp(App)
const pinia = createPinia()
const router = createAppRouter()

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

bootstrapFrontend({ pinia, router })
  .catch((error) => {
    // 入口初始化失败时仍然挂载应用，保证错误页和登录页可见。
    console.error('应用初始化失败', error)
  })
  .finally(() => {
    app.mount('#app')
  })
